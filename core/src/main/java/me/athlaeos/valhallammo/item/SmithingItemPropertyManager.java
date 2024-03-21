package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Scaling;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
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
        YamlConfiguration yaml = ConfigManager.getConfig( "skills/smithing.yml").get();

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
        return getTags(i).containsKey(treatment);
    }

    public static Map<Integer, Integer> getTags(ItemMeta i){
        Map<Integer, Integer> tags = new HashMap<>();
        String value = ItemUtils.getPDCString(NUMBER_TAGS, i, null);
        if (StringUtils.isEmpty(value)) return tags;
        for (String tag : value.split(",")){
            String[] args = tag.split(":");
            Integer id = Catch.catchOrElse(() -> Integer.parseInt(args[0]), null);
            if (id == null) continue;
            Integer level = Catch.catchOrElse(() -> Integer.parseInt(args[1]), null);
            tags.put(id, level == null ? 1 : level);
        }
        return tags;
    }

    public static void addTag(ItemMeta i, Integer tag, Integer level){
        Map<Integer, Integer> tags = getTags(i);
        tags.put(tag, level);
        setTags(i, tags);
    }

    public static void removeTag(ItemMeta i, Integer tag){
        Map<Integer, Integer> tags = getTags(i);
        tags.remove(tag);
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
        Map<Integer, Integer> tags = CustomFlag.hasFlag(meta, CustomFlag.HIDE_TAGS) ? new HashMap<>() : getTags(meta);
        for (Integer tag : tags.keySet().stream().filter(tagLore::containsKey).collect(Collectors.toSet())){
            String lore = tagLore.get(tag)
                    .replace("%lv_roman%", StringUtils.toRoman(Math.max(1, tags.get(tag))))
                    .replace("%lv_normal%", String.valueOf(tags.get(tag)));
            if (tagIndex <= 0) newLore.add(lore);
            else newLore.add(tagIndex, lore);
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
        List<String> currentLore = ItemUtils.getLore(meta);
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

    public static void setTags(ItemMeta meta, Map<Integer, Integer> tags){
        if (meta == null) return;

        if (tags == null || tags.isEmpty()) meta.getPersistentDataContainer().remove(NUMBER_TAGS);
        else meta.getPersistentDataContainer().set(NUMBER_TAGS, PersistentDataType.STRING,
                tags.entrySet().stream().map((e) -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(",")));

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


    public static void applyAttributeScaling(ItemMeta meta, Scaling scaling, int quality, String attribute, double minimumFraction){
        if (scaling == null) return;
        if (!ItemAttributesRegistry.hasCustomStats(meta)) ItemAttributesRegistry.applyVanillaStats(meta);
        AttributeWrapper defaultAttribute = ItemAttributesRegistry.getAttribute(meta, attribute, true);
        if (defaultAttribute == null) return;
        defaultAttribute = defaultAttribute.copy();

        double defaultValue = defaultAttribute.getValue();
        double result = Utils.round6Decimals(
                scaling.evaluate(
                        scaling.getExpression().replace("%rating%", String.valueOf(quality)),
                        defaultValue
                )
        );
        result = Math.max(minimumFraction * result, result);
        ItemAttributesRegistry.setStat(meta, attribute, result, defaultAttribute.isHidden(), false);
    }
}
