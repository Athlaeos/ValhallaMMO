package me.athlaeos.valhallammo.potioneffects.effect_triggers;

import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public interface EffectTrigger {
    String id();

    void onRegister();

    default void trigger(LivingEntity entity, List<PotionEffectWrapper> effects){
        for (PotionEffectWrapper effectWrapper : effects){
            if (effectWrapper.isVanilla()) entity.addPotionEffect(new PotionEffect(effectWrapper.getVanillaEffect(), (int) effectWrapper.getDuration(), (int) effectWrapper.getAmplifier(), true, false, false));
            else {
                CustomPotionEffect effect = new CustomPotionEffect(effectWrapper, (int) effectWrapper.getDuration(), effectWrapper.getAmplifier());
                PotionEffectRegistry.addEffect(entity, null, effect, false, 1, EntityPotionEffectEvent.Cause.PLUGIN);
            }
        }
    }

    default boolean shouldTrigger(LivingEntity entity){
        return EffectTriggerRegistry.isEntityAffectedByTrigger(entity, id());
    }

    interface ConstantTrigger extends EffectTrigger{
        int tickDelay();
    }
}
