package me.athlaeos.valhallammo.loot;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class LootTableConfiguration {
    private final Map<Material, String> blockLootTables = new HashMap<>();
    private final Map<EntityType, String> entityLootTables = new HashMap<>();
    private final Map<NamespacedKey, String> lootTableAdditions = new HashMap<>();
    private String fishingLootTable = null;

    public Map<NamespacedKey, String> getLootTableAdditions() { return lootTableAdditions; }
    public Map<EntityType, String> getEntityLootTables() { return entityLootTables; }
    public Map<Material, String> getBlockLootTables() { return blockLootTables; }
    public String getFishingLootTable() { return fishingLootTable; }
    public void setFishingLootTable(String fishingLootTable) { this.fishingLootTable = fishingLootTable; }
}
