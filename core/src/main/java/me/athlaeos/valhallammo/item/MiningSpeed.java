package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.utility.BlockUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MiningSpeed {
    private static final NamespacedKey SPEED_MULTIPLIER_BASE = new NamespacedKey(ValhallaMMO.getInstance(), "mining_speed_multiplier_base");
    private static final NamespacedKey SPEED_MULTIPLIER_SPECIFIC = new NamespacedKey(ValhallaMMO.getInstance(), "mining_speed_multiplier_specific");
    private static final NamespacedKey HARDNESS_TRANSLATIONS = new NamespacedKey(ValhallaMMO.getInstance(), "mining_hardness_translations");

    private static final Map<Material, Double> defaultMultipliers = new HashMap<>();
    private static final Map<Material, Map<Material, Double>> defaultExceptions = new HashMap<>();
    static {
        setDefaultMultiplier(2, "WOODEN_AXE", "WOODEN_HOE", "WOODEN_SHOVEL", "WOODEN_PICKAXE");
        setDefaultMultiplier(12, "GOLDEN_AXE", "GOLDEN_HOE", "GOLDEN_SHOVEL", "GOLDEN_PICKAXE");
        setDefaultMultiplier(4, "STONE_AXE", "STONE_HOE", "STONE_SHOVEL", "STONE_PICKAXE");
        setDefaultMultiplier(6, "IRON_AXE", "IRON_HOE", "IRON_SHOVEL", "IRON_PICKAXE");
        setDefaultMultiplier(8, "DIAMOND_AXE", "DIAMOND_HOE", "DIAMOND_SHOVEL", "DIAMOND_PICKAXE");
        setDefaultMultiplier(9, "NETHERITE_AXE", "NETHERITE_HOE", "NETHERITE_SHOVEL", "NETHERITE_PICKAXE");

        addDefaultExceptionTools(15, "COBWEB", "WOODEN_SWORD", "GOLDEN_SWORD", "STONE_SWORD", "IRON_SWORD", "DIAMOND_SWORD", "NETHERITE_SWORD");
        addDefaultExceptionTools(30, "BAMBOO", "WOODEN_SWORD", "GOLDEN_SWORD", "STONE_SWORD", "IRON_SWORD", "DIAMOND_SWORD", "NETHERITE_SWORD");
        addDefaultExceptionTools(30, "BAMBOO_SAPLING", "WOODEN_SWORD", "GOLDEN_SWORD", "STONE_SWORD", "IRON_SWORD", "DIAMOND_SWORD", "NETHERITE_SWORD");

        addDefaultException(1.5, Set.of("COCOA", "HAY_BALE", "JACK_O_LANTERN", "CARVED_PUMPKIN", "MELON", "PUMPKIN", "GLOW_LICHEN",
                "OAK_LEAVES", "BIRCH_LEAVES", "SPRUCE_LEAVES", "ACACIA_LEAVES", "DARK_OAK_LEAVES", "JUNGLE_LEAVES", "MANGROVE_LEAVES",
                "CHERRY_LEAVES", "AZALEA_LEAVES", "FLOWERING_AZALEA_LEAVES"),
                Set.of("WOODEN_SWORD", "GOLDEN_SWORD", "STONE_SWORD", "IRON_SWORD", "DIAMOND_SWORD", "NETHERITE_SWORD"));

        addDefaultExceptionTools(50, "COBWEB", "SHEARS");
        addDefaultExceptionBlocks(7, "SHEARS", "OAK_LEAVES", "BIRCH_LEAVES", "SPRUCE_LEAVES", "ACACIA_LEAVES",
                "DARK_OAK_LEAVES", "JUNGLE_LEAVES", "MANGROVE_LEAVES", "CHERRY_LEAVES", "AZALEA_LEAVES", "FLOWERING_AZALEA_LEAVES");
        addDefaultExceptionBlocks(5, "SHEARS", "WHITE_WOOL", "BLACK_WOOL", "BLUE_WOOL", "BROWN_WOOL",
                "CYAN_WOOL", "GRAY_WOOL", "GREEN_WOOL", "LIGHT_BLUE_WOOL", "LIGHT_GRAY_WOOL", "LIME_WOOL", "MAGENTA_WOOL",
                "ORANGE_WOOL", "PINK_WOOL", "PURPLE_WOOL", "RED_WOOL", "YELLOW_WOOL");
    }

    public static void addDefaultException(double multiplier, Collection<String> blocks, Collection<String> tools){
        for (String bS : blocks){
            Material b = ItemUtils.stringToMaterial(bS, null);
            if (b == null) continue;
            for (String tS : tools){
                Material t = ItemUtils.stringToMaterial(tS, null);
                if (t == null) continue;
                addDefaultException(multiplier, b, t);
            }
        }
    }

    public static void addDefaultExceptionBlocks(double multiplier, String tool, String... blocks){
        Material t = ItemUtils.stringToMaterial(tool, null);
        if (t == null) return;
        for (String bS : blocks){
            Material b = ItemUtils.stringToMaterial(bS, null);
            if (b == null) continue;
            addDefaultException(multiplier, b, t);
        }
    }

    public static void addDefaultExceptionTools(double multiplier, String block, String... tools){
        Material b = ItemUtils.stringToMaterial(block, null);
        if (b == null) return;
        for (String tS : tools){
            Material t = ItemUtils.stringToMaterial(tS, null);
            if (t == null) continue;
            addDefaultException(multiplier, b, t);
        }
    }

    public static void addDefaultException(double multiplier, Material block, Material tool){
        Map<Material, Double> exceptions = defaultExceptions.getOrDefault(tool, new HashMap<>());
        exceptions.put(block, multiplier);
        defaultExceptions.put(tool, exceptions);
    }

    public static void setDefaultMultiplier(double multiplier, Material... matches){
        for (Material m : matches){
            defaultMultipliers.put(m, multiplier);
        }
    }

    public static void setHardnessTranslations(ItemMeta m, Map<Material, Material> translations){
        m.getPersistentDataContainer().set(HARDNESS_TRANSLATIONS, PersistentDataType.STRING,
                translations.keySet().stream().map(
                        b -> b.toString() + ":" + translations.get(b).toString()
                ).collect(Collectors.joining(";"))
        );
    }

    public static void setDefaultMultiplier(double multiplier, String... matches){
        setDefaultMultiplier(multiplier, ItemUtils.getMaterialSet(matches).toArray(new Material[0]));
    }

    public static double getMultiplier(ItemMeta m){
        Material stored = ItemUtils.getStoredType(m);
        return m.getPersistentDataContainer().getOrDefault(SPEED_MULTIPLIER_BASE, PersistentDataType.DOUBLE, defaultMultipliers.getOrDefault(stored, 1D));
    }

    public static double getMultiplier(ItemMeta m, Material b){
        Map<Material, Double> exceptions = getExceptions(m);
        if (exceptions.containsKey(b)) return exceptions.get(b);
        Material stored = ItemUtils.getStoredType(m);
        AttributeWrapper miningLevelWrapper = ItemAttributesRegistry.getAttribute(m, "MINING_SPEED", false);
        if (miningLevelWrapper != null) return miningLevelWrapper.getValue();
        return m.getPersistentDataContainer().getOrDefault(SPEED_MULTIPLIER_BASE, PersistentDataType.DOUBLE, defaultMultipliers.getOrDefault(stored, 1D));
    }

    public static void setMultiplier(ItemMeta m, double multiplier){
        m.getPersistentDataContainer().set(SPEED_MULTIPLIER_BASE, PersistentDataType.DOUBLE, multiplier);
    }

    public static void addException(ItemMeta m, Material exception, double multiplier){
        Map<Material, Double> exceptions = getExceptions(m);
        exceptions.put(exception, multiplier);
        setExceptions(m, exceptions);
    }

    public static void addHardnessTranslation(ItemMeta m, Material from, Material to){
        Map<Material, Material> translations = getHardnessTranslations(m);
        translations.put(from, to);
        setHardnessTranslations(m, translations);
    }

    public static void setExceptions(ItemMeta m, Map<Material, Double> exceptions){
        m.getPersistentDataContainer().set(SPEED_MULTIPLIER_SPECIFIC, PersistentDataType.STRING,
                exceptions.keySet().stream().map(
                        b -> b.toString() + ":" + exceptions.get(b)
                ).collect(Collectors.joining(";"))
        );
    }

    public static Map<Material, Double> getExceptions(ItemMeta m){
        String stored = ItemUtils.getPDCString(SPEED_MULTIPLIER_SPECIFIC, m, "");
        Map<Material, Double> exceptions = new HashMap<>();
        if (!stored.isEmpty()){
            for (String exceptionString : stored.split(";")){
                String[] args = exceptionString.split(":");
                try {
                    Material b = Material.valueOf(args[0]);
                    double multiplier = StringUtils.parseDouble(args[1]);
                    exceptions.put(b, multiplier);
                } catch (IllegalArgumentException ignored){}
            }
        }
        return exceptions;
    }

    public static Map<Material, Material> getHardnessTranslations(ItemMeta m){
        String stored = ItemUtils.getPDCString(HARDNESS_TRANSLATIONS, m, "");
        Map<Material, Material> exceptions = new HashMap<>();
        if (!stored.isEmpty()){
            for (String exceptionString : stored.split(";")){
                String[] args = exceptionString.split(":");
                try {
                    Material b = Material.valueOf(args[0]);
                    Material t = Material.valueOf(args[1]);
                    exceptions.put(b, t);
                } catch (IllegalArgumentException ignored){}
            }
        }
        return exceptions;
    }

    public static float getHardness(ItemMeta m, Block block){
        if (BlockUtils.hasCustomHardness(block)) return BlockUtils.getHardness(block);
        Map<Material, Material> hardnessTranslations = getHardnessTranslations(m);
        if (hardnessTranslations.containsKey(block.getType())) return hardnessTranslations.get(block.getType()).getHardness();
        else return BlockUtils.getHardness(block);
    }
}
