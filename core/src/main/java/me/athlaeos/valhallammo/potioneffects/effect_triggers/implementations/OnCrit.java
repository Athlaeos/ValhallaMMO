package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.event.EntityCriticallyHitEvent;
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

public class OnCrit implements EffectTrigger, Listener {
    private static Listener singleListenerInstance = null;

    private final boolean inflictSelf;
    private final boolean onCritted;
    public OnCrit(boolean inflictSelf, boolean onAttacked){
        this.inflictSelf = inflictSelf;// if true, effects are applied on self. otherwise on other party
        this.onCritted = onAttacked;  // if true, effects are applied on self. otherwise on other party
    }

    @Override
    public String id() {
        return "on_crit" + (onCritted ? "ted" : "") + "_inflict_" + (inflictSelf ? "self" : "enemy");
    }

    @Override
    public void onRegister() {
        if (singleListenerInstance != null) return;
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
        singleListenerInstance = this;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCrit(EntityCriticallyHitEvent e){
        Entity trueDamager;
        if (e.getCritter() instanceof EnderPearl p && p.getShooter() instanceof Entity en) trueDamager = en;
        else if (e.getCritter() instanceof Projectile p && p.getShooter() instanceof Entity t) trueDamager = t;
        else trueDamager = e.getCritter();
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getEntity() instanceof LivingEntity le) ||
                e.isCancelled() || !(trueDamager instanceof LivingEntity damager)) return;
        EntityProperties attackerProperties = EntityCache.getAndCacheProperties(damager);
        EntityProperties victimProperties = EntityCache.getAndCacheProperties(le);

        EffectTrigger onCrittedInflictEnemy = EffectTriggerRegistry.getTrigger("on_critted_inflict_enemy");
        if (onCrittedInflictEnemy != null) {
            if (!victimProperties.getPermanentPotionEffects().getOrDefault("on_critted_inflict_enemy", new ArrayList<>()).isEmpty()) {
                onCrittedInflictEnemy.trigger(damager, victimProperties.getPermanentEffectCooldowns().get("on_critted_inflict_enemy"), victimProperties.getPermanentPotionEffects().getOrDefault("on_critted_inflict_enemy", new ArrayList<>()));
            }
        }

        EffectTrigger onCritInflictEnemy = EffectTriggerRegistry.getTrigger("on_crit_inflict_enemy");
        if (onCritInflictEnemy != null) {
            if (!attackerProperties.getPermanentPotionEffects().getOrDefault("on_crit_inflict_enemy", new ArrayList<>()).isEmpty()) {
                onCritInflictEnemy.trigger(le, attackerProperties.getPermanentEffectCooldowns().get("on_crit_inflict_enemy"), attackerProperties.getPermanentPotionEffects().getOrDefault("on_crit_inflict_enemy", new ArrayList<>()));
            }
        }

        EffectTrigger onCrittedInflictSelf = EffectTriggerRegistry.getTrigger("on_critted_inflict_self");
        if (onCrittedInflictSelf != null) {
            if (!victimProperties.getPermanentPotionEffects().getOrDefault("on_critted_inflict_self", new ArrayList<>()).isEmpty()) {
                onCrittedInflictSelf.trigger(le, victimProperties.getPermanentEffectCooldowns().get("on_critted_inflict_self"), victimProperties.getPermanentPotionEffects().getOrDefault("on_critted_inflict_self", new ArrayList<>()));
            }
        }

        EffectTrigger onCritInflictSelf = EffectTriggerRegistry.getTrigger("on_crit_inflict_self");
        if (onCritInflictSelf != null) {
            if (!attackerProperties.getPermanentPotionEffects().getOrDefault("on_crit_inflict_self", new ArrayList<>()).isEmpty()) {
                onCritInflictSelf.trigger(damager, attackerProperties.getPermanentEffectCooldowns().get("on_crit_inflict_self"), attackerProperties.getPermanentPotionEffects().getOrDefault("on_crit_inflict_self", new ArrayList<>()));
            }
        }
    }
}
