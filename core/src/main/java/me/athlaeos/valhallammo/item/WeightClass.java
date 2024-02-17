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
    LIGHT(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
            Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
            Material.TURTLE_HELMET, Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD,
            Material.DIAMOND_SWORD, Material.NETHERITE_SWORD),
    HEAVY(Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS,
            Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE,
            Material.NETHERITE_AXE, Material.TRIDENT),
    WEIGHTLESS();

    private static final NamespacedKey WEIGHT_CLASS = new NamespacedKey(ValhallaMMO.getInstance(), "weight_class");
    private final Collection<Material> matchingMaterials = new HashSet<>();

    WeightClass(Material... matches){
        this.matchingMaterials.addAll(Arrays.asList(matches));
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
