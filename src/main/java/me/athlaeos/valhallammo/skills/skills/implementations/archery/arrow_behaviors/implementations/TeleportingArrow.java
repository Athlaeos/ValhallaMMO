package me.athlaeos.valhallammo.skills.skills.implementations.archery.arrow_behaviors.implementations;

import me.athlaeos.valhallammo.particle.implementations.GenericParticle;
import me.athlaeos.valhallammo.skills.skills.implementations.archery.arrow_behaviors.ArrowBehavior;
import me.athlaeos.valhallammo.utility.AnimationUtils;
import org.bukkit.Particle;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public class TeleportingArrow extends ArrowBehavior {

    public TeleportingArrow(String name) {
        super(name);
    }

    @Override
    public void onShoot(EntityShootBowEvent e, double... args) {
        if (e.getProjectile() instanceof Projectile p) AnimationUtils.trailProjectile(p, new GenericParticle(Particle.PORTAL), 50);
    }

    @Override
    public void onHit(ProjectileHitEvent e, double... args) {
        if (e.getEntity().getShooter() instanceof LivingEntity l){
            EnderPearl pearl = l.launchProjectile(EnderPearl.class, e.getEntity().getVelocity().normalize());
            pearl.setShooter(l);
            pearl.teleport(e.getEntity().getLocation());
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
