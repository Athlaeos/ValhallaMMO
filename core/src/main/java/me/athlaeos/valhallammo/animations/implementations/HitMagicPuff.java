package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import static me.athlaeos.valhallammo.utility.Utils.oldOrNew;

public class HitMagicPuff extends Animation {

    public HitMagicPuff(String id) {
        super(id);
    }

    @Override
    public void animate(LivingEntity entity, Location location, Vector direction, int tick) {
        entity.getWorld().spawnParticle(Particle.valueOf(oldOrNew("CRIT_MAGIC", "ENCHANTED_HIT")), location, 10, 0.1, 0.1, 0.1);
        entity.getWorld().playSound(location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, .5F, 1F);
    }
}
