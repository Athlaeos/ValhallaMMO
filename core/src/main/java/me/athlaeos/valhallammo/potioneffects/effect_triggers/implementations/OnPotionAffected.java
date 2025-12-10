package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.event.EntityCustomPotionEffectEvent;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class OnPotionAffected implements EffectTrigger.ConfigurableTrigger, Listener {
    private static Listener singleListenerInstance = null;

    @Override
    public String id() {
        return "on_potion_affected_";
    }

    @Override
    public void onRegister() {
        if (singleListenerInstance != null) return;
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
        singleListenerInstance = this;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVanillaEffect(EntityPotionEffectEvent e){
        if (e.getAction() != EntityPotionEffectEvent.Action.ADDED || ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getEntity() instanceof LivingEntity le) ||
                e.isCancelled() || e.getNewEffect() == null) return;
        EntityProperties properties = EntityCache.getAndCacheProperties(le);

        for (String permanentEffect : properties.getPermanentPotionEffects().keySet()){
            if (!permanentEffect.startsWith(id())) continue;
            Collection<String> effects = new HashSet<>(Set.of(getArg(permanentEffect).split("/")));
            if (!effects.contains(e.getNewEffect().getType().getName())) continue;
            trigger(le, properties.getPermanentEffectCooldowns().get(permanentEffect), properties.getPermanentPotionEffects().get(permanentEffect));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCustomEffect(EntityCustomPotionEffectEvent e){
        if (e.getAction() != EntityPotionEffectEvent.Action.ADDED || ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getEntity() instanceof LivingEntity le) ||
                e.isCancelled() || e.getNewEffect() == null) return;
        EntityProperties properties = EntityCache.getAndCacheProperties(le);

        for (String permanentEffect : properties.getPermanentPotionEffects().keySet()){
            if (!permanentEffect.startsWith(id())) continue;
            Collection<String> effects = new HashSet<>(Set.of(getArg(permanentEffect).split("/")));
            if (!effects.contains(e.getNewEffect().getWrapper().getEffectName())) continue;
            trigger(le, properties.getPermanentEffectCooldowns().get(permanentEffect), properties.getPermanentPotionEffects().get(permanentEffect));
        }
    }

    @Override
    public String isValid(String arg) {
        String args = getArg(arg);
        String[] effects = args.split("/");
        if (effects.length == 0) return "&cInsufficient arguments. Format is <effects separated by slashes>.";
        return null;
    }

    @Override
    public String getUsage() {
        return "\"<effects separated by slashes>\", such as \"HARM/HEAL/SLOW_MINING/CRIT_CHANCE\"";
    }

    @Override
    public String asLore(String rawID) {
        String args = getArg(rawID);
        String[] effects = args.split("/");
        if (effects.length == 0) return "&cImproperly configured";
        return "&fReceive any of the following effects to trigger: " + String.join(", ", effects);
    }
}