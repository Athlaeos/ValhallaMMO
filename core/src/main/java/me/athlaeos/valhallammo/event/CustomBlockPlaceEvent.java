package me.athlaeos.valhallammo.event;

import me.athlaeos.valhallammo.dom.CustomPlacedBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class CustomBlockPlaceEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final CustomPlacedBlock block;

    public CustomBlockPlaceEvent(Player player, CustomPlacedBlock block) {
        super(player);
        this.block = block;
    }

    @Override public @NotNull HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }

    public CustomPlacedBlock getBlock() {
        return block;
    }
}