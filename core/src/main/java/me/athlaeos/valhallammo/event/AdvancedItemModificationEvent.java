package me.athlaeos.valhallammo.event;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AdvancedItemModificationEvent extends ItemModificationEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final ItemBuilder item2;

    public AdvancedItemModificationEvent(Player player, ItemBuilder item, ItemBuilder item2, List<DynamicItemModifier> modifiers, boolean sort, boolean use, boolean validate, int count){
        super(player, item, modifiers, sort, use, validate, count);
        this.item2 = item2;
    }

    public ItemBuilder getItem2() { return item2; }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
