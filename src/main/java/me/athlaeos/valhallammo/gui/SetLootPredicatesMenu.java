package me.athlaeos.valhallammo.gui;

import me.athlaeos.valhallammo.loot.predicates.LootPredicate;

import java.util.Collection;

public interface SetLootPredicatesMenu {
    void setPredicates(Collection<LootPredicate> predicates);

    Collection<LootPredicate> getPredicates();
}
