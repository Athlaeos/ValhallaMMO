package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemSkillRequirements {
    private static final NamespacedKey SKILL_REQUIREMENT = new NamespacedKey(ValhallaMMO.getInstance(), "item_skill_requirement");
    private static final NamespacedKey REQUIREMENT_TYPE = new NamespacedKey(ValhallaMMO.getInstance(), "item_skill_requirement_type");

    private static final String penaltyScaling;
    private static final Map<String, Double> attributePenalties = new HashMap<>();
    private static final String warningTooAdvanced;
    private static final int warningDisplayType;
    private static final int warningDisplayCooldown;

    static {
        YamlConfiguration c = ValhallaMMO.getPluginConfig();
        penaltyScaling = c.getString("item_skill_requirement_formula");
        ConfigurationSection valuesSection = c.getConfigurationSection("item_skill_requirement_penalties");
        if (valuesSection != null){
            for (String type : valuesSection.getKeys(false)){
                attributePenalties.put(type, c.getDouble("item_skill_requirement_penalties." + type));
            }
        }
        warningDisplayType = switch(c.getString("item_skill_requirement_warning", "CHAT")){
            case "CHAT" -> 2;
            case "ACTION_BAR" -> 1;
            default -> 0;
        };
        warningTooAdvanced = TranslationManager.getTranslation("warning_item_too_advanced");
        warningDisplayCooldown = c.getInt("item_skill_requirement_warning_delay", 600000);
    }

    /**
     * Returns the attribute penalty of the item
     * @param p the player for whom to check their skill requirements
     * @param item the item meta to check
     * @param attribute the attribute for which the penalty should be obtained
     * @return the penalty value
     */
    public static double getPenalty(Player p, ItemBuilder item, String attribute){
        if (!item.getMeta().getPersistentDataContainer().has(SKILL_REQUIREMENT, PersistentDataType.STRING) || !attributePenalties.containsKey(attribute)) return 0;
        double maxPenalty = attributePenalties.get(attribute);
        Collection<SkillRequirement> requirements = getSkillRequirements(item.getMeta());
        boolean shouldFulfillAny = requireAnySkillMatch(item.getMeta());
        for (SkillRequirement r : requirements){
            Profile profile = ProfileCache.getOrCache(p, r.skill.getProfileType());
            if (shouldFulfillAny){
                if (profile.getLevel() >= r.levelRequirement) return 0;
            } else {
                if (profile.getLevel() < r.levelRequirement){
                    Material stored = item.getItem().getType();
                    double fractionLevel = (double) profile.getLevel() / (double) r.levelRequirement;
                    double formulaResult = Utils.eval(penaltyScaling.replace("%fraction_level%", String.format("%.3f", fractionLevel)));
                    if (Timer.isCooldownPassed(p.getUniqueId(), "cooldown_warning_overleveled_item_" + stored.toString().toLowerCase(java.util.Locale.US))){
                        String message = warningTooAdvanced
                                .replace("%item%", ItemUtils.getItemName(item))
                                .replace("%skill%", r.skill.getDisplayName())
                                .replace("%level%", "" + r.levelRequirement);
                        if (warningDisplayType == 1 || warningDisplayType == 2){
                            switch (warningDisplayType){
                                case 1 -> Utils.sendActionBar(p, message);
                                case 2 -> Utils.sendMessage(p, message);
                            }
                            Timer.setCooldown(p.getUniqueId(), warningDisplayCooldown, "cooldown_warning_overleveled_item_" + stored.toString().toLowerCase(java.util.Locale.US));
                        }
                    }
                    return maxPenalty * Math.max(0, Math.min(1, formulaResult));
                }
            }
        }
        return 0;
    }

    /**
     * @param meta the meta to check
     * @return true if the player only needs to meet a single skill requirement to suffer no penalties, false if all skill requirements need to be met
     */
    public static boolean requireAnySkillMatch(ItemMeta meta){
        return meta.getPersistentDataContainer().has(REQUIREMENT_TYPE, PersistentDataType.INTEGER);
    }

    /**
     * Sets whether the player should meet all skill requirements of the item instead of just one.
     * @param meta the meta to set this property to
     * @param requirement true if only a single skill requirement has to be fulfilled, false if all of them have to be fulfilled
     */
    public static void setAnySkillMatch(ItemMeta meta, boolean requirement){
        if (requirement) meta.getPersistentDataContainer().set(REQUIREMENT_TYPE, PersistentDataType.INTEGER, 1);
        else meta.getPersistentDataContainer().remove(REQUIREMENT_TYPE);
    }

    /**
     * Adds a skill requirement to the item meta. If the player does not meet the requirements, the item's stats are reduced drastically.
     * @param meta the meta to set the skill requirement to
     * @param forSkill the skill for which the level has to be reached
     * @param required the level to be reached
     */
    public static void addSkillRequirement(ItemMeta meta, Skill forSkill, int required){
        Collection<SkillRequirement> existingRequirements = getSkillRequirements(meta);
        existingRequirements.add(new SkillRequirement(forSkill, required));
        setSkillRequirements(meta, existingRequirements);
    }

    /**
     * Removes all skill requirements off the item meta
     * @param meta the meta to clear
     */
    public static void removeSkillRequirements(ItemMeta meta){
        setSkillRequirements(meta, null);
        setAnySkillMatch(meta, false);
    }

    /**
     * Removes the skill requirement of a skill off the meta
     * @param meta the meta to remove the skill requirement of
     * @param skill the skill to remove its requirement of
     */
    public static void removeSkillRequirement(ItemMeta meta, String skill){
        Collection<SkillRequirement> existingRequirements = getSkillRequirements(meta);
        existingRequirements.removeIf(skillRequirement -> skillRequirement.skill.getType().equals(skill));
        setSkillRequirements(meta, existingRequirements);
    }

    /**
     * Sets all the skill requirements on the meta.
     * @param meta the meta to set its requirements on
     * @param requirements the requirements to apply
     */
    public static void setSkillRequirements(ItemMeta meta, Collection<SkillRequirement> requirements){
        if (requirements == null || requirements.isEmpty()) {
            meta.getPersistentDataContainer().remove(SKILL_REQUIREMENT);
        } else {
            meta.getPersistentDataContainer().set(SKILL_REQUIREMENT, PersistentDataType.STRING,
                    requirements.stream().map(r -> r.skill.getType() + ":" + r.levelRequirement).collect(Collectors.joining(";")));
        }
    }

    /**
     * Returns all skill requirements of the meta
     */
    public static Collection<SkillRequirement> getSkillRequirements(ItemMeta meta){
        Collection<SkillRequirement> requirements = new HashSet<>();
        if (meta == null) return requirements;
        String storedValue = ItemUtils.getPDCString(SKILL_REQUIREMENT, meta, "");
        if (StringUtils.isEmpty(storedValue)) return  requirements;
        String[] stringRequirements = storedValue.split(";");
        for (String requirement : stringRequirements){
            String[] args = requirement.split(":");
            if (args.length >= 2){
                try {
                    int level = Integer.parseInt(args[1]);
                    Skill skill = SkillRegistry.getSkill(args[0]);
                    if (skill == null) continue;
                    requirements.add(new SkillRequirement(skill, level));
                } catch (IllegalArgumentException ignored){
                }
            }
        }
        return requirements;
    }

    public record SkillRequirement(Skill skill, int levelRequirement) {}
}
