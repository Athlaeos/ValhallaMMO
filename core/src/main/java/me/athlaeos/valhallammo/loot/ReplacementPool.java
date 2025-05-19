package me.athlaeos.valhallammo.loot;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ReplacementPool {
    public static final ReplacementPool NONE = new ReplacementPool("", "");

    private final String parentTable;
    private final String key;
    private final Map<UUID, ReplacementEntry> entries = new HashMap<>();
    private final Collection<LootPredicate> predicates = new HashSet<>();
    private LootTable.PredicateSelection predicateSelection = LootTable.PredicateSelection.ANY;
    private SlotEntry toReplace = new SlotEntry(new ItemStack(Material.IRON_PICKAXE), new MaterialChoice()); // SlotEntries are typically meant for recipes, but they're also perfect for replacement tables

    public ReplacementPool(String key, String parentTable){
        this.key = key;
        this.parentTable = parentTable;
    }

    public ReplacementPool(String key, String parentTable, ItemStack toReplace){
        this.key = key;
        this.parentTable = parentTable;
        this.toReplace = new SlotEntry(toReplace, new MaterialChoice());
    }
    public ReplacementEntry addEntry(ItemStack drop){
        UUID random = UUID.randomUUID();
        ReplacementEntry entry = new ReplacementEntry(random, this.key, drop);
        entries.put(random, entry);
        return entry;
    }

    public String getParentTable() { return parentTable; }
    public String getKey() { return key; }
    public SlotEntry getToReplace() { return toReplace; }
    public LootTable.PredicateSelection getPredicateSelection() { return predicateSelection; }
    public Collection<LootPredicate> getPredicates() { return predicates; }
    public Map<UUID, ReplacementEntry> getEntries() { return entries; }

    public void setToReplace(SlotEntry toReplace) { this.toReplace = toReplace; }
    public void setPredicateSelection(LootTable.PredicateSelection predicateSelection) { this.predicateSelection = predicateSelection; }
}
