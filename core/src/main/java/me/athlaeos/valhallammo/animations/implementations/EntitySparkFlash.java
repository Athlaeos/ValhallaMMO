package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import static me.athlaeos.valhallammo.utility.Utils.oldOrNew;

public class EntitySparkFlash extends Animation {
    public EntitySparkFlash(String id) {
        super(id);
    }

    @Override
    public void animate(LivingEntity crafter, Location location, Vector direction, int tick) {
        if (location.getWorld() == null) return;
        location.getWorld().spawnParticle(Particle.valueOf(oldOrNew("FIREWORKS_SPARK", "FIREWORK")), location, 10);
        location.getWorld().spawnParticle(Particle.FLASH, location, 0);
    }
}
