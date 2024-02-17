package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class HitExplosionBurst extends Animation {

    public HitExplosionBurst(String id) {
        super(id);
    }

    @Override
    public void animate(LivingEntity entity, Location location, Vector direction, int tick) {
        entity.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location, 0);
        entity.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, .5F, 1F);
    }
}
