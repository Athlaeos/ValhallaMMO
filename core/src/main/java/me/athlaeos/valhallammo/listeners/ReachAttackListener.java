package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;

import java.util.*;

public class ReachAttackListener implements Listener {
    private static final Collection<UUID> cancelNextReachAttack = new HashSet<>();
    private static final Collection<UUID> reachHitTracker = new HashSet<>();
    private static final Map<UUID, ArmSwingReason> lastArmSwingReasons = new HashMap<>();
    public static Map<UUID, ArmSwingReason> getLastArmSwingReasons() {
        return lastArmSwingReasons;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwing(PlayerAnimationEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) ||
                e.getAnimationType() != PlayerAnimationType.ARM_SWING ||
                lastArmSwingReasons.getOrDefault(e.getPlayer().getUniqueId(), ArmSwingReason.ATTACK) != ArmSwingReason.ATTACK) return;
        EntityProperties properties = EntityCache.getAndCacheProperties(e.getPlayer());
        if (properties.getMainHand() != null && WeightClass.getWeightClass(properties.getMainHand().getMeta()) == WeightClass.WEIGHTLESS)
            return;  // only items with a weight class, or empty fist, can reach attack

        Player p = e.getPlayer();
        if (cancelNextReachAttack.remove(p.getUniqueId())) return;

        double reach = AccumulativeStatManager.getCachedStats("ATTACK_REACH_BONUS", e.getPlayer(), 10000, true);
        double multiplier = 1 + AccumulativeStatManager.getCachedStats("ATTACK_REACH_MULTIPLIER", e.getPlayer(), 10000, true);
        Timer.setCooldown(e.getPlayer().getUniqueId(), 0, "parry_vulnerable");
        Timer.setCooldown(e.getPlayer().getUniqueId(), 0, "parry_effective"); // swinging your weapon should also cancel parry attempts
        if (reach <= 0 || multiplier <= 0) return;
        reach = (EntityUtils.getPlayerReach(e.getPlayer()) + reach) * multiplier;
        Location eyes = p.getEyeLocation();
        Entity vehicle = p.getVehicle();
        // if the player is riding a vehicle their eye location will no longer be accurate, so this estimates the new eye height
        if (vehicle instanceof LivingEntity v) vehicle.getLocation().add(0, v.getEyeHeight() + (0.625 * p.getEyeHeight()), 0);

        RayTraceResult rayTrace = p.getWorld().rayTrace(eyes, eyes.getDirection(), reach - 0.1, FluidCollisionMode.NEVER, true, 0.1, entity -> !(entity.equals(p)) || e.getPlayer().getPassengers().contains(entity) || entity.equals(vehicle) || entity.isDead());
        if (rayTrace != null){
            Entity hit = rayTrace.getHitEntity();
            if (hit == null) return;
            p.attack(hit);
            reachHitTracker.add(p.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName())) return;

        if (e.getDamager() instanceof Player p) {
            // we don't want reach attacks to be executed when the player successfully attacked something in range (attack events are called before arm swing events)
            // so we cancel the reach attack executed when successfully attacked. however, reach attacks will also cause a damage event and that would cancel
            // the next reach attack. so we track successful reach attacks also. if the player used a reach attack to attack, the next reach attack isn't cancelled
            if (!ReachAttackListener.usedReachAttack(p)) ReachAttackListener.cancelNextReachAttack(p);

            double reach = AccumulativeStatManager.getCachedStats("ATTACK_REACH_BONUS", p, 10000, true);
            double multiplier = 1 + AccumulativeStatManager.getCachedStats("ATTACK_REACH_MULTIPLIER", p, 10000, true);
            double defaultReach = 3;
            double actualReach = (defaultReach + reach) * multiplier;
            if (actualReach < defaultReach){
                if (actualReach <= 0.5) {
                    e.setCancelled(true);
                    setLastArmSwingReason(p, ArmSwingReason.FAILED_ATTACK);
                    return;
                }
                RayTraceResult rayTrace = e.getDamager().getWorld().rayTrace(((LivingEntity) e.getDamager()).getEyeLocation(),
                        ((LivingEntity) e.getDamager()).getEyeLocation().getDirection(), actualReach - 0.5, FluidCollisionMode.NEVER, true, 0.5, (entity) -> !(entity.equals(e.getDamager()) || entity.equals(e.getDamager().getVehicle())));
                if (rayTrace == null || rayTrace.getHitEntity() == null) {
                    e.setCancelled(true);
                    setLastArmSwingReason(p, ArmSwingReason.FAILED_ATTACK);
                    return;
                }
            }

            setLastArmSwingReason(p, ArmSwingReason.ATTACK);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent e){
        if (e.getHand() == EquipmentSlot.HAND)
            switch (e.getAction()) {
                case RIGHT_CLICK_BLOCK -> setLastArmSwingReason(e.getPlayer(), ArmSwingReason.BLOCK_INTERACT);
                case LEFT_CLICK_BLOCK -> setLastArmSwingReason(e.getPlayer(), ArmSwingReason.BLOCK_DAMAGE);
                case RIGHT_CLICK_AIR -> setLastArmSwingReason(e.getPlayer(), ArmSwingReason.AIR_INTERACT);
                case LEFT_CLICK_AIR -> setLastArmSwingReason(e.getPlayer(), ArmSwingReason.ATTACK);
            }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractAtEntityEvent e){
        setLastArmSwingReason(e.getPlayer(), ArmSwingReason.ENTITY_INTERACT);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDrop(PlayerDropItemEvent e){
        setLastArmSwingReason(e.getPlayer(), ArmSwingReason.DROP_ITEM);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName())) return;
        setLastArmSwingReason(e.getPlayer(), ArmSwingReason.BLOCK_DAMAGE);
    }

    public static boolean usedReachAttack(Player p){
        return reachHitTracker.remove(p.getUniqueId());
    }

    public static void cancelNextReachAttack(Player p) {
        cancelNextReachAttack.add(p.getUniqueId());
    }

    public static void setLastArmSwingReason(Player p, ArmSwingReason reason){
        // this delay is in place because PlayerInteractAtEntityEvent is fired before PlayerInteractEvent, and if a player interacts with
        // an entity at max range the PlayerInteractEvent will think they're left clicking air which is an ATTACK action. To prevent this, although scuffed, a short
        // cooldown is put in place of 40ms (shorter than 1 tick) to prevent the PlayerInteractEvent from overriding the previously
        if (Timer.isCooldownPassed(p.getUniqueId(), "arm_swing_reason_delay")) {
            lastArmSwingReasons.put(p.getUniqueId(), reason);
            Timer.setCooldown(p.getUniqueId(), 40, "arm_swing_reason_delay");
        }
    }

    public enum ArmSwingReason{
        ATTACK,
        FAILED_ATTACK,
        BLOCK_DAMAGE,
        AIR_INTERACT,
        BLOCK_INTERACT,
        ENTITY_INTERACT,
        DROP_ITEM
    }
}
