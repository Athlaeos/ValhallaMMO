package me.athlaeos.valhallammo.block;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityExplodeBlockDestructionInfo extends BlockDestructionInfo{
    public EntityExplodeBlockDestructionInfo(Block block, Cancellable event) {
        super(block, event);
    }

    @Override
    public boolean isCancelled(Cancellable e) {
        if (!(e instanceof EntityExplodeEvent event)) return e.isCancelled();
        return event.blockList().contains(this.getBlock());
    }
}
