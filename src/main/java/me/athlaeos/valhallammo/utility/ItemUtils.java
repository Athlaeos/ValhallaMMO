package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ItemUtils {

    private static final Collection<Tag<Material>> tags = new HashSet<>(Arrays.asList(Tag.PLANKS, Tag.LOGS, Tag.ITEMS_STONE_TOOL_MATERIALS, Tag.ANVIL, Tag.CAULDRONS, 
    Tag.WOOL, Tag.BEDS, Tag.SAPLINGS, Tag.ITEMS_BANNERS, Tag.CANDLES, Tag.CARPETS, Tag.COAL_ORES, Tag.GOLD_ORES, Tag.IRON_ORES, Tag.LAPIS_ORES, Tag.COPPER_ORES, Tag.DIAMOND_ORES,
    Tag.EMERALD_ORES, Tag.REDSTONE_ORES, Tag.DOORS, Tag.FENCES, Tag.FENCE_GATES, Tag.SMALL_FLOWERS, Tag.ITEMS_BOATS, Tag.ITEMS_FISHES, Tag.ITEMS_MUSIC_DISCS, Tag.LEAVES,
    Tag.PRESSURE_PLATES, Tag.SAND, Tag.SIGNS, Tag.TALL_FLOWERS, Tag.TRAPDOORS, Tag.WALLS));
    
    private static final Map<Material, Tag<Material>> similarItemsMap = new HashMap<>();

    static {
        Tag<Material> buckets = new Tag<>() {
            private final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "tag_buckets");
            @Override
            public @NotNull NamespacedKey getKey() {return key;}
            private final Set<Material> buckets = Set.of(Material.MILK_BUCKET, Material.POWDER_SNOW_BUCKET, Material.LAVA_BUCKET, Material.WATER_BUCKET, Material.AXOLOTL_BUCKET, Material.COD_BUCKET, Material.PUFFERFISH_BUCKET, Material.SALMON_BUCKET, Material.TROPICAL_FISH_BUCKET);
            @Override
            public @NotNull Set<Material> getValues() {return buckets;}
            @Override
            public boolean isTagged(@NotNull Material arg0) {return buckets.contains(arg0);}
        };
        tags.add(buckets);

        similarItemsMap.put(Material.OAK_PLANKS, Tag.PLANKS);
        similarItemsMap.put(Material.OAK_LOG, Tag.LOGS);
        similarItemsMap.put(Material.ANVIL, Tag.ANVIL);
        similarItemsMap.put(Material.CAULDRON, Tag.CAULDRONS);
        similarItemsMap.put(Material.WHITE_WOOL, Tag.WOOL);
        similarItemsMap.put(Material.WHITE_BED, Tag.BEDS);
        similarItemsMap.put(Material.WHITE_BANNER, Tag.BANNERS);
        similarItemsMap.put(Material.WHITE_CANDLE, Tag.CANDLES);
        similarItemsMap.put(Material.WHITE_CARPET, Tag.CARPETS);
        similarItemsMap.put(Material.COAL_ORE, Tag.COAL_ORES);
        similarItemsMap.put(Material.GOLD_ORE, Tag.GOLD_ORES);
        similarItemsMap.put(Material.IRON_ORE, Tag.IRON_ORES);
        similarItemsMap.put(Material.LAPIS_ORE, Tag.LAPIS_ORES);
        similarItemsMap.put(Material.COPPER_ORE, Tag.COPPER_ORES);
        similarItemsMap.put(Material.EMERALD_ORE, Tag.EMERALD_ORES);
        similarItemsMap.put(Material.REDSTONE_ORE, Tag.REDSTONE_ORES);
        similarItemsMap.put(Material.OAK_DOOR, Tag.DOORS);
        similarItemsMap.put(Material.OAK_FENCE, Tag.FENCES);
        similarItemsMap.put(Material.OAK_FENCE_GATE, Tag.FENCE_GATES);
        similarItemsMap.put(Material.OAK_BOAT, Tag.ITEMS_BOATS);
        similarItemsMap.put(Material.OAK_LEAVES, Tag.LEAVES);
        similarItemsMap.put(Material.OAK_PRESSURE_PLATE, Tag.PRESSURE_PLATES);
        similarItemsMap.put(Material.SAND, Tag.SAND);
        similarItemsMap.put(Material.OAK_SIGN, Tag.SIGNS);
        similarItemsMap.put(Material.OAK_TRAPDOOR, Tag.TRAPDOORS);

    }

    public static Collection<Material> getMaterialList(Collection<String> materials){
        Collection<Material> m = new HashSet<>();
        if (materials == null) return m;
        for (String s : materials){
            try {
                m.add(Material.valueOf(s));
            } catch (IllegalArgumentException ignored){
            }
        }
        return m;
    }

    public static Collection<Material> getMaterialList(String... materials){
        return getMaterialList(Arrays.asList(materials));
    }

    public static Collection<Material> getSimilarMaterials(Material m){
        for (Tag<Material> tag : tags){
            if (tag.isTagged(m)) return tag.getValues();
        }
        return Set.of(m);
    }

    public static Material getBaseMaterial(Material m){
        for (Material baseVersion : similarItemsMap.keySet()){
            if (similarItemsMap.get(baseVersion).isTagged(m)) return baseVersion; 
        }
        return null;
    }

    public static boolean isSimilarMaterial(Material m, Material compareTo){
        if (m == compareTo) return true;
        for (Tag<Material> similarMaterialList : tags){
            if (similarMaterialList.isTagged(m) && similarMaterialList.isTagged(compareTo)) return true;
        }
        return false;
    }

    /**
     * Checks if the item lore, contains 'find' anywhere.
     * If it does, it is replaced by 'replace'.
     * If not found, 'replace' is appended on the end of the lore.
     * @param item the item to check the lore from
     * @param find the string to find in the lore
     * @param replace the string to replace that line with
     */
    public static void findAndReplaceLore(ItemStack item, String find, String replace){
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return;
        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) lore = new ArrayList<>();
        int index = -1;
        for (String l : lore){
            if (l.contains(find)){
                index = lore.indexOf(l);
                break;
            }
        }

        if (index != -1) {
            // match found
            if (StringUtils.isEmpty(replace)) lore.remove(index);
            else lore.set(index, Utils.chat(replace));
        } else {
            // no match found
            if (StringUtils.isEmpty(replace)) return;
            else lore.add(Utils.chat(replace));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static boolean isEmpty(ItemStack i){
        return i == null || i.getType().isAir();
    }

    /**
     * Attempts to fetch an ItemStack from the given config, given a path to it. If it doesn't find an ItemStack, it
     * will attempt to find a valid Material string and change the material of the default value to it.
     * If the path does not lead to any valid value, the default ItemStack is taken.
     * The file parameter is purely for information purposes, to specify the file in which the error occurred.
     * @param config the YamlConfiguration to fetch the icon from
     * @param path the path in the config pointing to the icon
     * @param file informational string specifying the file where the error is coming from
     * @param def the default icon in case no valid value is found
     * @return the ItemStack at the given path
     */
    public static ItemStack getIconFromConfig(YamlConfiguration config, String path, String file, ItemStack def){
        Object rawIcon = config.get(path);
        if (rawIcon instanceof ItemStack){
            return (ItemStack) rawIcon;
        } else {
            try {
                def.setType(Material.valueOf(config.getString(path, "")));
                return def;
            } catch (IllegalArgumentException ignored){
                ValhallaMMO.logWarning(
                        "ItemStack in config " + file + ":" + path + " did not lead to an item stack or proper material type. Defaulted to " + getItemName(def)
                );
            }
        }
        return def;
    }

    public static String getItemName(ItemStack i){
        String name;
        assert i.getItemMeta() != null;
        if (i.getItemMeta().hasDisplayName()) name = Utils.chat(i.getItemMeta().getDisplayName());
        else if (TranslationManager.getMaterialTranslations().getMaterialTranslations().containsKey(i.getType())) name = Utils.chat(TranslationManager.getMaterialTranslation(i.getType()));
        else if (i.getItemMeta().hasLocalizedName()) name = Utils.chat(i.getItemMeta().getLocalizedName());
        else name = me.athlaeos.valhallammo.utility.StringUtils.toPascalCase(i.getType().toString().replace("_", " "));
        return name;
    }

    /**
     * This method exists purely because ther are apparently differences between Spigot and forks off it like Paper or Purpur which cause 
     * {@link ItemStack#isSimilar(ItemStack)} or {@link ItemStack#equals(Object)} to not work reliably. String value comparison seems to work
     * better, and we're also re-applying the lore and display name of an item since the object notation may be different per item even if they
     * look identical.
     * @return true if the two items are similar, false otherwise
     */
    public static boolean isSimilar(ItemStack i1, ItemStack i2){
        if (i1.isSimilar(i2)) return true; // skip all the other logic if minecraft already thinks these two items are similar
        // but should they not be similar, do the following to still check if they're similar
        return reSetItemText(i1.clone()).toString().equals(reSetItemText(i2.clone()).toString());
    }

    public static ItemStack reSetItemText(ItemStack i){
        if (i == null) return null;
        ItemMeta iMeta = i.getItemMeta();
        if (iMeta == null) return null;
        if (iMeta.hasLore() && iMeta.getLore() != null){
            List<String> newLore = new ArrayList<>();
            for (String s : iMeta.getLore()){
                newLore.add(Utils.chat(s));
            }

            iMeta.setLore(newLore);
        }
        if (iMeta.hasDisplayName()) iMeta.setDisplayName(iMeta.getDisplayName());

        i.setItemMeta(iMeta);
        return i;
    }
}
