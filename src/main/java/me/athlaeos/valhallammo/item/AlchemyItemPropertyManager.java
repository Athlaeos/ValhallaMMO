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
        String qualityLore = getQualityLore(getQuality(meta));
        if (qualityLore == null || CustomFlag.hasFlag(meta, CustomFlag.HIDE_QUALITY)) return;

        if (newLore.isEmpty() || tagIndex < 0) newLore.add(qualityLore);
        else newLore.add(tagIndex, qualityLore);

        meta.setLore(newLore);
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

    public static void setTags(ItemMeta meta, Collection<Integer> tags){
        if (meta == null) return;

        if (tags.isEmpty()) meta.getPersistentDataContainer().remove(NUMBER_TAGS);
        else meta.getPersistentDataContainer().set(NUMBER_TAGS, PersistentDataType.STRING,
                tags.stream().map(String::valueOf).collect(Collectors.joining(",")));

        setTagLore(meta);
    }


    public static void setQuality(ItemMeta meta, Integer quality){
        if (meta == null) return;

        if (quality == null) meta.getPersistentDataContainer().remove(QUALITY_ALCHEMY);
        else meta.getPersistentDataContainer().set(QUALITY_ALCHEMY, PersistentDataType.INTEGER, quality);

        setQualityLore(meta);
    }

    public static void applyAttributeScaling(ItemMeta i, Scaling scaling, int quality, boolean duration, double minimumAmplifierFraction, double minimumDurationFraction){
        Map<String, PotionEffectWrapper> defaultWrappers = PotionEffectRegistry.getStoredEffects(i, true);
        Map<String, PotionEffectWrapper> newWrappers = new HashMap<>();
        for (PotionEffectWrapper wrapper : defaultWrappers.values()){
            double defaultStat = duration ? wrapper.getDuration() : wrapper.getAmplifier();
            double minimum = defaultStat * (duration ? minimumDurationFraction : minimumAmplifierFraction);
            if (!duration && wrapper.isVanilla()) minimum = ((1 + defaultStat) * minimum) - 1; // vanilla amplifiers start at 0, so 1 is added to make them 1. This 1 is subtracted again later.

            double result = Utils.round6Decimals(
                    scaling.evaluate(
                            scaling.getExpression().replace("%rating%", String.valueOf(quality)),
                            defaultStat
                    ));
            result = Math.max(minimum, result);

            if (duration) wrapper.setDuration((int) result);
            else wrapper.setAmplifier(wrapper.isVanilla() ? (int) result : result);
            newWrappers.put(wrapper.getEffect(), wrapper);
        }
        PotionEffectRegistry.setActualStoredEffects(i, newWrappers);
    }
}
