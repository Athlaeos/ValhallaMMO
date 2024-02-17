package me.athlaeos.valhallammo.loot;

import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import org.bukkit.Material;
import org.bukkit.loot.LootContext;

import java.util.*;

public class LootTable {
    private final String key;
    private final Map<String, LootPool> pools = new HashMap<>();
    private Material icon = Material.CHEST;
    private VanillaLootPreservationType vanillaLootPreservationType = VanillaLootPreservationType.KEEP;

    public LootTable(String key){
        this.key = key;
    }

    public LootPool addPool(String key){
        LootPool pool = new LootPool(key, this.key);
        pools.put(key, pool);
        return pool;
    }
    public String getKey() { return key; }
    public Material getIcon() { return icon; }
    public void setIcon(Material icon) { this.icon = icon; }
    public Map<String, LootPool> getPools() { return pools; }
    public VanillaLootPreservationType getVanillaLootPreservationType() { return vanillaLootPreservationType; }
    public void setVanillaLootPreservationType(VanillaLootPreservationType vanillaLootPreservationType) { this.vanillaLootPreservationType = vanillaLootPreservationType; }

    public boolean failsPredicates(PredicateSelection predicateSelection, LootType type, LootContext context, Collection<LootPredicate> predicates){
        if (predicates.isEmpty()) return false;
        return !switch (predicateSelection) {
            case ALL -> predicates.stream().allMatch(p -> p.isCompatibleWithLootType(type) && p.test(context));
            case ANY -> predicates.stream().anyMatch(p -> p.isCompatibleWithLootType(type) && p.test(context));
        };
    }

    public enum PredicateSelection{
        ANY,
        ALL
    }

    public enum LootType{
        BREAK,
        CONTAINER,
        FISH,
        KILL,
        PIGLIN_BARTER,
        ARCHAEOLOGY
    }

    public enum VanillaLootPreservationType{
        CLEAR, // all vanilla loot is removed, loot table handles all drops instead of it
        CLEAR_UNLESS_EMPTY, // all vanilla loot is removed only if loot table produces drops
        KEEP // all vanilla loot is kept, loot table drops in addition to it
    }
}
