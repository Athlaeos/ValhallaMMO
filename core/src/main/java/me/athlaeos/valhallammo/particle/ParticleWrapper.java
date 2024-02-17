package me.athlaeos.valhallammo.particle;

import org.bukkit.Location;

public abstract class ParticleWrapper {
    public abstract void spawnParticle(Location l, int count, double xOff, double yOff, double zOff);

    public void spawnParticle(Location l, int count){
        spawnParticle(l, count, 0, 0, 0);
    }
}
