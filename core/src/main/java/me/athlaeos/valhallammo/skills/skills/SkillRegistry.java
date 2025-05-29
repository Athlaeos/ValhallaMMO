package me.athlaeos.valhallammo.skills.skills;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc.SkillRequirementAdd;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards.SkillExperience;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardRegistry;
import me.athlaeos.valhallammo.skills.perk_rewards.implementations.*;
import me.athlaeos.valhallammo.skills.skills.implementations.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
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
    }

    private static void registerIfConfigEnabled(String key, Skill skill){
        YamlConfiguration config = ConfigManager.getConfig("config.yml").get();
        if (config.getBoolean("enabled_skills." + key, true)) registerSkill(skill);
    }

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

        skill.loadConfiguration();
        skill.perks.forEach(PerkRegistry::registerPerk);
    }

    public static boolean isRegistered(Class<? extends Skill> skill){
        return allSkills.containsKey(skill);
    }

    public static void reload() {
        PerkRegistry.clearRegistry();
        allSkills = Collections.unmodifiableMap(new HashMap<>());
        registerSkills();
    }

    public static void updateSkillProgression(Player p, boolean runPersistentStartingPerks){
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
            allSkills.values().forEach(s -> {
                ProfileRegistry.setSkillProfile(p, ProfileRegistry.getBlankProfile(p, s.getProfileType()), s.getProfileType());
            });

            getSkill(PowerSkill.class).updateSkillStats(p, runPersistentStartingPerks);
            allSkills.values().forEach(s -> {
                if (s instanceof PowerSkill) return;
                s.updateSkillStats(p, runPersistentStartingPerks);
            });
        });
    }
}
