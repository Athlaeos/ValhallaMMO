package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ResultChangingModifier {
    ItemStack getNewResult(Player crafter);
}
