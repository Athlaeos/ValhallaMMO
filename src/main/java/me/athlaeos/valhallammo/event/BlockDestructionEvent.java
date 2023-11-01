package me.athlaeos.valhallammo.event;

import me.athlaeos.valhallammo.block.BlockDestructionInfo;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

public class BlockDestructionEvent extends BlockEvent {
    private static final HandlerList handlers = new HandlerList();
    private final BlockDestructionInfo info;
    private final BlockDestructionReason reason;
    public BlockDestructionEvent(@NotNull Block theBlock, BlockDestructionInfo info, BlockDestructionReason reason) {
        super(theBlock);
        this.info = info;
        this.reason = reason;
    }

    public BlockDestructionInfo getInfo() {
        return info;
    }

    public BlockDestructionReason getReason() {
        return reason;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public enum BlockDestructionReason{
        PLAYER,
        BLOCK_EXPLOSION,
        ENTITY_EXPLOSION,
        BURN,
        FADE,
        REPLACED,
        DECAY
    }
}
