package me.athlaeos.valhallammo.skills.skills.implementations.smithing;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Scaling;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.MaterialClass;
import me.athlaeos.valhallammo.item.attributes.AttributeWrapper;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class SmithingItemPropertyManager {
    private static final NamespacedKey NUMBER_TAGS = new NamespacedKey(ValhallaMMO.getInstance(), "smithing_treatments");
    private static final NamespacedKey QUALITY_SMITHING = new NamespacedKey(ValhallaMMO.getInstance(), "quality_smithing");
    private static final NamespacedKey NEUTRAL_QUALITY = new NamespacedKey(ValhallaMMO.getInstance(), "neutral_quality");
    private static final NamespacedKey COUNTER = new NamespacedKey(ValhallaMMO.getInstance(), "counter");
    private static final NamespacedKey COUNTER_LIMIT = new NamespacedKey(ValhallaMMO.getInstance(), "counter_limit");

    private static final Map<Integer, String> qualityLore = new TreeMap<>();
    private static final Map<Integer, String> tagLore = new HashMap<>();
    private static final Map<Integer, String> tagRequiredErrors = new HashMap<>();
    private static final Map<Integer, String> tagForbiddenErrors = new HashMap<>();
    private static final Map<String, Map<MaterialClass, Scaling>> materialScalings = new HashMap<>();

    public static void register(String attribute, MaterialClass materialClass, Scaling scaling){
        if (attribute == null || materialClass == null || scaling == null) return;
        Map<MaterialClass, Scaling> newScaling = materialScalings.getOrDefault(attribute, new HashMap<>());
        newScaling.put(materialClass, scaling);
        materialScalings.put(attribute, newScaling);
    }

    public static void loadConfig(){
        String config = "skills/smithing.yml";
        YamlConfiguration yaml = ConfigManager.getConfig(config).get();

        for (MaterialClass materialClass : MaterialClass.values()){
            register("CUSTOM_MAX_DURABILITY", materialClass, Scaling.fromConfig(config, "scaling_durability." + materialClass.toString().toLowerCase()));
            register("GENERIC_ATTACK_DAMAGE", materialClass, Scaling.fromConfig(config, "scaling_damage." + materialClass.toString().toLowerCase()));
            register("GENERIC_ATTACK_SPEED", materialClass, Scaling.fromConfig(config, "scaling_speed." + materialClass.toString().toLowerCase()));
            register("GENERIC_ARMOR", materialClass, Scaling.fromConfig(config, "scaling_armor." + materialClass.toString().toLowerCase()));
            register("GENERIC_ARMOR_TOUGHNESS", materialClass, Scaling.fromConfig(config, "scaling_armor_toughness." + materialClass.toString().toLowerCase()));
            register("GENERIC_KNOCKBACK_RESISTANCE", materialClass, Scaling.fromConfig(config, "scaling_armor_knockbackresist." + materialClass.toString().toLowerCase()));
            register("GENERIC_MAX_HEALTH", materialClass, Scaling.fromConfig(config, "scaling_health." + materialClass.toString().toLowerCase()));
            register("GENERIC_MOVEMENT_SPEED", materialClass, Scaling.fromConfig(config, "scaling_movement_speed." + materialClass.toString().toLowerCase()));
            register("CUSTOM_KNOCKBACK", materialClass, Scaling.fromConfig(config, "scaling_knockback." + materialClass.toString().toLowerCase()));
            register("CUSTOM_DAMAGE_RESISTANCE", materialClass, Scaling.fromConfig(config, "scaling_damage_resistance." + materialClass.toString().toLowerCase()));
            register("CUSTOM_EXPLOSION_RESISTANCE", materialClass, Scaling.fromConfig(config, "scaling_explosion_resistance." + materialClass.toString().toLowerCase()));
            register("CUSTOM_FIRE_RESISTANCE", materialClass, Scaling.fromConfig(config, "scaling_fire_resistance." + materialClass.toString().toLowerCase()));
            register("CUSTOM_POISON_RESISTANCE", materialClass, Scaling.fromConfig(config, "scaling_poison_resistance." + materialClass.toString().toLowerCase()));
            register("CUSTOM_MAGIC_RESISTANCE", materialClass, Scaling.fromConfig(config, "scaling_magic_resistance." + materialClass.toString().toLowerCase()));
            register("CUSTOM_PROJECTILE_RESISTANCE", materialClass, Scaling.fromConfig(config, "scaling_projectile_resistance." + materialClass.toString().toLowerCase()));
        }
        register("CUSTOM_DRAW_STRENGTH", MaterialClass.BOW, Scaling.fromConfig(config,"scaling_shot_power.bow"));
        register("CUSTOM_DRAW_STRENGTH", MaterialClass.CROSSBOW, Scaling.fromConfig(config,"scaling_shot_power.crossbow"));

        ConfigurationSection qualitySection = yaml.getConfigurationSection("quality_lore");
        if (qualitySection != null){
            for (String r : qualitySection.getKeys(false)){
                try {
                    int rating = Integer.parseInt(r);
                    String lore = TranslationManager.translatePlaceholders(yaml.getString("quality_lore." + r));
                    if (lore != null) qualityLore.put(rating, Utils.chat(lore));
                } catch (IllegalArgumentException ignored){
                    ValhallaMMO.logWarning("Invalid quality rating in skills/smithing.yml: quality_lore." + r + " is not a number");
                }
            }
        }

        ConfigurationSection tagSection = yaml.getConfigurationSection("tag_lore");
        if (tagSection != null){
            for (String r : tagSection.getKeys(false)){
                try {
                    int tag = Integer.parseInt(r);
                    String lore = TranslationManager.translatePlaceholders(yaml.getString("tag_lore." + r));
                    if (lore != null) tagLore.put(tag, Utils.chat(lore));
                } catch (IllegalArgumentException ignored){
                    ValhallaMMO.logWarning("Invalid number tag in skills/smithing.yml: tag_lore." + r + " is not a number");
                }
            }
        }

        ConfigurationSection tagForbiddenSection = yaml.getConfigurationSection("tag_error_disallowed");
        if (tagForbiddenSection != null){
            for (String r : tagForbiddenSection.getKeys(false)){
                try {
                    int tag = Integer.parseInt(r);
                    String lore = TranslationManager.translatePlaceholders(yaml.getString("tag_error_disallowed." + r));
                    if (lore != null) tagForbiddenErrors.put(tag, Utils.chat(lore));
                } catch (IllegalArgumentException ignored){
                    ValhallaMMO.logWarning("Invalid number tag in skills/smithing.yml: tag_error_disallowed." + r + " is not a number");
                }
            }
        }

        ConfigurationSection tagRequiredSection = yaml.getConfigurationSection("tag_error_required");
        if (tagRequiredSection != null){
            for (String r : tagRequiredSection.getKeys(false)){
                try {
                    int tag = Integer.parseInt(r);
                    String lore = TranslationManager.translatePlaceholders(yaml.getString("tag_error_required." + r));
                    if (lore != null) tagRequiredErrors.put(tag, Utils.chat(lore));
                } catch (IllegalArgumentException ignored){
                    ValhallaMMO.logWarning("Invalid number tag in skills/smithing.yml: tag_error_required." + r + " is not a number");
                }
            }
        }
    }

    public static void reload(){
        qualityLore.clear();
        materialScalings.clear();
        loadConfig();
    }
    
    public static Scaling getScaling(ItemStack i, ItemMeta meta, String type){
        MaterialClass match = MaterialClass.getMatchingClass(meta);
        if (match == null) match = MaterialClass.OTHER;
        return materialScalings.getOrDefault(type, new HashMap<>()).get(match);
    }
    
    public static boolean hasTag(ItemMeta i, int treatment){
        return getTags(i).contains(treatment);
    }

    public static Collection<Integer> getTags(ItemMeta i){
        Collection<Integer> tags = new HashSet<>();
        String value = ItemUtils.getPDCString(NUMBER_TAGS, i, null);
        if (StringUtils.isEmpty(value)) return tags;
        for (String tag : value.split(",")){
            try {
                tags.add(Integer.parseInt(tag));
            } catch (NumberFormatException ignored){
            }
        }
        return tags;
    }

    public static void addTag(ItemMeta i, Integer... tag){
        Collection<Integer> tags = getTags(i);
        tags.addAll(Set.of(tag));
        setTags(i, tags);
    }

    public static void removeTag(ItemMeta i, Integer... tag){
        Collection<Integer> tags = getTags(i);
        tags.removeAll(Set.of(tag));
        setTags(i, tags);
    }

    public static void setTagLore(ItemMeta meta){
        if (meta == null) return;
        List<String> currentLore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        List<String> newLore = new ArrayList<>();
        int tagIndex = -1; // the purpose of this is to track where in the lore tags are placed, so the position doesn't change
        for (String l : currentLore){
            if (tagLore.containsValue(l)){
                if (tagIndex < 0) tagIndex = currentLore.indexOf(l);
            } else newLore.add(l);
        }
        Collection<Integer> tags = CustomFlag.hasFlag(meta, CustomFlag.HIDE_TAGS) ? new HashSet<>() : getTags(meta);
        for (Integer tag : tags.stream().filter(tagLore::containsKey).collect(Collectors.toSet())){
            if (tagIndex <= 0) newLore.add(tagLore.get(tag));
            else newLore.add(tagIndex, tagLore.get(tag));
        }
        meta.setLore(newLore);
    }

    public static String getTagLore(Integer i){
        return tagLore.get(i);
    }

    public static Map<Integer, String> getTagForbiddenErrors() {
        return tagForbiddenErrors;
    }

    public static Map<Integer, String> getTagRequiredErrors() {
        return tagRequiredErrors;
    }

    public static void setQualityLore(ItemMeta meta){
        if (meta == null) return;
        List<String> currentLore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        List<String> newLore = new ArrayList<>();
        int tagIndex = -1; // the purpose of this is to track where in the lore tags are placed, so the position doesn't change
        for (String l : currentLore){
            if (qualityLore.containsValue(l)){
                if (tagIndex < 0) tagIndex = currentLore.indexOf(l);
            } else newLore.add(l);
        }
        String qualityLore = getQualityLore(getQuality(meta), getNeutralQuality(meta));
        if (qualityLore == null || CustomFlag.hasFlag(meta, CustomFlag.HIDE_QUALITY)) return;

        if (newLore.isEmpty() || tagIndex < 0) newLore.add(qualityLore);
        else newLore.add(tagIndex, qualityLore);

        meta.setLore(newLore);
    }

    /**
     * Returns the lore an item should have given a quality value and neutral quality. <br>
     * In the recipes each item is arbitrarily given a quality value at which point they should be "vanilla or better" or
     * what this plugin considers "good" by default. That is the neutralQuality parameter in this method. <br>
     * This allows the plugin to assign different lores to different items, even if they have the same skill. <br>
     * If neutralQuality wasn't used (or is 0) a diamond pickaxe of, say, 50 quality would be considered "great" even if
     * its stats are significantly worse than vanilla's. Now we assign a neutralQuality of 70 to the diamond pickaxe,
     * at which point 50 quality would be considered "decent" as it's still 20 quality points under that required 70.
     * @param quality the quality of the item
     * @param neutralQuality the quality value at which point the item should be "good" or neutral
     * @return the lore of the relative quality of the item.
     */
    public static String getQualityLore(int quality, int neutralQuality){
        String lore = null;
        for (int q : qualityLore.keySet()) if (q + neutralQuality <= quality) lore = qualityLore.get(q);
        return lore;
    }

    public static int getQuality(ItemMeta meta){
        return ItemUtils.getPDCInt(QUALITY_SMITHING, meta, 0);
    }

    public static int getNeutralQuality(ItemMeta meta){
        return ItemUtils.getPDCInt(NEUTRAL_QUALITY, meta, 0);
    }

    public static int getCounter(ItemMeta meta){
        return ItemUtils.getPDCInt(COUNTER, meta, 0);
    }

    public static boolean hasSmithingQuality(ItemMeta meta){
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(QUALITY_SMITHING, PersistentDataType.INTEGER);
    }

    public static void setTags(ItemMeta meta, Collection<Integer> tags){
        if (meta == null) return;

        if (tags == null || tags.isEmpty()) meta.getPersistentDataContainer().remove(NUMBER_TAGS);
        else meta.getPersistentDataContainer().set(NUMBER_TAGS, PersistentDataType.STRING,
                tags.stream().map(String::valueOf).collect(Collectors.joining(",")));

        setTagLore(meta);
    }

    public static void setQuality(ItemMeta meta, Integer quality){
        if (meta == null) return;

        if (quality == null) meta.getPersistentDataContainer().remove(QUALITY_SMITHING);
        else meta.getPersistentDataContainer().set(QUALITY_SMITHING, PersistentDataType.INTEGER, quality);

        setQualityLore(meta);
    }

    public static void setNeutralQuality(ItemMeta meta, Integer quality){
        if (meta == null) return;

        if (quality == null) meta.getPersistentDataContainer().remove(NEUTRAL_QUALITY);
        else meta.getPersistentDataContainer().set(NEUTRAL_QUALITY, PersistentDataType.INTEGER, quality);

        setQualityLore(meta);
    }

    public static void setCounter(ItemMeta meta, Integer counter){
        if (meta == null) return;

        if (counter == null) meta.getPersistentDataContainer().remove(COUNTER);
        else meta.getPersistentDataContainer().set(COUNTER, PersistentDataType.INTEGER, counter);
    }

    public static int getCounterLimit(ItemMeta meta){
        return ItemUtils.getPDCInt(COUNTER_LIMIT, meta, 0);
    }

    public static void setCounterLimit(ItemMeta meta, Integer limit){
        if (meta == null) return;

        if (limit == null) meta.getPersistentDataContainer().remove(COUNTER_LIMIT);
        else meta.getPersistentDataContainer().set(COUNTER_LIMIT, PersistentDataType.INTEGER, limit);
    }


    public static void applyAttributeScaling(ItemStack i, ItemMeta meta, int quality, String attribute, double minimumFraction){
        if (ItemUtils.isEmpty(i)) return;
        Scaling scaling = getScaling(i, meta, attribute);
        if (scaling == null) return;
        if (ItemAttributesRegistry.getStats(meta, false).isEmpty()) ItemAttributesRegistry.applyVanillaStats(i, meta);
        AttributeWrapper defaultAttribute = ItemAttributesRegistry.getAttribute(meta, attribute, true);
        if (defaultAttribute == null) return;
        double defaultValue = defaultAttribute.getValue();
        double result = Utils.round6Decimals(
                scaling.evaluate(
                        scaling.getExpression().replace("%rating%", String.valueOf(quality)),
                        defaultValue
                ));
        result = Math.max(minimumFraction * result, result);
        ItemAttributesRegistry.setStat(i, meta, attribute, result, false);
    }
}
