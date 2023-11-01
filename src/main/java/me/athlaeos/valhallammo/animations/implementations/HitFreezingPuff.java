package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class HitFreezingPuff extends Animation {

    public HitFreezingPuff(String id) {
        super(id);
    }

    @Override
    public void animate(LivingEntity entity, Location location, Vector direction, int tick) {
        entity.getWorld().spawnParticle(Particle.END_ROD, location, 10, 0.1, 0.1, 0.1);
    }
}
