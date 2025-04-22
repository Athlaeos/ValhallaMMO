package me.athlaeos.valhallammo.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerBlocksDropItemsEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Map<Block, List<ItemStack>> blocksAndItems;

    public PlayerBlocksDropItemsEvent(Player player, Map<Block, List<ItemStack>> blocksAndItems) {
        super(player);
        this.blocksAndItems = blocksAndItems;
    }

    @Override public @NotNull HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }

    public Map<Block, List<ItemStack>> getBlocksAndItems() {
        return blocksAndItems;
    }
}