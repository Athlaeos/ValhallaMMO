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
        setDefaultProperties(Material.ENCHANTED_GOLDEN_APPLE, 4, 9.6F, FoodClass.MAGICAL, FoodClass.FRUIT, FoodClass.SWEET);
        setDefaultProperties(Material.GOLDEN_APPLE, 4, 9.6F, FoodClass.MAGICAL, FoodClass.FRUIT, FoodClass.SWEET);
        setDefaultProperties(Material.GOLDEN_CARROT, 6, 14.4f, FoodClass.MAGICAL, FoodClass.VEGETABLE);

        setDefaultProperties(Material.COOKED_CHICKEN, 6, 7.2F, FoodClass.MEAT);
        setDefaultProperties(Material.CHICKEN, 2, 1.2F, FoodClass.MEAT, FoodClass.RAW);
        setDefaultProperties(Material.COOKED_MUTTON, 6, 9.6F, FoodClass.MEAT);
        setDefaultProperties(Material.MUTTON, 2, 1.2F, FoodClass.MEAT, FoodClass.RAW);
        setDefaultProperties(Material.COOKED_PORKCHOP, 8, 12.8F, FoodClass.MEAT);
        setDefaultProperties(Material.PORKCHOP, 3, 1.8F, FoodClass.MEAT, FoodClass.RAW);
        setDefaultProperties(Material.COOKED_RABBIT, 5, 6F, FoodClass.MEAT);
        setDefaultProperties(Material.RABBIT, 3, 1.8F, FoodClass.MEAT, FoodClass.RAW);
        setDefaultProperties(Material.COOKED_BEEF, 8, 12.8F, FoodClass.MEAT);
        setDefaultProperties(Material.BEEF, 3, 1.8F, FoodClass.MEAT, FoodClass.RAW);
        setDefaultProperties(Material.RABBIT_STEW, 10, 12F, FoodClass.MEAT);

        setDefaultProperties(Material.SALMON, 2, 0.4F, FoodClass.SEAFOOD, FoodClass.RAW);
        setDefaultProperties(Material.COOKED_SALMON, 6, 9.6F, FoodClass.SEAFOOD);
        setDefaultProperties(Material.COD, 2, 0.4F, FoodClass.SEAFOOD, FoodClass.RAW);
        setDefaultProperties(Material.COOKED_COD, 5, 6F, FoodClass.SEAFOOD);
        setDefaultProperties(Material.TROPICAL_FISH, 1, 0.2F, FoodClass.SEAFOOD);

        setDefaultProperties(Material.BAKED_POTATO, 5, 6F, FoodClass.VEGETABLE);
        setDefaultProperties(Material.BEETROOT, 1, 1.2F, FoodClass.VEGETABLE);
        setDefaultProperties(Material.BEETROOT_SOUP, 6, 7.2F, FoodClass.VEGETABLE);
        setDefaultProperties(Material.CARROT, 3, 3.6F, FoodClass.VEGETABLE);
        setDefaultProperties(Material.DRIED_KELP, 1, 0.6F, FoodClass.VEGETABLE);
        setDefaultProperties(Material.MUSHROOM_STEW, 6, 7.2F, FoodClass.VEGETABLE);
        setDefaultProperties(Material.POTATO, 1, 0.6F, FoodClass.VEGETABLE, FoodClass.RAW);
        setDefaultProperties(Material.SUSPICIOUS_STEW, 6, 7.2F, FoodClass.VEGETABLE);

        setDefaultProperties(Material.BREAD, 5, 6f, FoodClass.GRAIN);

        setDefaultProperties(Material.CAKE, 14, 2.8F, FoodClass.SWEET, FoodClass.GRAIN);
        setDefaultProperties(Material.COOKIE, 2, 0.4F, FoodClass.SWEET, FoodClass.GRAIN);
        setDefaultProperties(Material.HONEY_BOTTLE, 6, 1.2F, FoodClass.SWEET);
        setDefaultProperties(Material.PUMPKIN_PIE, 8, 4.8F, FoodClass.SWEET, FoodClass.GRAIN);

        setDefaultProperties(Material.POISONOUS_POTATO, 2, 1.2F, FoodClass.VEGETABLE, FoodClass.SPOILED);
        setDefaultProperties(Material.PUFFERFISH, 1, 0.2F, FoodClass.SEAFOOD, FoodClass.SPOILED);
        setDefaultProperties(Material.ROTTEN_FLESH, 4, 0.8F, FoodClass.MEAT, FoodClass.SPOILED);
        setDefaultProperties(Material.SPIDER_EYE, 2, 3.2F, FoodClass.MEAT, FoodClass.SPOILED);

        setDefaultProperties(Material.APPLE, 4, 2.4F, FoodClass.FRUIT, FoodClass.SWEET);
        setDefaultProperties(Material.CHORUS_FRUIT, 4, 2.4F, FoodClass.FRUIT);
        setDefaultProperties(Material.GLOW_BERRIES, 2, 0.4F, FoodClass.FRUIT, FoodClass.SWEET);
        setDefaultProperties(Material.MELON_SLICE, 2, 1.2F, FoodClass.FRUIT, FoodClass.SWEET);
        setDefaultProperties(Material.SWEET_BERRIES, 2, 0.4F, FoodClass.FRUIT, FoodClass.SWEET);

        setDefaultProperties(Material.MILK_BUCKET, 0, 0F, FoodClass.DAIRY, FoodClass.BEVERAGE);

        setDefaultProperties(Material.POTION, 0, 0F, FoodClass.BEVERAGE, FoodClass.MAGICAL);
    }

    private static void setDefaultProperties(Material m, int foodValue, float saturationValue, FoodClass... types){
        defaultProperties.put(m, new FoodProperties(new HashSet<>(Set.of(types)), foodValue, saturationValue));
    }

    private static final NamespacedKey FOOD_CLASSES = ValhallaMMO.key("food_class");
    private static final NamespacedKey FOOD_VALUE = ValhallaMMO.key("food_value");
    private static final NamespacedKey SATURATION_VALUE = ValhallaMMO.key("saturation_value");
    private static final NamespacedKey CANCEL_POTION_EFFECTS = ValhallaMMO.key("cancel_potion_effects");
    private static final NamespacedKey CANCEL_DIMINISHING_RETURNS = ValhallaMMO.key("cancel_diminishing_returns");

    public static boolean isCustomFood(ItemMeta meta){
        PersistentDataContainer c = meta.getPersistentDataContainer();
        return c.has(FOOD_CLASSES, PersistentDataType.STRING) || c.has(FOOD_VALUE, PersistentDataType.INTEGER) ||
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

    public static Collection<FoodClass> getFoodClasses(ItemStack i, ItemMeta meta){
        if (meta == null) return null;
        String value = ItemUtils.getPDCString(FOOD_CLASSES, meta, null);
        if (value != null){
            String[] types = value.split(";");
            Collection<FoodClass> classes = new HashSet<>();
            for (String type : types){
                try {
                    classes.add(FoodClass.valueOf(type));
                } catch (IllegalArgumentException ignored) {}
            }
            return classes.isEmpty() ? null : classes;
        }
        FoodProperties properties = defaultProperties.get(i.getType());
        return properties == null ? null : properties.types;
    }

    public static void setFoodClasses(ItemMeta meta, Collection<FoodClass> foodClasses){
        if (meta == null) return;
        if (foodClasses == null || foodClasses.isEmpty()) meta.getPersistentDataContainer().remove(FOOD_CLASSES);
        else meta.getPersistentDataContainer().set(FOOD_CLASSES, PersistentDataType.STRING, String.join(";", foodClasses.stream().map(FoodClass::toString).toList()));
    }

    private record FoodProperties(Collection<FoodClass> types, int food, float saturation) {}
}
