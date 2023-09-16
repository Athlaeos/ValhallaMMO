package me.athlaeos.valhallammo.skills.skills.implementations.archery.arrow_behaviors.implementations;

import me.athlaeos.valhallammo.particle.implementations.GenericParticle;
import me.athlaeos.valhallammo.skills.skills.implementations.archery.arrow_behaviors.ArrowBehavior;
import me.athlaeos.valhallammo.utility.AnimationUtils;
import org.bukkit.Particle;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public class SmallFireballArrow extends ArrowBehavior {

    public SmallFireballArrow(String name) {
        super(name);
    }

    @Override
    public void onShoot(EntityShootBowEvent e, double... args) {
        if (args.length == 2){
            float radius = (float) args[0];
            boolean isIncendiary = ((int) args[1]) == 1;
            SmallFireball smallFireball = e.getEntity().launchProjectile(SmallFireball.class, e.getProjectile().getVelocity());
            smallFireball.setShooter(e.getEntity());
            smallFireball.setYield(radius);
            smallFireball.setIsIncendiary(isIncendiary);
            AnimationUtils.trailProjectile(smallFireball, new GenericParticle(Particle.FLAME), 50);
            e.setProjectile(smallFireball);
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
