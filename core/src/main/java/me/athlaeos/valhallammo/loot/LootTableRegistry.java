package me.athlaeos.valhallammo.loot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jeff_media.customblockdata.CustomBlockData;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.dom.Weighted;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.persistence.GsonAdapter;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class LootTableRegistry {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LootPredicate.class, new GsonAdapter<LootPredicate>("PRED_TYPE"))
            .registerTypeAdapter(DynamicItemModifier.class, new GsonAdapter<DynamicItemModifier>("MOD_TYPE"))
            .registerTypeAdapter(Weighted.class, new GsonAdapter<Weighted>("WEIGHTED_IMPL"))
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ItemStackGSONAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();
    private static final NamespacedKey STORED_LOOT_TABLE = new NamespacedKey(ValhallaMMO.getInstance(), "loot_table");
    private static final NamespacedKey FREE_SELECTION_TABLE = new NamespacedKey(ValhallaMMO.getInstance(), "free_selection_loot_table");
    private static final NamespacedKey FREE_SELECTION_ALLOW_DUPLICATES = new NamespacedKey(ValhallaMMO.getInstance(), "free_selection_allow_duplicates");
    private static final NamespacedKey LOOT_ITEM_SOUND = new NamespacedKey(ValhallaMMO.getInstance(), "loot_item_sound");

    private static final Map<String, LootTable> lootTables = new HashMap<>();
    private static final Map<Material, String> blockLootTables = new HashMap<>();
    private static final Map<EntityType, String> entityLootTables = new HashMap<>();
    private static final Map<NamespacedKey, String> lootTableAdditions = new HashMap<>();
    private static String fishingLootTableFish;
    private static String fishingLootTableTreasure;
    private static String fishingLootTableJunk;

    @SuppressWarnings("all")
    public static void loadFiles(){
        File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/loot_table_config.json");
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (BufferedReader lootConfigReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
            LootTableConfiguration configuration = gson.fromJson(lootConfigReader, LootTableConfiguration.class);
            if (configuration == null) configuration = new LootTableConfiguration();
            blockLootTables.putAll(configuration.getBlockLootTables());
            entityLootTables.putAll(configuration.getEntityLootTables());
            lootTableAdditions.putAll(configuration.getLootTableAdditions());
            fishingLootTableFish = configuration.getFishingLootTableFish();
            fishingLootTableJunk = configuration.getFishingLootTableJunk();
            fishingLootTableTreasure = configuration.getFishingLootTableTreasure();
        } catch (IOException exception){
            ValhallaMMO.logSevere(exception.getMessage());
            exception.printStackTrace();
        }
        File lootTablesFolder = new File(ValhallaMMO.getInstance().getDataFolder(), "/loot_tables");
        lootTablesFolder.mkdirs();
        File[] lootTables = lootTablesFolder.listFiles();
        if (lootTables != null){
            for (File lootTable : lootTables){
                if (!lootTable.getName().endsWith(".json")) continue;
                loadFromFile(lootTable);
            }
        }
    }

    public static void loadFromFile(File file){
        try (BufferedReader tableReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            LootTable table = gson.fromJson(tableReader, LootTable.class);
            registerLootTable(table);
        } catch (IOException exception){
            ValhallaMMO.logSevere(exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static List<ItemStack> getLoot(LootTable table, LootContext context, LootTable.LootType type){
        List<ItemStack> loot = new ArrayList<>();
        for (String poolName : table.getPools().keySet()){
            LootPool pool = table.getPools().get(poolName);
            if (table.failsPredicates(pool.getPredicateSelection(), type, context, pool.getPredicates())) continue;
            double rand = Utils.getRandom().nextDouble();
            if (rand > (pool.getDropChance() + (pool.getDropLuckChance() * context.getLuck()))) continue;

            Collection<LootEntry> selectedEntries = new ArrayList<>();
            if (pool.isWeighted()){
                // weighted selection
                selectedEntries.addAll(Utils.weightedSelection(pool.getEntries().values().stream().filter(e -> {
                    if (table.failsPredicates(e.getPredicateSelection(), type, context, e.getPredicates())) return false;
                    if (e.isGuaranteedPresent()){
                        selectedEntries.add(e);
                        return false;
                    }
                    return true;
                }).collect(Collectors.toList()), pool.getRolls(context), context.getLuck())); // weighted selection excluding any failed entries or guaranteed drops
            } else {
                for (LootEntry entry : pool.getEntries().values()){
                    if (table.failsPredicates(entry.getPredicateSelection(), type, context, entry.getPredicates())) continue;
                    double chance = entry.isGuaranteedPresent() ? 1 : entry.getChance(context.getLuck());

                    if (Utils.proc(chance, 0, true)) selectedEntries.add(entry);
                }
            }
            for (LootEntry selectedEntry : selectedEntries){
                ItemStack item;
                if (selectedEntry.getModifiers().isEmpty()){ // if no modifiers, no need to grab metadata from the item and so forth.
                    item = selectedEntry.getDrop().clone();
                } else {
                    ItemBuilder builder = new ItemBuilder(selectedEntry.getDrop());
                    if (context.getKiller() == null && selectedEntry.getModifiers().stream().anyMatch(DynamicItemModifier::requiresPlayer)) continue;
                    DynamicItemModifier.modify(builder, (Player) context.getKiller(), selectedEntry.getModifiers(), false, true, true);
                    if (CustomFlag.hasFlag(builder.getMeta(), CustomFlag.UNCRAFTABLE)) continue;
                    item = builder.get();
                }

                int quantityMin = Utils.randomAverage(selectedEntry.getBaseQuantityMin() + (Math.max(0, context.getLootingModifier()) * selectedEntry.getQuantityMinFortuneBase()));
                int quantityMax = Utils.randomAverage(selectedEntry.getBaseQuantityMax() + (Math.max(0, context.getLootingModifier()) * selectedEntry.getQuantityMaxFortuneBase()));
                if (quantityMax < quantityMin) quantityMax = quantityMin;
                int quantity = Utils.getRandom().nextInt(Math.min(1, quantityMax - quantityMin + 1)) + quantityMin;

                int trueQuantity = selectedEntry.getDrop().getAmount() * quantity;
                if (trueQuantity > 0) loot.addAll(ItemUtils.decompressStacks(Map.of(item, trueQuantity)));
                else loot.add(item);
            }
        }
        return loot;
    }

    @SuppressWarnings("all")
    public static void saveLootTables(){
        new File(ValhallaMMO.getInstance().getDataFolder(), "/loot_tables").mkdirs();
        for (LootTable table : lootTables.values()){
            File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/loot_tables/" + table.getKey() + ".json");
            try {
                f.createNewFile();
            } catch (IOException ignored){}
            try (FileWriter writer = new FileWriter(f)){
                gson.toJson(table, writer);
            } catch (IOException exception){
                ValhallaMMO.logSevere(exception.getMessage());
                exception.printStackTrace();
            }
        }

        LootTableConfiguration configuration = new LootTableConfiguration();
        configuration.getBlockLootTables().putAll(blockLootTables);
        configuration.getLootTableAdditions().putAll(lootTableAdditions);
        configuration.getEntityLootTables().putAll(entityLootTables);
        configuration.setFishingLootTableFish(fishingLootTableFish);
        configuration.setFishingLootTableJunk(fishingLootTableJunk);
        configuration.setFishingLootTableTreasure(fishingLootTableTreasure);
        File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/loot_table_config.json");
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (FileWriter writer = new FileWriter(f)){
            gson.toJson(configuration, writer);
        } catch (IOException exception){
            ValhallaMMO.logSevere(exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static void registerLootTable(LootTable table){
        lootTables.put(table.getKey(), table);
    }

    public static LootTable getLootTable(Material block){
        String table = blockLootTables.get(block);
        if (table == null) return null;
        return lootTables.get(table);
    }

    public static LootTable getLootTable(EntityType entity){
        String table = entityLootTables.get(entity);
        if (table == null) return null;
        return lootTables.get(table);
    }

    public static LootTable getLootTable(LootTables lootTable){
        return getLootTable(lootTable.getKey());
    }

    public static LootTable getLootTable(NamespacedKey lootTable){
        String table = lootTableAdditions.get(lootTable);
        if (table == null) return null;
        return lootTables.get(table);
    }

    public static LootTable getFishingFishLootTable(){
        if (fishingLootTableFish == null) return null;
        return lootTables.get(fishingLootTableFish);
    }

    public static LootTable getFishingTreasureLootTable(){
        if (fishingLootTableTreasure == null) return null;
        return lootTables.get(fishingLootTableTreasure);
    }

    public static LootTable getFishingJunkLootTable(){
        if (fishingLootTableJunk == null) return null;
        return lootTables.get(fishingLootTableJunk);
    }

    public static Map<Material, String> getBlockLootTables() {
        return blockLootTables;
    }

    public static Map<EntityType, String> getEntityLootTables() {
        return entityLootTables;
    }

    public static Map<NamespacedKey, String> getLootTableAdditions() {
        return lootTableAdditions;
    }

    public static String getFishingTableName() {
        return fishingLootTableFish;
    }

    public static String getFishingLootTableJunk() {
        return fishingLootTableJunk;
    }

    public static String getFishingLootTableTreasure() {
        return fishingLootTableTreasure;
    }

    public static void setFishingLootTableFish(String fishingLootTableFish) {
        LootTableRegistry.fishingLootTableFish = fishingLootTableFish;
    }

    public static void setFishingLootTableTreasure(String fishingLootTableTreasure) {
        LootTableRegistry.fishingLootTableTreasure = fishingLootTableTreasure;
    }

    public static void setFishingLootTableJunk(String fishingLootTableJunk) {
        LootTableRegistry.fishingLootTableJunk = fishingLootTableJunk;
    }

    public static Map<String, LootTable> getLootTables() {
        return lootTables;
    }

    public static LootTable getLootTable(ItemMeta meta){
        return lootTables.get(ItemUtils.getPDCString(STORED_LOOT_TABLE, meta, ""));
    }

    public static LootTable getLootTable(Entity e){
        return lootTables.getOrDefault(e.getPersistentDataContainer().getOrDefault(STORED_LOOT_TABLE, PersistentDataType.STRING, ""), getLootTable(e.getType()));
    }

    public static LootTable getLootTable(Block b, Material def){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        return lootTables.getOrDefault(customBlockData.getOrDefault(STORED_LOOT_TABLE, PersistentDataType.STRING, ""), getLootTable(def));
    }

    public static void setLootTable(ItemMeta meta, LootTable table){
        if (table == null) meta.getPersistentDataContainer().remove(STORED_LOOT_TABLE);
        else meta.getPersistentDataContainer().set(STORED_LOOT_TABLE, PersistentDataType.STRING, table.getKey());
    }

    public static void setLootTable(Entity e, LootTable table){
        if (table == null) e.getPersistentDataContainer().remove(STORED_LOOT_TABLE);
        else e.getPersistentDataContainer().set(STORED_LOOT_TABLE, PersistentDataType.STRING, table.getKey());
    }

    public static void setLootTable(Block b, LootTable table){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        if (table == null) customBlockData.remove(STORED_LOOT_TABLE);
        else customBlockData.set(STORED_LOOT_TABLE, PersistentDataType.STRING, table.getKey());
    }

    public static void setFreeSelectionTable(ItemMeta meta, boolean freeSelection, boolean allowRepeats){
        if (freeSelection) meta.getPersistentDataContainer().set(FREE_SELECTION_TABLE, PersistentDataType.BYTE, (byte) 1);
        else meta.getPersistentDataContainer().remove(FREE_SELECTION_TABLE);
        if (allowRepeats) meta.getPersistentDataContainer().set(FREE_SELECTION_ALLOW_DUPLICATES, PersistentDataType.BYTE, (byte) 1);
        else meta.getPersistentDataContainer().remove(FREE_SELECTION_ALLOW_DUPLICATES);
    }

    /**
     * If true, the item on which the loot table is present instead opens a GUI showing the items that the player will get (guaranteed entries)
     * as well as the entries that they can choose from. They may choose entries equal to the amount of rolls determined by the <b>first loot pool found</b>.
     * If allowRepeatedFreeSelection() is also true, they may choose the same entry several times. If all "rolls" are spent, they may claim the reward.
     * @return true if the item should open a gui when interacted with, if it has a loot table
     */
    public static boolean isFreeSelectionTable(ItemMeta meta){
        return meta.getPersistentDataContainer().has(FREE_SELECTION_TABLE, PersistentDataType.BYTE);
    }

    /**
     * Repeated selection means that the player is allowed to select a drop several times, in case they want it more than once.
     * @return true if the player can select the same entry several times
     */
    public static boolean allowRepeatedFreeSelection(ItemMeta meta){
        return meta.getPersistentDataContainer().has(FREE_SELECTION_ALLOW_DUPLICATES, PersistentDataType.BYTE);
    }

    public static void setLootSound(ItemMeta meta, Sound sound){
        if (sound == null) meta.getPersistentDataContainer().remove(LOOT_ITEM_SOUND);
        else meta.getPersistentDataContainer().set(LOOT_ITEM_SOUND, PersistentDataType.STRING, sound.toString());
    }

    public static Sound getLootSound(ItemMeta meta){
        try {
            return Sound.valueOf(meta.getPersistentDataContainer().getOrDefault(LOOT_ITEM_SOUND, PersistentDataType.STRING, ""));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
