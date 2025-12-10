package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.event.EntityParryEntityEvent;
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

public class OnParry implements EffectTrigger, Listener {
    private static Listener singleListenerInstance = null;

    private final boolean inflictSelf;
    private final boolean onParried;
    public OnParry(boolean inflictSelf, boolean onAttacked){
        this.inflictSelf = inflictSelf;// if true, effects are applied on self. otherwise on other party
        this.onParried = onAttacked;  // if true, effects are applied on self. otherwise on other party
    }

    @Override
    public String id() {
        return "on_parr" + (onParried ? "ied" : "y") + "_inflict_" + (inflictSelf ? "self" : "enemy");
    }

    @Override
    public void onRegister() {
        if (singleListenerInstance != null) return;
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
        singleListenerInstance = this;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onParry(EntityParryEntityEvent e){
        Entity trueDamager;
        if (e.getParried() instanceof EnderPearl p && p.getShooter() instanceof Entity en) trueDamager = en;
        else if (e.getParried() instanceof Projectile p && p.getShooter() instanceof Entity t) trueDamager = t;
        else trueDamager = e.getParried();
        if (e.getType() != EntityParryEntityEvent.ParryType.SUCCESSFUL || ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getEntity() instanceof LivingEntity le) ||
                e.isCancelled() || !(trueDamager instanceof LivingEntity damager)) return;
        EntityProperties attackerProperties = EntityCache.getAndCacheProperties(damager);
        EntityProperties victimProperties = EntityCache.getAndCacheProperties(le);

        EffectTrigger onParryInflictEnemy = EffectTriggerRegistry.getTrigger("on_parry_inflict_enemy");
        if (onParryInflictEnemy != null) {
            if (!victimProperties.getPermanentPotionEffects().getOrDefault("on_parry_inflict_enemy", new ArrayList<>()).isEmpty()) {
                onParryInflictEnemy.trigger(damager, victimProperties.getPermanentEffectCooldowns().get("on_parry_inflict_enemy"), victimProperties.getPermanentPotionEffects().getOrDefault("on_parry_inflict_enemy", new ArrayList<>()));
            }
        }

        EffectTrigger onParriedInflictEnemy = EffectTriggerRegistry.getTrigger("on_parried_inflict_enemy");
        if (onParriedInflictEnemy != null) {
            if (!attackerProperties.getPermanentPotionEffects().getOrDefault("on_parried_inflict_enemy", new ArrayList<>()).isEmpty()) {
                onParriedInflictEnemy.trigger(le, attackerProperties.getPermanentEffectCooldowns().get("on_parried_inflict_enemy"), attackerProperties.getPermanentPotionEffects().getOrDefault("on_parried_inflict_enemy", new ArrayList<>()));
            }
        }

        EffectTrigger onParryInflictSelf = EffectTriggerRegistry.getTrigger("on_parry_inflict_self");
        if (onParryInflictSelf != null) {
            if (!victimProperties.getPermanentPotionEffects().getOrDefault("on_parry_inflict_self", new ArrayList<>()).isEmpty()) {
                onParryInflictSelf.trigger(le, victimProperties.getPermanentEffectCooldowns().get("on_parry_inflict_self"), victimProperties.getPermanentPotionEffects().getOrDefault("on_parry_inflict_self", new ArrayList<>()));
            }
        }

        EffectTrigger onParriedInflictSelf = EffectTriggerRegistry.getTrigger("on_parried_inflict_self");
        if (onParriedInflictSelf != null) {
            if (!attackerProperties.getPermanentPotionEffects().getOrDefault("on_parried_inflict_self", new ArrayList<>()).isEmpty()) {
                onParriedInflictSelf.trigger(damager, attackerProperties.getPermanentEffectCooldowns().get("on_parried_inflict_self"), attackerProperties.getPermanentPotionEffects().getOrDefault("on_parried_inflict_self", new ArrayList<>()));
            }
        }
    }
}
