package me.athlaeos.valhallammo.hooks;

import net.coreprotect.listener.block.BlockExplodeListener;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.List;

public class CoreProtectHook extends PluginHook{

    public CoreProtectHook() {
        super("CoreProtect");
    }

    public static void markBlocksExploded(World world, List<Block> blocks){
        BlockExplodeListener.processBlockExplode("#tnt", world, blocks);
    }

    @Override
    public void whenPresent() {}
}
