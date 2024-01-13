package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.entities.EntityAttributeStats;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.utility.GlobalEffect;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;

public class JoinLeaveListener implements Listener {
    private final NamespacedKey HEALTH = new NamespacedKey(ValhallaMMO.getInstance(), "cached_health");

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        ProfileRegistry.getPersistence().loadProfile(e.getPlayer());
        EntityCache.getAndCacheProperties(e.getPlayer());
        PotionEffectRegistry.updatePlayerAffectedStatus(e.getPlayer());
        GlobalEffect.temporarilyRevealBossBar(e.getPlayer());
        EntityAttributeStats.updateStats(e.getPlayer());

        double health = e.getPlayer().getPersistentDataContainer().getOrDefault(HEALTH, PersistentDataType.DOUBLE, -1D);
        if (health > 0){
            AttributeInstance maxHealth = e.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealth != null){
                if (maxHealth.getValue() < health) health = maxHealth.getValue();
                e.getPlayer().setHealth(health);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        // the following code is to remove valhallammo's attribute modifiers off players when they log off
        // this is to prevent, in the case valhallammo is being uninstalled, no unintended attributes remain
        // stuck on the player.
        EntityUtils.removeUniqueAttribute(e.getPlayer(), EntityAttributeStats.NEGATIVE_KNOCKBACK, "valhalla_negative_knockback_taken", Attribute.GENERIC_KNOCKBACK_RESISTANCE);

        e.getPlayer().getPersistentDataContainer().set(HEALTH, PersistentDataType.DOUBLE, e.getPlayer().getHealth());
        for (EntityAttributeStats.AttributeDataHolder holder : EntityAttributeStats.getAttributesToUpdate().values()){
            EntityUtils.removeUniqueAttribute(e.getPlayer(), holder.uuid(), holder.name(), holder.type());
        }

        EntityUtils.removeUniqueAttribute(e.getPlayer(), EntityAttributeStats.ARMOR_NULLIFIER, "armor_nullifier", Attribute.GENERIC_ARMOR);
        EntityUtils.removeUniqueAttribute(e.getPlayer(), EntityAttributeStats.ARMOR_DISPLAY, "armor_display", Attribute.GENERIC_ARMOR);

        ProfileRegistry.getPersistence().saveProfile(e.getPlayer());
    }
}
