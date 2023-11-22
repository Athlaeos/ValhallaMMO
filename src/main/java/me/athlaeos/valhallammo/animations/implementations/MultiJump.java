package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class MultiJump extends Animation {
    public MultiJump(String id) {
        super(id);
    }

    @Override
    public void animate(LivingEntity entity, Location location, Vector direction, int tick) {
        for (int i = 0; i < 20; i++)
            entity.getWorld().spawnParticle(Particle.CLOUD, entity.getBoundingBox().getCenter().toLocation(entity.getWorld()), 0, (Utils.getRandom().nextDouble() - 0.5) * 0.4, -0.2, (Utils.getRandom().nextDouble() - 0.5) * 0.4);
        entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_GRASS_STEP, 1F, 1F);
        entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_SPORE_BLOSSOM_STEP, 1F, 1F);
    }
}
