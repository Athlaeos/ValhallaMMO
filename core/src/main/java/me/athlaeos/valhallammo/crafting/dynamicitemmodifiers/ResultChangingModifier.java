package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

import org.bukkit.inventory.ItemStack;

public interface ResultChangingModifier {
    ItemStack getNewResult(ModifierContext context);
}
