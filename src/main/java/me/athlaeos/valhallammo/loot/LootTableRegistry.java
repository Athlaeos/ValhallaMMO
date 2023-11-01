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
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

    private static final Map<String, LootTable> lootTables = new HashMap<>();
    private static final Map<Material, String> blockLootTables = new HashMap<>();
    private static final Map<EntityType, String> entityLootTables = new HashMap<>();
    private static final Map<NamespacedKey, String> lootTableAdditions = new HashMap<>();
    private static String fishingLootTable;

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
            fishingLootTable = configuration.getFishingLootTable();
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
                try (BufferedReader tableReader = new BufferedReader(new FileReader(lootTable, StandardCharsets.UTF_8))) {
                    LootTable table = gson.fromJson(tableReader, LootTable.class);
                    registerLootTable(table);
                } catch (IOException exception){
                    ValhallaMMO.logSevere(exception.getMessage());
                    exception.printStackTrace();
                }
            }
        }
    }

    public static List<ItemStack> getLoot(LootTable table, LootContext context, LootTable.LootType type){
        List<ItemStack> loot = new ArrayList<>();
        for (String poolName : table.getPools().keySet()){
            LootPool pool = table.getPools().get(poolName);
            if (table.failsPredicates(pool.getPredicateSelection(), type, context, pool.getPredicates())) continue;
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
                    TranslationManager.translateItemMeta(builder.getMeta());
                    item = builder.get();
                }

                int quantityMin = Utils.randomAverage(selectedEntry.getBaseQuantityMin() + (Math.max(0, context.getLootingModifier()) * selectedEntry.getQuantityMinFortuneBase()));
                int quantityMax = Utils.randomAverage(selectedEntry.getBaseQuantityMax() + (Math.max(0, context.getLootingModifier()) * selectedEntry.getQuantityMaxFortuneBase()));
                if (quantityMax < quantityMin) quantityMax = quantityMin;
                int quantity = Utils.getRandom().nextInt(Math.max(1, quantityMax - quantityMin)) + quantityMin;

                int trueQuantity = selectedEntry.getDrop().getAmount() * quantity;
                if (trueQuantity > 0 ) {
                    loot.addAll(ItemUtils.decompressStacks(Map.of(item, trueQuantity)));
                } else continue;

                loot.add(item);
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
        configuration.setFishingLootTable(fishingLootTable);
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

    public static LootTable getFishingLootTable(){
        if (fishingLootTable == null) return null;
        return lootTables.get(fishingLootTable);
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
        return fishingLootTable;
    }

    public static void setFishingLootTable(String fishingLootTable) {
        LootTableRegistry.fishingLootTable = fishingLootTable;
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
}
