package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.entity.Player;

public interface RelationalItemModifier {
    void processItem(Player p, ItemBuilder i1, ItemBuilder i2, boolean use, boolean validate, int count);

    record RelationalResult(ItemBuilder i1, ItemBuilder i2){}
}
