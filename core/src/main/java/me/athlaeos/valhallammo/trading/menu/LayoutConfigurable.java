package me.athlaeos.valhallammo.trading.menu;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface LayoutConfigurable {
    void setConfiguration(int rowCount, int primaryButtonIndex, ItemStack primaryButtonIcon, List<Integer> secondaryIndexes);
}
