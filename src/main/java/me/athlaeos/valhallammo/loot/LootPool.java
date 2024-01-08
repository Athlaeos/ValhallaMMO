package me.athlaeos.valhallammo.loot;

import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.*;

public class LootPool {
    private final String parentTable;
    private final String key;
    private final Map<UUID, LootEntry> entries = new HashMap<>();
    private final Collection<LootPredicate> predicates = new HashSet<>();
    private boolean weighted = false;
    // weighted tables roll x amount of times through its available entries, while chanced tables roll through each entry and applies its chance
    private int weightedRolls = 1;
    private double bonusLuckRolls = 0.5;
    private double dropChance = 1;
    private double dropLuckChance = 0;
    private LootTable.PredicateSelection predicateSelection = LootTable.PredicateSelection.ANY;

    public LootPool(String key, String parentTable){
        this.key = key;
        this.parentTable = parentTable;
    }

    public String getKey() { return key; }
    public String getParentTable() { return parentTable; }
    public LootEntry addEntry(ItemStack drop){
        UUID random = UUID.randomUUID();
        LootEntry entry = new LootEntry(random, drop, this.key);
        entries.put(random, entry);
        return entry;
    }

    public Map<UUID, LootEntry> getEntries() { return entries; }
    public Collection<LootPredicate> getPredicates() { return predicates; }
    public int getWeightedRolls() { return weightedRolls; }
    public int getRolls(LootContext context){
        return Utils.randomAverage(weightedRolls + (context.getLuck() * bonusLuckRolls));
    }
    public boolean isWeighted() { return weighted; }
    public double getBonusLuckRolls() { return bonusLuckRolls; }
    public LootTable.PredicateSelection getPredicateSelection() { return predicateSelection; }
    public double getDropChance() { return dropChance; }
    public double getDropLuckChance() { return dropLuckChance; }
    public void setWeightedRolls(int weightedRolls) { this.weightedRolls = weightedRolls; }
    public void setWeighted(boolean weighted) { this.weighted = weighted; }
    public void setBonusLuckRolls(double bonusLuckRolls) { this.bonusLuckRolls = bonusLuckRolls; }
    public void setPredicateSelection(LootTable.PredicateSelection predicateSelection) { this.predicateSelection = predicateSelection; }
    public void setDropChance(double dropChance) { this.dropChance = dropChance; }
    public void setDropLuckChance(double dropLuckChance) { this.dropLuckChance = dropLuckChance; }
}
