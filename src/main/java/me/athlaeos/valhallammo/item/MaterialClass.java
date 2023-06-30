package me.athlaeos.valhallammo.item;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.utility.StringUtils;

public enum MaterialClass {
    WOOD(Material.WOODEN_PICKAXE, Material.WOODEN_AXE, Material.WOODEN_HOE, Material.WOODEN_SHOVEL,
    Material.WOODEN_SWORD, Material.FISHING_ROD, Material.CARROT_ON_A_STICK, Material.WARPED_FUNGUS_ON_A_STICK),
    BOW(Material.BOW),
    CROSSBOW(Material.CROSSBOW),
    LEATHER(Material.LEATHER_BOOTS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET,
    Material.LEATHER_LEGGINGS),
    STONE(Material.STONE_PICKAXE, Material.STONE_AXE, Material.STONE_HOE, Material.STONE_SHOVEL,
    Material.STONE_SWORD),
    CHAINMAIL(Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_HELMET,
    Material.CHAINMAIL_LEGGINGS),
    GOLD(Material.GOLDEN_PICKAXE, Material.GOLDEN_SHOVEL, Material.GOLDEN_HOE, Material.GOLDEN_AXE,
    Material.GOLDEN_SWORD, Material.GOLDEN_BOOTS, Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE,
    Material.GOLDEN_LEGGINGS),
    IRON(Material.IRON_PICKAXE, Material.IRON_SHOVEL, Material.IRON_HOE, Material.IRON_AXE,
    Material.IRON_SWORD, Material.IRON_BOOTS, Material.IRON_HELMET, Material.IRON_CHESTPLATE,
    Material.IRON_LEGGINGS, Material.SHIELD, Material.FLINT_AND_STEEL, Material.SHEARS),
    DIAMOND(Material.DIAMOND_PICKAXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_HOE,
    Material.DIAMOND_AXE, Material.DIAMOND_SWORD, Material.DIAMOND_BOOTS, Material.DIAMOND_HELMET,
    Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS),
    NETHERITE(Material.NETHERITE_PICKAXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE,
    Material.NETHERITE_AXE, Material.NETHERITE_SWORD, Material.NETHERITE_BOOTS, Material.NETHERITE_HELMET,
    Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS),
    PRISMARINE(Material.TRIDENT),
    MEMBRANE(Material.ELYTRA),
    OTHER();

    private Collection<Material> matchingMaterials = new HashSet<>();

    MaterialClass(Material... matchingMaterials){
        this.matchingMaterials.addAll(Arrays.asList(matchingMaterials));
    }

    public Collection<Material> getMatchingMaterials() {
        return matchingMaterials;
    }

    private final static NamespacedKey materialTypeKey = new NamespacedKey(ValhallaMMO.getInstance(), "material_type");

    public static MaterialClass getMatchingClass(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        String value = meta.getPersistentDataContainer().getOrDefault(materialTypeKey, PersistentDataType.STRING, "");
        if (!StringUtils.isEmpty(value)){
            try {
                return MaterialClass.valueOf(value);
            } catch (IllegalArgumentException ignored){}
        }
        for (MaterialClass type : values()){
            if (type.getMatchingMaterials().contains(item.getType())) return type;
        }
        return OTHER;
    }

    public static void setMaterialType(ItemStack item, MaterialClass type){
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (type == null) meta.getPersistentDataContainer().remove(materialTypeKey);
        else meta.getPersistentDataContainer().set(materialTypeKey, PersistentDataType.STRING, type.toString());
        item.setItemMeta(meta);
    }
}
