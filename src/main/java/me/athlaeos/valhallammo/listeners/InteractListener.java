package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.block.BlockInteractConversions;
import me.athlaeos.valhallammo.utility.Parryer;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class InteractListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onParry(PlayerInteractEvent e){
        if (e.useItemInHand() == Event.Result.DENY || e.getPlayer().getAttackCooldown() < 0.9 ||
                ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.getHand() == EquipmentSlot.OFF_HAND) return;
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) Parryer.attemptParry(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockConvert(PlayerInteractEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.useItemInHand() == Event.Result.DENY ||
                e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null || !Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "cooldown_block_conversions")) return;
        if (BlockInteractConversions.trigger(e.getPlayer(), e.getClickedBlock())) e.setCancelled(true);
        Timer.setCooldown(e.getPlayer().getUniqueId(), 250, "cooldown_block_conversions");
    }
}
