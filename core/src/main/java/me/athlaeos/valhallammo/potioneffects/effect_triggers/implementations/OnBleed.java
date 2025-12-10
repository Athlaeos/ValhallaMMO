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

import java.util.ArrayList;

public class OnBleed implements EffectTrigger, Listener {
    private static Listener singleListenerInstance = null;

    private final boolean inflictSelf;
    private final boolean onBled;
    public OnBleed(boolean inflictSelf, boolean onAttacked){
        this.inflictSelf = inflictSelf;// if true, effects are applied on self. otherwise on other party
        this.onBled = onAttacked;  // if true, effects are applied on self. otherwise on other party
    }

    @Override
    public String id() {
        return "on_bl" + (onBled ? "ed" : "eed") + "_inflict_" + (inflictSelf ? "self" : "enemy");
    }

    @Override
    public void onRegister() {
        if (singleListenerInstance != null) return;
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
        singleListenerInstance = this;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBleed(EntityBleedEvent e){
        Entity trueDamager;
        if (e.getBleeder() instanceof EnderPearl p && p.getShooter() instanceof Entity en) trueDamager = en;
        else if (e.getBleeder() instanceof Projectile p && p.getShooter() instanceof Entity t) trueDamager = t;
        else trueDamager = e.getBleeder();
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getEntity() instanceof LivingEntity le) ||
                e.isCancelled() || !(trueDamager instanceof LivingEntity damager)) return;
        EntityProperties attackerProperties = EntityCache.getAndCacheProperties(damager);
        EntityProperties victimProperties = EntityCache.getAndCacheProperties(le);

        EffectTrigger onBledInflictEnemy = EffectTriggerRegistry.getTrigger("on_bled_inflict_enemy");
        if (onBledInflictEnemy != null) {
            if (!victimProperties.getPermanentPotionEffects().getOrDefault("on_bled_inflict_enemy", new ArrayList<>()).isEmpty()) {
                onBledInflictEnemy.trigger(damager, victimProperties.getPermanentEffectCooldowns().get("on_bled_inflict_enemy"), victimProperties.getPermanentPotionEffects().getOrDefault("on_bled_inflict_enemy", new ArrayList<>()));
            }
        }

        EffectTrigger onBleedInflictEnemy = EffectTriggerRegistry.getTrigger("on_bleed_inflict_enemy");
        if (onBleedInflictEnemy != null) {
            if (!attackerProperties.getPermanentPotionEffects().getOrDefault("on_bleed_inflict_enemy", new ArrayList<>()).isEmpty()) {
                onBleedInflictEnemy.trigger(le, attackerProperties.getPermanentEffectCooldowns().get("on_bleed_inflict_enemy"), attackerProperties.getPermanentPotionEffects().getOrDefault("on_bleed_inflict_enemy", new ArrayList<>()));
            }
        }

        EffectTrigger onBledInflictSelf = EffectTriggerRegistry.getTrigger("on_bled_inflict_self");
        if (onBledInflictSelf != null) {
            if (!victimProperties.getPermanentPotionEffects().getOrDefault("on_bled_inflict_self", new ArrayList<>()).isEmpty()) {
                onBledInflictSelf.trigger(le, victimProperties.getPermanentEffectCooldowns().get("on_bled_inflict_self"), victimProperties.getPermanentPotionEffects().getOrDefault("on_bled_inflict_self", new ArrayList<>()));
            }
        }

        EffectTrigger onBleedInflictSelf = EffectTriggerRegistry.getTrigger("on_bleed_inflict_self");
        if (onBleedInflictSelf != null) {
            if (!attackerProperties.getPermanentPotionEffects().getOrDefault("on_bleed_inflict_self", new ArrayList<>()).isEmpty()) {
                onBleedInflictSelf.trigger(damager, attackerProperties.getPermanentEffectCooldowns().get("on_bleed_inflict_self"), attackerProperties.getPermanentPotionEffects().getOrDefault("on_bleed_inflict_self", new ArrayList<>()));
            }
        }
    }
}
