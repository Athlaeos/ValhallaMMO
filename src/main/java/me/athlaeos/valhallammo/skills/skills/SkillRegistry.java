package me.athlaeos.valhallammo.skills.skills;

import me.athlaeos.valhallammo.configuration.ConfigManager;
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

    public static void registerSkills(){
        registerSkill(new PowerSkill("POWER"));
         registerIfConfigEnabled("alchemy", new AlchemySkill("ALCHEMY"));
         registerIfConfigEnabled("smithing", new SmithingSkill("SMITHING"));
         registerIfConfigEnabled("enchanting", new EnchantingSkill("ENCHANTING"));
        // registerIfConfigEnabled("farming", new AlchemySkill("FARMING"));
         registerIfConfigEnabled("mining", new MiningSkill("MINING"));
        // registerIfConfigEnabled("landscaping", new AlchemySkill("LANDSCAPING"));
         registerIfConfigEnabled("archery", new ArcherySkill("ARCHERY"));
         registerIfConfigEnabled("armor_light", new LightArmorSkill("LIGHT_ARMOR"));
         registerIfConfigEnabled("armor_heavy", new HeavyArmorSkill("HEAVY_ARMOR"));
         registerIfConfigEnabled("weapons_light", new LightWeaponsSkill("LIGHT_WEAPONS"));
         registerIfConfigEnabled("weapons_heavy", new HeavyWeaponsSkill("HEAVY_WEAPONS"));
    }

    private static void registerIfConfigEnabled(String key, Skill skill){
        YamlConfiguration config = ConfigManager.getConfig("config.yml").get();
        if (config.getBoolean("enabled_skills." + key, true)) registerSkill(skill);
    }

    public static Map<Class<?>, Skill> getAllSkills() {
        return allSkills;
    }

    public static <T extends Skill> Skill getSkill(Class<T> skill){
        if (!allSkills.containsKey(skill)) throw new IllegalArgumentException("Skill " + skill.getSimpleName() + " was not registered for usage");
        return allSkills.get(skill);
    }

    public static Skill getSkill(String skill){
        return allSkills.values().stream().filter(s -> s.getType().equalsIgnoreCase(skill)).findAny().orElse(null);
    }

    public static void registerSkill(Skill skill){
        Map<Class<?>, Skill> skills = new HashMap<>(allSkills);
        skills.put(skill.getClass(), skill);
        allSkills = Collections.unmodifiableMap(skills);

        skill.perks.forEach(PerkRegistry::registerPerk);
        PerkRewardRegistry.register(new SkillReset("reset_skill_" + skill.getType().toLowerCase())); // TODO test
        PerkRewardRegistry.register(new SkillRefund("refund_skill_" + skill.getType().toLowerCase()));
        PerkRewardRegistry.register(new SkillLevelsAdd("skill_levels_add_" + skill.getType().toLowerCase(), skill));
        PerkRewardRegistry.register(new SkillEXPAdd("skill_exp_add_" + skill.getType().toLowerCase(), skill));
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
        allSkills.values().forEach(s -> s.updateSkillStats(p, runPersistentStartingPerks));
    }
}
