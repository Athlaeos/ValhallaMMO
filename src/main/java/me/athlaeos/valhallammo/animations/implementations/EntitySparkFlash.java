package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class EntitySparkFlash extends Animation {
    public EntitySparkFlash(String id) {
        super(id);
    }

    @Override
    public void animate(LivingEntity crafter, Location location, Vector direction, int tick) {
        if (location.getWorld() == null) return;
        location.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location, 10);
        location.getWorld().spawnParticle(Particle.FLASH, location, 0);
    }
}
