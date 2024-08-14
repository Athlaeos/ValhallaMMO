package me.athlaeos.valhallammo.loot;

import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import org.bukkit.Material;
import org.bukkit.loot.LootContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ReplacementTable {
    private final String key;
    private final Map<String, ReplacementPool> replacementPools = new HashMap<>();
    private Material icon = Material.KNOWLEDGE_BOOK;

    public ReplacementTable(String key){
        this.key = key;
    }

    public String getKey() { return key; }
    public Map<String, ReplacementPool> getReplacementPools() { return replacementPools; }
    public Material getIcon() { return icon; }

    public void setIcon(Material icon) { this.icon = icon; }

    public ReplacementPool addPool(String key){
        ReplacementPool pool = new ReplacementPool(key, this.key);
        replacementPools.put(key, pool);
        return pool;
    }

    public boolean failsPredicates(LootTable.PredicateSelection predicateSelection, LootTable.LootType type, LootContext context, Collection<LootPredicate> predicates){
        if (predicates.isEmpty()) return false;
        return !switch (predicateSelection) {
            case ALL -> predicates.stream().allMatch(p -> !p.isCompatibleWithLootType(type) || p.test(context));
            case ANY -> predicates.stream().anyMatch(p -> !p.isCompatibleWithLootType(type) || p.test(context));
        };
    }
}
