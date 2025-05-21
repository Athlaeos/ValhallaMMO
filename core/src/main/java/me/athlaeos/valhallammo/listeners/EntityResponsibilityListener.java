package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.CustomDamageType;
import me.athlaeos.valhallammo.entities.EntityClassification;
import me.athlaeos.valhallammo.potioneffects.EffectResponsibility;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.version.PotionEffectMappings;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class EntityResponsibilityListener implements Listener {
    private final Map<PotionEffectType, CustomDamageType> effectToDamageTypeMapping = new HashMap<>();
    {
        effectToDamageTypeMapping.put(PotionEffectMappings.POISON.getPotionEffectType(), CustomDamageType.POISON);
        effectToDamageTypeMapping.put(PotionEffectMappings.INSTANT_DAMAGE.getPotionEffectType(), CustomDamageType.MAGIC);
        effectToDamageTypeMapping.put(PotionEffectMappings.INSTANT_HEALTH.getPotionEffectType(), CustomDamageType.MAGIC);
        effectToDamageTypeMapping.put(PotionEffectMappings.WITHER.getPotionEffectType(), CustomDamageType.NECROTIC);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFireAspect(EntityCombustByEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getEntity() instanceof LivingEntity v)) return;

        Entity damager = e.getCombuster();
        if (damager instanceof Projectile a && a.getShooter() instanceof Entity combuster) {
            EffectResponsibility.markResponsible(v.getUniqueId(), combuster.getUniqueId(), CustomDamageType.FIRE, (int) (e.getDuration() * 20) + 10);
        } else {
            EffectResponsibility.markResponsible(v.getUniqueId(), damager.getUniqueId(), CustomDamageType.FIRE, (int) (e.getDuration() * 20) + 10);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPoisonSplash(PotionSplashEvent e){
        ItemStack potion = e.getPotion().getItem();
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) ||
                ItemUtils.isEmpty(potion) || !(e.getPotion().getShooter() instanceof LivingEntity shooter)) return;

        for (PotionEffect effect : e.getPotion().getEffects()){
            CustomDamageType type = effectToDamageTypeMapping.get(effect.getType());
            if (type == null) continue;
            for (LivingEntity affected : e.getAffectedEntities()){
                boolean isUndead = EntityClassification.matchesClassification(affected.getType(), EntityClassification.UNDEAD);
                // undead creatures are an exception, because they are harmed from healing and healed from harm
                if (isUndead && effect.getType() == PotionEffectMappings.INSTANT_DAMAGE.getPotionEffectType()) continue;
                if (!isUndead && effect.getType() == PotionEffectMappings.INSTANT_HEALTH.getPotionEffectType()) continue;
                EffectResponsibility.markResponsible(
                        affected.getUniqueId(),
                        shooter.getUniqueId(),
                        type,
                        (effect.getType().isInstant() ? 0 : (int) (effect.getDuration() * e.getIntensity(affected))) + 10
                );
                // instant effects are given a static responsibility range of 10 ticks, or half a second.
                // other effects are given their duration plus half a second to account for precision errors
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLingeringCloudHit(AreaEffectCloudApplyEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) ||
                !(e.getEntity().getSource() instanceof LivingEntity shooter)) return;

        for (PotionEffect effect : e.getEntity().getCustomEffects()){
            CustomDamageType type = effectToDamageTypeMapping.get(effect.getType());
            if (type == null) continue;
            for (LivingEntity affected : e.getAffectedEntities()){
                boolean isUndead = EntityClassification.matchesClassification(affected.getType(), EntityClassification.UNDEAD);
                // undead creatures are an exception, because they are harmed from healing and healed from harm
                if (isUndead && effect.getType() == PotionEffectMappings.INSTANT_DAMAGE.getPotionEffectType()) continue;
                if (!isUndead && effect.getType() == PotionEffectMappings.INSTANT_HEALTH.getPotionEffectType()) continue;
                EffectResponsibility.markResponsible(
                        affected.getUniqueId(),
                        shooter.getUniqueId(),
                        type,
                        (effect.getType().isInstant() ? 0 : effect.getDuration()) + 10
                );
                // instant effects are given a static responsibility range of 10 ticks, or half a second.
                // other effects are given their duration plus half a second to account for precision errors
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent e){
        EffectResponsibility.clearResponsibility(e.getEntity().getUniqueId());
    }
}
