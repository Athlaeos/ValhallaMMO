package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.utility.MathUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class HitFireFlame extends Animation {

    public HitFireFlame(String id) {
        super(id);
    }

    @Override
    public void animate(LivingEntity entity, Location location, Vector direction, int tick) {
        List<Location> circle = new ArrayList<>(MathUtils.getRandomPointsInCircle(location, 0.5, 30, false));

        MathUtils.transformExistingPoints(location, 0, direction.toLocation(entity.getWorld()).getPitch() + 90, 0, 1, circle);
        MathUtils.transformExistingPoints(location, direction.toLocation(entity.getWorld()).getYaw() + 180, 0, 0, 1, circle);
        for (Location l : circle){
            entity.getWorld().spawnParticle(Particle.FLAME, l, 0,
                    (l.getX() - location.getX()) * 0.25,
                    (l.getY() - location.getY()) * 0.25,
                    (l.getZ() - location.getZ()) * 0.25
            );
        }
        entity.getWorld().playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, .5F, 1F);
    }
}
