package me.athlaeos.valhallammo.loot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jeff_media.customblockdata.CustomBlockData;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.dom.Weighted;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.persistence.GsonAdapter;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import me.athlaeos.valhallammo.utility.ItemUtils;
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

public class LootTableRegistry {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LootPredicate.class, new GsonAdapter<LootPredicate>("PRED_TYPE"))
            .registerTypeAdapter(DynamicItemModifier.class, new GsonAdapter<DynamicItemModifier>("MOD_TYPE"))
            .registerTypeAdapter(Weighted.class, new GsonAdapter<Weighted>("WEIGHTED_IMPL"))
            .registerTypeAdapter(IngredientChoice.class, new GsonAdapter<IngredientChoice>("CHOICE"))
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ItemStackGSONAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();
    private static final NamespacedKey STORED_LOOT_TABLE = new NamespacedKey(ValhallaMMO.getInstance(), "loot_table");
    private static final NamespacedKey FREE_SELECTION_TABLE = new NamespacedKey(ValhallaMMO.getInstance(), "free_selection_loot_table");
    private static final NamespacedKey FREE_SELECTION_ALLOW_DUPLICATES = new NamespacedKey(ValhallaMMO.getInstance(), "free_selection_allow_duplicates");
    private static final NamespacedKey LOOT_ITEM_SOUND = new NamespacedKey(ValhallaMMO.getInstance(), "loot_item_sound");

    private static final Map<String, ReplacementTable> replacementTables = new HashMap<>(); // registry for all replacement tables
    private static final Map<String, String> blockReplacementTables = new HashMap<>(); // all loot tables active on blocks
    private static final Map<String, String> entityReplacementTables = new HashMap<>(); // all loot tables active on entity drops
    private static final Map<NamespacedKey, String> keyedReplacementTables = new HashMap<>(); //
    private static String globalReplacementTable = null; // replacement tables that are active on all loot regardless of type
    private static final Map<String, Map<String, ReplacementPool>> replacementTableCache = new HashMap<>();

    private static final Map<String, LootTable> lootTables = new HashMap<>(); // registry for all loot tables
    private static final Map<String, String> blockLootTables = new HashMap<>(); // all loot tables active on blocks
    private static final Map<String, String> entityLootTables = new HashMap<>(); // all loot tables active on entity drops
    private static final Map<NamespacedKey, String> lootTableAdditions = new HashMap<>(); //
    private static String fishingLootTableFish;
    private static String fishingLootTableTreasure;
    private static String fishingLootTableJunk;
    private static String fishingReplacementTableFish;
    private static String fishingReplacementTableTreasure;
    private static String fishingReplacementTableJunk;

    private static LootTableConfiguration lootTableConfiguration;
    private static ReplacementTableConfiguration replacementTableConfiguration;

    @SuppressWarnings("all")
    public static void loadFiles(){
        File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/loot_table_config.json");
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (BufferedReader lootConfigReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
            LootTableConfiguration configuration = gson.fromJson(lootConfigReader, LootTableConfiguration.class);
            if (configuration == null) configuration = new LootTableConfiguration();
            lootTableConfiguration = configuration;
            applyConfiguration(configuration);
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
                MinecraftVersion fileVersionFilter = Arrays.stream(MinecraftVersion.values())
                        .filter(v -> v.getVersionString() != null && lootTable.getName().contains(v.getVersionString() + "+"))
                        .findFirst().orElse(null);
                // if a version is in the loot table's name, then it will not be loaded if the current minecraft version is older than it
                if (fileVersionFilter != null && !MinecraftVersion.currentVersionNewerThan(fileVersionFilter)) continue;
                loadFromFile(lootTable);
            }
        }

        File f2 = new File(ValhallaMMO.getInstance().getDataFolder(), "/replacement_table_config.json");
        try {
            f2.createNewFile();
        } catch (IOException ignored){}
        try (BufferedReader lootConfigReader = new BufferedReader(new FileReader(f2, StandardCharsets.UTF_8))) {
            ReplacementTableConfiguration configuration = gson.fromJson(lootConfigReader, ReplacementTableConfiguration.class);
            if (configuration == null) configuration = new ReplacementTableConfiguration();
            replacementTableConfiguration = configuration;
            applyConfiguration(configuration);
        } catch (IOException exception){
            ValhallaMMO.logSevere(exception.getMessage());
            exception.printStackTrace();
        }
        File replacementTablesFolder = new File(ValhallaMMO.getInstance().getDataFolder(), "/replacement_tables");
        replacementTablesFolder.mkdirs();
        File[] replacementTables = replacementTablesFolder.listFiles();
        if (replacementTables != null){
            for (File lootTable : replacementTables){
                if (!lootTable.getName().endsWith(".json")) continue;
                MinecraftVersion fileVersionFilter = Arrays.stream(MinecraftVersion.values())
                        .filter(v -> v.getVersionString() != null && lootTable.getName().contains(v.getVersionString() + "+"))
                        .findFirst().orElse(null);
                // if a version is in the loot table's name, then it will not be loaded if the current minecraft version is older than it
                if (fileVersionFilter != null && !MinecraftVersion.currentVersionNewerThan(fileVersionFilter)) continue;
                loadReplacementTableFromFile(lootTable);
            }
        }
    }

    public static void applyConfiguration(LootTableConfiguration lootTableConfiguration){
        if (lootTableConfiguration == null) return;

        blockLootTables.putAll(lootTableConfiguration.getBlockLootTables());
        entityLootTables.putAll(lootTableConfiguration.getEntityLootTables());
        lootTableAdditions.putAll(lootTableConfiguration.getLootTableAdditions());
        if (lootTableConfiguration.getFishingLootTableFish() != null) fishingLootTableFish = lootTableConfiguration.getFishingLootTableFish();
        if (lootTableConfiguration.getFishingLootTableJunk() != null) fishingLootTableJunk = lootTableConfiguration.getFishingLootTableJunk();
        if (lootTableConfiguration.getFishingLootTableTreasure() != null) fishingLootTableTreasure = lootTableConfiguration.getFishingLootTableTreasure();
    }

    public static void applyConfiguration(ReplacementTableConfiguration replacementTableConfiguration){
        if (replacementTableConfiguration == null) return;

        blockReplacementTables.putAll(replacementTableConfiguration.getBlockReplacementTables());
        entityReplacementTables.putAll(replacementTableConfiguration.getEntityReplacementTables());
        keyedReplacementTables.putAll(replacementTableConfiguration.getKeyedReplacementTables());
        if (replacementTableConfiguration.getGlobalReplacementTable() != null) globalReplacementTable = replacementTableConfiguration.getGlobalReplacementTable();
        if (replacementTableConfiguration.getFishingReplacementTableFish() != null) fishingReplacementTableFish = replacementTableConfiguration.getFishingReplacementTableFish();
        if (replacementTableConfiguration.getFishingReplacementTableFish() != null) fishingReplacementTableJunk = replacementTableConfiguration.getFishingReplacementTableJunk();
        if (replacementTableConfiguration.getFishingReplacementTableJunk() != null) fishingReplacementTableTreasure = replacementTableConfiguration.getFishingReplacementTableTreasure();
    }

    public static LootTableConfiguration getLootTableConfiguration() {
        return lootTableConfiguration;
    }

    public static ReplacementTableConfiguration getReplacementTableConfiguration() {
        return replacementTableConfiguration;
    }

    public static void loadLootTable(File file){
        loadFromFile(file);
    }

    public static void loadFromFile(File file){
        try (BufferedReader tableReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            LootTable table = gson.fromJson(tableReader, LootTable.class);
            registerLootTable(table, true);
        } catch (IOException exception){
            ValhallaMMO.logSevere(exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static void loadReplacementTableFromFile(File file){
        try (BufferedReader tableReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            ReplacementTable table = gson.fromJson(tableReader, ReplacementTable.class);
            registerReplacementTable(table, true);
        } catch (IOException exception){
            ValhallaMMO.logSevere(exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static ItemStack getReplacement(ReplacementTable table, LootContext context, LootTable.LootType type, ItemStack toReplace){
        if (table == null) return null;

        Map<String, ReplacementPool> cachedTableMap = replacementTableCache.computeIfAbsent(table.getKey(), key -> new HashMap<>());
        ReplacementPool pool = cachedTableMap.compute(toReplace.toString(), (key, cached) -> {
            if (cached == null) {
                for (ReplacementPool possible : table.getReplacementPools().values()) {
                    if (possible.getToReplace().getOption().matches(possible.getToReplace().getItem(), toReplace)) {
                        return possible;
                    }
                }
                return ReplacementPool.NONE;
            }
            return cached;
        });

        if (pool == ReplacementPool.NONE || table.failsPredicates(pool.getPredicateSelection(), type, context, pool.getPredicates())) {
            return null;
        }

        Collection<ReplacementEntry> entries = pool.getEntries().values();
        entries.removeIf(entry -> (context.getKiller() == null && DynamicItemModifier.requiresPlayer(entry.getModifiers())) || table.failsPredicates(entry.getPredicateSelection(), type, context, entry.getPredicates()));
        List<ReplacementEntry> weighted = Utils.weightedSelection(entries, 1, context.getLuck(), context.getLootingModifier());
        if (weighted.isEmpty()) return null;

        ReplacementEntry selectedEntry = weighted.get(0);
        if (selectedEntry == null || ItemUtils.isEmpty(selectedEntry.getReplaceBy())) return null;

        ItemBuilder builder = selectedEntry.tinker()
                ? new ItemBuilder(toReplace)
                : new ItemBuilder(selectedEntry.getReplaceBy());

        DynamicItemModifier.modify(ModifierContext.builder(builder).crafter((Player) context.getKiller()).executeUsageMechanics().validate().get(), selectedEntry.getModifiers());
        return CustomFlag.hasFlag(builder.getMeta(), CustomFlag.UNCRAFTABLE) ? null : builder.get();
    }

    public static List<ItemStack> getLoot(LootTable table, LootContext context, LootTable.LootType type){
        List<ItemStack> loot = new ArrayList<>();
        for (LootPool pool : table.getPools().values()) {
            if (table.failsPredicates(pool.getPredicateSelection(), type, context, pool.getPredicates())
                    || Utils.getRandom().nextDouble() > (pool.getDropChance() + (pool.getDropLuckChance() * context.getLuck()))) continue;

            List<LootEntry> passed = new ArrayList<>();
            List<LootEntry> selectedEntries = new ArrayList<>();
            for (LootEntry entry : pool.getEntries().values()) {
                if (!table.failsPredicates(entry.getPredicateSelection(), type, context, entry.getPredicates())) {
                    (entry.isGuaranteedPresent() ? selectedEntries : passed).add(entry);                }
            }
            if (passed.isEmpty() && selectedEntries.isEmpty()) continue;

            if (pool.isWeighted()) {
                selectedEntries.addAll(Utils.weightedSelection(passed, pool.getRolls(context), context.getLuck(), context.getLootingModifier()));
            } else {
                for (LootEntry entry : passed){
                    if (Utils.proc(entry.getChance(context.getLuck()), 0, true)) {
                        selectedEntries.add(entry);
                    }
                }
            }

            for (LootEntry selectedEntry : selectedEntries){
                if (ItemUtils.isEmpty(selectedEntry.getDrop())) continue;
                ItemStack item;
                if (selectedEntry.getModifiers().isEmpty()){ // if no modifiers, no need to grab metadata from the item and so forth.
                    item = selectedEntry.getDrop().clone();
                } else {
                    ItemBuilder builder = new ItemBuilder(selectedEntry.getDrop());
                    if (context.getKiller() == null && DynamicItemModifier.requiresPlayer(selectedEntry.getModifiers())) continue;
                    DynamicItemModifier.modify(ModifierContext.builder(builder).crafter((Player) context.getKiller()).executeUsageMechanics().validate().get(), selectedEntry.getModifiers());
                    if (CustomFlag.hasFlag(builder.getMeta(), CustomFlag.UNCRAFTABLE)) continue;
                    item = builder.get();
                }

                int quantityMin = Utils.randomAverage(selectedEntry.getBaseQuantityMin() + (Math.max(0, context.getLootingModifier()) * selectedEntry.getQuantityMinFortuneBase()));
                int quantityMax = Utils.randomAverage(selectedEntry.getBaseQuantityMax() + (Math.max(0, context.getLootingModifier()) * selectedEntry.getQuantityMaxFortuneBase()));
                if (quantityMax < quantityMin) quantityMax = quantityMin;
                int quantity = Utils.getRandom().nextInt(Math.max(1, quantityMax - quantityMin + 1)) + quantityMin;

                int trueQuantity = selectedEntry.getDrop().getAmount() * quantity;
                if (trueQuantity > 0) loot.addAll(ItemUtils.decompressStacks(Map.of(item, trueQuantity)));
                else loot.add(item);
            }
        }
        return loot;
    }

    @SuppressWarnings("all")
    public static void saveAll(){
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

        LootTableConfiguration lootConfiguration = new LootTableConfiguration();
        lootConfiguration.getBlockLootTables().putAll(blockLootTables);
        lootConfiguration.getLootTableAdditions().putAll(lootTableAdditions);
        lootConfiguration.getEntityLootTables().putAll(entityLootTables);
        lootConfiguration.setFishingLootTableFish(fishingLootTableFish);
        lootConfiguration.setFishingLootTableJunk(fishingLootTableJunk);
        lootConfiguration.setFishingLootTableTreasure(fishingLootTableTreasure);
        File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/loot_table_config.json");
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (FileWriter writer = new FileWriter(f)){
            gson.toJson(lootConfiguration, writer);
        } catch (IOException exception){
            ValhallaMMO.logSevere(exception.getMessage());
            exception.printStackTrace();
        }

        new File(ValhallaMMO.getInstance().getDataFolder(), "/replacement_tables").mkdirs();
        for (ReplacementTable table : replacementTables.values()){
            File f2 = new File(ValhallaMMO.getInstance().getDataFolder(), "/replacement_tables/" + table.getKey() + ".json");
            try {
                f2.createNewFile();
            } catch (IOException ignored){}
            try (FileWriter writer = new FileWriter(f2)){
                gson.toJson(table, writer);
            } catch (IOException exception){
                ValhallaMMO.logSevere(exception.getMessage());
                exception.printStackTrace();
            }
        }

        ReplacementTableConfiguration replacementConfiguration = new ReplacementTableConfiguration();
        replacementConfiguration.getBlockReplacementTables().putAll(blockReplacementTables);
        replacementConfiguration.getKeyedReplacementTables().putAll(keyedReplacementTables);
        replacementConfiguration.getEntityReplacementTables().putAll(entityReplacementTables);
        replacementConfiguration.setGlobalReplacementTables(globalReplacementTable);
        replacementConfiguration.setFishingReplacementTableFish(fishingReplacementTableFish);
        replacementConfiguration.setFishingReplacementTableJunk(fishingReplacementTableJunk);
        replacementConfiguration.setFishingReplacementTableTreasure(fishingReplacementTableTreasure);
        File f2 = new File(ValhallaMMO.getInstance().getDataFolder(), "/replacement_table_config.json");
        try {
            f2.createNewFile();
        } catch (IOException ignored){}
        try (FileWriter writer = new FileWriter(f2)){
            gson.toJson(replacementConfiguration, writer);
        } catch (IOException exception){
            ValhallaMMO.logSevere(exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static void resetReplacementTableCache(){
        replacementTableCache.clear();
    }

    public static void registerLootTable(LootTable table, boolean overwrite){
        if (overwrite || !lootTables.containsKey(table.getKey())) lootTables.put(table.getKey(), table);
    }

    public static void registerReplacementTable(ReplacementTable table, boolean overwrite){
        if (overwrite || !replacementTables.containsKey(table.getKey())) {
            replacementTables.put(table.getKey(), table);
            resetReplacementTableCache();
        }
    }

    public static LootTable getLootTable(Material block){
        String table = blockLootTables.get(block.toString());
        if (table == null) return null;
        return lootTables.get(table);
    }

    public static LootTable getLootTable(EntityType entity){
        String table = entityLootTables.get(entity.toString());
        if (table == null) return null;
        return lootTables.get(table);
    }

    public static ReplacementTable getReplacementTable(Material block){
        String table = blockReplacementTables.get(block.toString());
        if (table == null) return null;
        return replacementTables.get(table);
    }

    public static ReplacementTable getReplacementTable(EntityType entity){
        String table = entityReplacementTables.get(entity.toString());
        if (table == null) return null;
        return replacementTables.get(table);
    }

    public static LootTable getLootTable(LootTables lootTable){
        return getLootTable(lootTable.getKey());
    }

    public static ReplacementTable getReplacementTable(LootTables lootTable){
        return getReplacementTable(lootTable.getKey());
    }

    public static LootTable getLootTable(NamespacedKey lootTable){
        String table = lootTableAdditions.get(lootTable);
        if (table == null) return null;
        return lootTables.get(table);
    }

    public static ReplacementTable getReplacementTable(NamespacedKey replacementTable){
        String table = keyedReplacementTables.get(replacementTable);
        if (table == null) return null;
        return replacementTables.get(table);
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

    public static ReplacementTable getFishingFishReplacementTable(){
        if (fishingLootTableFish == null) return null;
        return replacementTables.get(fishingReplacementTableFish);
    }

    public static ReplacementTable getFishingTreasureReplacementTable(){
        if (fishingLootTableTreasure == null) return null;
        return replacementTables.get(fishingReplacementTableTreasure);
    }

    public static ReplacementTable getFishingJunkLReplacementTable(){
        if (fishingLootTableJunk == null) return null;
        return replacementTables.get(fishingReplacementTableJunk);
    }

    public static Map<String, String> getBlockLootTables() {
        return blockLootTables;
    }

    public static Map<String, String> getEntityLootTables() {
        return entityLootTables;
    }

    public static Map<NamespacedKey, String> getLootTableAdditions() {
        return lootTableAdditions;
    }

    public static Map<String, String> getBlockReplacementTables() {
        return blockReplacementTables;
    }

    public static Map<String, String> getEntityReplacementTables() {
        return entityReplacementTables;
    }

    public static Map<NamespacedKey, String> getKeyedReplacementTables() {
        return keyedReplacementTables;
    }

    public static String getGlobalReplacementTableName() {
        return globalReplacementTable;
    }

    public static ReplacementTable getGlobalReplacementTable(){
        return replacementTables.get(globalReplacementTable);
    }

    public static String getFishingLootTableFish() {
        return fishingLootTableFish;
    }

    public static String getFishingLootTableJunk() {
        return fishingLootTableJunk;
    }

    public static String getFishingLootTableTreasure() {
        return fishingLootTableTreasure;
    }

    public static String getFishingReplacementTableFish() {
        return fishingReplacementTableFish;
    }

    public static String getFishingReplacementTableJunk() {
        return fishingReplacementTableJunk;
    }

    public static String getFishingReplacementTableTreasure() {
        return fishingReplacementTableTreasure;
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

    public static void setFishingReplacementTableJunk(String fishingReplacementTableJunk) {
        LootTableRegistry.fishingReplacementTableJunk = fishingReplacementTableJunk;
    }

    public static void setFishingReplacementTableFish(String fishingReplacementTableFish) {
        LootTableRegistry.fishingReplacementTableFish = fishingReplacementTableFish;
    }

    public static void setFishingReplacementTableTreasure(String fishingReplacementTableTreasure) {
        LootTableRegistry.fishingReplacementTableTreasure = fishingReplacementTableTreasure;
    }

    public static Map<String, LootTable> getLootTables() {
        return lootTables;
    }

    public static Map<String, ReplacementTable> getReplacementTables() {
        return replacementTables;
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

    public static void setGlobalReplacementTable(String globalReplacementTable) {
        LootTableRegistry.globalReplacementTable = globalReplacementTable;
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
