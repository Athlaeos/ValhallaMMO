package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class HandSwitchListener implements Listener {
    private static final Map<UUID, DelayedHandUpdate> taskLimiters = new HashMap<>();
    private static final Collection<UUID> playersWhoSwitchedItems = new HashSet<>();

    private void updateHands(Player who){
        DelayedHandUpdate update = taskLimiters.get(who.getUniqueId());
        if (update != null) update.refresh();
        else {
            update = new DelayedHandUpdate(who);
            update.runTaskTimer(ValhallaMMO.getInstance(), 1L, 1L);
        }
        taskLimiters.put(who.getUniqueId(), update);
    }

    private static void reset(LivingEntity l){
        EntityCache.resetHands(l);
        AccumulativeStatManager.updateStats(l);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHotbarSwitch(PlayerItemHeldEvent e){
        // if the player has switched their main hand record their UUID
        // this is done with a .5s delay to make sure this isn't spam-able which could potentially lag the server
        if (Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "hand_equipment_reset")){
            Timer.setCooldown(e.getPlayer().getUniqueId(), 500, "hand_equipment_reset");
            playersWhoSwitchedItems.add(e.getPlayer().getUniqueId());
            updateHands(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemBreak(PlayerItemBreakEvent e){
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            reset(e.getPlayer());
            playersWhoSwitchedItems.remove(e.getPlayer().getUniqueId());
        }, 1L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e){
        // if the player has their UUID recorded and interacts with any item, update their hand equipment.
        // if we refresh the player's hand equipment every interact packet or held item switch event it could lag the
        // server if such packets are spammed to the server
        if (playersWhoSwitchedItems.remove(e.getPlayer().getUniqueId())){
            reset(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAttack(EntityDamageByEntityEvent e){
        // if the player has their UUID recorded and interacts with any item, update their hand equipment.
        // if we refresh the player's hand equipment every interact packet or held item switch event it could lag the
        // server if such packets are spammed to the server
        if (e.getDamager() instanceof LivingEntity l && playersWhoSwitchedItems.remove(e.getDamager().getUniqueId())){
            reset(l);
        }
    }

    private static class DelayedHandUpdate extends BukkitRunnable {
        private static final int delay = 10; // after 0.5 seconds update hands
        private int timer = delay;
        private final Player who;

        public DelayedHandUpdate(Player who){
            this.who = who;
        }

        @Override
        public void run() {
            if (timer <= 0){
                reset(who);
                taskLimiters.remove(who.getUniqueId());
                playersWhoSwitchedItems.remove(who.getUniqueId());
                cancel();
            } else {
                timer--;
            }
        }

        public void refresh(){
            timer = delay;
        }
    }
}
