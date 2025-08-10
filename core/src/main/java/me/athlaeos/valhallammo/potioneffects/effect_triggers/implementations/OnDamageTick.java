package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.CustomDamageType;
import me.athlaeos.valhallammo.listeners.EntityDamagedListener;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTriggerRegistry;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class OnDamageTick implements EffectTrigger, Listener {
    private static Listener singleListenerInstance = null;
    private static final Map<String, CustomDamageType> damageTypes = new HashMap<>();
    private static final Map<String, String> damageTypesToIDMappings = new HashMap<>();

    private final CustomDamageType damageType;
    public OnDamageTick(CustomDamageType damageType){
        this.damageType = damageType;
    }

    @Override
    public String id() {
        return damageType == null ? "on_damaged" : ("on_" + damageType.getType().toLowerCase(Locale.US) + "_damage");
    }

    @Override
    public void onRegister() {
        if (damageType != null) {
            damageTypes.put(damageType.getType(), damageType);
            damageTypesToIDMappings.put(damageType.getType(), id());
        }
        if (singleListenerInstance != null) return;
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
        singleListenerInstance = this;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamaged(EntityDamageEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getEntity() instanceof LivingEntity le) || e.isCancelled()) return;
        EntityProperties properties = EntityCache.getAndCacheProperties(le);
        if (!properties.getPermanentPotionEffects().isEmpty() && !properties.getPermanentPotionEffects().getOrDefault("on_damaged", new ArrayList<>()).isEmpty()) {
            trigger(le, properties.getPermanentPotionEffects().getOrDefault("on_damaged", new ArrayList<>()));
        }

        String damageCause = EntityDamagedListener.getLastDamageCause(le);
        if (damageCause == null) return;
        CustomDamageType type = CustomDamageType.getCustomType(damageCause);
        if (type == null || !damageTypes.containsKey(type.getType())) return;
        String id = damageTypesToIDMappings.get(type.getType());
        if (id == null) return;
        EffectTrigger trigger = EffectTriggerRegistry.getTrigger(id);
        if (trigger == null || !trigger.shouldTrigger(le)) return;
        if (properties.getPermanentPotionEffects().getOrDefault(id, new ArrayList<>()).isEmpty()) return;
        trigger(le, properties.getPermanentPotionEffects().getOrDefault(id, new ArrayList<>()));
    }
}
