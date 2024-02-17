package me.athlaeos.valhallammo.item.arrow_attributes.implementations;

import me.athlaeos.valhallammo.particle.implementations.GenericParticle;
import me.athlaeos.valhallammo.item.item_attributes.ArrowBehavior;
import me.athlaeos.valhallammo.utility.AnimationUtils;
import org.bukkit.Particle;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public class LightningArrow extends ArrowBehavior {

    public LightningArrow(String name) {
        super(name);
    }

    @Override
    public void onShoot(EntityShootBowEvent e, double... args) {
        if (e.getProjectile() instanceof Projectile p) AnimationUtils.trailProjectile(p, new GenericParticle(Particle.SOUL_FIRE_FLAME), 50);
    }

    @Override
    public void onHit(ProjectileHitEvent e, double... args) {
        if (args.length == 1){
            boolean requiresRain = ((int) args[0]) == 1;
            if (requiresRain && !e.getEntity().getWorld().isThundering() && !e.getEntity().getWorld().hasStorm()) return;

            e.getEntity().getWorld().strikeLightning(getHitLocation(e));
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
