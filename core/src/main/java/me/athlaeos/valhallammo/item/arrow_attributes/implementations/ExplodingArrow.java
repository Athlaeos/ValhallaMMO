package me.athlaeos.valhallammo.item.arrow_attributes.implementations;

import me.athlaeos.valhallammo.particle.implementations.GenericParticle;
import me.athlaeos.valhallammo.item.item_attributes.ArrowBehavior;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
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

public class ExplodingArrow extends ArrowBehavior {

    public ExplodingArrow(String name) {
        super(name);
    }

    @Override
    public void onShoot(EntityShootBowEvent e, double... args) {
        if (e.getProjectile() instanceof Projectile p) AnimationUtils.trailProjectile(p, new GenericParticle(Particle.FLAME), 50);
    }

    @Override
    public void onHit(ProjectileHitEvent e, double... args) {
        if (args.length == 3){
            float radius = (float) args[0];
            boolean destructive = ((int) args[1]) == 1;
            boolean fire = ((int) args[2]) == 1;
            Location hit = getHitLocation(e);
            if (e.getEntity().getShooter() instanceof Entity s){
                double multiplier = AccumulativeStatManager.getCachedStats("EXPLOSION_RADIUS_MULTIPLIER", s, 10000, true);
                radius *= (float) (1 + multiplier);
                e.getEntity().getWorld().createExplosion(hit, radius, fire, destructive, s);
            } else
                e.getEntity().getWorld().createExplosion(hit, radius, fire, destructive, e.getEntity());
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
