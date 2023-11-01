package me.athlaeos.valhallammo.loot.predicates;

import me.athlaeos.valhallammo.loot.predicates.implementations.BlockMaterial;

import java.util.HashMap;
import java.util.Map;

public class PredicateRegistry {
    private static final Map<String, LootPredicate> predicates = new HashMap<>();

    static {
        register(new BlockMaterial());
    }

    public static void register(LootPredicate predicate){
        predicates.put(predicate.getKey(), predicate);
    }

    public static LootPredicate createPredicate(String name){
        if (!predicates.containsKey(name)) throw new IllegalArgumentException("Loot Predicate " + name + " doesn't exist");
        return predicates.get(name).createNew();
    }

    public static Map<String, LootPredicate> getPredicates() {
        return predicates;
    }
}
