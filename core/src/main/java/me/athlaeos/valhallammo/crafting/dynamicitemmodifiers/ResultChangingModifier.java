package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ResultChangingModifier {
    ItemStack getNewResult(Player crafter, ItemBuilder item);
}
