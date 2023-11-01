package me.athlaeos.valhallammo.block;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;

public class GenericBlockDestructionInfo extends BlockDestructionInfo{
    public GenericBlockDestructionInfo(Block block, Cancellable event) {
        super(block, event);
    }

    @Override
    public boolean isCancelled(Cancellable e) {
        return e.isCancelled();
    }
}
