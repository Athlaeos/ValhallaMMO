package me.athlaeos.valhallammo;

import me.athlaeos.valhallammo.commands.CommandManager;
import me.athlaeos.valhallammo.commands.ProfileCommand;
import me.athlaeos.valhallammo.commands.RedeemCommand;
import me.athlaeos.valhallammo.commands.SkillsCommand;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.configuration.ConfigUpdater;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.gui.MenuListener;
import me.athlaeos.valhallammo.hooks.PAPIHook;
import me.athlaeos.valhallammo.hooks.PluginHook;
import me.athlaeos.valhallammo.hooks.VaultHook;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.listeners.*;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.nms.NMS;
import me.athlaeos.valhallammo.nms.NetworkHandlerImpl;
import me.athlaeos.valhallammo.nms.PacketListener;
import me.athlaeos.valhallammo.persistence.Database;
import me.athlaeos.valhallammo.persistence.ProfilePersistence;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.*;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.skills.perkresourcecost.ResourceExpenseRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.item.AlchemyItemPropertyManager;
import me.athlaeos.valhallammo.skills.perkunlockconditions.UnlockConditionRegistry;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import me.athlaeos.valhallammo.tools.BlockHardnessStick;
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
import java.util.*;

public class ValhallaMMO extends JavaPlugin {

    private static NMS nms = null;
    private static PacketListener packetListener = null;
    private static ValhallaMMO instance;
    private static boolean resourcePackConfigForced = false;
    private static final Map<Class<? extends PluginHook>, PluginHook> pluginHooks = new HashMap<>();
    private static YamlConfiguration pluginConfig;
    private final static Collection<String> worldBlacklist = new HashSet<>();

    {
        instance = this;
    }

    public static ValhallaMMO getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        // initialize all resources required for the various managers to use.
        registerHook(new VaultHook());
        registerHook(new PAPIHook());
        registerHook(new WorldGuardHook());

        // save and update configs
        pluginConfig = saveAndUpdateConfig("config.yml");

        String lang = pluginConfig.getString("language", "en-us");
        save("languages/en-us.json");
        save("languages/" + lang + ".json");
        save("languages/materials/" + lang + ".json");
        save("recipes/grid_recipes.json");
        save("recipes/brewing_recipes.json");
        save("recipes/cooking_recipes.json");
        save("recipes/immersive_recipes.json");
        save("recipes/cauldron_recipes.json");
        save("recipes/smithing_recipes.json");
        saveConfig("recipes/disabled_recipes.yml");
        TranslationManager.load(lang);
        // initialize modifiers and perk rewards
        ResourceExpenseRegistry.registerDefaultExpenses();
        UnlockConditionRegistry.registerDefaultConditions();

    }

    @Override
    public void onEnable() {
        resourcePackConfigForced = pluginConfig.getBoolean("resource_pack_config_override");
        saveConfig("skills/power_progression.yml");
        saveConfig("skills/power.yml");
        saveConfig("skills/smithing.yml");
        saveConfig("skills/alchemy.yml");
        saveConfig("skills/archery.yml");
        saveConfig("skills/enchanting.yml");
        saveConfig("skills/light_armor.yml");
        saveConfig("skills/heavy_armor.yml");
        saveConfig("skills/light_weapons.yml"); // TODO config saving
        saveConfig("skills/heavy_weapons.yml");
        saveConfig("skills/mining.yml");
        saveAndUpdateConfig("gui_details.yml");

        if (setupNMS()){
            packetListener = new PacketListener(new NetworkHandlerImpl());
            packetListener.addAll();
            registerListener(packetListener);
            registerListener(new CustomBreakSpeedListener(), "custom_mining_speeds");
            logInfo("NMS version " + nms.getClass().getSimpleName() + " registered!");
        } else {
            logWarning("No NMS version found for your server version");
            logWarning("This version may not be compatible with ValhallaMMO (1.17+) yet and may not work properly, and the following features are disabled:");
            logWarning("    > Custom block breaking speeds");
            logWarning("    > Advanced book editing");
        }

        ItemAttributesRegistry.loadDefaults();
        PotionEffectRegistry.loadDefaults();
        SmithingItemPropertyManager.loadConfig();
        AlchemyItemPropertyManager.loadConfig();
        SkillRegistry.registerSkills();

        ProfileRegistry.setupDatabase();
        ProfilePersistence connection = ProfileRegistry.getPersistence();

        if (ConfigManager.getConfig("config.yml").get().getBoolean("metrics", true)){
            new Metrics(this, 14942).addCustomChart(new Metrics.SimplePie("using_database_for_player_data", () -> connection instanceof Database db && db.getConnection() != null ? "Yes" : "No"));
        }

        registerListener(new ArmorSwitchListener());
        registerListener(new BlockListener());
        registerListener(new BrewingStandListener());
        registerListener(new CauldronCraftingListener());
        registerListener(new ChatListener());
        registerListener(new CookingListener());
        registerListener(new CraftingTableListener());
        registerListener(new DeathListener());
        registerListener(new EnchantmentListener());
        registerListener(new EntityAttackListener());
        registerListener(new EntityDamagedListener());
        registerListener(new EntitySpawnListener());
        registerListener(new HandSwitchListener());
        registerListener(new HealthRegenerationListener());
        registerListener(new ImmersiveRecipeListener());
        registerListener(new InteractListener());
        registerListener(new ItemConsumptionListener());
        registerListener(new ItemDamageListener());
        registerListener(new JoinLeaveListener());
        registerListener(new LootListener());
        registerListener(new MenuListener());
        registerListener(new MovementListener());
        registerListener(new PotionEffectListener());
        registerListener(new ProjectileListener());
        registerListener(new ReachAttackListener());
        registerListener(new RecipeDiscoveryListener());
        registerListener(new SmithingTableListener());

        registerListener(new BlockHardnessStick());

        registerCommand(new CommandManager(), "valhalla");
        registerCommand(new SkillsCommand(), "skills");
        registerCommand(new RedeemCommand(), "redeem");
        registerCommand(new ProfileCommand(PowerProfile.class), "power");
        registerCommand(new ProfileCommand(SmithingProfile.class), "smithing");
        registerCommand(new ProfileCommand(AlchemyProfile.class), "alchemy");
        registerCommand(new ProfileCommand(EnchantingProfile.class), "enchanting");
        registerCommand(new ProfileCommand(LightWeaponsProfile.class), "lightweapons");
        registerCommand(new ProfileCommand(HeavyWeaponsProfile.class), "heavyweapons");
        registerCommand(new ProfileCommand(ArcheryProfile.class), "archery");
        registerCommand(new ProfileCommand(LightArmorProfile.class), "lightarmor");
        registerCommand(new ProfileCommand(HeavyArmorProfile.class), "heavyarmor");
        registerCommand(new ProfileCommand(MiningProfile.class), "mining");
        // TODO new profile command per profile

        CustomRecipeRegistry.loadFiles();
        LootTableRegistry.loadFiles();

        // During reloads profiles are persisted. This makes sure profiles of players who are already online are ensured
        // to be loaded, otherwise their progress is reset
        for (Player p : getServer().getOnlinePlayers()){
            ProfileRegistry.getPersistence().loadProfile(p);
        }

        worldBlacklist.addAll(pluginConfig.getStringList("world_blacklist"));
        if (PotionEffectRegistry.getCustomEffectDisplay() != null) PotionEffectRegistry.getCustomEffectDisplay().start();
    }

    @Override
    public void onDisable() {
        ProfileRegistry.getPersistence().saveAllProfiles();
        if (ProfileRegistry.getPersistence() instanceof Database database) {
            try {
                database.getConnection().close();
            } catch (SQLException ignored){
                logSevere("Could not close connection");
            }
        }

        CustomRecipeRegistry.saveRecipes(false);
        LootTableRegistry.saveLootTables();
        if (packetListener != null) packetListener.closeAll();
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

    public static NMS getNms() {
        return nms;
    }

    private void registerListener(Listener listener){
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerListener(Listener listener, String configKey){
        if (pluginConfig.getBoolean(configKey)) registerListener(listener);
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

    public static boolean isWorldBlacklisted(String world) {
        return worldBlacklist.contains(world);
    }
}
