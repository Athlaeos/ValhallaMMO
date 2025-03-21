package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.entities.EntityAttributeStats;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.skills.perk_rewards.implementations.FlightReward;
import me.athlaeos.valhallammo.utility.GlobalEffect;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class JoinLeaveListener implements Listener {
    private final NamespacedKey HEALTH = new NamespacedKey(ValhallaMMO.getInstance(), "cached_health");
    private static final Collection<UUID> loadedProfiles = new HashSet<>();

    public static Collection<UUID> getLoadedProfiles() {
        return loadedProfiles;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        ProfileRegistry.getPersistence().loadProfile(e.getPlayer());
        EntityCache.getAndCacheProperties(e.getPlayer());
        PotionEffectRegistry.updatePlayerAffectedStatus(e.getPlayer());
        GlobalEffect.temporarilyRevealBossBar(e.getPlayer());
        EntityAttributeStats.updateStats(e.getPlayer());
        PlayerMenuUtilManager.removePlayerMenuUtility(e.getPlayer().getUniqueId());

        double health = e.getPlayer().getPersistentDataContainer().getOrDefault(HEALTH, PersistentDataType.DOUBLE, -1D);
        if (health > 0){
            AttributeInstance maxHealth = e.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealth != null){
                if (maxHealth.getValue() < health) health = maxHealth.getValue();
                e.getPlayer().setHealth(health);
            }
        }

        FlightReward.setFlight(e.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e){
        // the following code is to remove valhallammo's attribute modifiers off players when they log off
        // this is to prevent, in the case valhallammo is being uninstalled, no unintended attributes remain
        // stuck on the player.
        e.getPlayer().getPersistentDataContainer().set(HEALTH, PersistentDataType.DOUBLE, e.getPlayer().getHealth());
        EntityAttributeStats.removeStats(e.getPlayer());
        PotionEffectRegistry.markAsUnaffected(e.getPlayer());

        if (loadedProfiles.contains(e.getPlayer().getUniqueId())) {
            ProfileRegistry.getPersistence().saveProfile(e.getPlayer());
            loadedProfiles.remove(e.getPlayer().getUniqueId());
        }

        FlightReward.setFlight(e.getPlayer(), false);
    }
}
