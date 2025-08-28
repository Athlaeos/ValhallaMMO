package me.athlaeos.valhallammo.potioneffects.effect_triggers;

import me.athlaeos.valhallammo.dom.CustomDamageType;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations.*;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public class EffectTriggerRegistry {
    private static final Map<UUID, Collection<String>> entitiesAffectedCache = new HashMap<>();
    private static final Map<String, EffectTrigger> registeredTriggers = new HashMap<>();

    static {
        register(new Constant());
        for (CustomDamageType type : CustomDamageType.getRegisteredTypes().values())
            register(new OnDamageTick(type)); // on_<type>_damage (type lowercase)
        register(new OnDamageTick(null)); // on_damaged
        register(new OnAttack(false, false)); // on_attack_inflict_enemy
        register(new OnAttack(false, true)); // on_attacked_inflict_enemy
        register(new OnAttack(true, false)); // on_attack_inflict_self
        register(new OnAttack(true, true)); // on_attacked_inflict_self
        register(new WhileMovementModifier(null)); // while_walking
        register(new WhileMovementModifier(true)); // while_sprinting
        register(new WhileMovementModifier(false)); // while_sneaking
        register(new WhileStandingStill(true)); // while_standing_still
        register(new WhileStandingStill(false)); // while_moving
        register(new WhileCombatStatus(true)); // while_in_combat
        register(new WhileCombatStatus(false)); // while_out_of_combat
        register(new DayTimeOrLightExposure(null, true, null)); // while_light
        register(new DayTimeOrLightExposure(true, null, null)); // while_day
        register(new DayTimeOrLightExposure(null, false, null)); // while_dark
        register(new DayTimeOrLightExposure(false, null, null)); // while_night
        register(new DayTimeOrLightExposure(false, false, null)); // while_night_or_dark
        register(new DayTimeOrLightExposure(false, true, null)); // while_night_or_light
        register(new DayTimeOrLightExposure(true, false, null)); // while_day_or_dark
        register(new DayTimeOrLightExposure(true, true, null)); // while_day_or_light
        register(new DayTimeOrLightExposure(null, true, true)); // while_light_and_outside
        register(new DayTimeOrLightExposure(null, true, false)); // while_light_and_sheltered
        register(new DayTimeOrLightExposure(true, null, true)); // while_day_and_outside
        register(new DayTimeOrLightExposure(true, null, false)); // while_day_and_sheltered
        register(new DayTimeOrLightExposure(null, false, true)); // while_dark_and_outside
        register(new DayTimeOrLightExposure(null, false, false)); // while_dark_and_sheltered
        register(new DayTimeOrLightExposure(false, null, true)); // while_night_and_outside
        register(new DayTimeOrLightExposure(false, null, false)); // while_night_and_sheltered
        register(new DayTimeOrLightExposure(false, false, true)); // while_night_or_dark_and_outside
        register(new DayTimeOrLightExposure(false, false, false)); // while_night_or_dark_and_sheltered
        register(new DayTimeOrLightExposure(false, true, true)); // while_night_or_light_and_outside
        register(new DayTimeOrLightExposure(false, true, false)); // while_night_or_light_and_sheltered
        register(new DayTimeOrLightExposure(true, false, true)); // while_day_or_dark_and_outside
        register(new DayTimeOrLightExposure(true, false, false)); // while_day_or_dark_and_sheltered
        register(new DayTimeOrLightExposure(true, true, true)); // while_day_or_light_and_outside
        register(new DayTimeOrLightExposure(true, true, false)); // while_day_or_light_and_sheltered
        register(new Submerged(true)); // while_in_water
        register(new Submerged(false)); // while_not_in_water
    }

    public static void register(EffectTrigger trigger){
        if (trigger.id() == null) return;
        registeredTriggers.put(trigger.id(), trigger);
        trigger.onRegister();
    }

    public static EffectTrigger getTrigger(String id){
        return registeredTriggers.get(id);
    }

    public static Map<String, EffectTrigger> getRegisteredTriggers() {
        return new HashMap<>(registeredTriggers);
    }

    public static boolean isEntityAffectedByTrigger(LivingEntity entity, String triggerType){
        return entitiesAffectedCache.getOrDefault(entity.getUniqueId(), new HashSet<>()).contains(triggerType);
    }

    public static void setEntityAffected(LivingEntity entity, String triggerType, boolean affected){
        Collection<String> affectedByTriggers = entitiesAffectedCache.getOrDefault(entity.getUniqueId(), new HashSet<>());
        if (affected) affectedByTriggers.add(triggerType);
        else affectedByTriggers.remove(triggerType);
        if (affectedByTriggers.isEmpty()) entitiesAffectedCache.remove(entity.getUniqueId());
        else entitiesAffectedCache.put(entity.getUniqueId(), affectedByTriggers);
    }

    public static void setEntityTriggerTypesAffected(LivingEntity entity, Collection<String> affectedByTriggers){
        if (affectedByTriggers.isEmpty()) entitiesAffectedCache.remove(entity.getUniqueId());
        else entitiesAffectedCache.put(entity.getUniqueId(), affectedByTriggers);
    }
}
