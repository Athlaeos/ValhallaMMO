package me.athlaeos.valhallammo.skills.skills.implementations.archery.arrow_behaviors.implementations;

import me.athlaeos.valhallammo.particle.implementations.GenericParticle;
import me.athlaeos.valhallammo.skills.skills.implementations.archery.arrow_behaviors.ArrowBehavior;
import me.athlaeos.valhallammo.utility.AnimationUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public class VeryExplosiveArrow extends ArrowBehavior {

    public VeryExplosiveArrow(String name) {
        super(name);
    }

    @Override
    public void onShoot(EntityShootBowEvent e, double... args) {
        if (e.getProjectile() instanceof Projectile p) AnimationUtils.trailProjectile(p, new GenericParticle(Particle.FLAME), 50);
    }

    @Override
    public void onHit(ProjectileHitEvent e, double... args) {
        if (args.length == 4){
            float radius = (float) args[0];
            boolean destructive = ((int) args[1]) == 1;
            boolean fire = ((int) args[2]) == 1;
            int count = (int) args[3];
            Location hit = getHitLocation(e);
            for (int i = 0; i < count; i++){
                if (e.getEntity().getShooter() instanceof Entity s)
                    e.getEntity().getWorld().createExplosion(hit, radius, fire, destructive, s);
                else
                    e.getEntity().getWorld().createExplosion(hit, radius, fire, destructive, e.getEntity());
            }
            e.getEntity().remove();
        }
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
