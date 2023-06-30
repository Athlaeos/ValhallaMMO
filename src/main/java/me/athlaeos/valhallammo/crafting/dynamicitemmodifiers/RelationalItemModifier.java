package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface RelationalItemModifier {
    RelationalResult processItem(Player p, ItemStack i1, ItemStack i2, boolean use, boolean validate, int count);

    record RelationalResult(ItemStack i1, ItemStack i2){}
}
