package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTriggerRegistry;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;

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
                e.isCancelled() || !(trueDamager instanceof LivingEntity damager)) return;
        EntityProperties attackerProperties = EntityCache.getAndCacheProperties(le);
        EntityProperties victimProperties = EntityCache.getAndCacheProperties(damager);

        EffectTrigger onAttackedInflictEnemy = EffectTriggerRegistry.getTrigger("on_attacked_inflict_enemy");
        if (onAttackedInflictEnemy != null && onAttackedInflictEnemy.shouldTrigger(le)) {
            if (!victimProperties.getPermanentPotionEffects().getOrDefault("on_attacked_inflict_enemy", new ArrayList<>()).isEmpty()) {
                onAttackedInflictEnemy.trigger(le, victimProperties.getPermanentPotionEffects().getOrDefault("on_attacked_inflict_enemy", new ArrayList<>()));
            }
        }

        EffectTrigger onAttackInflictEnemy = EffectTriggerRegistry.getTrigger("on_attack_inflict_enemy");
        if (onAttackInflictEnemy != null && onAttackInflictEnemy.shouldTrigger(le)) {
            if (!victimProperties.getPermanentPotionEffects().getOrDefault("on_attack_inflict_enemy", new ArrayList<>()).isEmpty()) {
                onAttackInflictEnemy.trigger(le, victimProperties.getPermanentPotionEffects().getOrDefault("on_attack_inflict_enemy", new ArrayList<>()));
            }
        }

        EffectTrigger onAttackedInflictSelf = EffectTriggerRegistry.getTrigger("on_attacked_inflict_self");
        if (onAttackedInflictSelf != null && onAttackedInflictSelf.shouldTrigger(le)) {
            if (!victimProperties.getPermanentPotionEffects().getOrDefault("on_attacked_inflict_self", new ArrayList<>()).isEmpty()) {
                onAttackedInflictSelf.trigger(le, victimProperties.getPermanentPotionEffects().getOrDefault("on_attacked_inflict_self", new ArrayList<>()));
            }
        }

        EffectTrigger onAttackInflictSelf = EffectTriggerRegistry.getTrigger("on_attack_inflict_self");
        if (onAttackInflictSelf != null && onAttackInflictSelf.shouldTrigger(le)) {
            if (!victimProperties.getPermanentPotionEffects().getOrDefault("on_attack_inflict_self", new ArrayList<>()).isEmpty()) {
                onAttackInflictSelf.trigger(le, victimProperties.getPermanentPotionEffects().getOrDefault("on_attack_inflict_self", new ArrayList<>()));
            }
        }
    }
}
