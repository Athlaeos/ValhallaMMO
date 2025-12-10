package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.entities.EntityClassification;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class OnKill implements EffectTrigger.ConfigurableTrigger, Listener {
    private static Listener singleListenerInstance = null;

    @Override
    public String id() {
        return "on_kill_";
    }

    @Override
    public void onRegister() {
        if (singleListenerInstance != null) return;
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
        singleListenerInstance = this;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onKill(EntityDeathEvent e){
        Player killer = e.getEntity().getKiller();
        if (killer == null || ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || EntityClassification.matchesClassification(e.getEntityType(), EntityClassification.UNALIVE)) return;
        EntityProperties properties = EntityCache.getAndCacheProperties(killer);

        for (String permanentPotionEffect : properties.getPermanentPotionEffects().keySet()){
            if (!permanentPotionEffect.startsWith(id())) continue;
            String arg = getArg(permanentPotionEffect);
            Collection<String> validEntities = new HashSet<>(Set.of(arg.split("/")));
            if (validEntities.isEmpty() || validEntities.contains(e.getEntityType().toString()))
                trigger(killer, properties.getPermanentEffectCooldowns().get(permanentPotionEffect), properties.getPermanentPotionEffects().get(permanentPotionEffect));
        }
    }

    @Override
    public String isValid(String arg) {
        String args = getArg(arg);
        String[] entities = args.split("/");
        for (String entity : entities){
            if (Catch.catchOrElse(() -> EntityType.valueOf(entity), null) == null) return "&cEntity type " + entity + " was given, but it's not valid";
        }
        return null;
    }

    @Override
    public String getUsage() {
        return "\"<entities separated by slashes>\", such as \"COW/PIG/ZOMBIE/WARDEN\"";
    }
}
