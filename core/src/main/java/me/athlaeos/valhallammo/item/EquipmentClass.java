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
    SWORD("swords", "GOLDEN_SWORD", "COPPER_SWORD", "STONE_SWORD", "WOODEN_SWORD", "NETHERITE_SWORD", "DIAMOND_SWORD", "IRON_SWORD"),
    SPEAR("spears", "GOLDEN_SPEAR", "COPPER_SPEAR", "STONE_SPEAR", "WOODEN_SPEAR", "NETHERITE_SPEAR", "DIAMOND_SPEAR", "IRON_SPEAR"),
    BOW("bows", "BOW"),
    CROSSBOW("crossbows", "CROSSBOW"),
    TRIDENT("tridents", "TRIDENT"),
    MACE("maces", "MACE"),
    HELMET("helmets", "PLAYER_HEAD", "SKELETON_SKULL", "ZOMBIE_HEAD", "WITHER_SKELETON_SKULL", "CARVED_PUMPKIN", "LEATHER_HELMET", "COPPER_HELMET", "CHAINMAIL_HELMET", "GOLDEN_HELMET", "IRON_HELMET", "DIAMOND_HELMET", "NETHERITE_HELMET", "TURTLE_HELMET"),
    CHESTPLATE("chestplates", "LEATHER_CHESTPLATE", "COPPER_CHESTPLATE", "CHAINMAIL_CHESTPLATE", "GOLDEN_CHESTPLATE", "IRON_CHESTPLATE", "DIAMOND_CHESTPLATE", "NETHERITE_CHESTPLATE"),
    LEGGINGS("leggings", "LEATHER_LEGGINGS", "COPPER_LEGGINGS", "CHAINMAIL_LEGGINGS", "GOLDEN_LEGGINGS", "IRON_LEGGINGS", "DIAMOND_LEGGINGS", "NETHERITE_LEGGINGS"),
    BOOTS("boots", "LEATHER_BOOTS", "COPPER_BOOTS", "CHAINMAIL_BOOTS", "GOLDEN_BOOTS", "IRON_BOOTS", "DIAMOND_BOOTS", "NETHERITE_BOOTS"),
    SHEARS("shears", "SHEARS"),
    FLINT_AND_STEEL("flint_and_steels", "FLINT_AND_STEEL"),
    FISHING_ROD("fishing_rods", "FISHING_ROD"),
    ELYTRA("elytras", "ELYTRA"),
    PICKAXE("pickaxes", "WOODEN_PICKAXE", "COPPER_PICKAXE", "STONE_PICKAXE", "GOLDEN_PICKAXE", "IRON_PICKAXE", "DIAMOND_PICKAXE", "NETHERITE_PICKAXE"),
    AXE("axes", "WOODEN_AXE", "STONE_AXE", "COPPER_AXE", "GOLDEN_AXE", "IRON_AXE", "DIAMOND_AXE", "NETHERITE_AXE"),
    SHOVEL("shovels", "WOODEN_SHOVEL", "COPPER_SHOVEL", "STONE_SHOVEL", "GOLDEN_SHOVEL", "IRON_SHOVEL", "DIAMOND_SHOVEL", "NETHERITE_SHOVEL"),
    HOE("hoes", "WOODEN_HOE", "STONE_HOE", "COPPER_HOE", "GOLDEN_HOE", "IRON_HOE", "DIAMOND_HOE", "NETHERITE_HOE"),
    SHIELD("shields", "SHIELD"),
    OTHER("other_equipment"),
    TRINKET("trinkets");

    private final Tag<Material> matchingMaterials;
    private final static NamespacedKey equipmentClassKey = ValhallaMMO.key("equipment_class");

    EquipmentClass(String key, String... matches) {
        Set<Material> tagged = new HashSet<>(ItemUtils.getMaterialSet(matches));
        this.matchingMaterials = new Tag<>() {
            private final NamespacedKey k = ValhallaMMO.key("tag_" + key);
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