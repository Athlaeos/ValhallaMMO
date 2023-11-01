package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class HitNecroticPuff extends Animation {

    public HitNecroticPuff(String id) {
        super(id);
    }

    @Override
    public void animate(LivingEntity entity, Location location, Vector direction, int tick) {
        entity.getWorld().spawnParticle(Particle.SOUL, location, 10, 0.1, 0.1, 0.1);
        entity.getWorld().playSound(location, Sound.ENTITY_WITHER_HURT, .5F, 0.5F);
    }
}
