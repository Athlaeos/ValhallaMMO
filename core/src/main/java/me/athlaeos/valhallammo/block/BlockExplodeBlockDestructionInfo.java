package me.athlaeos.valhallammo.block;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockExplodeEvent;

public class BlockExplodeBlockDestructionInfo extends BlockDestructionInfo{
    public BlockExplodeBlockDestructionInfo(Block block, Cancellable event) {
        super(block, event);
    }

    @Override
    public boolean isCancelled(Cancellable e) {
        if (!(e instanceof BlockExplodeEvent event)) return e.isCancelled();
        return event.blockList().contains(this.getBlock());
    }
}
