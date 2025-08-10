package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.CustomDamageType;
import me.athlaeos.valhallammo.listeners.EntityDamagedListener;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OnAttack implements EffectTrigger, Listener {
    private static Listener singleListenerInstance = null;

    private final boolean inflictSelf;
    private final boolean onAttacked;
    public OnAttack(boolean inflictSelf, boolean onAttacked){
        this.inflictSelf = inflictSelf; // if true, effects are applied on self. otherwise on other party
        this.onAttacked = onAttacked; // if true, triggers when attacked. otherwise when attacking
    }

    @Override
    public String id() {
        return "on_attack" + (onAttacked ? "ed" : "") + "_inflict_" + (inflictSelf ? "self" : "enemy");
    }

    @Override
    public void onRegister() {
        if (singleListenerInstance != null) return;
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
        singleListenerInstance = this;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamaged(EntityDamageByEntityEvent e){
        Entity trueDamager = EntityUtils.getTrueDamager(e);
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getEntity() instanceof LivingEntity le) ||
                !shouldTrigger(le) || e.isCancelled() || !(trueDamager instanceof LivingEntity damager)) return;
        EntityProperties targetProperties = onAttacked ? EntityCache.getAndCacheProperties(le) : EntityCache.getAndCacheProperties(damager);
        if (targetProperties.getPermanentPotionEffects().isEmpty()) return;
        trigger(inflictSelf ? le : damager, targetProperties.getPermanentPotionEffects().getOrDefault(id(), new ArrayList<>()));
    }
}
