package me.athlaeos.valhallammo.crafting.craftanimations;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public abstract class CraftAnimation {
    private final String id;
    public CraftAnimation(String id){
        this.id = id;
    }

    public String id() {
        return id;
    }

    public abstract void animate(Player crafter, Block block, int tick);
}
