package me.athlaeos.valhallammo.potioneffects.effect_triggers;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.CustomDamageType;
import me.athlaeos.valhallammo.dom.MoonPhase;
import me.athlaeos.valhallammo.item.FoodClass;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations.*;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class EffectTriggerRegistry {
    private static final NamespacedKey KEY_COOLDOWN_PROPERTIES = ValhallaMMO.key("permanent_effects_cooldown_properties");
    private static final Map<UUID, Collection<String>> entitiesAffectedCache = new HashMap<>();
    private static final Map<String, EffectTrigger> registeredTriggers = new HashMap<>();
    private static final Map<String, EffectTrigger> configurableTriggers = new HashMap<>();

    static {
        register(new Constant());
        for (CustomDamageType type : CustomDamageType.getRegisteredTypes().values())
            register(new OnDamageTick(type)); // on_<type>_damage (type lowercase)
        register(new OnDamageTick(null)); // on_damaged
        register(new OnAttack(false, false)); // on_attack_inflict_enemy<effect:optional>
        register(new OnAttack(false, true)); // on_attacked_inflict_enemy<effect:optional>
        register(new OnAttack(true, false)); // on_attack_inflict_self<effect:optional>
        register(new OnAttack(true, true)); // on_attacked_inflict_self<effect:optional>
        register(new WhileMovementModifier(null)); // while_walking
        register(new WhileMovementModifier(true)); // while_sprinting
        register(new WhileMovementModifier(false)); // while_sneaking
        register(new WhileStandingStill(true)); // while_standing_still
        register(new WhileStandingStill(false)); // while_moving
        register(new WhileCombatStatus(true)); // while_in_combat
        register(new WhileCombatStatus(false)); // while_out_of_combat
        register(new WhileDayTimeOrLightExposure(null, true, null)); // while_light
        register(new WhileDayTimeOrLightExposure(true, null, null)); // while_day
        register(new WhileDayTimeOrLightExposure(null, false, null)); // while_dark
        register(new WhileDayTimeOrLightExposure(false, null, null)); // while_night
        register(new WhileDayTimeOrLightExposure(false, false, null)); // while_night_or_dark
        register(new WhileDayTimeOrLightExposure(false, true, null)); // while_night_or_light
        register(new WhileDayTimeOrLightExposure(true, false, null)); // while_day_or_dark
        register(new WhileDayTimeOrLightExposure(true, true, null)); // while_day_or_light
        register(new WhileDayTimeOrLightExposure(null, true, true)); // while_light_and_outside
        register(new WhileDayTimeOrLightExposure(null, true, false)); // while_light_and_sheltered
        register(new WhileDayTimeOrLightExposure(true, null, true)); // while_day_and_outside
        register(new WhileDayTimeOrLightExposure(true, null, false)); // while_day_and_sheltered
        register(new WhileDayTimeOrLightExposure(null, false, true)); // while_dark_and_outside
        register(new WhileDayTimeOrLightExposure(null, false, false)); // while_dark_and_sheltered
        register(new WhileDayTimeOrLightExposure(false, null, true)); // while_night_and_outside
        register(new WhileDayTimeOrLightExposure(false, null, false)); // while_night_and_sheltered
        register(new WhileDayTimeOrLightExposure(false, false, true)); // while_night_or_dark_and_outside
        register(new WhileDayTimeOrLightExposure(false, false, false)); // while_night_or_dark_and_sheltered
        register(new WhileDayTimeOrLightExposure(false, true, true)); // while_night_or_light_and_outside
        register(new WhileDayTimeOrLightExposure(false, true, false)); // while_night_or_light_and_sheltered
        register(new WhileDayTimeOrLightExposure(true, false, true)); // while_day_or_dark_and_outside
        register(new WhileDayTimeOrLightExposure(true, false, false)); // while_day_or_dark_and_sheltered
        register(new WhileDayTimeOrLightExposure(true, true, true)); // while_day_or_light_and_outside
        register(new WhileDayTimeOrLightExposure(true, true, false)); // while_day_or_light_and_sheltered
        register(new WhileSubmerged(true)); // while_in_water
        register(new WhileSubmerged(false)); // while_not_in_water
        register(new OnHealthReached()); // on_health_reached_<percentage>_<healed/harmed/both>
        register(new WhileHealthThreshold()); // while_health_<above/below>_<percentage>
        for (MoonPhase phase : MoonPhase.values()) register(new WhileMoonPhase(phase)); // while_moon_phase_<phase>
        for (FoodClass foodClass : FoodClass.values()) register(new OnConsumption(foodClass)); // on_eat_<food_class>
        register(new WhileInWorld(false)); // while_in_world_<world>
        register(new WhileInWorld(true)); // while_not_in_world_<world>
        register(new WhileMounted(false)); // while_mounted
        register(new WhileMounted(true)); // while_unmounted
        register(new WhileFlying(false)); // while_flying
        register(new WhileFlying(true)); // while_not_flying
        register(new WhileBlocking(false)); // while_blocking
        register(new WhileBlocking(true)); // while_not_blocking
        register(new OnParry(false, false)); // on_parry_inflict_enemy
        register(new OnParry(false, true)); // on_parried_inflict_enemy
        register(new OnParry(true, false)); // on_parry_inflict_self
        register(new OnParry(true, true)); // on_parried_inflict_self
        register(new OnCrit(false, false)); // on_crit_inflict_enemy
        register(new OnCrit(false, true)); // on_critted_inflict_enemy
        register(new OnCrit(true, false)); // on_crit_inflict_self
        register(new OnCrit(true, true)); // on_critted_inflict_self
        register(new OnBleed(false, false)); // on_bleed_inflict_enemy
        register(new OnBleed(false, true)); // on_bled_inflict_enemy
        register(new OnBleed(true, false)); // on_bleed_inflict_self
        register(new OnBleed(true, true)); // on_bled_inflict_self
        register(new OnStun(false, false)); // on_stun_inflict_enemy
        register(new OnStun(false, true)); // on_stunned_inflict_enemy
        register(new OnStun(true, false)); // on_stun_inflict_self
        register(new OnStun(true, true)); // on_stunned_inflict_self
        register(new OnExpCollected()); // on_collect_exp
        register(new OnBlockBreak()); // on_block_break_
        register(new OnBlockPlace()); // on_block_place_
        register(new WhilePotionAffected(false)); // while_potion_affected_
        register(new WhilePotionAffected(true)); // while_potion_unaffected_
        register(new OnPotionAffected()); // on_potion_affected_
        register(new WhileSwimming(false)); // while_swimming
        register(new WhileSwimming(true)); // while_not_swimming
        register(new WhileSubmergedOrRaining(true)); // while_touching_water
        register(new WhileSubmergedOrRaining(false)); // while_not_touching_water
        register(new OnKill()); // on_kill_<entities>
        register(new WhileOnFire(false)); // while_on_fire
        register(new WhileOnFire(true)); // while_not_on_fire
        register(new WhileRaining(false)); // while_raining
        register(new WhileRaining(true)); // while_not_raining
        register(new WhileOnBlock(false)); // while_on_blocks_<blocks>
        register(new WhileOnBlock(true)); // while_not_on_blocks_<blocks>
        register(new WhileInBiome(false)); // while_in_biomes_<blocks>
        register(new WhileInBiome(true)); // while_not_in_biomes_<blocks>
    }

    public static void register(EffectTrigger trigger){
        if (trigger.id() == null) return;
        if (trigger instanceof EffectTrigger.ConfigurableTrigger) configurableTriggers.put(trigger.id(), trigger);
        registeredTriggers.put(trigger.id(), trigger);
        trigger.onRegister();
    }

    public static EffectTrigger getTrigger(String id){
        EffectTrigger trigger = registeredTriggers.get(id);
        if (trigger == null){
            for (String triggerID : configurableTriggers.keySet()){
                if (id.startsWith(triggerID)) return configurableTriggers.get(triggerID);
            }
        }
        return trigger;
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

    public static EffectTrigger.CooldownProperties getCooldownProperties(ItemBuilder item){
        String data = item.getMeta().getPersistentDataContainer().get(KEY_COOLDOWN_PROPERTIES, PersistentDataType.STRING);
        if (data == null) return null;
        return EffectTrigger.CooldownProperties.deserialize(data);
    }

    public static void setCooldownProperties(ItemBuilder item, EffectTrigger.CooldownProperties properties){
        if (properties == null) item.getMeta().getPersistentDataContainer().remove(KEY_COOLDOWN_PROPERTIES);
        else item.stringTag(KEY_COOLDOWN_PROPERTIES, properties.serialize());
    }
}
