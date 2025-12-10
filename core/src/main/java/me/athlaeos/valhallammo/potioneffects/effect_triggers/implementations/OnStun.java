package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.event.EntityStunEvent;
import me.athlaeos.valhallammo.event.EntityStunEvent;
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

import java.util.ArrayList;

public class OnStun implements EffectTrigger, Listener {
    private static Listener singleListenerInstance = null;

    private final boolean inflictSelf;
    private final boolean onStunned;
    public OnStun(boolean inflictSelf, boolean onAttacked){
        this.inflictSelf = inflictSelf;// if true, effects are applied on self. otherwise on other party
        this.onStunned = onAttacked;  // if true, effects are applied on self. otherwise on other party
    }

    @Override
    public String id() {
        return "on_stun" + (onStunned ? "ned" : "") + "_inflict_" + (inflictSelf ? "self" : "enemy");
    }

    @Override
    public void onRegister() {
        if (singleListenerInstance != null) return;
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
        singleListenerInstance = this;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onStun(EntityStunEvent e){
        Entity trueDamager;
        if (e.getCausedBy() instanceof EnderPearl p && p.getShooter() instanceof Entity en) trueDamager = en;
        else if (e.getCausedBy() instanceof Projectile p && p.getShooter() instanceof Entity t) trueDamager = t;
        else trueDamager = e.getCausedBy();
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getEntity() instanceof LivingEntity le) ||
                e.isCancelled() || !(trueDamager instanceof LivingEntity damager)) return;
        EntityProperties attackerProperties = EntityCache.getAndCacheProperties(damager);
        EntityProperties victimProperties = EntityCache.getAndCacheProperties(le);

        EffectTrigger onStunnedInflictEnemy = EffectTriggerRegistry.getTrigger("on_stunned_inflict_enemy");
        if (onStunnedInflictEnemy != null) {
            if (!victimProperties.getPermanentPotionEffects().getOrDefault("on_stunned_inflict_enemy", new ArrayList<>()).isEmpty()) {
                onStunnedInflictEnemy.trigger(damager, victimProperties.getPermanentEffectCooldowns().get("on_stunned_inflict_enemy"), victimProperties.getPermanentPotionEffects().getOrDefault("on_stunned_inflict_enemy", new ArrayList<>()));
            }
        }

        EffectTrigger onStunInflictEnemy = EffectTriggerRegistry.getTrigger("on_stun_inflict_enemy");
        if (onStunInflictEnemy != null) {
            if (!attackerProperties.getPermanentPotionEffects().getOrDefault("on_stun_inflict_enemy", new ArrayList<>()).isEmpty()) {
                onStunInflictEnemy.trigger(le, attackerProperties.getPermanentEffectCooldowns().get("on_stun_inflict_enemy"), attackerProperties.getPermanentPotionEffects().getOrDefault("on_stun_inflict_enemy", new ArrayList<>()));
            }
        }

        EffectTrigger onStunnedInflictSelf = EffectTriggerRegistry.getTrigger("on_stunned_inflict_self");
        if (onStunnedInflictSelf != null) {
            if (!victimProperties.getPermanentPotionEffects().getOrDefault("on_stunned_inflict_self", new ArrayList<>()).isEmpty()) {
                onStunnedInflictSelf.trigger(le, victimProperties.getPermanentEffectCooldowns().get("on_stunned_inflict_self"), victimProperties.getPermanentPotionEffects().getOrDefault("on_stunned_inflict_self", new ArrayList<>()));
            }
        }

        EffectTrigger onStunInflictSelf = EffectTriggerRegistry.getTrigger("on_stun_inflict_self");
        if (onStunInflictSelf != null) {
            if (!attackerProperties.getPermanentPotionEffects().getOrDefault("on_stun_inflict_self", new ArrayList<>()).isEmpty()) {
                onStunInflictSelf.trigger(damager, attackerProperties.getPermanentEffectCooldowns().get("on_stun_inflict_self"), attackerProperties.getPermanentPotionEffects().getOrDefault("on_stun_inflict_self", new ArrayList<>()));
            }
        }
    }
}
