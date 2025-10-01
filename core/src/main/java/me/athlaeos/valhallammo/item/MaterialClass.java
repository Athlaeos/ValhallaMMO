package me.athlaeos.valhallammo.item;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.jetbrains.annotations.NotNull;

public enum MaterialClass {
    WOOD("wood_equipment", "WOODEN_PICKAXE", "WOODEN_AXE", "WOODEN_HOE", "WOODEN_SHOVEL",
    "WOODEN_SWORD", "WOODEN_SPEAR", "FISHING_ROD", "CARROT_ON_A_STICK", "WARPED_FUNGUS_ON_A_STICK"),
    BOW("material_bow", "BOW"),
    CROSSBOW("material_crossbow", "CROSSBOW"),
    LEATHER("leather_equipment", "LEATHER_BOOTS", "LEATHER_CHESTPLATE", "LEATHER_HELMET",
    "LEATHER_LEGGINGS"),
    STONE("stone_equipment", "STONE_PICKAXE", "STONE_AXE", "STONE_HOE", "STONE_SHOVEL",
    "STONE_SWORD", "STONE_SPEAR"),
    COPPER("copper_equipment", "COPPER_PICKAXE", "COPPER_AXE", "COPPER_HOE", "COPPER_SHOVEL",
    "COPPER_SWORD", "COPPER_SPEAR", "COPPER_HELMET", "COPPER_CHESTPLATE", "COPPER_LEGGINGS", "COPPER_BOOTS"),
    CHAINMAIL("chainmail_equipment", "CHAINMAIL_BOOTS", "CHAINMAIL_CHESTPLATE", "CHAINMAIL_HELMET",
    "CHAINMAIL_LEGGINGS"),
    GOLD("gold_equipment", "GOLDEN_PICKAXE", "GOLDEN_SHOVEL", "GOLDEN_HOE", "GOLDEN_AXE",
    "GOLDEN_SWORD", "GOLDEN_SPEAR", "GOLDEN_BOOTS", "GOLDEN_HELMET", "GOLDEN_CHESTPLATE", "GOLDEN_LEGGINGS"),
    IRON("iron_equipment", "IRON_PICKAXE", "IRON_SHOVEL", "IRON_HOE", "IRON_AXE",
    "IRON_SWORD", "IRON_SPEAR", "IRON_BOOTS", "IRON_HELMET", "IRON_CHESTPLATE", "IRON_LEGGINGS", "SHIELD", "FLINT_AND_STEEL", "SHEARS"),
    DIAMOND("diamond_equipment", "DIAMOND_PICKAXE", "DIAMOND_SHOVEL", "DIAMOND_HOE",
    "DIAMOND_AXE", "DIAMOND_SWORD", "DIAMOND_SPEAR", "DIAMOND_BOOTS", "DIAMOND_HELMET", "DIAMOND_CHESTPLATE", "DIAMOND_LEGGINGS"),
    NETHERITE("netherite_equipment", "NETHERITE_PICKAXE", "NETHERITE_SHOVEL", "NETHERITE_HOE",
    "NETHERITE_AXE", "NETHERITE_SWORD", "NETHERITE_SPEAR", "NETHERITE_BOOTS", "NETHERITE_HELMET", "NETHERITE_CHESTPLATE", "NETHERITE_LEGGINGS"),
    PRISMARINE("prismarine_equipment", "TRIDENT"),
    ENDERIC("enderic_equipment"),
    CUSTOM_1("custom_1_equipment"),
    CUSTOM_2("custom_2_equipment"),
    CUSTOM_3("custom_3_equipment"),
    CUSTOM_4("custom_4_equipment"),
    CUSTOM_5("custom_5_equipment"),
    CUSTOM_6("custom_6_equipment"),
    CUSTOM_7("custom_7_equipment"),
    CUSTOM_8("custom_8_equipment"),
    CUSTOM_9("custom_9_equipment"),
    CUSTOM_10("custom_10_equipment"),
    OTHER("other_materials", "MACE");

    private final Tag<Material> matchingMaterials;

    MaterialClass(String key, String... matchingMaterials){
        Set<Material> tagged = new HashSet<>(ItemUtils.getMaterialSet(matchingMaterials));
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

    private final static NamespacedKey materialTypeKey = new NamespacedKey(ValhallaMMO.getInstance(), "material_type");

    public static MaterialClass getMatchingClass(ItemMeta meta){
        if (meta == null) return null;
        Material base = ItemUtils.getStoredType(meta);
        String value = meta.getPersistentDataContainer().getOrDefault(materialTypeKey, PersistentDataType.STRING, "");
        if (!StringUtils.isEmpty(value)){
            try {
                return MaterialClass.valueOf(value);
            } catch (IllegalArgumentException ignored){}
        }
        if (base != null){
            for (MaterialClass type : values()){
                if (type.getMatchingMaterials().contains(base)) return type;
            }
        }
        return null;
    }

    public static MaterialClass getMatchingClass(Material base){
        if (base != null){
            for (MaterialClass type : values()){
                if (type.getMatchingMaterials().contains(base)) return type;
            }
        }
        return null;
    }

    public static void setMaterialType(ItemMeta meta, MaterialClass type){
        if (type == null) meta.getPersistentDataContainer().remove(materialTypeKey);
        else meta.getPersistentDataContainer().set(materialTypeKey, PersistentDataType.STRING, type.toString());
    }
}
