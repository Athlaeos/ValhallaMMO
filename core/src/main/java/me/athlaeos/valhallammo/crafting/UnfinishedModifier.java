package me.athlaeos.valhallammo.crafting;

import me.athlaeos.valhallammo.item.ItemBuilder;

public interface UnfinishedModifier {
    void finish(ItemBuilder item, Object with);
}
