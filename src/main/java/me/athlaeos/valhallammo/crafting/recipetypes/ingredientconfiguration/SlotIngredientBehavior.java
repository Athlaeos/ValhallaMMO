package me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration;

import org.bukkit.event.inventory.CraftItemEvent;

public interface SlotIngredientBehavior {
    void onCraft(CraftItemEvent e, int gridIndex);
}
