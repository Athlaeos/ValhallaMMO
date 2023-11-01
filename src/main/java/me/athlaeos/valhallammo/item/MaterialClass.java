package me.athlaeos.valhallammo.item;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.jetbrains.annotations.NotNull;

public enum MaterialClass {
    WOOD("wood_equipment", Material.WOODEN_PICKAXE, Material.WOODEN_AXE, Material.WOODEN_HOE, Material.WOODEN_SHOVEL,
    Material.WOODEN_SWORD, Material.FISHING_ROD, Material.CARROT_ON_A_STICK, Material.WARPED_FUNGUS_ON_A_STICK),
    BOW("material_bow", Material.BOW),
    CROSSBOW("material_crossbow", Material.CROSSBOW),
    LEATHER("leather_equipment", Material.LEATHER_BOOTS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET,
    Material.LEATHER_LEGGINGS),
    STONE("stone_equipment", Material.STONE_PICKAXE, Material.STONE_AXE, Material.STONE_HOE, Material.STONE_SHOVEL,
    Material.STONE_SWORD),
    CHAINMAIL("chainmail_equipment", Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_HELMET,
    Material.CHAINMAIL_LEGGINGS),
    GOLD("gold_equipment", Material.GOLDEN_PICKAXE, Material.GOLDEN_SHOVEL, Material.GOLDEN_HOE, Material.GOLDEN_AXE,
    Material.GOLDEN_SWORD, Material.GOLDEN_BOOTS, Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE,
    Material.GOLDEN_LEGGINGS),
    IRON("iron_equipment", Material.IRON_PICKAXE, Material.IRON_SHOVEL, Material.IRON_HOE, Material.IRON_AXE,
    Material.IRON_SWORD, Material.IRON_BOOTS, Material.IRON_HELMET, Material.IRON_CHESTPLATE,
    Material.IRON_LEGGINGS, Material.SHIELD, Material.FLINT_AND_STEEL, Material.SHEARS),
    DIAMOND("diamond_equipment", Material.DIAMOND_PICKAXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_HOE,
    Material.DIAMOND_AXE, Material.DIAMOND_SWORD, Material.DIAMOND_BOOTS, Material.DIAMOND_HELMET,
    Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS),
    NETHERITE("netherite_equipment", Material.NETHERITE_PICKAXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE,
    Material.NETHERITE_AXE, Material.NETHERITE_SWORD, Material.NETHERITE_BOOTS, Material.NETHERITE_HELMET,
    Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS),
    PRISMARINE("prismarine_equipment", Material.TRIDENT),
    ENDERIC("enderic_equipment", Material.ELYTRA),
    OTHER("other_materials");

    private final Tag<Material> matchingMaterials;

    MaterialClass(String key, Material... matchingMaterials){
        Set<Material> tagged = Set.of(matchingMaterials);
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
