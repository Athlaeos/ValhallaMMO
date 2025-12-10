package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import me.athlaeos.valhallammo.version.AttributeMappings;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class OnHealthReached implements EffectTrigger.ConfigurableTrigger, Listener {
    private static Listener singleListenerInstance = null;

    @Override
    public String id() {
        return "on_health_reached_";
    }

    @Override
    public void onRegister() {
        if (singleListenerInstance != null) return;
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
        singleListenerInstance = this;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityHealed(EntityRegainHealthEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getEntity() instanceof LivingEntity le) || e.isCancelled()) return;
        EntityProperties properties = EntityCache.getAndCacheProperties(le);
        Collection<String> onHealthReachedEffects = new HashSet<>();
        for (String permanentEffect : properties.getPermanentPotionEffects().keySet()){
            if (permanentEffect.startsWith(id())) onHealthReachedEffects.add(permanentEffect);
        }
        if (onHealthReachedEffects.isEmpty()) return;

        AttributeInstance maxHealthInstance = le.getAttribute(AttributeMappings.MAX_HEALTH.getAttribute());
        if (maxHealthInstance == null) return;
        double maxHealth = maxHealthInstance.getValue();
        if (maxHealth <= 0) return;
        double fractionMaxHealthFrom = Math.max(0, Math.min(1, le.getHealth() / maxHealth));
        double fractionMaxHealthTo = Math.max(0, Math.min(1, (le.getHealth() + e.getAmount()) / maxHealth));

        for (String permanentEffect : onHealthReachedEffects){
            List<PotionEffectWrapper> effects = properties.getPermanentPotionEffects().get(permanentEffect);
            if (effects == null || effects.isEmpty()) continue;
            String arg = getArg(permanentEffect);
            String[] args = arg.split("_");
            boolean worksOnHeal = args[1].equals("healed") || args[1].equals("both");
            if (!worksOnHeal) continue;
            double threshold = Catch.catchOrElse(() -> Double.parseDouble(args[0]), -1D);
            if (threshold <= 0) continue;
            if (fractionMaxHealthFrom < threshold && fractionMaxHealthTo >= threshold)
                trigger(le, permanentEffect, properties.getPermanentEffectCooldowns().get(permanentEffect), effects);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamaged(EntityDamageEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getEntity() instanceof LivingEntity le) || e.isCancelled()) return;
        EntityProperties properties = EntityCache.getAndCacheProperties(le);
        Collection<String> onHealthReachedEffects = new HashSet<>();
        for (String permanentEffect : properties.getPermanentPotionEffects().keySet()){
            if (permanentEffect.startsWith(id())) onHealthReachedEffects.add(permanentEffect);
        }
        if (onHealthReachedEffects.isEmpty()) return;

        AttributeInstance maxHealthInstance = le.getAttribute(AttributeMappings.MAX_HEALTH.getAttribute());
        if (maxHealthInstance == null) return;
        double maxHealth = maxHealthInstance.getValue();
        if (maxHealth <= 0) return;
        double fractionMaxHealthFrom = Math.max(0, Math.min(1, le.getHealth() / maxHealth));

        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            double fractionMaxHealthTo = Math.max(0, Math.min(1, le.getHealth() / maxHealth));

            for (String permanentEffect : onHealthReachedEffects){
                List<PotionEffectWrapper> effects = properties.getPermanentPotionEffects().get(permanentEffect);
                if (effects == null || effects.isEmpty()) continue;
                String arg = getArg(permanentEffect);
                String[] args = arg.split("_");
                boolean worksOnHarm = args[1].equals("harmed") || args[1].equals("both");
                if (!worksOnHarm) continue;
                double threshold = Catch.catchOrElse(() -> Double.parseDouble(args[0]), -1D);
                if (threshold <= 0) continue;
                if (fractionMaxHealthFrom > threshold && fractionMaxHealthTo <= threshold)
                    trigger(le, permanentEffect, properties.getPermanentEffectCooldowns().get(permanentEffect), effects);
            }
        }, 2L);
    }

    @Override
    public String isValid(String arg) {
        String[] args = getArg(arg).split("_");
        double doubleValue = Catch.catchOrElse(() -> Double.parseDouble(args[0]), -1D);
        if (doubleValue <= 0 || doubleValue > 1) return "&cInvalid, first arg must be a number between 0 and 1";
        if (args.length > 1 && !args[1].equalsIgnoreCase("healed") && !args[1].equalsIgnoreCase("harmed") && !args[1].equalsIgnoreCase("both")) return "&cInvalid, second arg must be healed/harmed/both";
        return null;
    }

    @Override
    public String getUsage() {
        return "\"<percentage>_<healed/harmed/both>\". <percentage> for the fraction of health to reach for effect to trigger. <healed/harmed/both> if you want it to trigger only when the entity HEALS, is HARMED, or simply passes the threshold in health. " +
                "Example: say \"0.3_harmed\" for the effect to trigger only when taking damage to below 30% health. \"healed\" would trigger the effect if the entity heals to above 30% health";
    }

    @Override
    public String asLore(String rawID) {
        String args = getArg(rawID);
        if (args.isEmpty()) return "&cUnconfigured";
        if (isValid(rawID) != null) return "&cImproperly configured";
        String[] values = args.split("_");
        String percentage = String.format("%.0f%%", Double.parseDouble(values[0]) * 100);
        String explained = values[1].equals("healed") ? "&ahealed &fto above " + percentage + " HP" :
                values[1].equals("harmed") ? "&edamaged &fto below " + percentage + " HP" :
                        "health passes " + percentage + " HP from either healing or damage";
        return "&fTriggers when " + explained;
    }
}
