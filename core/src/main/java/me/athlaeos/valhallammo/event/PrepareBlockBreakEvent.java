package me.athlaeos.valhallammo.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

public class PrepareBlockBreakEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Block block;
    private final Player player;
    private final Collection<Block> additionalBlocks = new HashSet<>();
    private boolean cancelled = false;

    public PrepareBlockBreakEvent(@NotNull Block block, Player player) {
        super(true);
        this.block = block;
        this.player = player;
    }

    /**
     * Returns the list of additional blocks to be broken by this event. Added blocks will also receive the damage animation and receive damage
     * as long as the main block is still being damaged. To make sure the blocks in this list break ahead of the main block, they are damaged a little
     * initially.
     * @return the list of additional blocks to be damaged
     */
    public Collection<Block> getAdditionalBlocks() {
        return additionalBlocks;
    }

    public Player getPlayer() {
        return player;
    }

    public Block getBlock() {
        return block;
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
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
