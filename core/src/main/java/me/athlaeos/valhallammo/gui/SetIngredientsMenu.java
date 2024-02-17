package me.athlaeos.valhallammo.gui;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface SetIngredientsMenu {
    void setIngredients(Map<ItemStack, Integer> resultModifiers);

    Map<ItemStack, Integer> getIngredients();
}
