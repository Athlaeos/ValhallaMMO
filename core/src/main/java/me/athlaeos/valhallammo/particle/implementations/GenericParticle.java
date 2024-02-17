package me.athlaeos.valhallammo.particle.implementations;

import me.athlaeos.valhallammo.particle.ParticleWrapper;
import org.bukkit.Location;
import org.bukkit.Particle;

public class GenericParticle extends ParticleWrapper {
    private final Particle particle;
    public GenericParticle(Particle particle){
        this.particle = particle;
    }

    @Override
    public void spawnParticle(Location l, int count, double xOff, double yOff, double zOff) {
        if (l.getWorld() == null) return;
        l.getWorld().spawnParticle(particle, l, count, xOff, yOff, zOff);
    }
}
