package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class HandSwitchListener implements Listener {
    private final Collection<UUID> playersWhoSwitchedItems = new HashSet<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHotbarSwitch(PlayerItemHeldEvent e){
        // if the player has switched their main hand record their UUID
        // this is done with a .5s delay to make sure this isn't spam-able which could potentially lag the server
        if (Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "hand_equipment_reset")){
            Timer.setCooldown(e.getPlayer().getUniqueId(), 500, "hand_equipment_reset");
            playersWhoSwitchedItems.add(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e){
        // if the player has their UUID recorded and interacts with any item, update their hand equipment.
        // if we refresh the player's hand equipment every interact packet or held item switch event it could lag the
        // server if such packets are spammed to the server
        if (playersWhoSwitchedItems.remove(e.getPlayer().getUniqueId())){
            EntityCache.resetHands(e.getPlayer());
        }
    }
}
