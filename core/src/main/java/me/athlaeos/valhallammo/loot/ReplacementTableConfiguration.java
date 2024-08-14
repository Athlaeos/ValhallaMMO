package me.athlaeos.valhallammo.loot;

import org.bukkit.NamespacedKey;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ReplacementTableConfiguration {
    private final Map<String, String> blockReplacementTables = new HashMap<>();
    private final Map<String, String> entityReplacementTables = new HashMap<>();
    private final Map<NamespacedKey, String> keyedReplacementTables = new HashMap<>();
    private String globalReplacementTables = null;
    private String fishingReplacementTableFish = null;
    private String fishingReplacementTableTreasure = null;
    private String fishingReplacementTableJunk = null;

    public String getGlobalReplacementTable() { return globalReplacementTables; }
    public Map<NamespacedKey, String> getKeyedReplacementTables() { return keyedReplacementTables; }
    public Map<String, String> getBlockReplacementTables() { return blockReplacementTables; }
    public Map<String, String> getEntityReplacementTables() { return entityReplacementTables; }
    public String getFishingReplacementTableFish() { return fishingReplacementTableFish; }
    public String getFishingReplacementTableJunk() { return fishingReplacementTableJunk; }
    public String getFishingReplacementTableTreasure() { return fishingReplacementTableTreasure; }

    public void setGlobalReplacementTables(String globalReplacementTables) { this.globalReplacementTables = globalReplacementTables; }
    public void setFishingReplacementTableFish(String fishingReplacementTableFish) { this.fishingReplacementTableFish = fishingReplacementTableFish; }
    public void setFishingReplacementTableJunk(String fishingReplacementTableJunk) { this.fishingReplacementTableJunk = fishingReplacementTableJunk; }
    public void setFishingReplacementTableTreasure(String fishingReplacementTableTreasure) { this.fishingReplacementTableTreasure = fishingReplacementTableTreasure; }
}
