package me.athlaeos.valhallammo.particle.implementations;

import me.athlaeos.valhallammo.particle.ParticleWrapper;
import org.bukkit.Location;

import static me.athlaeos.valhallammo.utility.Utils.oldOrNew;

public class RedstoneParticle extends ParticleWrapper {
    private final org.bukkit.Particle.DustOptions options;

    public RedstoneParticle(org.bukkit.Particle.DustOptions options){
        this.options = options;
    }

    @Override
    public void spawnParticle(Location l, int count, double xOff, double yOff, double zOff) {
        if (l.getWorld() == null) return;
        l.getWorld().spawnParticle(org.bukkit.Particle.valueOf(oldOrNew("REDSTONE", "DUST")), l, count, xOff, yOff, zOff, options);
    }
}
