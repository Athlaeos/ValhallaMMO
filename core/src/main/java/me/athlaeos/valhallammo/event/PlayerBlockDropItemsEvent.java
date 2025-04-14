package me.athlaeos.valhallammo.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerBlockDropItemsEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Block block;
    private final List<ItemStack> items;

    public PlayerBlockDropItemsEvent(Player player, Block block, List<ItemStack> items) {
        super(player);
        this.block = block;
        this.items = items;
    }

    @Override public @NotNull HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }

    public Block getBlock() {
        return block;
    }

    public List<ItemStack> getItems() {
        return items;
    }
}