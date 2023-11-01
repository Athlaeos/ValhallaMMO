package me.athlaeos.valhallammo.animations;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public abstract class Animation {
    private final String id;
    public Animation(String id){
        this.id = id;
    }

    public String id() {
        return id;
    }

    public abstract void animate(LivingEntity entity, Location location, Vector direction, int tick);
}
