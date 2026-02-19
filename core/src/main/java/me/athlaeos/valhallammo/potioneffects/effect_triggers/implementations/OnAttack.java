package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.CustomDamageType;
import me.athlaeos.valhallammo.listeners.EntityDamagedListener;
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

public class OnAttack implements EffectTrigger.ConfigurableTrigger, Listener {
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
        EntityProperties attackerProperties = EntityCache.getAndCacheProperties(damager);
        EntityProperties victimProperties = EntityCache.getAndCacheProperties(le);
        String damageCause = EntityDamagedListener.getLastDamageCause(le);
        CustomDamageType type = CustomDamageType.getCustomType(damageCause);
        trigger(attackerProperties, victimProperties, damager, le, "");
        if (type != null) trigger(attackerProperties, victimProperties, damager, le, type.getType());
    }

    private void trigger(EntityProperties attackerProperties, EntityProperties victimProperties, LivingEntity damager, LivingEntity le, String insertion){
        EffectTrigger onAttackedInflictEnemy = EffectTriggerRegistry.getTrigger("on_attacked_inflict_enemy" + insertion);
        if (onAttackedInflictEnemy != null) {
            if (!victimProperties.getPermanentPotionEffects().getOrDefault("on_attacked_inflict_enemy" + insertion, new ArrayList<>()).isEmpty()) {
                onAttackedInflictEnemy.trigger(damager, victimProperties.getPermanentEffectCooldowns().get("on_attacked_inflict_enemy" + insertion), victimProperties.getPermanentPotionEffects().getOrDefault("on_attacked_inflict_enemy" + insertion, new ArrayList<>()));
            }
        }

        EffectTrigger onAttackInflictEnemy = EffectTriggerRegistry.getTrigger("on_attack_inflict_enemy" + insertion);
        if (onAttackInflictEnemy != null) {
            if (!attackerProperties.getPermanentPotionEffects().getOrDefault("on_attack_inflict_enemy" + insertion, new ArrayList<>()).isEmpty()) {
                onAttackInflictEnemy.trigger(le, attackerProperties.getPermanentEffectCooldowns().get("on_attack_inflict_enemy" + insertion), attackerProperties.getPermanentPotionEffects().getOrDefault("on_attack_inflict_enemy" + insertion, new ArrayList<>()));
            }
        }

        EffectTrigger onAttackedInflictSelf = EffectTriggerRegistry.getTrigger("on_attacked_inflict_self" + insertion);
        if (onAttackedInflictSelf != null) {
            if (!victimProperties.getPermanentPotionEffects().getOrDefault("on_attacked_inflict_self" + insertion, new ArrayList<>()).isEmpty()) {
                onAttackedInflictSelf.trigger(le, victimProperties.getPermanentEffectCooldowns().get("on_attacked_inflict_self" + insertion), victimProperties.getPermanentPotionEffects().getOrDefault("on_attacked_inflict_self" + insertion, new ArrayList<>()));
            }
        }

        EffectTrigger onAttackInflictSelf = EffectTriggerRegistry.getTrigger("on_attack_inflict_self" + insertion);
        if (onAttackInflictSelf != null) {
            if (!attackerProperties.getPermanentPotionEffects().getOrDefault("on_attack_inflict_self" + insertion, new ArrayList<>()).isEmpty()) {
                onAttackInflictSelf.trigger(damager, attackerProperties.getPermanentEffectCooldowns().get("on_attack_inflict_self" + insertion), attackerProperties.getPermanentPotionEffects().getOrDefault("on_attack_inflict_self" + insertion, new ArrayList<>()));
            }
        }
    }

    @Override
    public String isValid(String arg) {
        String cleanedArg = getArg(arg);
        if (cleanedArg.isEmpty() || cleanedArg.equals("any")) return null;
        CustomDamageType type = CustomDamageType.getCustomType(cleanedArg);
        return type == null ? "&cInvalid damage type, valid types are " + String.join(", ", CustomDamageType.getRegisteredTypes().keySet()) : null;
    }

    @Override
    public String getUsage() {
        return "\"<custom_damage_type>\", which can be any of " + String.join(", ", CustomDamageType.getRegisteredTypes().keySet()) + ", or 'any' for any damage type";
    }

    @Override
    public String asLore(String rawID) {
        String arg = getArg(rawID);
        if (arg.isEmpty()) {
            if (rawID.startsWith("on_attack_inflict_self")) return "&fWhen you attack, applies effects on you";
            if (rawID.startsWith("on_attacked_inflict_self")) return "&fWhen you're attacked, applies effects on you";
            if (rawID.startsWith("on_attack_inflict_enemy")) return "&fWhen you attack, applies effects on your opponent";
            if (rawID.startsWith("on_attacked_inflict_enemy")) return "&fWhen you're attacked, applies effects on your opponent";
        } else {
            if (rawID.startsWith("on_attack_inflict_self")) return "&fWhen you attack, applies effects on you only if the damage type is &e" + arg;
            if (rawID.startsWith("on_attacked_inflict_self")) return "&fWhen you're attacked, applies effects on you only if the damage type is &e" + arg;
            if (rawID.startsWith("on_attack_inflict_enemy")) return "&fWhen you attack, applies effects on your opponent only if the damage type is &e" + arg;
            if (rawID.startsWith("on_attacked_inflict_enemy")) return "&fWhen you're attacked, applies effects on your opponent only if the damage type is &e" + arg;
        }
        return ConfigurableTrigger.super.asLore(rawID);
    }
}
