package me.athlaeos.valhallammo.event;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemModificationEvent extends PlayerEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final ItemBuilder item;
    private final List<DynamicItemModifier> modifiers;
    private boolean sort;
    private boolean use;
    private boolean validate;
    private int count;

    public ItemModificationEvent(Player player, ItemBuilder item, List<DynamicItemModifier> modifiers, boolean sort, boolean use, boolean validate, int count){
        super(player);
        this.item = item;
        this.modifiers = modifiers;
        this.sort = sort;
        this.use = use;
        this.validate = validate;
        this.count = count;
    }

    public ItemBuilder getItem() { return item; }
    public List<DynamicItemModifier> getModifiers() { return modifiers; }
    public boolean use() { return use; }
    public boolean validate() { return validate; }
    public int getCount() { return count; }
    public boolean sort() { return sort; }

    public void setCount(int count) { this.count = count; }
    public void setSort(boolean sort) { this.sort = sort; }
    public void setUse(boolean use) { this.use = use; }
    public void setValidate(boolean validate) { this.validate = validate; }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
