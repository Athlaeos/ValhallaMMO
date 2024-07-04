package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.utility.BlockUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
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

    private static final NamespacedKey EMBEDDED_TOOL_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "embedded_tools");

    /**
     * Adds an embedded tool to the given item meta
     * @param m the item on which to add an embedded tool
     * @param tool the embedded tool to add
     */
    public static void addEmbeddedTool(ItemMeta m, ItemStack tool){
        Collection<EmbeddedTool> embeddedTools = getEmbeddedTools(m);
        embeddedTools.add(new EmbeddedTool(tool));
        setEmbeddedTools(m, embeddedTools);
    }

    /**
     * Adds an embedded tool to the given item meta
     * @param m the item on which to add an embedded tool
     * @param tool the embedded tool to add
     */
    public static void addEmbeddedTool(ItemMeta m, Material tool){
        Collection<EmbeddedTool> embeddedTools = getEmbeddedTools(m);
        embeddedTools.add(new EmbeddedTool(tool));
        setEmbeddedTools(m, embeddedTools);
    }

    /**
     * Returns the best tool embedded in the given item meta to mine the given block. If the best embedded tool is
     * only a material, then a new ItemStack is created on which the given meta is applied. If the best embedded tool
     * is already a full ItemStack, then it is returned instead.
     * @param m The item meta to get the most optimal embedded tool from
     * @param b the block which is being used to determine the most optimal tool to mine it
     * @return the most optimal tool in the form of an ItemStack with which to mine the block with, or null if no tools are stored
     */
    public static ItemBuilder getOptimalEmbeddedTool(ItemMeta m, Block b){
        Collection<EmbeddedTool> embeddedTools = getEmbeddedTools(m);
        return getOptimalEmbeddedTool(embeddedTools, m, b);
    }

    /**
     * Returns the best tool embedded in the given item meta to mine the given block. If the best embedded tool is
     * only a material, then a new ItemStack is created on which the given meta is applied. If the best embedded tool
     * is already a full ItemStack, then it is returned instead.
     * @param m The item meta to get the most optimal embedded tool from
     * @param b the block which is being used to determine the most optimal tool to mine it
     * @return the most optimal tool in the form of an ItemStack with which to mine the block with, or null if no tools are stored
     */
    public static ItemBuilder getOptimalEmbeddedTool(Collection<EmbeddedTool> embeddedTools, ItemMeta m, Block b){
        if (embeddedTools.isEmpty()) return null;
        ItemBuilder bestTool = null;
        float bestMiningPower = 1;
        for (EmbeddedTool tool : embeddedTools){
            ItemBuilder item;
            if (tool.m != null) {
                item = new ItemBuilder(new ItemStack(tool.m), m);
            } else if (!ItemUtils.isEmpty(tool.i)){
                item = new ItemBuilder(tool.i);
            } else continue;
            float power = ValhallaMMO.getNms().toolPower(item.getItem(), b);
            if (power > bestMiningPower){
                bestTool = item;
                bestMiningPower = power;
            }
        }
        return bestTool;
    }

    /**
     * Sets the given embedded tools to the item meta
     * @param m the item meta on which to store these embedded tools
     * @param embeddedTools the embedded tools to store
     */
    public static void setEmbeddedTools(ItemMeta m, Collection<EmbeddedTool> embeddedTools){
        if (embeddedTools == null || embeddedTools.isEmpty()){
            m.getPersistentDataContainer().remove(EMBEDDED_TOOL_KEY);
        } else {
            String encodedTools = embeddedTools.stream().map(t -> t.m != null ? t.m.toString() : ItemUtils.serialize(t.i)).collect(Collectors.joining("<splitter>"));
            m.getPersistentDataContainer().set(EMBEDDED_TOOL_KEY, PersistentDataType.STRING, encodedTools);
        }
    }

    /**
     * Collects all embedded tools from the given meta. An embedded tool can either be a plain Material or a full ItemStack
     * @param m the meta from which to get its embedded tools
     * @return all embedded tools
     */
    public static Collection<EmbeddedTool> getEmbeddedTools(ItemMeta m){
        String stored = ItemUtils.getPDCString(EMBEDDED_TOOL_KEY, m, null);
        Collection<EmbeddedTool> embeddedTools = new HashSet<>();
        if (stored == null || stored.isEmpty()) return embeddedTools;
        String[] itemStrings = stored.split("<splitter>");
        for (String s : itemStrings){
            Material baseMaterial = Catch.catchOrElse(() -> Material.valueOf(s), null);
            if (baseMaterial != null) embeddedTools.add(new EmbeddedTool(baseMaterial));
            else {
                ItemStack item = ItemUtils.deserialize(s);
                if (ItemUtils.isEmpty(item)) continue;
                embeddedTools.add(new EmbeddedTool(item));
            }
        }
        return embeddedTools;
    }

    public static class EmbeddedTool {
        private final Material m;
        private final ItemStack i;

        public EmbeddedTool(Material m){
            this.m = m;
            this.i = null;
        }

        public EmbeddedTool(ItemStack i){
            this.i = i;
            this.m = null;
        }

        public Material getMaterial() {
            return m;
        }

        public ItemStack getItem() {
            return i;
        }
    }
}
