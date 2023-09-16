package me.athlaeos.valhallammo.skills.skills.implementations.archery.arrow_behaviors.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.skills.skills.implementations.archery.arrow_behaviors.ArrowBehavior;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class NoGravityArrow extends ArrowBehavior {

    public NoGravityArrow(String name) {
        super(name);
    }

    @Override
    public void onShoot(EntityShootBowEvent e, double... args) {
        e.getProjectile().setGravity(false);
        new BukkitRunnable(){
            @Override
            public void run() {
                e.getProjectile().remove();
            }
        }.runTaskLater(ValhallaMMO.getInstance(), 200);
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
