package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.item.*;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class ItemUtils {
    private static final Map<UUID, ItemBuilder> storedProjectileCache = new HashMap<>();
    private static final Map<UUID, Long> storedProjectileCachedAt = new HashMap<>();

    private static final Collection<Tag<Material>> materialTags = new HashSet<>(Arrays.asList(Tag.PLANKS, Tag.LOGS, Tag.ITEMS_STONE_TOOL_MATERIALS, Tag.ANVIL, Tag.CAULDRONS,
    Tag.WOOL, Tag.BEDS, Tag.SAPLINGS, Tag.ITEMS_BANNERS, Tag.CANDLES, Tag.COAL_ORES, Tag.GOLD_ORES, Tag.IRON_ORES, Tag.LAPIS_ORES, Tag.COPPER_ORES, Tag.DIAMOND_ORES,
    Tag.EMERALD_ORES, Tag.REDSTONE_ORES, Tag.DOORS, Tag.FENCES, Tag.FENCE_GATES, Tag.SMALL_FLOWERS, Tag.ITEMS_BOATS, Tag.ITEMS_FISHES, Tag.LEAVES,
    Tag.PRESSURE_PLATES, Tag.SAND, Tag.SIGNS, Tag.TRAPDOORS, Tag.WALLS));
    
    private static final Map<Material, Tag<Material>> similarItemsMap = new HashMap<>();
    private static final Map<Material, String> itemCategoryTranslation = new HashMap<>();
    private static final Map<MaterialClass, String> materialClassTranslation = new HashMap<>();
    private static final Map<EquipmentClass, String> equipmentClassTranslation = new HashMap<>();

    private static final Collection<Material> nonAirMaterials = new HashSet<>();
    private static final Material[] nonAirMaterialsArray;

    // Contains some of the items that may be used in the off hand. If the main hand does not have such an item and the
    // off hand does, it can be assumed the off hand is used in combat or events or whatever
    private static final Collection<Material> offhandUsableItems = new HashSet<>(getMaterialSet("BOW",
            "CROSSBOW", "SNOWBALL", "EGG", "ENDER_PEARL", "ENDER_EYE", "EXPERIENCE_BOTTLE", "FISHING_ROD",
            "FLINT_AND_STEEL", "FIREWORK_ROCKET", "SPLASH_POTION", "LINGERING_POTION", "TRIDENT"));

    public static Collection<Material> getOffhandUsableItems() {
        return offhandUsableItems;
    }

    public static boolean usedMainHand(ItemBuilder mainHand, ItemBuilder offHand){
        if (offHand == null && mainHand != null) return true;
        if (mainHand != null && offhandUsableItems.contains(mainHand.getItem().getType())) return true;
        return offHand == null || !offhandUsableItems.contains(offHand.getItem().getType());
    }

    static {
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_19)) materialTags.add(Tag.WOOL_CARPETS);

        Tag<Material> buckets = of("buckets", Material.BUCKET, Material.MILK_BUCKET, Material.POWDER_SNOW_BUCKET, Material.LAVA_BUCKET, Material.WATER_BUCKET, Material.AXOLOTL_BUCKET, Material.COD_BUCKET, Material.PUFFERFISH_BUCKET, Material.SALMON_BUCKET, Material.TROPICAL_FISH_BUCKET);
        materialTags.add(buckets);
        Tag<Material> potions = of("potions", Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION);
        materialTags.add(potions);
        Tag<Material> stoneBricks = of("stone_bricks", Material.STONE, Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS, Material.CHISELED_STONE_BRICKS, Material.MOSSY_STONE_BRICKS);
        materialTags.add(stoneBricks);
        Tag<Material> deepslateBricks = of("deepslate_bricks", Material.DEEPSLATE, Material.COBBLED_DEEPSLATE, Material.POLISHED_DEEPSLATE, Material.DEEPSLATE_BRICKS, Material.CRACKED_DEEPSLATE_BRICKS, Material.DEEPSLATE_TILES, Material.CRACKED_DEEPSLATE_TILES, Material.CHISELED_DEEPSLATE);
        materialTags.add(deepslateBricks);
        Tag<Material> quartzBricks = of("quartz_bricks", Material.QUARTZ_BRICKS, Material.CHISELED_QUARTZ_BLOCK, Material.QUARTZ_BLOCK, Material.SMOOTH_QUARTZ, Material.QUARTZ_PILLAR);
        materialTags.add(quartzBricks);
        Tag<Material> blackstoneBricks = of("blackstone_bricks", Material.BLACKSTONE, Material.POLISHED_BLACKSTONE, Material.POLISHED_BLACKSTONE_BRICKS, Material.CHISELED_POLISHED_BLACKSTONE, Material.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        materialTags.add(blackstoneBricks);
        Tag<Material> endstoneBricks = of("end_bricks", Material.END_STONE, Material.END_STONE_BRICKS);
        materialTags.add(endstoneBricks);

        registerSimilarItem(Material.OAK_PLANKS, Tag.PLANKS, "ingredient_any_plank");
        registerSimilarItem(Material.OAK_LOG, Tag.LOGS, "ingredient_any_log");
        registerSimilarItem(Material.ANVIL, Tag.ANVIL, "ingredient_any_anvil");
        registerSimilarItem(Material.CAULDRON, Tag.CAULDRONS, "ingredient_any_cauldron");
        registerSimilarItem(Material.WHITE_WOOL, Tag.WOOL, "ingredient_any_wool");
        registerSimilarItem(Material.WHITE_BED, Tag.BEDS, "ingredient_any_bed");
        registerSimilarItem(Material.WHITE_BANNER, Tag.BANNERS, "ingredient_any_banner");
        registerSimilarItem(Material.WHITE_CANDLE, Tag.CANDLES, "ingredient_any_candle");
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_19)) registerSimilarItem(Material.WHITE_CARPET, Tag.WOOL_CARPETS, "ingredient_any_carpet");
        registerSimilarItem(Material.COAL_ORE, Tag.COAL_ORES, "ingredient_any_coal_ore");
        registerSimilarItem(Material.DIAMOND_ORE, Tag.DIAMOND_ORES, "ingredient_any_diamond_ore");
        registerSimilarItem(Material.GOLD_ORE, Tag.GOLD_ORES, "ingredient_any_gold_ore");
        registerSimilarItem(Material.IRON_ORE, Tag.IRON_ORES, "ingredient_any_iron_ore");
        registerSimilarItem(Material.LAPIS_ORE, Tag.LAPIS_ORES, "ingredient_any_lapis_ore");
        registerSimilarItem(Material.COPPER_ORE, Tag.COPPER_ORES, "ingredient_any_copper_ore");
        registerSimilarItem(Material.EMERALD_ORE, Tag.EMERALD_ORES, "ingredient_any_emerald_ore");
        registerSimilarItem(Material.REDSTONE_ORE, Tag.REDSTONE_ORES, "ingredient_any_redstone_ore");
        registerSimilarItem(Material.OAK_DOOR, Tag.DOORS, "ingredient_any_door");
        registerSimilarItem(Material.OAK_FENCE, Tag.FENCES, "ingredient_any_fence");
        registerSimilarItem(Material.OAK_FENCE_GATE, Tag.FENCE_GATES, "ingredient_any_fence_gate");
        registerSimilarItem(Material.OAK_BOAT, Tag.ITEMS_BOATS, "ingredient_any_boat");
        registerSimilarItem(Material.OAK_LEAVES, Tag.LEAVES, "ingredient_any_leaves");
        registerSimilarItem(Material.OAK_PRESSURE_PLATE, Tag.PRESSURE_PLATES, "ingredient_any_pressure_plate");
        registerSimilarItem(Material.SAND, Tag.SAND, "ingredient_any_sand");
        registerSimilarItem(Material.OAK_SIGN, Tag.SIGNS, "ingredient_any_sign");
        registerSimilarItem(Material.OAK_TRAPDOOR, Tag.TRAPDOORS, "ingredient_any_trap_door");
        registerSimilarItem(Material.POTION, potions, "ingredient_any_potion");
        registerSimilarItem(Material.BUCKET, buckets, "ingredient_any_bucket");
        registerSimilarItem(Material.STONE_BRICKS, stoneBricks, "ingredient_any_stone_bricks");
        registerSimilarItem(Material.DEEPSLATE_BRICKS, deepslateBricks, "ingredient_any_deepslate_bricks");
        registerSimilarItem(Material.QUARTZ_BRICKS, quartzBricks, "ingredient_any_quartz_bricks");
        registerSimilarItem(Material.POLISHED_BLACKSTONE_BRICKS, blackstoneBricks, "ingredient_any_blackstone_bricks");
        registerSimilarItem(Material.END_STONE_BRICKS, endstoneBricks, "ingredient_any_end_bricks");
        materialClassTranslation.put(MaterialClass.WOOD, "ingredient_any_wood_equipment");
        materialClassTranslation.put(MaterialClass.BOW, "ingredient_any_bow");
        materialClassTranslation.put(MaterialClass.CROSSBOW, "ingredient_any_crossbow");
        materialClassTranslation.put(MaterialClass.LEATHER, "ingredient_any_leather_equipment");
        materialClassTranslation.put(MaterialClass.STONE, "ingredient_any_stone_equipment");
        materialClassTranslation.put(MaterialClass.CHAINMAIL, "ingredient_any_chainmail_equipment");
        materialClassTranslation.put(MaterialClass.GOLD, "ingredient_any_gold_equipment");
        materialClassTranslation.put(MaterialClass.IRON, "ingredient_any_iron_equipment");
        materialClassTranslation.put(MaterialClass.DIAMOND, "ingredient_any_diamond_equipment");
        materialClassTranslation.put(MaterialClass.NETHERITE, "ingredient_any_netherite_equipment");
        materialClassTranslation.put(MaterialClass.PRISMARINE, "ingredient_any_prismarine_equipment");
        materialClassTranslation.put(MaterialClass.ENDERIC, "ingredient_any_enderic_equipment");
        materialClassTranslation.put(MaterialClass.OTHER, "ingredient_any_other_equipment");
        equipmentClassTranslation.put(EquipmentClass.SWORD, "ingredient_any_sword");
        equipmentClassTranslation.put(EquipmentClass.BOW, "ingredient_any_bow");
        equipmentClassTranslation.put(EquipmentClass.CROSSBOW, "ingredient_any_crossbow");
        equipmentClassTranslation.put(EquipmentClass.TRIDENT, "ingredient_any_trident");
        equipmentClassTranslation.put(EquipmentClass.HELMET, "ingredient_any_helmet");
        equipmentClassTranslation.put(EquipmentClass.CHESTPLATE, "ingredient_any_chestplate");
        equipmentClassTranslation.put(EquipmentClass.LEGGINGS, "ingredient_any_leggings");
        equipmentClassTranslation.put(EquipmentClass.BOOTS, "ingredient_any_boots");
        equipmentClassTranslation.put(EquipmentClass.SHEARS, "ingredient_any_shears");
        equipmentClassTranslation.put(EquipmentClass.FLINT_AND_STEEL, "ingredient_any_flint_and_steel");
        equipmentClassTranslation.put(EquipmentClass.FISHING_ROD, "ingredient_any_fishing_rod");
        equipmentClassTranslation.put(EquipmentClass.ELYTRA, "ingredient_any_elytra");
        equipmentClassTranslation.put(EquipmentClass.PICKAXE, "ingredient_any_pickaxe");
        equipmentClassTranslation.put(EquipmentClass.AXE, "ingredient_any_axe");
        equipmentClassTranslation.put(EquipmentClass.SHOVEL, "ingredient_any_shovel");
        equipmentClassTranslation.put(EquipmentClass.HOE, "ingredient_any_hoe");
        equipmentClassTranslation.put(EquipmentClass.SHIELD, "ingredient_any_shield");
        equipmentClassTranslation.put(EquipmentClass.OTHER, "ingredient_any_other");
        equipmentClassTranslation.put(EquipmentClass.TRINKET, "ingredient_any_trinket");

        Arrays.stream(Material.values()).filter(m -> !m.isAir() && m.isItem()).forEach(nonAirMaterials::add);
        nonAirMaterialsArray = nonAirMaterials.toArray(new Material[0]);
    }

    public static void startProjectileRunnableCache(){
        // cleans up the stored projectile cache every 10 seconds, removing items if they've been in there for 10 seconds or longer
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskTimer(ValhallaMMO.getInstance(), () -> {
            for (UUID uuid : new HashMap<>(storedProjectileCache).keySet()){
                if (storedProjectileCachedAt.get(uuid) + 10000 > System.currentTimeMillis()) {
                    storedProjectileCachedAt.remove(uuid);
                    storedProjectileCache.remove(uuid);
                }
            }
        }, 200L, 200L);
    }

    private static void registerSimilarItem(Material base, Tag<Material> subTypes, String translation){
        similarItemsMap.put(base, subTypes);
        itemCategoryTranslation.put(base, translation);
    }

    public static String getGenericTranslation(Material base){
        base = getBaseMaterial(base);
        return itemCategoryTranslation.containsKey(base) ? TranslationManager.getTranslation(itemCategoryTranslation.get(base)) : TranslationManager.getMaterialTranslation(base);
    }

    public static String getGenericTranslation(MaterialClass base){
        return materialClassTranslation.containsKey(base) ? TranslationManager.getTranslation(materialClassTranslation.get(base)) : me.athlaeos.valhallammo.utility.StringUtils.toPascalCase(base.toString().replace("_", " "));
    }

    public static String getGenericTranslation(EquipmentClass base){
        return equipmentClassTranslation.containsKey(base) ? TranslationManager.getTranslation(equipmentClassTranslation.get(base)) : me.athlaeos.valhallammo.utility.StringUtils.toPascalCase(base.toString().replace("_", " "));
    }

    public static Tag<Material> of(String key, Material... materials){
        Set<Material> tagged = Set.of(materials);
        return new Tag<>() {
            private final NamespacedKey k = new NamespacedKey(ValhallaMMO.getInstance(), "tag_" + key);
            @Override public boolean isTagged(@NotNull Material material) { return tagged.contains(material); }
            @NotNull @Override public Set<Material> getValues() { return tagged; }
            @NotNull @Override public NamespacedKey getKey() { return k; }
        };
    }

    public static Collection<Material> getNonAirMaterials() {
        return nonAirMaterials;
    }

    public static Material[] getNonAirMaterialsArray() {
        return nonAirMaterialsArray;
    }

    public static Map<Material, Tag<Material>> getSimilarItemsMap() {
        return similarItemsMap;
    }

    public static Collection<Material> getMaterialSet(Collection<String> materials){
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

    public static void addItem(Player player, ItemStack i, boolean setOwnership){
        Map<Integer, ItemStack> excess = player.getInventory().addItem(i);
        if (!excess.isEmpty()){
            for (Integer slot : excess.keySet()){
                ItemStack slotItem = excess.get(slot);
                Item drop = player.getWorld().dropItem(player.getLocation(), slotItem);
                if (setOwnership) drop.setOwner(player.getUniqueId());
            }
        }
    }

    public static Collection<Material> getMaterialSet(String... materials){
        return getMaterialSet(Arrays.asList(materials));
    }

    public static Collection<Material> getSimilarMaterials(Material m){
        Collection<Material> similarMaterials = new HashSet<>(Set.of(m));
        for (Tag<Material> tag : materialTags){
            if (tag.isTagged(m)) similarMaterials.addAll(tag.getValues());
        }
        return similarMaterials;
    }

    public static Material getBaseMaterial(Material m){
        for (Material baseVersion : similarItemsMap.keySet()){
            if (similarItemsMap.get(baseVersion).isTagged(m)) return baseVersion; 
        }
        return m;
    }

    private static final Map<Material, Collection<Tag<Material>>> similarMaterialCache = new HashMap<>();
    /**
     * Checks if the material m is similar to compareTo
     * @param m the material (usually base material)
     * @param compareTo the material to check if it's similar to that
     * @return true if they're similar, false if not
     */
    public static boolean isSimilarMaterial(Material m, Material compareTo){
        if (m == compareTo) return true;
        if (similarMaterialCache.containsKey(m) && similarMaterialCache.get(m).stream().anyMatch(t -> t.getValues().contains(m) && t.getValues().contains(compareTo))) return true;
        Collection<Tag<Material>> existingTags = similarMaterialCache.getOrDefault(m, new HashSet<>());
        for (Tag<Material> tag : materialTags){
            if (tag.isTagged(m) && tag.isTagged(compareTo)) {
                existingTags.add(tag);
                similarMaterialCache.put(m, existingTags);
                return true;
            }
        }
        return false;
    }

    public static List<ItemStack> decompressStacks(Map<ItemStack, Integer> contents){
        List<ItemStack> listedItems = new ArrayList<>();
        for (ItemStack i : contents.keySet()){
            int amount = contents.get(i);
            int limiter = 54;
            do {
                ItemStack copy = i.clone();
                copy.setAmount(Math.min(amount, i.getMaxStackSize()));
                listedItems.add(copy);
                amount -= copy.getAmount();
                limiter--;
            } while (amount > 0 && limiter > 0);
        }
        return listedItems;
    }

    public static Map<ItemStack, Integer> compressStacks(List<ItemStack> items){
        Map<ItemStack, Integer> contents = new HashMap<>();
        for (ItemStack i : items){
            if (ItemUtils.isEmpty(i)) continue;
            ItemStack clone = i.clone();
            int itemAmount = clone.getAmount();
            clone.setAmount(1);
            if (contents.containsKey(clone)){
                contents.put(clone, contents.get(clone) + itemAmount);
            } else {
                contents.put(clone, itemAmount);
            }
        }
        return contents;
    }


    public static void replaceOrAddLore(ItemBuilder builder, String find, String replacement){
        if (builder == null) return;
        List<String> lore = builder.getLore() == null ? new ArrayList<>() : builder.getLore();
        replaceOrAddLore(lore, find, replacement);
        builder.lore(lore);
    }

    public static void replaceOrAddLore(ItemMeta meta, String find, String replacement){
        if (meta == null) return;
        find = ChatColor.stripColor(Utils.chat(find));
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        replaceOrAddLore(lore, find, replacement);
        meta.setLore(lore);
    }

    public static void replaceOrAddLore(List<String> original, String find, String replacement){
        find = ChatColor.stripColor(Utils.chat(find));
        if (original == null) return;
        int index = -1;
        for (String l : original){
            if (l.contains(find)){
                index = original.indexOf(l);
                break;
            }
        }

        if (index != -1) {
            // match found
            if (StringUtils.isEmpty(replacement)) original.remove(index);
            else original.set(index, Utils.chat(replacement));
        } else {
            // no match found
            if (!StringUtils.isEmpty(replacement))
                original.add(Utils.chat(replacement));
        }
    }

    public static boolean isEmpty(ItemStack i){
        return i == null || i.getType().isAir() || i.getAmount() <= 0;
    }

    private static final NamespacedKey TYPE_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "temporary_type_storage");

    /**
     * Wrapper to get the item meta of the item. The type of the item is immediately stored as temporary variable onto the meta.
     * If the meta is to be returned to the item, {@link ItemUtils#setItemMeta(ItemStack, ItemMeta)} is expected to be used.
     * It's not a big deal if it's not used, you just have a jump nbt tag left on the item.
     * @param i the item to get the item meta from
     * @return the item meta, if any. Null if the item is null or air or if the returned meta is also null.
     */
    public static ItemMeta getItemMeta(ItemStack i){
        if (isEmpty(i)) return null;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return null;
        storeType(meta, i.getType());
        return meta;
    }

    public static void storeType(ItemMeta meta, Material material) {
        if (meta == null) return;
        meta.getPersistentDataContainer().set(TYPE_KEY, PersistentDataType.STRING, material.toString());
    }

    public static void setMetaNoClone(ItemStack i, ItemMeta meta) {
        meta.getPersistentDataContainer().remove(TYPE_KEY);
        i.setItemMeta(meta);
    }

    /**
     * Sets the item meta to the item, removing the temporary type variable from the meta first.
     * @param i the item to set the item meta to
     * @param meta the item meta to put on the item
     */
    public static void setItemMeta(ItemStack i, ItemMeta meta){
        meta = meta.clone();
        meta.getPersistentDataContainer().remove(TYPE_KEY);
        i.setItemMeta(meta);
    }

    /**
     * Gets the stored type from the item meta, if any
     * @param meta the meta to get the stored type of
     * @return the Material type if one is present
     */
    public static Material getStoredType(ItemMeta meta){
        return stringToMaterial(meta.getPersistentDataContainer().get(TYPE_KEY, PersistentDataType.STRING), null);
    }

    /**
     * Updates the stored type on the metadata. Should be used whenever {@link ItemStack#setType(Material)} is used.
     * @param meta the meta to update its stored material tag on
     * @param newType the new material to store on the meta
     */
    public static void updateStoredType(ItemMeta meta, Material newType){
        if (meta.getPersistentDataContainer().has(TYPE_KEY, PersistentDataType.STRING))
            meta.getPersistentDataContainer().set(TYPE_KEY, PersistentDataType.STRING, newType.toString());
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
            String value = config.getString(path, "");
            try {
                def.setType(Material.valueOf(value));
                return def;
            } catch (IllegalArgumentException ignored){
                ValhallaMMO.logWarning(
                        "ItemStack/Material " + value + " in config " + file + ":" + path + " did not lead to an item stack or proper material type. Defaulted to " + getItemName(getItemMeta(def))
                );
            }
        }
        return def;
    }

    public static ItemStack itemOrAir(ItemStack item){
        if (isEmpty(item)) return new ItemStack(Material.AIR);
        return item;
    }

    public static String getItemName(ItemMeta meta){
        String name;
        if (meta == null) return "null";
        Material base = getStoredType(meta);
        if (base == null) return "null";
        if (meta.hasDisplayName()) name = meta.getDisplayName();
        else if (TranslationManager.getMaterialTranslations().getMaterialTranslations().containsKey(base.toString())) name = Utils.chat(TranslationManager.getMaterialTranslation(base));
        else name = Utils.chat(me.athlaeos.valhallammo.utility.StringUtils.toPascalCase("&r" + base.toString().replace("_", " ")));
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
        ItemMeta i1Meta = reSetItemText(getItemMeta(i1));
        ItemMeta i2Meta = reSetItemText(getItemMeta(i2));
        if (i1Meta == null && i2Meta == null) return true;
        if (i1Meta == null || i2Meta == null) return false;
        ItemStack i1Clone = i1.clone();
        i1Clone.setAmount(1);
        ItemStack i2Clone = i2.clone();
        i2Clone.setAmount(1);
        i1Clone.setItemMeta(i1Meta);
        i2Clone.setItemMeta(i2Meta);
        String i1String = i1Clone.toString();
        String i2String = i2Clone.toString();
        // unhandled remover
        if (i1String.contains(" unhandled=") || i2String.contains(" unhandled=")){
            String match = StringUtils.substringBetween(i1String, " unhandled=", ",");
            if (match != null) i1String = i1String.replace(" unhandled=" + match + ",", "");
            String match2 = StringUtils.substringBetween(i2String, " unhandled=", ",");
            if (match2 != null) i2String = i2String.replace(" unhandled=" + match2 + ",", "");
        }
        return i1String.equals(i2String);
    }

    public static ItemMeta reSetItemText(ItemMeta meta){
        if (meta == null) return null;
        if (meta.hasLore() && meta.getLore() != null){
            List<String> newLore = new ArrayList<>();
            for (String s : meta.getLore()){
                newLore.add(Utils.chat(s));
            }

            meta.setLore(newLore);
        }
        if (meta.hasDisplayName()) meta.setDisplayName(meta.getDisplayName());

        return meta;
    }

    public static List<String> setListPlaceholder(List<String> original, String placeholder, List<String> replaceWith){
        List<String> lore = new ArrayList<>();
        for (String s : original){
            if (s.contains(placeholder)) lore.addAll(replaceWith);
            else lore.add(s);
        }
        return lore;
    }

    public static List<String> setListPlaceholder(List<String> original, String placeholder, String replaceWith){
        List<String> lore = new ArrayList<>();
        for (String s : original){
            lore.add(s.replace(placeholder, replaceWith));
        }
        return lore;
    }

    public static List<String> getLore(ItemStack i){
        ItemMeta meta = getItemMeta(i);
        if (meta != null && meta.hasLore() && meta.getLore() != null) return meta.getLore();
        return new ArrayList<>();
    }

    public static List<String> getLore(ItemMeta meta){
        if (meta != null && meta.hasLore() && meta.getLore() != null) return meta.getLore();
        return new ArrayList<>();
    }

    public static int getPDCInt(NamespacedKey key, ItemStack i, int def){
        ItemMeta meta = getItemMeta(i);
        if (isEmpty(i) || meta == null) return def;
        return meta.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, def);
    }

    public static String getPDCString(NamespacedKey key, ItemStack i, String def){
        if (isEmpty(i)) return def;
        ItemMeta meta = getItemMeta(i);
        if (meta == null) return def;
        String value = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return value == null ? def : value;
    }

    public static double getPDCDouble(NamespacedKey key, ItemStack i, double def){
        ItemMeta meta = getItemMeta(i);
        if (isEmpty(i) || meta == null) return def;
        return meta.getPersistentDataContainer().getOrDefault(key, PersistentDataType.DOUBLE, def);
    }

    public static int getPDCInt(NamespacedKey key, ItemMeta i, int def){
        return i.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, def);
    }

    public static double getPDCDouble(NamespacedKey key, ItemMeta i, double def){
        return i.getPersistentDataContainer().getOrDefault(key, PersistentDataType.DOUBLE, def);
    }

    public static String getPDCString(NamespacedKey key, ItemMeta i, String def){
        String value = i.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return value == null ? def : value;
    }

    public static Map<SlotEntry, Integer> getItemTotals(Collection<SlotEntry> items){
        Map<SlotEntry, Integer> totals = new HashMap<>();
        Map<ItemStack, SlotEntry> mapping = new HashMap<>();
        for (SlotEntry i : items){
            if (ItemUtils.isEmpty(i.getItem())) continue;
            ItemStack clone = i.getItem().clone();
            clone.setAmount(1);
            SlotEntry mappedTo = mapping.get(clone);
            if (mappedTo == null) {
                mapping.put(clone, i);
                mappedTo = i;
            }

            int existingAmount = totals.getOrDefault(mappedTo, 0);
            totals.put(mappedTo, existingAmount + i.getItem().getAmount());
        }
        return totals;
    }

    public static Material stringToMaterial(String material, Material def){
        if (material == null || material.isEmpty()) return def;
        Material found = Material.getMaterial(material);
        return found == null ? def : found;
    }

    public static EquipmentSlot getEquipmentSlot(ItemMeta meta){
        EquipmentClass clazz = EquipmentClass.getMatchingClass(meta);
        return clazz == null ? EquipmentSlot.HAND : switch (clazz) {
            case BOOTS -> EquipmentSlot.FEET;
            case LEGGINGS -> EquipmentSlot.LEGS;
            case CHESTPLATE, ELYTRA, TRINKET -> EquipmentSlot.CHEST;
            case HELMET -> EquipmentSlot.HEAD;
            default -> EquipmentSlot.HAND;
        };
    }

    public static void removeIfLoreContains(ItemMeta meta, String find){
        if (meta == null || meta.getLore() == null) return;
        final String stripped = ChatColor.stripColor(Utils.chat(find));
        List<String> lore = meta.getLore();
        lore.removeIf(l -> l.contains(stripped));
        meta.setLore(lore);
    }

    public static void removeIfLoreContains(ItemBuilder i, String find) {
        if (i == null || i.getLore() == null) return;
        final String stripped = ChatColor.stripColor(Utils.chat(find));
        List<String> lore = i.getLore();
        lore.removeIf(l -> l.contains(stripped));
        i.lore(lore);
    }

    /**
     * Damages the given item by an amount, disregarding attributes like unbreaking
     * @param who the player possessing the item
     * @param item the item to damage
     * @param damage the amount to damage it
     * @param breakEffect the effect to play should the item break
     * @return true if the item would break as a result of the damage dealt, false otherwise
     */
    public static boolean damageItem(Player who, ItemStack item, int damage, EntityEffect breakEffect){
        return damageItem(who, item, damage, breakEffect, false);
    }
    /**
     * Damages the given item by an amount. If "respectAttributes" is true, attributes such as the Unbreaking enchantment
     * the "UNBREAKABLE" item flag are respected.
     * If Unbreaking procs it will not always prevent damage, but it will multiply the damage by the chance for unbreaking items to take damage
     * This chance is equal to 1/(unbreakingLevel + 1), so 4 damage would be reduced to 1 with unbreaking 3.
     * @param who the player possessing the item
     * @param item the item to damage
     * @param damage the amount to damage it
     * @param breakEffect the effect to play should the item break
     * @return true if the item would break as a result of the damage dealt, false otherwise
     */
    public static boolean damageItem(Player who, ItemStack item, int damage, EntityEffect breakEffect, boolean respectAttributes){
        ItemMeta meta = getItemMeta(item);
        if (meta instanceof Damageable && item.getType().getMaxDurability() > 0){
            if (respectAttributes){
                if (meta.isUnbreakable()) return false;
                int unbreakableLevel = item.getEnchantmentLevel(EnchantmentMappings.UNBREAKING.getEnchantment());
                if (unbreakableLevel > 0){
                    double damageChance = 1D / (unbreakableLevel + 1D);
                    damage = Utils.randomAverage(damage * damageChance);
                    if (damage == 0) return false;
                }
            }
            PlayerItemDamageEvent event = new PlayerItemDamageEvent(who, item, damage);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()){
                if (!CustomDurabilityManager.hasCustomDurability(meta)){
                    Damageable damageable = (Damageable) meta;
                    damageable.setDamage(damageable.getDamage() + damage);
                    if (damageable.getDamage() > item.getType().getMaxDurability()){
                        who.playEffect(breakEffect);
                        return true;
                    } else {
                        setMetaNoClone(item, damageable);
                    }
                } else {
                    if (CustomDurabilityManager.getDurability(meta, false) <= 0){
                        who.playEffect(breakEffect);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean allMatchInTypeAndData(List<ItemStack> items){
        items = items.stream().filter(i -> !isEmpty(i)).toList();
        if (items.size() <= 1) return true;
        BiPredicate<ItemStack, ItemStack> predicate = (itemStack, itemStack2) -> {
            ItemMeta m1 = getItemMeta(itemStack);
            ItemMeta m2 = getItemMeta(itemStack2);
            if (m1 == null || m2 == null) return false;
            if (itemStack.getType() != itemStack2.getType()) return false;
            if (m1.hasCustomModelData() != m2.hasCustomModelData()) return false;
            return m1.getCustomModelData() == m2.getCustomModelData();
        };
        for (int i = 0; i < items.size() - 2; i++){
            if (!predicate.test(items.get(i), items.get(i + 1))) return false;
        }
        return true;
    }

    /**
     * Returns how many times the given itemstack can fit in the player's inventory, also considers the item's quantity
     * @param p the player in which to fit the item
     * @param item the item to fit
     * @return the amount of times the item can fit in the player's inventory
     */
    public static int maxInventoryFit(Player p, ItemStack item){
        int times = 0;
        for (ItemStack i : p.getInventory().getStorageContents()){
            if (times >= 64) break; // inventory has enough space, no longer need to check
            if (isEmpty(i)) times += (int) Math.floor(item.getType().getMaxStackSize() / (double) item.getAmount());
            else if (item.getType().getMaxStackSize() > 1 && i.isSimilar(item) &&
                    i.getAmount() + item.getAmount() <= item.getType().getMaxStackSize())
                times += (int) Math.floor((i.getType().getMaxStackSize() - i.getAmount()) / (double) item.getAmount());
        }
        return times;
    }

    public static void calculateClickEvent(InventoryClickEvent e, int maxAmount, Integer... slotsToCover){
        Player p = (Player) e.getWhoClicked();
        ItemStack cursor = p.getItemOnCursor();
        ItemStack clickedItem = e.getCurrentItem();
        if (e.getClickedInventory() == null) return;
        Inventory openInventory = e.getView().getTopInventory();
        e.setCancelled(true);
        if (e.getClickedInventory() instanceof PlayerInventory){
            // player inventory item clicked
            if (e.isShiftClick() && !isEmpty(clickedItem)){
                // shift click, check if slotsToCalculate are available for new items, otherwise do nothing more as there's no slot to transfer to
                for (Integer i : slotsToCover){
                    ItemStack slotItem = openInventory.getItem(i);
                    if (isEmpty(slotItem)) {
                        if (clickedItem.getAmount() <= maxAmount) {
                            openInventory.setItem(i, clickedItem);
                            e.setCurrentItem(null);
                        }
                        else {
                            ItemStack itemToPut = clickedItem.clone();
                            itemToPut.setAmount(maxAmount);
                            if (clickedItem.getAmount() - maxAmount <= 0) e.setCurrentItem(null);
                            else clickedItem.setAmount(clickedItem.getAmount() - maxAmount);
                            openInventory.setItem(i, itemToPut);
                        }
                        return;
                    } else if (slotItem.isSimilar(clickedItem)) {
                        // similar slot item, add as much as possible
                        if (slotItem.getAmount() < maxAmount) {
                            int amountToTransfer = Math.min(clickedItem.getAmount(), maxAmount - slotItem.getAmount());
                            if (clickedItem.getAmount() == amountToTransfer) {
                                e.setCurrentItem(null);
                            } else {
                                if (clickedItem.getAmount() - amountToTransfer <= 0) e.setCurrentItem(null);
                                else clickedItem.setAmount(clickedItem.getAmount() - amountToTransfer);
                            }
                            slotItem.setAmount(slotItem.getAmount() + amountToTransfer);
                            return;
                        }
                    }
                }
                // no available slot found, do nothing more
            } else e.setCancelled(false); // regular inventory click, do nothing special
        } else if (e.getClickedInventory().equals(e.getView().getTopInventory())){
            // opened inventory clicked
            if (legalClickTypes.contains(e.getClick())) { // inconsequential action used, allow event and do nothing more
                e.setCancelled(false);
                return;
            }
            if (illegalClickTypes.contains(e.getClick())) return; // incalculable action used, event is cancelled and do nothing more
            // other actions have to be calculated
            if (e.isLeftClick() || e.isRightClick()) {
                // transfer or swap all if not similar
                if (isEmpty(cursor)){
                    // pick up clicked item, should be fine
                    e.setCancelled(false);
                } else {
                    if (isEmpty(clickedItem)){
                        int amountToTransfer = (e.isRightClick() ? 1 : maxAmount);
                        if (cursor.getAmount() > amountToTransfer){
                            ItemStack itemToTransfer = cursor.clone();
                            itemToTransfer.setAmount(amountToTransfer);
                            e.setCurrentItem(itemToTransfer);
                            cursor.setAmount(cursor.getAmount() - amountToTransfer);
                            p.setItemOnCursor(cursor);
                        } else {
                            e.setCurrentItem(cursor);
                            p.setItemOnCursor(null);
                        }
                    } else {
                        // swap or transfer items
                        if (cursor.isSimilar(clickedItem)){
                            // are similar, transfer as much as possible
                            int clickedMax = Math.min(clickedItem.getType().getMaxStackSize(), maxAmount);
                            if (clickedItem.getAmount() < clickedMax){
                                int amountToTransfer = e.isRightClick() ? 1 : Math.min(cursor.getAmount(), clickedMax - clickedItem.getAmount());
                                if (cursor.getAmount() == amountToTransfer) {
                                    p.setItemOnCursor(null);
                                } else {
                                    cursor.setAmount(cursor.getAmount() - amountToTransfer);
                                    p.setItemOnCursor(cursor);
                                }
                                clickedItem.setAmount(clickedItem.getAmount() + amountToTransfer);
                            } // clicked item already equals or exceeds max amount, do nothing more
                        } else {
                            // not similar, swap items if cursor has valid amount
                            if (cursor.getAmount() <= maxAmount){
                                // valid amount, swap items
                                ItemStack temp = cursor.clone();
                                p.setItemOnCursor(clickedItem);
                                e.setCurrentItem(temp);
                            } // invalid amount, do nothing more
                        }
                    }
                }
            }
        }
    }

    public static String serialize(ItemStack itemStack) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeObject(itemStack);

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception ignored) {}
        return null;
    }

    public static ItemStack deserialize(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack i = (ItemStack) dataInput.readObject();
            dataInput.close();
            return i;
        } catch (ClassNotFoundException | IOException ignored) {}
        return null;
    }

    private static final Collection<ClickType> legalClickTypes = Set.of(ClickType.DROP, ClickType.CONTROL_DROP,
            ClickType.MIDDLE, ClickType.WINDOW_BORDER_LEFT, ClickType.WINDOW_BORDER_RIGHT, ClickType.UNKNOWN, ClickType.CREATIVE, ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT);
    private static final Collection<ClickType> illegalClickTypes = Set.of(ClickType.SWAP_OFFHAND, ClickType.NUMBER_KEY, ClickType.DOUBLE_CLICK);

    public static void calculateDragEvent(InventoryDragEvent e, int maxAmount, Integer... affectedSlots){
        ItemStack cursor = e.getCursor();
        int newAmount = isEmpty(cursor) ? 0 : cursor.getAmount();
        Collection<Integer> slots = Set.of(affectedSlots);
        e.setCancelled(true);
        for (Integer slot : e.getNewItems().keySet()){
            ItemStack newItem = e.getNewItems().get(slot);
            if (slots.contains(slot) && newItem.getAmount() > maxAmount) { // dragged slot would be capped afterwards, subtract excess
                int excess = newItem.getAmount() - maxAmount;
                newAmount += excess;
                e.getNewItems().get(slot).setAmount(maxAmount);
            }
        }
        for (Integer slot : e.getNewItems().keySet()){
            e.getView().setItem(slot, e.getNewItems().get(slot));
        }
        ItemStack oldCursor = e.getOldCursor().clone();
        oldCursor.setAmount(newAmount);

        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            if (oldCursor.getAmount() > 0) e.getWhoClicked().setItemOnCursor(oldCursor);
            else e.getWhoClicked().setItemOnCursor(null);
        }, 1L);
    }

    public static int timesContained(List<ItemStack> inventory, Map<ItemStack, Integer> ingredients, IngredientChoice matcher){
        if (ingredients.isEmpty()) return Short.MAX_VALUE;

        Map<ItemStack, Integer> countMapped = new HashMap<>();
        for (ItemStack ingredient : ingredients.keySet()){
            ingredient = ingredient.clone();
            ingredient.setAmount(1);
            boolean anyMatch = false;
            for (ItemStack i : inventory){
                if (isEmpty(i)) continue;
                if (matcher.matches(ingredient, i)) {
                    int amount = countMapped.getOrDefault(ingredient, 0);
                    countMapped.put(ingredient, amount + i.getAmount());
                    anyMatch = true;
                }
            }
            if (!anyMatch) return 0;
        }

        int minimumAmount = 100000;
        for (ItemStack ingredient : ingredients.keySet()){
            int required = ingredients.get(ingredient);
            ingredient = ingredient.clone();
            ingredient.setAmount(1);

            if (!countMapped.containsKey(ingredient)) return 0;
            int timesContained = (int) Math.floor((double) countMapped.get(ingredient) / (double) required);
            minimumAmount = Math.min(timesContained, minimumAmount);
        }
        return minimumAmount;
    }

    public static List<ItemStack> removeItems(List<ItemStack> contents, Map<ItemStack, Integer> ingredients, int count, IngredientChoice matcher) {
        if (ingredients.isEmpty()) return new ArrayList<>();
        List<ItemStack> removedItems = new ArrayList<>();
        for (ItemStack ingredient : ingredients.keySet()){
            ingredient = ingredient.clone();
            int amountRequired = ingredients.get(ingredient) * count;
            for (ItemStack i : new ArrayList<>(contents)){
                if (matcher.matches(ingredient, i)) {
                    int amount = i.getAmount();
                    if (amount > amountRequired){
                        ItemStack removed = i.clone();
                        removed.setAmount(amount - amountRequired);
                        removedItems.add(removed);
                        i.setAmount(amount - amountRequired);
                    } else {
                        removedItems.add(i.clone());
                        contents.remove(i);
                    }
                    amountRequired -= Math.min(amount, amountRequired);
                }
            }
            if (amountRequired > 0) return null; // if there's any required items left, contents doesn't contain everything
        }
        return removedItems;
    }

    public static List<ItemStack> removeItems(Inventory inventory, Map<ItemStack, Integer> ingredients, int count, IngredientChoice matcher) {
        if (ingredients.isEmpty()) return new ArrayList<>();
        List<ItemStack> removedItems = new ArrayList<>();
        for (ItemStack ingredient : ingredients.keySet()){
            ingredient = ingredient.clone();
            int amountRequired = ingredients.get(ingredient) * count;
            for (int i = 0; i < inventory.getStorageContents().length; i++){
                ItemStack item = inventory.getItem(i);
                if (ItemUtils.isEmpty(item)) continue;
                if (matcher.matches(ingredient, item)) {
                    int amount = item.getAmount();
                    if (amount > amountRequired){
                        ItemStack removed = item.clone();
                        removed.setAmount(amount - amountRequired);
                        removedItems.add(removed);
                        item.setAmount(amount - amountRequired);
                    } else {
                        removedItems.add(item.clone());
                        inventory.setItem(i, null);
                    }
                    amountRequired -= Math.min(amount, amountRequired);
                }
            }
            if (amountRequired > 0) return null; // if there's any required items left, contents doesn't contain everything
        }
        return removedItems;
    }

    private static final Collection<Material> consumables = Set.of(Material.ARROW, Material.SPECTRAL_ARROW, Material.TIPPED_ARROW,
            Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.SNOWBALL, Material.ENDER_PEARL, Material.ENDER_EYE,
            Material.EXPERIENCE_BOTTLE, Material.EGG, Material.MILK_BUCKET);

    /**
     * Consumables include all items that may be eaten, drank, thrown, or shot.
     * If any of these items have a potion effect stored on them, hitting entities with them will not apply its effects.
     * @return True if consumable and its stored potion effects are not allowed to apply. False otherwise
     */
    public static boolean isConsumable(Material m){
        return m.isEdible() || consumables.contains(m);
    }

    public static ItemBuilder getStoredItem(Entity entity){
        if (storedProjectileCache.containsKey(entity.getUniqueId())) return storedProjectileCache.get(entity.getUniqueId());
        if (entity.hasMetadata("entity_stored_item_data")){
            List<MetadataValue> metaData = entity.getMetadata("entity_stored_item_data");
            if (!metaData.isEmpty()){
                try {
                    ItemStack item = deserialize(metaData.get(0).asString());
                    if (isEmpty(item)) return null;
                    ItemBuilder builder = new ItemBuilder(item);
                    storedProjectileCache.put(entity.getUniqueId(), builder);
                    storedProjectileCachedAt.put(entity.getUniqueId(), System.currentTimeMillis());
                    return builder;
                } catch (Exception ignored){
                    ValhallaMMO.logSevere("Another plugin is using metadata key 'entity_stored_item_data' and not using the proper data type");
                }
            }
        }
        return null;
    }

    public static void storeItem(Entity entity, ItemStack item){
        entity.setMetadata("entity_stored_item_data", new FixedMetadataValue(ValhallaMMO.getInstance(), serialize(item)));
    }

    /**
     * Multiplies the given item quantities by a multiplier. It is assumed the given items come from a BlockDropItemEvent in which
     * case adding items is illegal. If extra itemstacks are required to be added to the list, they will be included in the returned list.
     * @param items the items the block dropped, and multiplied
     * @param multiplier the multiplier for the item quantities, uses {@link Utils#randomAverage(double)} to calculate new amount
     * @param forgiving if true, the quantity can never go below 1.
     * @param filter if an item does not pass the predicate, it is ignored
     * @return the list of items that could not be added to the drops list and so should be scheduled in for dropping after the fact
     */
    public static List<ItemStack> multiplyDrops(List<Item> items, double multiplier, boolean forgiving, Predicate<Item> filter){
        Iterator<Item> iterator = items.iterator();
        List<ItemStack> extraItems = new ArrayList<>();
        while (iterator.hasNext()){
            Item i = iterator.next();

            if (filter != null && !filter.test(i)) continue;
            ItemStack item = i.getItemStack();
            int newAmount = Math.max(forgiving ? 1 : 0, Utils.randomAverage(multiplier * item.getAmount()));
            if (newAmount == 0) items.remove(i);
            else {
                if (newAmount > item.getMaxStackSize()){
                    while(newAmount > item.getMaxStackSize()){
                        ItemStack newDrop = item.clone();
                        newDrop.setAmount(item.getMaxStackSize());
                        newAmount -= item.getMaxStackSize();
                        extraItems.add(newDrop);
                    }
                }
                item.setAmount(newAmount);
                i.setItemStack(item);
            }
        }
        return extraItems;
    }

    /**
     * Multiplies the given item quantities by a multiplier.
     * @param items the items the block dropped, and multiplied
     * @param multiplier the multiplier for the item quantities, uses {@link Utils#randomAverage(double)} to calculate new amount
     * @param forgiving if true, the quantity can never go below 1.
     * @param filter if an item does not pass the predicate, it is ignored and added to the output list unaltered
     */
    public static void multiplyItems(List<ItemStack> items, double multiplier, boolean forgiving, Predicate<ItemStack> filter){
        for (ItemStack item : new ArrayList<>(items)) {
            if (filter != null && !filter.test(item)) continue;
            int newAmount = Math.max(forgiving ? 1 : 0, Utils.randomAverage(multiplier * item.getAmount()));
            if (newAmount == 0) items.remove(item);
            else {
                if (newAmount > item.getMaxStackSize()) {
                    while (newAmount > item.getMaxStackSize()) {
                        ItemStack newDrop = item.clone();
                        newDrop.setAmount(item.getMaxStackSize());
                        newAmount -= item.getMaxStackSize();
                        items.add(newDrop);
                    }
                }
                item.setAmount(newAmount);
            }
        }
    }

    /**
     * ItemUtils caches items stored in projectiles as well, so that this may be used to avoid having to repeatedly get items from entities and getting their properties
     * @return the map of cached projectiles with their item
     */
    public static Map<UUID, ItemBuilder> getStoredProjectileCache() {
        return storedProjectileCache;
    }

    private static final Collection<Material> instantlyBreakingItems = getMaterialSet(
            "AIR", "GRASS", "TALL_GRASS", "SHORT_GRASS", "END_ROD", "BARRIER", "BRAIN_CORAL",
            "BRAIN_CORAL_FAN", "BUBBLE_CORAL", "BUBBLE_CORAL_FAN", "FIRE_CORAL", "FIRE_CORAL_FAN", "HORN_CORAL",
            "HORN_CORAL_FAN", "TUBE_CORAL", "TUBE_CORAL_FAN", "DEAD_BRAIN_CORAL", "DEAD_BRAIN_CORAL_FAN",
            "DEAD_BUBBLE_CORAL", "DEAD_BUBBLE_CORAL_FAN", "DEAD_FIRE_CORAL", "DEAD_FIRE_CORAL_FAN", "DEAD_HORN_CORAL",
            "DEAD_HORN_CORAL_FAN", "DEAD_TUBE_CORAL", "DEAD_TUBE_CORAL_FAN", "TORCH", "REDSTONE_TORCH",
            "WALL_TORCH", "REDSTONE_WALL_TORCH", "FERN", "LARGE_FERN", "BEETROOTS", "WHEAT", "POTATOES",
            "CARROTS", "OAK_SAPLING", "DARK_OAK_SAPLING", "SPRUCE_SAPLING", "ACACIA_SAPLING", "BIRCH_SAPLING",
            "JUNGLE_SAPLING", "FLOWER_POT", "POPPY", "DANDELION", "ALLIUM", "BLUE_ORCHID", "AZURE_BLUET",
            "RED_TULIP", "ORANGE_TULIP", "WHITE_TULIP", "PINK_TULIP", "OXEYE_DAISY", "CORNFLOWER",
            "LILY_OF_THE_VALLEY", "WITHER_ROSE", "SUNFLOWER", "LILAC", "ROSE_BUSH", "PEONY", "LILY_PAD",
            "FIRE", "DEAD_BUSH", "MELON_STEM", "PUMPKIN_STEM", "BROWN_MUSHROOM", "RED_MUSHROOM",
            "NETHER_WART", "REDSTONE_WIRE", "COMPARATOR", "REPEATER", "SLIME_BLOCK", "STRUCTURE_VOID",
            "SUGAR_CANE", "TNT", "TRIPWIRE", "TRIPWIRE_HOOK", "WARPED_FUNGUS", "CRIMSON_FUNGUS",
            "HONEY_BLOCK", "NETHER_SPROUTS", "CRIMSON_ROOTS", "WARPED_ROOTS", "TWISTING_VINES_PLANT",
            "WEEPING_VINES_PLANT", "SMALL_DRIPLEAF", "CAVE_VINES_PLANT", "CAVE_VINES", "SPORE_BLOSSOM",
            "AZALEA", "FLOWERING_AZALEA", "DECORATED_POT", "FROGSPAWN", "PINK_PETALS", "PITCHER_CROP",
            "PITCHER_PLANT", "TORCHFLOWER", "TORCHFLOWER_CROP"
    );

    public static boolean breaksInstantly(Material m){
        return instantlyBreakingItems.contains(m);
    }
}
