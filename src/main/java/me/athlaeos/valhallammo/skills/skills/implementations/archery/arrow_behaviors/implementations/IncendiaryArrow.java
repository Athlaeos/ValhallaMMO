package me.athlaeos.valhallammo.skills.skills.implementations.archery.arrow_behaviors.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.particle.implementations.GenericParticle;
import me.athlaeos.valhallammo.skills.skills.implementations.archery.arrow_behaviors.ArrowBehavior;
import me.athlaeos.valhallammo.utility.AnimationUtils;
import me.athlaeos.valhallammo.utility.BlockUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public class IncendiaryArrow extends ArrowBehavior {

    public IncendiaryArrow(String name) {
        super(name);
    }

    @Override
    public void onShoot(EntityShootBowEvent e, double... args) {
        if (e.getProjectile() instanceof Projectile p) AnimationUtils.trailProjectile(p, new GenericParticle(Particle.FLAME), 50);
    }

    @Override
    public void onHit(ProjectileHitEvent e, double... args) {
        if (args.length >= 2){
            int fireTicks = (int) args[0];
            int radius = (int) args[1];
            double density = args.length > 2 ? args[2] : 1;
            Location hit = getHitLocation(e);
            if (hit.getWorld() == null) return;

            for (Block b : BlockUtils.getBlocksTouchingAnything(hit.getBlock(), radius, radius, radius)){
                if (!b.getType().isAir()) continue;
                if (Utils.getRandom().nextDouble() < density){
                    BlockIgniteEvent igniteEvent = new BlockIgniteEvent(b, BlockIgniteEvent.IgniteCause.ARROW, e.getEntity());
                    ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(igniteEvent);
                    if (!igniteEvent.isCancelled()) b.setType(Material.FIRE);
                }
            }
            for (Entity entity : hit.getWorld().getNearbyEntities(hit, radius, radius, radius)){
                if (entity instanceof LivingEntity && entity.getFireTicks() < fireTicks){
                    entity.setFireTicks(fireTicks);
                }
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
