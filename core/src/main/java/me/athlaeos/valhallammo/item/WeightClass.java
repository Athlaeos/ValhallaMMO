package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public enum WeightClass {
    LIGHT("LEATHER_HELMET", "LEATHER_CHESTPLATE", "LEATHER_LEGGINGS", "LEATHER_BOOTS",
            "CHAINMAIL_HELMET", "CHAINMAIL_CHESTPLATE", "CHAINMAIL_LEGGINGS", "CHAINMAIL_BOOTS",
            "DIAMOND_HELMET", "DIAMOND_CHESTPLATE", "DIAMOND_LEGGINGS", "DIAMOND_BOOTS",
            "TURTLE_HELMET", "WOODEN_SWORD", "STONE_SWORD", "IRON_SWORD", "GOLDEN_SWORD",
            "DIAMOND_SWORD", "NETHERITE_SWORD"),
    HEAVY("GOLDEN_HELMET", "GOLDEN_CHESTPLATE", "GOLDEN_LEGGINGS", "GOLDEN_BOOTS",
            "IRON_HELMET", "IRON_CHESTPLATE", "IRON_LEGGINGS", "IRON_BOOTS",
            "NETHERITE_HELMET", "NETHERITE_CHESTPLATE", "NETHERITE_LEGGINGS", "NETHERITE_BOOTS",
            "WOODEN_AXE", "STONE_AXE", "IRON_AXE", "GOLDEN_AXE", "DIAMOND_AXE",
            "NETHERITE_AXE", "TRIDENT", "MACE"),
    WEIGHTLESS();

    private static final NamespacedKey WEIGHT_CLASS = new NamespacedKey(ValhallaMMO.getInstance(), "weight_class");
    private final Collection<Material> matchingMaterials = new HashSet<>();

    WeightClass(String... matches){
        this.matchingMaterials.addAll(ItemUtils.getMaterialSet(matches));
    }

    public static WeightClass getWeightClass(ItemMeta meta){
        if (meta == null) return WEIGHTLESS;
        Material stored = ItemUtils.getStoredType(meta);
        String value = ItemUtils.getPDCString(WEIGHT_CLASS, meta, null);
        if (value != null){
            try {
                return WeightClass.valueOf(value);
            } catch (IllegalArgumentException ignored) {}
        }
        if (stored != null){
            for (WeightClass type : values()){
                if (type.matchingMaterials.contains(stored)) return type;
            }
        }
        return WEIGHTLESS;
    }

    public static boolean hasDefinedWeightClass(ItemMeta meta){
        if (meta == null) return false;
        Material stored = ItemUtils.getStoredType(meta);
        String value = ItemUtils.getPDCString(WEIGHT_CLASS, meta, null);
        if (value != null){
            try {
                return true;
            } catch (IllegalArgumentException ignored) {}
        }
        if (stored != null){
            for (WeightClass type : values()){
                if (type.matchingMaterials.contains(stored)) return true;
            }
        }
        return false;
    }

    public static void setWeightClass(ItemMeta meta, WeightClass weightClass){
        if (meta == null) return;
        if (weightClass == null) meta.getPersistentDataContainer().remove(WEIGHT_CLASS);
        else meta.getPersistentDataContainer().set(WEIGHT_CLASS, PersistentDataType.STRING, weightClass.toString());
    }

    public static int getArmorWeightClassCount(LivingEntity entity, WeightClass weightClass){
        int count = 0;
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return 0;
        if (!ItemUtils.isEmpty(equipment.getHelmet()) && getWeightClass(ItemUtils.getItemMeta(equipment.getHelmet())) == weightClass) count++;
        if (!ItemUtils.isEmpty(equipment.getChestplate()) && getWeightClass(ItemUtils.getItemMeta(equipment.getChestplate())) == weightClass) count++;
        if (!ItemUtils.isEmpty(equipment.getLeggings()) && getWeightClass(ItemUtils.getItemMeta(equipment.getLeggings())) == weightClass) count++;
        if (!ItemUtils.isEmpty(equipment.getBoots()) && getWeightClass(ItemUtils.getItemMeta(equipment.getBoots())) == weightClass) count++;
        return count;
    }

    public Collection<Material> getMatchingMaterials() {
        return matchingMaterials;
    }
}
