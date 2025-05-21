package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public enum EquipmentClass {
    SWORD("swords", Material.GOLDEN_SWORD, Material.STONE_SWORD, Material.WOODEN_SWORD, Material.NETHERITE_SWORD, Material.DIAMOND_SWORD, Material.IRON_SWORD),
    BOW("bows", Material.BOW),
    CROSSBOW("crossbows", Material.CROSSBOW),
    TRIDENT("tridents", Material.TRIDENT),
    HELMET("helmets", Material.PLAYER_HEAD, Material.SKELETON_SKULL, Material.ZOMBIE_HEAD, Material.WITHER_SKELETON_SKULL, Material.CARVED_PUMPKIN, Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.GOLDEN_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.TURTLE_HELMET),
    CHESTPLATE("chestplates", Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE),
    LEGGINGS("leggings", Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS),
    BOOTS("boots", Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.GOLDEN_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS),
    SHEARS("shears", Material.SHEARS),
    FLINT_AND_STEEL("flint_and_steels", Material.FLINT_AND_STEEL),
    FISHING_ROD("fishing_rods", Material.FISHING_ROD),
    ELYTRA("elytras", Material.ELYTRA),
    PICKAXE("pickaxes", Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.GOLDEN_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE),
    AXE("axes", Material.WOODEN_AXE, Material.STONE_AXE, Material.GOLDEN_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE),
    SHOVEL("shovels", Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.GOLDEN_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL),
    HOE("hoes", Material.WOODEN_HOE, Material.STONE_HOE, Material.GOLDEN_HOE, Material.IRON_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE),
    SHIELD("shields", Material.SHIELD),
    OTHER("other_equipment"),
    TRINKET("trinkets");

    private final Tag<Material> matchingMaterials;
    private final static NamespacedKey equipmentClassKey = new NamespacedKey(ValhallaMMO.getInstance(), "equipment_class");

    EquipmentClass(String key, Material... matches) {
        Set<Material> tagged = Set.of(matches);
        this.matchingMaterials = new Tag<>() {
            private final NamespacedKey k = new NamespacedKey(ValhallaMMO.getInstance(), "tag_" + key);
            @Override public boolean isTagged(@NotNull Material material) { return tagged.contains(material); }
            @NotNull @Override public Set<Material> getValues() { return tagged; }
            @NotNull @Override public NamespacedKey getKey() { return k; }
        };
    }


    public Tag<Material> getTag() {
        return matchingMaterials;
    }
    public Collection<Material> getMatchingMaterials() {
        return matchingMaterials.getValues();
    }

    public static EquipmentClass getMatchingClass(ItemMeta meta) {
        if (meta == null) return null;
        Material base = ItemUtils.getStoredType(meta);
        if (meta.getPersistentDataContainer().has(equipmentClassKey, PersistentDataType.STRING)) {
            String value = meta.getPersistentDataContainer().get(equipmentClassKey, PersistentDataType.STRING);
            if (value != null) {
                try {
                    return EquipmentClass.valueOf(value);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        if (base != null) return getMatchingClass(base);
        return null;
    }

    public static EquipmentClass getMatchingClass(Material material){
        for (EquipmentClass tc : EquipmentClass.values()) {
            if (tc.getMatchingMaterials().contains(material)) {
                return tc;
            }
        }
        return null;
    }

    public static void setEquipmentClass(ItemMeta meta, EquipmentClass type) {
        if (type == null)
            meta.getPersistentDataContainer().remove(equipmentClassKey);
        else
            meta.getPersistentDataContainer().set(equipmentClassKey, PersistentDataType.STRING, type.toString());
    }

    public static boolean isArmor(ItemMeta meta) {
        EquipmentClass equipmentClass = getMatchingClass(meta);
        return equipmentClass != null && equipmentClass.isArmor();
    }

    public static boolean isHandHeld(ItemMeta meta) {
        EquipmentClass equipmentClass = getMatchingClass(meta);
        return equipmentClass != null && !equipmentClass.isArmor();
    }

    public static boolean isArmor(EquipmentClass equipmentClass) {
        return equipmentClass != null && equipmentClass.isArmor();
    }

    public boolean isArmor() {
        return this == HELMET || this == CHESTPLATE || this == LEGGINGS || this == BOOTS;
    }
}
