package me.athlaeos.valhallammo.block;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;

public abstract class BlockDestructionInfo {
    private final Block block;
    private final Cancellable event;

    public BlockDestructionInfo(Block block, Cancellable event){
        this.block = block;
        this.event = event;
    }

    public Block getBlock() {
        return block;
    }

    public Cancellable getEvent() {
        return event;
    }

    public abstract boolean isCancelled(Cancellable e);
}
