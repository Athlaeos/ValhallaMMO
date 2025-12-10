package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.event.EntityBleedEvent;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTriggerRegistry;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

import java.util.ArrayList;

public class OnExpCollected implements EffectTrigger, Listener {
    private static Listener singleListenerInstance = null;

    @Override
    public String id() {
        return "on_collect_exp";
    }

    @Override
    public void onRegister() {
        if (singleListenerInstance != null) return;
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
        singleListenerInstance = this;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBleed(PlayerExpChangeEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.getAmount() <= 0) return;
        EntityProperties properties = EntityCache.getAndCacheProperties(e.getPlayer());

        trigger(e.getPlayer(), properties.getPermanentEffectCooldowns().get(id()), properties.getPermanentPotionEffects().getOrDefault(id(), new ArrayList<>()));
    }
}
