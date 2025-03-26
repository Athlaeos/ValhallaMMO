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
            }
        }
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
