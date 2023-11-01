package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.utility.Parryer;
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
}
