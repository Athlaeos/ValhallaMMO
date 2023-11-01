package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Scaling;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.EntityEffect;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CustomDurabilityManager {
    private static final NamespacedKey DURABILITY = new NamespacedKey(ValhallaMMO.getInstance(), "durability");
    private static final NamespacedKey MAX_DURABILITY = new NamespacedKey(ValhallaMMO.getInstance(), "max_durability");

    /**
     * Damages an item's custom durability by the given amount. This method does not let you know if the item should break
     * and doesn't account for non-custom tools. It's recommended to use {@link ItemUtils#damageItem(Player, ItemStack, int, EntityEffect, boolean)}
     * to damage items as this also calls the appropriate events.
     * @param item the item to damage
     * @param damage the amount to damage the item with
     */
    public static void damage(ItemStack item, ItemMeta meta, int damage){
        if (meta == null) return;
        if (item.getType().getMaxDurability() > 0 && meta.getPersistentDataContainer().has(DURABILITY, PersistentDataType.INTEGER)){
            int durability = meta.getPersistentDataContainer().getOrDefault(DURABILITY, PersistentDataType.INTEGER, 0);
            int maxDurability = meta.getPersistentDataContainer().getOrDefault(MAX_DURABILITY, PersistentDataType.INTEGER, 0);
            if (durability > maxDurability) durability = maxDurability;
            durability = Math.max(0, Math.min(maxDurability, durability - damage));
            meta.getPersistentDataContainer().set(DURABILITY, PersistentDataType.INTEGER, durability);
            updateLore(item, meta);
        }
    }

    /**
     * Sets the item's custom current and max durability. If either durability or maxDurability are <0, remove the attributes.
     * The item's lore will be updated to reflect the durability change.
     * @param item the item to set the durability
     * @param durability the current durability to set to the item
     * @param maxDurability the max durability to set to the item
     */
    public static void setDurability(ItemStack item, ItemMeta meta, int durability, int maxDurability){
        if (meta == null) return;
        if (item.getType().getMaxDurability() > 0){
            if (durability < 0 || maxDurability < 0){
                meta.getPersistentDataContainer().remove(DURABILITY);
                meta.getPersistentDataContainer().remove(MAX_DURABILITY);
            } else {
                meta.getPersistentDataContainer().set(DURABILITY, PersistentDataType.INTEGER, Math.min(maxDurability, durability));
                meta.getPersistentDataContainer().set(MAX_DURABILITY, PersistentDataType.INTEGER, Math.max(maxDurability, durability));
            }
            updateLore(item, meta);
        }
    }

    /**
     * Sets an item's durability to a fraction of its max durability. Works for both vanilla and custom durability.
     * No matter the fraction, the new durability will always be at least 1.
     * @param item the item to set its new current durability
     * @param fraction the fraction of max durability the item should be set to
     */
    public static void setDurability(ItemStack item, ItemMeta meta, double fraction){
        if (ItemUtils.isEmpty(item)) return;
        fraction = Math.max(0, Math.min(1, fraction));
        if (meta == null) return;
        boolean hasCustom = meta.getPersistentDataContainer().has(DURABILITY, PersistentDataType.INTEGER);
        int maxDurability = hasCustom ? getDurability(item, meta, true) : item.getType().getMaxDurability();
        int newDurability = Math.max(1, (int) Math.round(fraction * maxDurability));
        if (item.getType().getMaxDurability() > 0 && meta instanceof Damageable d){
            if (hasCustom){
                setDurability(item, meta, newDurability, maxDurability);
            } else {
                d.setDamage(maxDurability - (maxDurability - newDurability));
            }
        }
    }
    public static double getDurabilityFraction(ItemStack item, ItemMeta meta){
        if (meta == null) return 0;
        boolean hasCustom = meta.getPersistentDataContainer().has(DURABILITY, PersistentDataType.INTEGER);
        int maxDurability = hasCustom ? getDurability(item, meta, true) : item.getType().getMaxDurability();
        if (item.getType().getMaxDurability() > 0 && meta instanceof Damageable d){
            if (hasCustom){
                return (double) getDurability(item, meta, false) / (double) maxDurability;
            } else {
                return (double) (maxDurability - (maxDurability - d.getDamage())) / maxDurability;
            }
        }
        return 0;
    }

    /**
     * Returns the item's current or max durability. Works for both vanilla and custom tools.
     * @param item the item to get its durability from
     * @param max whether you want to have the max durability (true) or current durability (false)
     * @return the (max)durability of the item
     */
    public static int getDurability(ItemStack item, ItemMeta meta, boolean max){
        if (ItemUtils.isEmpty(item)) return 0;
        if (meta == null) return 0;
        if (item.getType().getMaxDurability() > 0 && meta instanceof Damageable d){
            boolean hasCustom = meta.getPersistentDataContainer().has(DURABILITY, PersistentDataType.INTEGER);
            if (hasCustom)
                return Math.max(0, meta.getPersistentDataContainer().getOrDefault(max ? MAX_DURABILITY : DURABILITY, PersistentDataType.INTEGER, 0));
            else {
                return max ? item.getType().getMaxDurability() : item.getType().getMaxDurability() - d.getDamage();
            }
        }
        return 0;
    }

    /**
     * @param meta the item meta to check if it has custom durability
     * @return true if it has custom durability, false otherwise
     */
    public static boolean hasCustomDurability(ItemMeta meta){
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(DURABILITY, PersistentDataType.INTEGER) ||
                meta.getPersistentDataContainer().has(MAX_DURABILITY, PersistentDataType.INTEGER);
    }

    /**
     * Updates the item's lore to reflect custom durability changes, or removes it if the item has no custom durability.
     * Lore is also removed if the item has the HIDE_DURABILITY {@link CustomFlag}
     * @param item the item to update
     */
    public static void updateLore(ItemStack item, ItemMeta meta){
        if (meta == null) return;
        String translation = TranslationManager.getTranslation("translation_durability");
        if (item.getType().getMaxDurability() > 0 && meta instanceof Damageable d){
            int maxVanillaDurability = item.getType().getMaxDurability();
            int maxCustomDurability = getDurability(item, meta, true);
            int customDurability = getDurability(item, meta, false);
            double fraction = Math.max(0, Math.min(1, (double) customDurability / (double) maxCustomDurability));
            int newVanillaDurability = maxVanillaDurability - (int) Math.ceil(fraction * maxVanillaDurability);
            d.setDamage(newVanillaDurability);

            if (CustomFlag.hasFlag(meta, CustomFlag.HIDE_DURABILITY) || !hasCustomDurability(meta)){
                ItemUtils.removeIfLoreContains(meta, translation);
            } else {
                ItemUtils.replaceOrAddLore(meta,
                        translation,
                        String.format("%s %d/%d", translation, customDurability, maxCustomDurability));
            }
        } else {
            ItemUtils.removeIfLoreContains(meta, translation);
        }
    }

    /**
     * Scales an item's durability values given a quality value. This method is coded differently from {@link SmithingItemPropertyManager#applyAttributeScaling(ItemStack, ItemMeta, int, String, double)}
     * as that method requires items to have a default custom durability attribute, which vanilla items are not configured to have.
     * @param item the item to scale its durability based on quality
     * @param quality the quality to scale the item with
     * @param minimumFraction the minimum fraction of the item's max durability the item should be left with after calculations
     */
    public static void applyDurabilityScaling(ItemStack item, ItemMeta meta, int quality, double minimumFraction){
        if (ItemUtils.isEmpty(item)) return;
        Scaling scaling = SmithingItemPropertyManager.getScaling(item, meta, "CUSTOM_MAX_DURABILITY");
        if (meta == null || scaling == null) return;
        if (item.getType().getMaxDurability() > 0 && meta instanceof Damageable d){
            AttributeWrapper durabilityWrapper = ItemAttributesRegistry.getAttribute(meta, "CUSTOM_MAX_DURABILITY", true);
            int defaultDurability = durabilityWrapper == null ? item.getType().getMaxDurability() : (int) durabilityWrapper.getValue();
            int minimum = (int) Math.round(Math.max(1, minimumFraction * defaultDurability));
            int newMaxDurability = Math.max(minimum, (int) Math.round(scaling.evaluate(scaling.getExpression().replace("%rating%", String.valueOf(quality)), defaultDurability)));
            double fraction;
            if (hasCustomDurability(meta)){
                fraction = (double) getDurability(item, meta, false) / (double) getDurability(item, meta, true);
            } else {
                fraction = (double) (item.getType().getMaxDurability() - d.getDamage()) / (double) item.getType().getMaxDurability();
            }
            fraction = Math.max(0, Math.min(1, fraction));
            int newDurability = (int) Math.ceil(fraction * newMaxDurability);
            setDurability(item, meta, newDurability, newMaxDurability);
        }
    }
}
