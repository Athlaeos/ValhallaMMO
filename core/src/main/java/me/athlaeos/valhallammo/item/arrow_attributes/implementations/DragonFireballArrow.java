package me.athlaeos.valhallammo.item.arrow_attributes.implementations;

import me.athlaeos.valhallammo.particle.implementations.GenericParticle;
import me.athlaeos.valhallammo.item.item_attributes.ArrowBehavior;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.AnimationUtils;
import org.bukkit.Particle;
import org.bukkit.entity.DragonFireball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public class DragonFireballArrow extends ArrowBehavior {

    public DragonFireballArrow(String name) {
        super(name);
    }

    @Override
    public void onShoot(EntityShootBowEvent e, double... args) {
        if (args.length == 2){
            float radius = (float) args[0];
            boolean isIncendiary = ((int) args[1]) == 1;

            double multiplier = AccumulativeStatManager.getCachedStats("EXPLOSION_RADIUS_MULTIPLIER", e.getEntity(), 10000, true);
            radius *= (float) (1 + multiplier);

            DragonFireball dragonFireball = e.getEntity().launchProjectile(DragonFireball.class, e.getProjectile().getVelocity());
            dragonFireball.setShooter(e.getEntity());
            dragonFireball.setYield(radius);
            dragonFireball.setIsIncendiary(isIncendiary);
            AnimationUtils.trailProjectile(dragonFireball, new GenericParticle(Particle.FLAME), 50);
            e.setProjectile(dragonFireball);
        }
    }

    @Override
    public void onHit(ProjectileHitEvent e, double... args) {

    }

    @Override
    public void onLaunch(ProjectileLaunchEvent e, double... args) {

    }

    @Override
    public void onDamage(EntityDamageByEntityEvent e, double... args) {

    }

    @Override
    public void onPickup(PlayerPickupArrowEvent e, double... args) {

    }
}
