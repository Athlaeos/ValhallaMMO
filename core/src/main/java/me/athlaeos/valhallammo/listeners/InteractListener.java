package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.block.BlockInteractConversions;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Parryer;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class InteractListener implements Listener {
    private static final Collection<UUID> attackedWithOffhand = new HashSet<>();
    private static final boolean dualWieldingEnabled = ValhallaMMO.getPluginConfig().getBoolean("dual_wielding", true);
    private static final UUID attackSpeedUUID = UUID.fromString("9bc1100a-0748-4476-ace4-603bf73585b1");

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRightClickWeaponAction(PlayerInteractEvent e){
        if (e.useItemInHand() == Event.Result.DENY ||
                ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName())) return;
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            EntityProperties properties = EntityCache.getAndCacheProperties(e.getPlayer());
            boolean dualWielding = properties.getMainHand() != null && properties.getOffHand() != null &&
                    WeightClass.getWeightClass(properties.getMainHand().getMeta()) != WeightClass.WEIGHTLESS &&
                    WeightClass.getWeightClass(properties.getOffHand().getMeta()) != WeightClass.WEIGHTLESS; // only items with a weight class can dual wield attack
            if (!dualWielding) {
                if (e.getHand() == EquipmentSlot.OFF_HAND || e.getPlayer().getAttackCooldown() < 0.9) return;
                Parryer.attemptParry(e.getPlayer());
            } else if (dualWieldingEnabled && e.getHand() == EquipmentSlot.OFF_HAND) {
                double reach = AccumulativeStatManager.getCachedStats("ATTACK_REACH_BONUS", e.getPlayer(), 10000, true);
                double multiplier = 1 + AccumulativeStatManager.getCachedStats("ATTACK_REACH_MULTIPLIER", e.getPlayer(), 10000, true);
                Timer.setCooldown(e.getPlayer().getUniqueId(), 0, "parry_vulnerable");
                Timer.setCooldown(e.getPlayer().getUniqueId(), 0, "parry_effective"); // swinging your weapon should also cancel parry attempts
                reach = (EntityUtils.getPlayerReach(e.getPlayer()) + reach) * multiplier;
                if (reach <= 0 || multiplier <= 0) return;
                Location eyes = e.getPlayer().getEyeLocation();
                Entity vehicle = e.getPlayer().getVehicle();
                // if the player is riding a vehicle their eye location will no longer be accurate, so this estimates the new eye height
                if (vehicle instanceof LivingEntity v) vehicle.getLocation().add(0, v.getEyeHeight() + (0.625 * e.getPlayer().getEyeHeight()), 0);

                e.getPlayer().swingMainHand();
                e.getPlayer().swingOffHand();
                RayTraceResult rayTrace = e.getPlayer().getWorld().rayTrace(eyes, eyes.getDirection(), reach - 0.1, FluidCollisionMode.NEVER, true, 0.1, entity -> !(entity.equals(e.getPlayer())) || e.getPlayer().getPassengers().contains(entity) || entity.equals(vehicle) || entity.isDead());
                if (rayTrace != null){
                    Entity hit = rayTrace.getHitEntity();
                    if (hit instanceof LivingEntity l) {
                        attackedWithOffhand.add(e.getPlayer().getUniqueId());

                        swapHands(e.getPlayer());
                        AttributeWrapper mainHandAttackSpeed = ItemAttributesRegistry.getAnyAttribute(properties.getMainHand().getMeta(), "GENERIC_ATTACK_SPEED");
                        AttributeWrapper offHandAttackSpeed = ItemAttributesRegistry.getAnyAttribute(properties.getOffHand().getMeta(), "GENERIC_ATTACK_SPEED");
                        double mainHandSpeed = 0;
                        if (mainHandAttackSpeed != null) mainHandSpeed = mainHandAttackSpeed.getValue();
                        double offHandSpeed = 0;
                        if (offHandAttackSpeed != null) offHandSpeed = offHandAttackSpeed.getValue();
                        EntityUtils.addUniqueAttribute(e.getPlayer(), attackSpeedUUID, "valhalla_dual_wield_attack_speed_offset", Attribute.GENERIC_ATTACK_SPEED, mainHandSpeed - offHandSpeed, AttributeModifier.Operation.ADD_NUMBER);
                        e.getPlayer().attack(l);
                        ValhallaMMO.getNms().resetAttackCooldown(e.getPlayer());
                        EntityUtils.removeUniqueAttribute(e.getPlayer(), "valhalla_dual_wield_attack_speed_offset", Attribute.GENERIC_ATTACK_SPEED);
                        swapHands(e.getPlayer());

                        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () ->
                            attackedWithOffhand.remove(e.getPlayer().getUniqueId()), 2L);
                    }
                }
            }
        }
    }

    private void swapHands(Player p){
        ItemStack mainHand = p.getInventory().getItemInMainHand();
        ItemStack offHand = p.getInventory().getItemInOffHand();
        if (!ItemUtils.isEmpty(mainHand)) mainHand = mainHand.clone();
        if (!ItemUtils.isEmpty(offHand)) offHand = offHand.clone();

        p.getInventory().setItemInMainHand(offHand);
        p.getInventory().setItemInOffHand(mainHand);
        p.updateInventory();
    }

    public static boolean attackedWithOffhand(Entity l){
        return attackedWithOffhand.contains(l.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockConvert(PlayerInteractEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.useItemInHand() == Event.Result.DENY ||
                e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null ||
                !Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "cooldown_block_conversions") ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_ABILITIES_BLOCKCONVERSIONS)) return;
        if (BlockInteractConversions.trigger(e.getPlayer(), e.getClickedBlock())) e.setCancelled(true);
        Timer.setCooldown(e.getPlayer().getUniqueId(), 250, "cooldown_block_conversions");
    }
}
