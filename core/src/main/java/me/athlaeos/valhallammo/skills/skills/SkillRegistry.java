package me.athlaeos.valhallammo.skills.skills;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc.SkillRequirementAdd;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards.SkillExperience;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.ConfigurableProfile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.BooleanProperties;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardRegistry;
import me.athlaeos.valhallammo.skills.perk_rewards.implementations.*;
import me.athlaeos.valhallammo.skills.skills.implementations.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SkillRegistry {
    private static Map<Class<?>, Skill> allSkills = Collections.unmodifiableMap(new HashMap<>());
    private static Map<String, Skill> allSkillsByType = Collections.unmodifiableMap(new HashMap<>());

    public static void registerSkills(){
         registerSkill(new PowerSkill("POWER"));
         registerIfConfigEnabled("alchemy", new AlchemySkill("ALCHEMY"));
         registerIfConfigEnabled("smithing", new SmithingSkill("SMITHING"));
         registerIfConfigEnabled("enchanting", new EnchantingSkill("ENCHANTING"));
         registerIfConfigEnabled("farming", new FarmingSkill("FARMING"));
         registerIfConfigEnabled("mining", new MiningSkill("MINING"));
         registerIfConfigEnabled("fishing", new FishingSkill("FISHING"));
         registerIfConfigEnabled("digging", new DiggingSkill("DIGGING"));
         registerIfConfigEnabled("woodcutting", new WoodcuttingSkill("WOODCUTTING"));
         registerIfConfigEnabled("archery", new ArcherySkill("ARCHERY"));
         registerIfConfigEnabled("armor_light", new LightArmorSkill("LIGHT_ARMOR"));
         registerIfConfigEnabled("armor_heavy", new HeavyArmorSkill("HEAVY_ARMOR"));
         registerIfConfigEnabled("weapons_light", new LightWeaponsSkill("LIGHT_WEAPONS"));
         registerIfConfigEnabled("weapons_heavy", new HeavyWeaponsSkill("HEAVY_WEAPONS"));
         registerIfConfigEnabled("martial_arts", new MartialArtsSkill("MARTIAL_ARTS"));
         registerIfConfigEnabled("trading", new TradingSkill("TRADING"));

        loadConfigurableSkills();

        for (Skill s : allSkillsByType.values()){
            s.loadConfiguration();
            s.perks.forEach(PerkRegistry::registerPerk);
        }
    }

    @SuppressWarnings("all")
    public static void loadConfigurableSkills(){
        File lootTablesFolder = new File(ValhallaMMO.getInstance().getDataFolder(), "/skills/custom");
        lootTablesFolder.mkdirs();
        File[] skills = lootTablesFolder.listFiles();
        if (skills != null){
            for (File skillFile : skills){
                if (!skillFile.getName().endsWith(".yml")) continue;
                ValhallaMMO.getInstance().save("skills/custom/" + skillFile.getName());
                YamlConfiguration config = ConfigManager.getConfig("skills/custom/" + skillFile.getName()).get();
                if (!config.getBoolean("enabled")) continue;
                ValhallaMMO.logFine("Registered custom skill " + skillFile.getName());
                String type = skillFile.getName().replace(".yml", "").toUpperCase(Locale.US);
                ConfigurableProfile profile = new ConfigurableProfile(null, type);

                ConfigurationSection ints = config.getConfigurationSection("stats.ints");
                if (ints != null){
                    for (String key : ints.getKeys(false)){
                        profile.intStat(key, config.getInt("stats.ints." + key), new PropertyBuilder().perkReward().format(StatFormat.INT).create());
                    }
                }
                ConfigurationSection floats = config.getConfigurationSection("stats.floats");
                if (floats != null){
                    for (String key : floats.getKeys(false)){
                        profile.floatStat(key, (float) config.getDouble("stats.floats." + key), new PropertyBuilder().perkReward().format(StatFormat.FLOAT_P2).create());
                    }
                }
                ConfigurationSection doubles = config.getConfigurationSection("stats.doubles");
                if (doubles != null){
                    for (String key : doubles.getKeys(false)){
                        profile.doubleStat(key, config.getDouble("stats.doubles." + key), new PropertyBuilder().perkReward().format(StatFormat.FLOAT_P2).create());
                    }
                }
                ConfigurationSection booleans = config.getConfigurationSection("stats.booleans");
                if (booleans != null){
                    for (String key : booleans.getKeys(false)){
                        profile.booleanStat(key, config.getBoolean("stats.booleans." + key), new BooleanProperties(true, true));
                    }
                }
                String profileName = type.toLowerCase(Locale.US);
                ConfigurationSection sets = config.getConfigurationSection("stats.sets");
                if (sets != null){
                    for (String key : sets.getKeys(false)){
                        profile.stringSetStat(key, config.getStringList("stats.sets." + key));
                        PerkRewardRegistry.register(new ProfileStringListAdd(String.format("%s_%s_add", profileName, key), key, type));
                        PerkRewardRegistry.register(new ProfileStringListRemove(String.format("%s_%s_remove", profileName, key), key, type));
                        PerkRewardRegistry.register(new ProfileStringListClear(String.format("%s_%s_clear", profileName, key), key, type));
                    }
                }

                ProfileRegistry.registerConfigurableProfileType(profile);
                registerSkill(new ConfigurableSkill(type, config.getInt("order", 999)));
            }
        }
    }

    private static void registerIfConfigEnabled(String key, Skill skill){
        YamlConfiguration config = ConfigManager.getConfig("config.yml").get();
        if (config.getBoolean("enabled_skills." + key, true)) registerSkill(skill);
    }

    @Deprecated(forRemoval = true)
    public static Map<Class<?>, Skill> getAllSkills() {
        return allSkills;
    }

    public static Map<String, Skill> getAllSkillsByType() {
        return allSkillsByType;
    }

    public static <T extends Skill> Skill getSkill(Class<T> skill){
        if (!allSkills.containsKey(skill)) throw new IllegalArgumentException("Skill " + skill.getSimpleName() + " was not registered for usage");
        return allSkills.get(skill);
    }

    public static Skill getSkill(String skill){
        return allSkillsByType.get(skill);
    }

    public static void registerSkill(Skill skill){
        Map<Class<?>, Skill> skills = new HashMap<>(allSkills);
        skills.put(skill.getClass(), skill);
        allSkills = Collections.unmodifiableMap(skills);
        Map<String, Skill> skillsByType = new HashMap<>(allSkillsByType);
        skillsByType.put(skill.getType(), skill);
        allSkillsByType = Collections.unmodifiableMap(skillsByType);

        PerkRewardRegistry.register(new SkillReset("reset_skill_" + skill.getType().toLowerCase(java.util.Locale.US), skill.getType()));
        PerkRewardRegistry.register(new SkillRefund("refund_skill_" + skill.getType().toLowerCase(java.util.Locale.US)));
        PerkRewardRegistry.register(new SkillLevelsAdd("skill_levels_add_" + skill.getType().toLowerCase(java.util.Locale.US), skill));
        PerkRewardRegistry.register(new SkillEXPAdd("skill_exp_add_" + skill.getType().toLowerCase(java.util.Locale.US), skill));
        ModifierRegistry.register(new SkillExperience("reward_" + skill.getType().toLowerCase(java.util.Locale.US) + "_experience", skill.getType()));
        ModifierRegistry.register(new SkillRequirementAdd("requirement_add_" + skill.getType().toLowerCase(java.util.Locale.US), skill.getType()));
    }

    public static boolean isRegistered(Class<? extends Skill> skill){
        return allSkills.containsKey(skill);
    }

    public static boolean isRegistered(String skill){
        return allSkillsByType.containsKey(skill);
    }

    public static void reload() {
        PerkRegistry.clearRegistry();
        allSkills = Collections.unmodifiableMap(new HashMap<>());
        allSkillsByType = Collections.unmodifiableMap(new HashMap<>());
        registerSkills();
    }

    public static void updateSkillProgression(Player p, boolean runPersistentStartingPerks){
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
            allSkillsByType.values().forEach(s -> {
                if (!(s instanceof ConfigurableSkill)){
                    ProfileRegistry.setSkillProfile(p, ProfileRegistry.getBlankProfile(p, s.getProfileType()), s.getProfileType());
                }
            });

            getSkill(PowerSkill.class).updateSkillStats(p, runPersistentStartingPerks);
            allSkills.values().forEach(s -> {
                if (s instanceof PowerSkill || s instanceof ConfigurableSkill) return;
                s.updateSkillStats(p, runPersistentStartingPerks);
            });
        });
    }

    public static void updateConfigurableSkillProgression(Player p, boolean runPersistentStartingPerks){
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
            allSkillsByType.values().forEach(s -> {
                if (s instanceof ConfigurableSkill c){
                    ProfileRegistry.setSkillProfile(p, ProfileRegistry.getBlankConfigurableProfile(p, c.type), s.getProfileType());
                }
            });

            allSkills.values().forEach(s -> {
                if (!(s instanceof ConfigurableSkill)) return;
                s.updateSkillStats(p, runPersistentStartingPerks);
            });
        });
    }
}
