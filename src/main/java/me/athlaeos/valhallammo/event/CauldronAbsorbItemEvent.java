package me.athlaeos.valhallammo.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CauldronAbsorbItemEvent extends BlockEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player thrower;
    private ItemStack absorbedItem;

    public CauldronAbsorbItemEvent(@NotNull Block theBlock, ItemStack absorbedItem, Player thrower) {
        super(theBlock);
        this.absorbedItem = absorbedItem;
        this.thrower = thrower;
    }

    public Player getThrower() {
        return thrower;
    }

    public ItemStack getAbsorbedItem() {
        return absorbedItem;
    }

    public void setAbsorbedItem(ItemStack absorbedItem) {
        this.absorbedItem = absorbedItem;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}