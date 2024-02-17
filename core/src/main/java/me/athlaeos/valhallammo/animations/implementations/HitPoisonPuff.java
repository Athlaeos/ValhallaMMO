package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class HitPoisonPuff extends Animation {

    public HitPoisonPuff(String id) {
        super(id);
    }

    private static final double r = 45/255D;
    private static final double g = 100/255D;
    private static final double b = 40/255D;

    @Override
    public void animate(LivingEntity entity, Location location, Vector direction, int tick) {
        for (int i = 0; i < 10; i++) entity.getWorld().spawnParticle(Particle.SPELL, location, 0, r, g, b, 1); // color is a sickly green
        entity.getWorld().playSound(location, Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, .5F, .5F);
    }
}
