package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Scaling;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
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
    public static void damage(ItemBuilder item, int damage){
        Material baseType = item.getItem().getType();
        if (baseType.getMaxDurability() > 0 && item.getMeta().getPersistentDataContainer().has(DURABILITY, PersistentDataType.INTEGER)){
            int durability = item.getMeta().getPersistentDataContainer().getOrDefault(DURABILITY, PersistentDataType.INTEGER, 0);
            int maxDurability = item.getMeta().getPersistentDataContainer().getOrDefault(MAX_DURABILITY, PersistentDataType.INTEGER, 0);
            if (durability > maxDurability) durability = maxDurability;
            durability = Math.max(0, Math.min(maxDurability, durability - damage));
            item.intTag(DURABILITY, durability);
            updateLore(item);
        }
    }

    /**
     * Sets the item's custom current and max durability. If either durability or maxDurability are <0, remove the attributes.
     * The item's lore will be updated to reflect the durability change.
     * @param item the item to set the durability
     * @param durability the current durability to set to the item
     * @param maxDurability the max durability to set to the item
     */
    public static void setDurability(ItemBuilder item, int durability, int maxDurability){
        Material baseType = item.getItem().getType();
        if (baseType.getMaxDurability() > 0){
            if (durability < 0 || maxDurability < 0){
                item.getMeta().getPersistentDataContainer().remove(DURABILITY);
                item.getMeta().getPersistentDataContainer().remove(MAX_DURABILITY);
            } else {
                item.intTag(DURABILITY, Math.min(maxDurability, durability));
                item.intTag(DURABILITY, Math.max(maxDurability, durability));
            }
            updateLore(item);
        }
    }

    /**
     * Sets an item's durability to a fraction of its max durability. Works for both vanilla and custom durability.
     * No matter the fraction, the new durability will always be at least 1.
     * @param item the item to set its new current durability
     * @param fraction the fraction of max durability the item should be set to
     */
    public static void setDurability(ItemBuilder item, double fraction){
        Material baseType = item.getItem().getType();
        fraction = Math.max(0, Math.min(1, fraction));
        boolean hasCustom = item.getMeta().getPersistentDataContainer().has(DURABILITY, PersistentDataType.INTEGER);
        int maxDurability = hasCustom ? getDurability(item.getMeta(), true) : baseType.getMaxDurability();
        int newDurability = Math.max(1, (int) Math.round(fraction * maxDurability));
        if (baseType.getMaxDurability() > 0 && item instanceof Damageable d){
            if (hasCustom){
                setDurability(item, newDurability, maxDurability);
            } else {
                d.setDamage(maxDurability - (maxDurability - newDurability));
            }
        }
    }
    public static double getDurabilityFraction(ItemMeta meta){
        Material baseType = ItemUtils.getStoredType(meta);
        if (baseType == null) return 0;
        boolean hasCustom = meta.getPersistentDataContainer().has(DURABILITY, PersistentDataType.INTEGER);
        int maxDurability = hasCustom ? getDurability(meta, true) : baseType.getMaxDurability();
        if (baseType.getMaxDurability() > 0 && meta instanceof Damageable d){
            if (hasCustom){
                return (double) getDurability(meta, false) / (double) maxDurability;
            } else {
                return (double) (maxDurability - (maxDurability - d.getDamage())) / maxDurability;
            }
        }
        return 0;
    }

    /**
     * Returns the item's current or max durability. Works for both vanilla and custom tools.
     * @param meta the item to get its durability from
     * @param max whether you want to have the max durability (true) or current durability (false)
     * @return the (max)durability of the item
     */
    public static int getDurability(ItemMeta meta, boolean max){
        Material baseType = ItemUtils.getStoredType(meta);
        if (baseType == null) return 0;
        if (baseType.getMaxDurability() > 0 && meta instanceof Damageable d){
            boolean hasCustom = meta.getPersistentDataContainer().has(DURABILITY, PersistentDataType.INTEGER);
            if (hasCustom)
                return Math.max(0, meta.getPersistentDataContainer().getOrDefault(max ? MAX_DURABILITY : DURABILITY, PersistentDataType.INTEGER, 0));
            else {
                return max ? baseType.getMaxDurability() : baseType.getMaxDurability() - d.getDamage();
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
    public static void updateLore(ItemBuilder item){
        Material baseType = item.getItem().getType();
        String translation = TranslationManager.getTranslation("translation_durability");
        if (baseType.getMaxDurability() > 0 && item instanceof Damageable d){
            int maxVanillaDurability = baseType.getMaxDurability();
            int maxCustomDurability = getDurability(item.getMeta(), true);
            int customDurability = getDurability(item.getMeta(), false);
            double fraction = Math.max(0, Math.min(1, (double) customDurability / (double) maxCustomDurability));
            int newVanillaDurability = maxVanillaDurability - (int) Math.ceil(fraction * maxVanillaDurability);
            d.setDamage(newVanillaDurability);

            if (CustomFlag.hasFlag(item.getMeta(), CustomFlag.HIDE_DURABILITY) || !hasCustomDurability(item.getMeta())){
                ItemUtils.removeIfLoreContains(item, translation);
            } else {
                ItemUtils.replaceOrAddLore(item,
                        translation,
                        String.format("%s %d/%d", translation, customDurability, maxCustomDurability));
            }
        } else {
            ItemUtils.removeIfLoreContains(item, translation);
        }
    }

    /**
     * Scales an item's durability values given a quality value. This method is coded differently from {@link SmithingItemPropertyManager#applyAttributeScaling(ItemMeta, Scaling, int, String, double)}
     * as that method requires items to have a default custom durability attribute, which vanilla items are not configured to have.
     * @param item the item to scale its durability based on quality
     * @param quality the quality to scale the item with
     * @param minimumFraction the minimum fraction of the item's max durability the item should be left with after calculations
     */
    public static void applyDurabilityScaling(ItemBuilder item, Scaling scaling, int quality, double minimumFraction){
        Material baseMaterial = item.getItem().getType();
        if (baseMaterial.getMaxDurability() > 0 && item instanceof Damageable d){
            AttributeWrapper durabilityWrapper = ItemAttributesRegistry.getAttribute(item.getMeta(), "CUSTOM_MAX_DURABILITY", true);
            int defaultDurability = durabilityWrapper == null ? baseMaterial.getMaxDurability() : (int) durabilityWrapper.getValue();
            int minimum = (int) Math.round(Math.max(1, minimumFraction * defaultDurability));
            int newMaxDurability = Math.max(minimum, (int) Math.round(scaling.evaluate(scaling.getExpression().replace("%rating%", String.valueOf(quality)), defaultDurability)));
            double fraction;
            if (hasCustomDurability(item.getMeta())){
                fraction = (double) getDurability(item.getMeta(), false) / (double) getDurability(item.getMeta(), true);
            } else {
                fraction = (double) (baseMaterial.getMaxDurability() - d.getDamage()) / (double) baseMaterial.getMaxDurability();
            }
            fraction = Math.max(0, Math.min(1, fraction));
            int newDurability = (int) Math.ceil(fraction * newMaxDurability);
            setDurability(item, newDurability, newMaxDurability);
        }
    }
}
