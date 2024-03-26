package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class FoodPropertyManager {
    private static final Map<Material, FoodProperties> defaultProperties = new HashMap<>();

    static {
        setDefaultProperties(Material.ENCHANTED_GOLDEN_APPLE, FoodClass.MAGICAL, 4, 9.6F);
        setDefaultProperties(Material.GOLDEN_APPLE, FoodClass.MAGICAL, 4, 9.6F);
        setDefaultProperties(Material.GOLDEN_CARROT, FoodClass.MAGICAL, 6, 14.4f);

        setDefaultProperties(Material.COOKED_CHICKEN, FoodClass.MEAT, 6, 7.2F);
        setDefaultProperties(Material.CHICKEN, FoodClass.MEAT, 2, 1.2F);
        setDefaultProperties(Material.COOKED_MUTTON, FoodClass.MEAT, 6, 9.6F);
        setDefaultProperties(Material.MUTTON, FoodClass.MEAT, 2, 1.2F);
        setDefaultProperties(Material.COOKED_PORKCHOP, FoodClass.MEAT, 8, 12.8F);
        setDefaultProperties(Material.PORKCHOP, FoodClass.MEAT, 3, 1.8F);
        setDefaultProperties(Material.COOKED_RABBIT, FoodClass.MEAT, 5, 6F);
        setDefaultProperties(Material.RABBIT, FoodClass.MEAT, 3, 1.8F);
        setDefaultProperties(Material.COOKED_BEEF, FoodClass.MEAT, 8, 12.8F);
        setDefaultProperties(Material.BEEF, FoodClass.MEAT, 3, 1.8F);
        setDefaultProperties(Material.RABBIT_STEW, FoodClass.MEAT, 10, 12F);

        setDefaultProperties(Material.SALMON, FoodClass.SEAFOOD, 2, 0.4F);
        setDefaultProperties(Material.COOKED_SALMON, FoodClass.SEAFOOD, 6, 9.6F);
        setDefaultProperties(Material.COD, FoodClass.SEAFOOD, 2, 0.4F);
        setDefaultProperties(Material.COOKED_COD, FoodClass.SEAFOOD, 5, 6F);
        setDefaultProperties(Material.TROPICAL_FISH, FoodClass.SEAFOOD, 1, 0.2F);

        setDefaultProperties(Material.BAKED_POTATO, FoodClass.VEGETABLE, 5, 6F);
        setDefaultProperties(Material.BEETROOT, FoodClass.VEGETABLE, 1, 1.2F);
        setDefaultProperties(Material.BEETROOT_SOUP, FoodClass.VEGETABLE, 6, 7.2F);
        setDefaultProperties(Material.CARROT, FoodClass.VEGETABLE, 3, 3.6F);
        setDefaultProperties(Material.DRIED_KELP, FoodClass.VEGETABLE, 1, 0.6F);
        setDefaultProperties(Material.MUSHROOM_STEW, FoodClass.VEGETABLE, 6, 7.2F);
        setDefaultProperties(Material.POTATO, FoodClass.VEGETABLE, 1, 0.6F);
        setDefaultProperties(Material.SUSPICIOUS_STEW, FoodClass.VEGETABLE, 6, 7.2F);

        setDefaultProperties(Material.BREAD, FoodClass.GRAIN, 5, 6f);

        setDefaultProperties(Material.CAKE, FoodClass.SWEET, 14, 2.8F);
        setDefaultProperties(Material.COOKIE, FoodClass.SWEET, 2, 0.4F);
        setDefaultProperties(Material.HONEY_BOTTLE, FoodClass.SWEET, 6, 1.2F);
        setDefaultProperties(Material.PUMPKIN_PIE, FoodClass.SWEET, 8, 4.8F);

        setDefaultProperties(Material.POISONOUS_POTATO, FoodClass.SPOILED, 2, 1.2F);
        setDefaultProperties(Material.PUFFERFISH, FoodClass.SPOILED, 1, 0.2F);
        setDefaultProperties(Material.ROTTEN_FLESH, FoodClass.SPOILED, 4, 0.8F);
        setDefaultProperties(Material.SPIDER_EYE, FoodClass.SPOILED, 2, 3.2F);

        setDefaultProperties(Material.APPLE, FoodClass.FRUIT, 4, 2.4F);
        setDefaultProperties(Material.CHORUS_FRUIT, FoodClass.FRUIT, 4, 2.4F);
        setDefaultProperties(Material.GLOW_BERRIES, FoodClass.FRUIT, 2, 0.4F);
        setDefaultProperties(Material.MELON_SLICE, FoodClass.FRUIT, 2, 1.2F);
        setDefaultProperties(Material.SWEET_BERRIES, FoodClass.FRUIT, 2, 0.4F);

        setDefaultProperties(Material.MILK_BUCKET, FoodClass.DAIRY, 0, 0F);

        setDefaultProperties(Material.POTION, FoodClass.BEVERAGE, 0, 0F);
    }

    private static void setDefaultProperties(Material m, FoodClass type, int foodValue, float saturationValue){
        defaultProperties.put(m, new FoodProperties(type, foodValue, saturationValue));
    }

    private static final NamespacedKey FOOD_CLASS = new NamespacedKey(ValhallaMMO.getInstance(), "food_class");
    private static final NamespacedKey FOOD_VALUE = new NamespacedKey(ValhallaMMO.getInstance(), "food_value");
    private static final NamespacedKey SATURATION_VALUE = new NamespacedKey(ValhallaMMO.getInstance(), "saturation_value");
    private static final NamespacedKey CANCEL_POTION_EFFECTS = new NamespacedKey(ValhallaMMO.getInstance(), "cancel_potion_effects");
    private static final NamespacedKey CANCEL_DIMINISHING_RETURNS = new NamespacedKey(ValhallaMMO.getInstance(), "cancel_diminishing_returns");

    public static boolean isCustomFood(ItemMeta meta){
        PersistentDataContainer c = meta.getPersistentDataContainer();
        return c.has(FOOD_CLASS, PersistentDataType.STRING) || c.has(FOOD_VALUE, PersistentDataType.INTEGER) ||
                c.has(SATURATION_VALUE, PersistentDataType.DOUBLE) || c.has(CANCEL_POTION_EFFECTS, PersistentDataType.SHORT) ||
                c.has(CANCEL_DIMINISHING_RETURNS, PersistentDataType.SHORT);
    }

    /**
     * @param meta the food item
     * @return true if the food item doesn't contribute to diminishing returns, false if it does
     */
    public static boolean shouldCancelDiminishingReturns(ItemMeta meta){
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(CANCEL_DIMINISHING_RETURNS, PersistentDataType.SHORT);
    }

    /**
     * Sets whether the food item should not contribute to diminishing returns
     * @param meta the food item
     * @param cancel true if the food item shouldn't contribute to diminishing returns
     */
    public static void setCancelDiminishingReturns(ItemMeta meta, boolean cancel){
        if (meta == null) return;
        if (!cancel) meta.getPersistentDataContainer().remove(CANCEL_DIMINISHING_RETURNS);
        else meta.getPersistentDataContainer().set(CANCEL_DIMINISHING_RETURNS, PersistentDataType.SHORT, (short) 1);
    }

    /**
     * @param meta the food item
     * @return true if the food item shouldn't grant the potion effects it has by default, if any (does not include customly added potion effects). An example of such an item is a golden apple
     */
    public static boolean shouldCancelDefaultPotionEffects(ItemMeta meta){
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(CANCEL_POTION_EFFECTS, PersistentDataType.SHORT);
    }

    /**
     * Sets whether the food item should grant its potion effects or not (for example, golden apples)
     * @param meta the food item
     * @param cancel true if the food item shouldn't grants any potion effects it has by default (golden apple, rotten flesh, pufferfish, etc.)
     */
    public static void setCancelPotionEffects(ItemMeta meta, boolean cancel){
        if (meta == null) return;
        if (!cancel) meta.getPersistentDataContainer().remove(CANCEL_POTION_EFFECTS);
        else meta.getPersistentDataContainer().set(CANCEL_POTION_EFFECTS, PersistentDataType.SHORT, (short) 1);
    }

    /**
     * The saturation value of the food item
     * @param i the base food item, only used for its material to determine default its value
     * @param meta the meta of the food item
     * @return the saturation value the food item will grant. If no custom saturation value is present, the default is used
     */
    public static float getSaturationValue(ItemStack i, ItemMeta meta){
        if (meta == null) return 0;
        FoodProperties properties = defaultProperties.get(i.getType());
        float defaultSaturation = properties == null ? 0 : properties.saturation;
        return meta.getPersistentDataContainer().getOrDefault(SATURATION_VALUE, PersistentDataType.FLOAT, defaultSaturation);
    }

    /**
     * Sets the custom saturation of the food item
     * @param meta the food item meta
     * @param foodValue the new saturation value. If null, custom saturation value is removed
     */
    public static void setSaturationValue(ItemMeta meta, Float foodValue){
        if (meta == null) return;
        if (foodValue == null) meta.getPersistentDataContainer().remove(FOOD_VALUE);
        else meta.getPersistentDataContainer().set(SATURATION_VALUE, PersistentDataType.FLOAT, foodValue);
    }

    public static int getFoodValue(ItemStack i, ItemMeta meta){
        if (meta == null) return 0;
        FoodProperties properties = defaultProperties.get(i.getType());
        int defaultFood = properties == null ? 0 : properties.food;
        return meta.getPersistentDataContainer().getOrDefault(FOOD_VALUE, PersistentDataType.INTEGER, defaultFood);
    }

    public static void setFoodValue(ItemMeta meta, Integer foodValue){
        if (meta == null) return;
        if (foodValue == null) meta.getPersistentDataContainer().remove(FOOD_VALUE);
        else meta.getPersistentDataContainer().set(FOOD_VALUE, PersistentDataType.INTEGER, foodValue);
    }

    public static FoodClass getFoodClass(ItemStack i, ItemMeta meta){
        if (meta == null) return null;
        String value = ItemUtils.getPDCString(FOOD_CLASS, meta, null);
        if (value != null){
            try {
                return FoodClass.valueOf(value);
            } catch (IllegalArgumentException ignored) {}
        }
        FoodProperties properties = defaultProperties.get(i.getType());
        return properties == null ? null : properties.type;
    }

    public static void setFoodClass(ItemMeta meta, FoodClass foodClass){
        if (meta == null) return;
        if (foodClass == null) meta.getPersistentDataContainer().remove(FOOD_CLASS);
        else meta.getPersistentDataContainer().set(FOOD_CLASS, PersistentDataType.STRING, foodClass.toString());
    }

    private record FoodProperties(FoodClass type, int food, float saturation) {}
}
