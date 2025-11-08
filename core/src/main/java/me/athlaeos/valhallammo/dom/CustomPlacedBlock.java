package me.athlaeos.valhallammo.dom;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class CustomPlacedBlock {
    private final String customType;
    private final Block block;
    private final BlockState state;
    private final boolean properState;

    public CustomPlacedBlock(String customType, Block block, BlockState state){
        this.customType = customType;
        this.block = block;
        this.state = state;
        this.properState = true;
    }
    public CustomPlacedBlock(String customType, Block block){
        this.customType = customType;
        this.block = block;
        this.state = block.getState();
        this.properState = false;
    }

    public String getCustomType() {
        return customType;
    }

    public Block getBlock() {
        return block;
    }

    public BlockState getState() {
        return state;
    }

    public boolean hasProperState() {
        return properState;
    }
}
