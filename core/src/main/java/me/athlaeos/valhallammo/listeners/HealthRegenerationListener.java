package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class HealthRegenerationListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHealthRegenerated(EntityRegainHealthEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName())) return;
        if (e.getEntity() instanceof LivingEntity l){
            double multiplier = 1 + AccumulativeStatManager.getCachedStats("HEALING_BONUS", l, 10000, true);
            e.setAmount(e.getAmount() * multiplier);
        }
    }
}
