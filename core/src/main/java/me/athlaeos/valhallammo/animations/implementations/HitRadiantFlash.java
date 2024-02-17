package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class HitRadiantFlash extends Animation {

    public HitRadiantFlash(String id) {
        super(id);
    }

    @Override
    public void animate(LivingEntity entity, Location location, Vector direction, int tick) {
        entity.getWorld().spawnParticle(Particle.FLASH, location, 0);
        entity.getWorld().playSound(location, Sound.ITEM_TRIDENT_RETURN, .5F, 1F);
    }
}
