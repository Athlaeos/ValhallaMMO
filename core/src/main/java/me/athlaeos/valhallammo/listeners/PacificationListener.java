package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;

import java.util.*;

public class PacificationListener implements Listener {
    private final Map<UUID, Collection<UUID>> harmedBy = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTarget(EntityTargetLivingEntityEvent e){
        if (e.isCancelled() || !(e.getTarget() instanceof Player p)) return;
        if (harmedBy.getOrDefault(e.getEntity().getUniqueId(), new HashSet<>()).contains(p.getUniqueId())) return;
        EntityProperties playerProperties = EntityCache.getAndCacheProperties(p);
        if (!playerProperties.getActivePotionEffects().containsKey("PACIFICATION")) return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByEntityEvent e){
        if (e.isCancelled()) return;
        Entity trueDamager = EntityUtils.getTrueDamager(e);
        if (!(trueDamager instanceof Player p)) return;
        EntityProperties playerProperties = EntityCache.getAndCacheProperties(p);
        if (!playerProperties.getActivePotionEffects().containsKey("PACIFICATION")) return;
        Collection<UUID> harmedSet = harmedBy.getOrDefault(e.getEntity().getUniqueId(), new HashSet<>());
        harmedSet.add(p.getUniqueId());
        harmedBy.put(e.getEntity().getUniqueId(), harmedSet);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent e){
        harmedBy.remove(e.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUnload(EntitiesUnloadEvent e){
        e.getEntities().forEach(en -> harmedBy.remove(en.getUniqueId()));
    }
}
