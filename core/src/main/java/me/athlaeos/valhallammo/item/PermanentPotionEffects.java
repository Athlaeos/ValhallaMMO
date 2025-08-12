package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTriggerRegistry;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class PermanentPotionEffects {
    private static final NamespacedKey PERMANENT_EFFECTS = new NamespacedKey(ValhallaMMO.getInstance(), "permanent_potion_effects");
    private static final AdditionType additionType = Catch.catchOrElse(
            () -> AdditionType.valueOf(ValhallaMMO.getPluginConfig().getString("permanent_effect_stacking", "HIGHEST")),
            AdditionType.HIGHEST,
            "Invalid permanent_effect_stacking value given in config.yml"
    );

    private static final Collection<UUID> entitiesWithPermanentEffects = new HashSet<>();

    private static final Map<String, Integer> triggerDelay = new HashMap<>();
    public static void initializeRunnable(){
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskTimer(ValhallaMMO.getInstance(), () -> {
            Map<String, Integer> triggersToDelay = new HashMap<>();
            for (UUID uuid : new HashSet<>(entitiesWithPermanentEffects)){
                Entity e = ValhallaMMO.getInstance().getServer().getEntity(uuid);
                if (e == null || !e.isValid() || e.isDead() || !(e instanceof LivingEntity l) || (e instanceof Player p && !p.isOnline())) {
                    entitiesWithPermanentEffects.remove(uuid);
                    if (e instanceof LivingEntity l) EffectTriggerRegistry.setEntityTriggerTypesAffected(l, new ArrayList<>());
                    continue;
                }
                EntityProperties properties = EntityCache.getAndCacheProperties(l);
                Map<String, List<PotionEffectWrapper>> permanentEffects = properties.getPermanentPotionEffects();
                if (permanentEffects.isEmpty()){
                    entitiesWithPermanentEffects.remove(uuid);
                    continue;
                }
                for (String triggerID : permanentEffects.keySet()){
                    EffectTrigger trigger = EffectTriggerRegistry.getTrigger(triggerID);
                    if (trigger instanceof EffectTrigger.ConstantTrigger constantTrigger) {
                        triggersToDelay.put(triggerID, constantTrigger.tickDelay());
                        if (triggerDelay.getOrDefault(triggerID, 0) > 0) continue;
                        if (!constantTrigger.shouldTrigger(l)) continue;
                        trigger.trigger(l, permanentEffects.getOrDefault(triggerID, new ArrayList<>()));
                    }
                }
            }
            for (String trigger : triggersToDelay.keySet()){
                int existingDelay = triggerDelay.getOrDefault(trigger, 0);
                if (existingDelay > 0) triggerDelay.put(trigger, existingDelay - 10);
                else triggerDelay.put(trigger, existingDelay + triggersToDelay.get(trigger));
            }
        }, 10L, 10L);
    }

    public static Map<String, List<PotionEffectWrapper>> fromString(String str){
        Map<String, List<PotionEffectWrapper>> effects = new HashMap<>();
        if (StringUtils.isEmpty(str)) return effects;
        String[] effectStrings = str.split(";");
        for (String s : effectStrings){
            String[] args = s.split(":");
            if (args.length <= 1) continue;
            double amplifier = Catch.catchOrElse(() -> Double.parseDouble(args[1]), -1D);
            if (amplifier < 0) continue;
            PotionEffectWrapper wrapper = Catch.catchOrElse(() -> PotionEffectRegistry.getEffect(args[0]), null);
            if (wrapper == null) continue;
            int duration = args.length > 2 ? Catch.catchOrElse(() -> Integer.parseInt(args[2]), 0) : (args[0].equals("NIGHT_VISION") ? 300 : 100);
            if (duration == 0) continue;
            wrapper.setDuration(duration);
            wrapper.setAmplifier(amplifier);

            String trigger = args.length > 3 ? args[3] : "constant";
            List<PotionEffectWrapper> existingWrappers = effects.getOrDefault(trigger, new ArrayList<>());
            existingWrappers.add(wrapper);
            effects.put(trigger, existingWrappers);
        }
        return effects;
    }

    public static Map<String, List<PotionEffectWrapper>> getPermanentPotionEffects(ItemMeta meta){
        return fromString(meta.getPersistentDataContainer().get(PERMANENT_EFFECTS, PersistentDataType.STRING));
    }

    public static void setPermanentPotionEffects(ItemMeta meta, Map<String, List<PotionEffectWrapper>> effects){
        List<String> dataStrings = new ArrayList<>();
        for (String trigger : effects.keySet()){
            for (PotionEffectWrapper wrapper : effects.getOrDefault(trigger, new ArrayList<>())) {
                dataStrings.add(wrapper.getEffect() + ":" + wrapper.getAmplifier() + ":" + wrapper.getDuration() + ":" + trigger);
            }
        }
        meta.getPersistentDataContainer().set(PERMANENT_EFFECTS, PersistentDataType.STRING, String.join(";", dataStrings));
    }

    public static Map<String, List<PotionEffectWrapper>> getCombinedEffects(List<Map<String, List<PotionEffectWrapper>>> effects){
        Map<String, Map<String, Double>> totalEffectAmplifiers = new HashMap<>();
        Map<String, Map<String, Integer>> totalEffectDurations = new HashMap<>();
        for (Map<String, List<PotionEffectWrapper>> effectMap : effects){
            for (String target : effectMap.keySet()){
                List<PotionEffectWrapper> targetEffects = effectMap.getOrDefault(target, new ArrayList<>());
                for (PotionEffectWrapper effect : targetEffects){
                    Map<String, Double> combinedAmplifiersOfTarget = totalEffectAmplifiers.getOrDefault(target, new HashMap<>());
                    if (effect.isVanilla()) combinedAmplifiersOfTarget.put(effect.getEffect(), additionType.get(combinedAmplifiersOfTarget.getOrDefault(effect.getEffect(), -1D), effect.getAmplifier() + 1) - 1);
                    else combinedAmplifiersOfTarget.put(effect.getEffect(), additionType.get(combinedAmplifiersOfTarget.getOrDefault(effect.getEffect(), 0D), effect.getAmplifier()));
                    totalEffectAmplifiers.put(target, combinedAmplifiersOfTarget);

                    Map<String, Integer> combinedDurationsOfTarget = totalEffectDurations.getOrDefault(target, new HashMap<>());
                    combinedDurationsOfTarget.put(effect.getEffect(), (int) AdditionType.HIGHEST.get(combinedDurationsOfTarget.getOrDefault(effect.getEffect(), 0), effect.getDuration()));
                    totalEffectDurations.put(target, combinedDurationsOfTarget);
                }
            }
        }
        Map<String, List<PotionEffectWrapper>> finalEffects = new HashMap<>();
        for (String trigger : totalEffectAmplifiers.keySet()){
            for (String type : totalEffectAmplifiers.getOrDefault(trigger, new HashMap<>()).keySet()){
                PotionEffectWrapper effect = Catch.catchOrElse(() -> PotionEffectRegistry.getEffect(type), null);
                if (effect == null) continue;
                double amplifier = totalEffectAmplifiers.getOrDefault(trigger, new HashMap<>()).getOrDefault(type, 0D);
                int duration = totalEffectDurations.getOrDefault(trigger, new HashMap<>()).getOrDefault(type, 0);
                if (duration == 0 || (!effect.isVanilla() && amplifier > -0.00001 && amplifier < 0.00001)) continue; // amplifier is practically 0
                effect.setAmplifier(amplifier);
                effect.setDuration(duration);
                List<PotionEffectWrapper> otherEffects = finalEffects.getOrDefault(trigger, new ArrayList<>());
                otherEffects.add(effect);
                finalEffects.put(trigger, otherEffects);
            }
        }
        return finalEffects;
    }

    public static void setHasPermanentEffects(LivingEntity l){
        entitiesWithPermanentEffects.add(l.getUniqueId());
    }

    public static void setHasNoPermanentEffects(LivingEntity l){
        entitiesWithPermanentEffects.remove(l.getUniqueId());
    }

    public static boolean hasPermanentEffects(LivingEntity l){
        return entitiesWithPermanentEffects.contains(l.getUniqueId());
    }

    private enum AdditionType{
        HIGHEST(Math::max),
        ADD(Double::sum);
        final Comparator compare;

        AdditionType(Comparator compare){
            this.compare = compare;
        }

        private double get(double i1, double i2){
            return compare.get(i1, i2);
        }
    }

    private interface Comparator{
        double get(double i1, double i2);
    }
}
