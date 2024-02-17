package me.athlaeos.valhallammo.item.item_attributes;

import org.bukkit.Location;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public abstract class ArrowBehavior {
    private final String name;

    public ArrowBehavior(String name){
        this.name = name;
    }

    public abstract void onShoot(EntityShootBowEvent e, double... args);
    public abstract void onHit(ProjectileHitEvent e, double... args);
    public abstract void onLaunch(ProjectileLaunchEvent e, double... args);
    public abstract void onDamage(EntityDamageByEntityEvent e, double... args);
    public abstract void onPickup(PlayerPickupArrowEvent e, double... args);

    public String getName() {
        return name;
    }

    protected Location getHitLocation(ProjectileHitEvent e){
        if (e.getHitBlock() != null)
            return e.getHitBlockFace() == null ?
                    e.getHitBlock().getLocation().add(0.5, 0.5, 0.5) :
                    e.getHitBlock().getRelative(e.getHitBlockFace()).getLocation();
        else if (e.getHitEntity() != null) return e.getHitEntity().getLocation();
        return e.getEntity().getLocation();
    }
}
