package me.athlaeos.valhallammo;

import me.athlaeos.valhallammo.commands.CommandManager;
import me.athlaeos.valhallammo.commands.ProfileCommand;
import me.athlaeos.valhallammo.commands.SkillsCommand;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.configuration.ConfigUpdater;
import me.athlaeos.valhallammo.gui.MenuListener;
import me.athlaeos.valhallammo.hooks.PAPIHook;
import me.athlaeos.valhallammo.hooks.PluginHook;
import me.athlaeos.valhallammo.hooks.VaultHook;
import me.athlaeos.valhallammo.listeners.JoinLeaveListener;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.nms.NMS;
import me.athlaeos.valhallammo.persistence.Database;
import me.athlaeos.valhallammo.persistence.ProfilePersistence;
import me.athlaeos.valhallammo.progression.perkresourcecost.ResourceExpenseRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileManager;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.progression.perkunlockconditions.UnlockConditionRegistry;
import me.athlaeos.valhallammo.progression.skills.SkillRegistry;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValhallaMMO extends JavaPlugin {

    private NMS nms = null;
    private static ValhallaMMO instance;
    private static boolean resourcePackConfigForced = false;
    private static final Map<Class<? extends PluginHook>, PluginHook> pluginHooks = new HashMap<>();
    private static YamlConfiguration pluginConfig;

    {
        instance = this;
        registerHook(new VaultHook());
        registerHook(new PAPIHook());
    }

    public static ValhallaMMO getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        // initialize all resources required for the various managers to use.

        // save and update configs
        pluginConfig = saveAndUpdateConfig("config.yml");

        String lang = pluginConfig.getString("language", "en-us");
        save("languages/" + lang + ".json");
        save("languages/materials/" + lang + ".json");
        TranslationManager.load(lang);
        // initialize modifiers and perk rewards
        ResourceExpenseRegistry.registerDefaultExpenses();
        UnlockConditionRegistry.registerDefaultConditions();

    }

    @Override
    public void onEnable() {
        resourcePackConfigForced = pluginConfig.getBoolean("resource_pack_config_override");
        saveConfig("skills/progression_player.yml");
        saveConfig("skills/skill_player.yml");

        if (setupNMS()){
            logInfo("NMS version " + nms.getClass().getSimpleName() + " registered!");
        } else {
            logWarning("No NMS version found for your server version");
            logWarning("This version may not be compatible with ValhallaMMO (1.17+) yet and may not work properly, and the following features are disabled:");
            logWarning("    > Custom block breaking speeds");
            logWarning("    > Advanced book editing");
            logWarning("    > Multi-jumping");
        }

        SkillRegistry.registerSkills();

        ProfileManager.setupDatabase();
        ProfilePersistence connection = ProfileManager.getPersistence();

        if (ConfigManager.getConfig("config.yml").get().getBoolean("metrics", true)){
            new Metrics(this, 14942).addCustomChart(new Metrics.SimplePie("using_database_for_player_data", () -> connection instanceof Database db && db.getConnection() != null ? "Yes" : "No"));
        }

        registerListener(new JoinLeaveListener());
        registerListener(new MenuListener());

        registerCommand(new CommandManager(), "valhalla");
        registerCommand(new SkillsCommand(), "skills");
        registerCommand(new ProfileCommand(PowerProfile.class), "power");
        // TODO new profile command per profile

        // During reloads profiles are persisted. This makes sure profiles of players who are already online are ensured
        // to be loaded, otherwise their progress is reset
        for (Player p : getServer().getOnlinePlayers()){
            ProfileManager.getPersistence().loadProfile(p);
        }
    }

    @Override
    public void onDisable() {
        ProfileManager.getPersistence().saveAllProfiles();
        if (ProfileManager.getPersistence() instanceof Database database) {
            try {
                database.getConnection().close();
            } catch (SQLException ignored){
                logSevere("Could not close connection");
            }
        }
    }

    private boolean setupNMS() {
        try {
            String version = getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> clazz = Class.forName("me.athlaeos.valhallammo.nms.NMS_" + version);

            if (NMS.class.isAssignableFrom(clazz)) {
                nms = (NMS) clazz.getDeclaredConstructor().newInstance();
            }

            return nms != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void registerListener(Listener listener){
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerCommand(CommandExecutor command, String cmd){
        PluginCommand c = ValhallaMMO.getInstance().getCommand(cmd);
        if (c == null) return;
        c.setExecutor(command);
    }

    public YamlConfiguration saveConfig(String name){
        save(name);
        return ConfigManager.saveConfig(name).get();
    }
    public void save(String name){
        File file = new File(this.getDataFolder(), name);
        if (!file.exists()){
            this.saveResource(name, false);
        }
    }

    private void updateConfig(String name){
        File configFile = new File(getDataFolder(), name);
        try {
            ConfigUpdater.update(instance, name, configFile, new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateConfig(String name, List<String> excludedSections){
        File configFile = new File(getDataFolder(), name);
        try {
            ConfigUpdater.update(instance, name, configFile, excludedSections);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private YamlConfiguration saveAndUpdateConfig(String config){
        save(config);
        updateConfig(config);
        return saveConfig(config);
    }

    private YamlConfiguration saveAndUpdateConfig(String config, List<String> excludedSections){
        save(config);
        updateConfig(config, excludedSections);
        return saveConfig(config);
    }

    public static void logInfo(String message){
        instance.getServer().getLogger().info("[ValhallaMMO] " + message);
    }

    public static void logWarning(String warning){
        instance.getServer().getLogger().warning("[ValhallaMMO] " + warning);
    }
    public static void logFine(String warning){
        instance.getServer().getLogger().fine("[ValhallaMMO] " + warning);
        Utils.sendMessage(instance.getServer().getConsoleSender(), "&a[ValhallaMMO] " + warning);
    }

    public static void logSevere(String help){
        instance.getServer().getLogger().severe("[ValhallaMMO] " + help);
    }

    public static YamlConfiguration getPluginConfig() {
        return pluginConfig;
    }

    private static void registerHook(PluginHook hook){
        if (hook.isPresent()) pluginHooks.put(hook.getClass(), hook);
    }

    public static boolean isHookFunctional(Class<? extends PluginHook> hook){
        return pluginHooks.containsKey(hook);
    }

    @SuppressWarnings("unchecked")
    public static <T extends PluginHook> T getHook(Class<T> hook){
        return (T) pluginHooks.get(hook);
    }

    public static boolean isResourcePackConfigForced() {
        return resourcePackConfigForced;
    }
}
