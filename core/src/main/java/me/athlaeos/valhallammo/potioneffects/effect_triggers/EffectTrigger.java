package me.athlaeos.valhallammo.potioneffects.effect_triggers;

import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public interface EffectTrigger {
    String id();

    void onRegister();

    default void trigger(LivingEntity entity, String id, CooldownProperties cooldown, List<PotionEffectWrapper> effects){
        if (cooldown != null && cooldown.cooldown > 0 && !Timer.isCooldownPassed(entity.getUniqueId(), id)) return;
        for (PotionEffectWrapper effectWrapper : effects){
            if (effectWrapper.isVanilla()) entity.addPotionEffect(new PotionEffect(effectWrapper.getVanillaEffect(), (int) effectWrapper.getDuration(), (int) effectWrapper.getAmplifier(), true, false, false));
            else {
                CustomPotionEffect effect = new CustomPotionEffect(effectWrapper, (int) effectWrapper.getDuration(), effectWrapper.getAmplifier());
                PotionEffectRegistry.addEffect(entity, null, effect, false, 1, EntityPotionEffectEvent.Cause.PLUGIN);
            }
        }
        if (cooldown != null && cooldown.cooldown > 0) applyCooldown(entity, id, cooldown.cooldown, cooldown.cdrAffected);
    }

    default void trigger(LivingEntity entity, CooldownProperties cooldown, List<PotionEffectWrapper> effects){
        trigger(entity, id(), cooldown, effects);
    }

    default void applyCooldown(LivingEntity entity, String id, int cooldown, boolean cdrAffected){
        if (cdrAffected) Timer.setCooldownIgnoreIfPermission(entity, cooldown, id);
        else Timer.setCooldown(entity.getUniqueId(), cooldown, id);
    }

    default boolean shouldTrigger(LivingEntity entity){
        return EffectTriggerRegistry.isEntityAffectedByTrigger(entity, id());
    }

    interface ConstantTrigger extends EffectTrigger{
        int tickDelay();
    }

    interface ConstantConfigurableTrigger extends ConfigurableTrigger, ConstantTrigger{}

    interface ConfigurableTrigger extends EffectTrigger{
        String isValid(String arg);

        String getUsage();

        default boolean shouldTrigger(LivingEntity entity, String arg){
            return EffectTrigger.super.shouldTrigger(entity);
        }

        default String getArg(String rawID){
            return rawID.replaceFirst(id(), "");
        }

        default String asLore(String rawID){
            return getArg(rawID);
        }
    }

    record CooldownProperties(boolean cdrAffected, int cooldown){
        public String serialize(){
            return String.format("%s;%d", cdrAffected, cooldown);
        }

        public static CooldownProperties deserialize(String string){
            String[] args = string.split(";");
            return new CooldownProperties(args[0].equals("true"), Integer.parseInt(args[1]));
        }
    }
}
