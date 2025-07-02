package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Scaling;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class AlchemyItemPropertyManager {
    private static final NamespacedKey NUMBER_TAGS = new NamespacedKey(ValhallaMMO.getInstance(), "alchemy_treatments");
    private static final NamespacedKey QUALITY_ALCHEMY = new NamespacedKey(ValhallaMMO.getInstance(), "quality_alchemy");

    private static final Map<Integer, String> qualityLore = new TreeMap<>();
    private static final Map<Integer, String> tagLore = new HashMap<>();
    private static final Map<Integer, String> tagRequiredErrors = new HashMap<>();
    private static final Map<Integer, String> tagForbiddenErrors = new HashMap<>();

    private static double qualityRoundingPrecision = 10;

    public static Map<Integer, String> getTagRequiredErrors() {
        return tagRequiredErrors;
    }

    public static Map<Integer, String> getTagLore() {
        return tagLore;
    }

    public static String getTagLore(Integer i){
        return tagLore.get(i);
    }

    public static Map<Integer, String> getTagForbiddenErrors() {
        return tagForbiddenErrors;
    }

    public static void loadConfig(){
        YamlConfiguration config = ConfigManager.getConfig("skills/alchemy.yml").get();

        qualityRoundingPrecision = config.getDouble("quality_rounding_precision", 10D);

        ConfigurationSection qualitySection = config.getConfigurationSection("quality_lore");
        if (qualitySection != null){
            for (String r : qualitySection.getKeys(false)){
                try {
                    int rating = Integer.parseInt(r);
                    String lore = TranslationManager.translatePlaceholders(config.getString("quality_lore." + r));
                    if (lore != null) qualityLore.put(rating, Utils.chat(lore));
                } catch (IllegalArgumentException ignored){
                    ValhallaMMO.logWarning("Invalid quality rating in skills/alchemy.yml: quality_lore." + r + " is not a number");
                }
            }
        }

        ConfigurationSection tagSection = config.getConfigurationSection("tag_lore");
        if (tagSection != null){
            for (String r : tagSection.getKeys(false)){
                try {
                    int tag = Integer.parseInt(r);
                    String lore = TranslationManager.translatePlaceholders(config.getString("tag_lore." + r));
                    if (lore != null) tagLore.put(tag, Utils.chat(lore));
                } catch (IllegalArgumentException ignored){
                    ValhallaMMO.logWarning("Invalid number tag in skills/alchemy.yml: tag_lore." + r + " is not a number");
                }
            }
        }

        ConfigurationSection tagForbiddenSection = config.getConfigurationSection("tag_error_disallowed");
        if (tagForbiddenSection != null){
            for (String r : tagForbiddenSection.getKeys(false)){
                try {
                    int tag = Integer.parseInt(r);
                    String lore = TranslationManager.translatePlaceholders(config.getString("tag_error_disallowed." + r));
                    if (lore != null) tagForbiddenErrors.put(tag, Utils.chat(lore));
                } catch (IllegalArgumentException ignored){
                    ValhallaMMO.logWarning("Invalid number tag in skills/alchemy.yml: tag_error_disallowed." + r + " is not a number");
                }
            }
        }

        ConfigurationSection tagRequiredSection = config.getConfigurationSection("tag_error_required");
        if (tagRequiredSection != null){
            for (String r : tagRequiredSection.getKeys(false)){
                try {
                    int tag = Integer.parseInt(r);
                    String lore = TranslationManager.translatePlaceholders(config.getString("tag_error_required." + r));
                    if (lore != null) tagRequiredErrors.put(tag, Utils.chat(lore));
                } catch (IllegalArgumentException ignored){
                    ValhallaMMO.logWarning("Invalid number tag in skills/alchemy.yml: tag_error_required." + r + " is not a number");
                }
            }
        }
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

    public static void setTagLore(ItemBuilder item){
        if (item == null) return;
        List<String> currentLore = item.getLore() != null ? item.getLore() : new ArrayList<>();
        List<String> newLore = new ArrayList<>();
        int tagIndex = -1; // the purpose of this is to track where in the lore tags are placed, so the position doesn't change
        for (String l : currentLore){
            if (tagLore.containsValue(l)){
                if (tagIndex < 0) tagIndex = currentLore.indexOf(l);
            } else newLore.add(l);
        }
        Collection<Integer> tags = CustomFlag.hasFlag(item.getMeta(), CustomFlag.HIDE_TAGS) ? new HashSet<>() : getTags(item.getMeta());
        for (Integer tag : tags.stream().filter(tagLore::containsKey).collect(Collectors.toSet())){
            if (tagIndex <= 0) newLore.add(tagLore.get(tag));
            else newLore.add(tagIndex, tagLore.get(tag));
        }
        item.lore(newLore);
    }

    public static void addTag(ItemBuilder i, Integer... tag){
        Collection<Integer> tags = getTags(i.getMeta());
        tags.addAll(Set.of(tag));
        setTags(i, tags);
    }

    public static void removeTag(ItemBuilder i, Integer... tag){
        Collection<Integer> tags = getTags(i.getMeta());
        tags.removeAll(Set.of(tag));
        setTags(i, tags);
    }

    public static void setQualityLore(ItemBuilder item){
        if (item == null) return;
        List<String> currentLore = item.getLore() != null ? item.getLore() : new ArrayList<>();
        List<String> newLore = new ArrayList<>();
        int tagIndex = -1; // the purpose of this is to track where in the lore tags are placed, so the position doesn't change
        for (String l : currentLore){
            if (qualityLore.containsValue(l)){
                if (tagIndex < 0) tagIndex = currentLore.indexOf(l);
            } else newLore.add(l);
        }
        String qualityLore = getQualityLore(getQuality(item.getMeta()));
        if (qualityLore == null || CustomFlag.hasFlag(item.getMeta(), CustomFlag.HIDE_QUALITY)) return;

        if (newLore.isEmpty() || tagIndex < 0) newLore.add(qualityLore);
        else newLore.add(tagIndex, qualityLore);

        item.lore(newLore);
    }

    public static String getQualityLore(int quality){
        String lore = null;
        for (int q : qualityLore.keySet()) if (q <= quality) lore = qualityLore.get(q);
        return lore;
    }

    public static int getQuality(ItemMeta i){
        return ItemUtils.getPDCInt(QUALITY_ALCHEMY, i, 0);
    }

    public static boolean hasAlchemyQuality(ItemMeta meta){
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(QUALITY_ALCHEMY, PersistentDataType.INTEGER);
    }

    public static void setTags(ItemBuilder item, Collection<Integer> tags){
        if (item == null) return;

        if (tags == null || tags.isEmpty()) item.getMeta().getPersistentDataContainer().remove(NUMBER_TAGS);
        else item.stringTag(NUMBER_TAGS,
                tags.stream().map(String::valueOf).collect(Collectors.joining(",")));

        setTagLore(item);
    }


    public static void setQuality(ItemBuilder item, Integer quality){
        if (item == null) return;

        if (quality == null) item.getMeta().getPersistentDataContainer().remove(QUALITY_ALCHEMY);
        else item.intTag(QUALITY_ALCHEMY, (int) Math.round(Utils.roundToMultiple(quality, qualityRoundingPrecision)));

        setQualityLore(item);
    }

    public static void applyAttributeScaling(ItemBuilder i, Scaling scaling, int quality, boolean duration, double minimumAmplifierFraction, double minimumDurationFraction){
        Map<String, PotionEffectWrapper> defaultWrappers = PotionEffectRegistry.getStoredEffects(i.getMeta(), true);
        Map<String, PotionEffectWrapper> actualWrappers = PotionEffectRegistry.getStoredEffects(i.getMeta(), false);
        Map<String, PotionEffectWrapper> newWrappers = new HashMap<>();
        for (PotionEffectWrapper wrapper : defaultWrappers.values()){
            PotionEffectWrapper actual = actualWrappers.get(wrapper.getEffect());
            if (actual == null) continue;
            double defaultStat = duration ? wrapper.getDuration() : wrapper.getAmplifier();
            double minimum = defaultStat * (duration ? minimumDurationFraction : minimumAmplifierFraction);
            if (!duration && wrapper.isVanilla()) minimum = ((1 + defaultStat) * minimum) - 1; // vanilla amplifiers start at 0, so 1 is added to make them 1. This 1 is subtracted again later.

            double result = Utils.round6Decimals(
                    scaling.evaluate(
                            scaling.getExpression().replace("%rating%", String.valueOf(quality)),
                            defaultStat
                    ));
            result = Math.max(minimum, result);

            if (duration) actual.setDuration((int) result);
            else actual.setAmplifier(wrapper.isVanilla() ? (int) result : result);
            newWrappers.put(actual.getEffect(), actual);
        }
        PotionEffectRegistry.setActualStoredEffects(i, newWrappers);
    }
}
