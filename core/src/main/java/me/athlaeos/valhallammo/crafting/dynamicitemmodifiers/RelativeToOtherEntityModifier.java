package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface RelativeToOtherEntityModifier {
    void process(Player crafter, Entity entity, ItemBuilder i, boolean use, boolean validate, int timesExecuted);
}
