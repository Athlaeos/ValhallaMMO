package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class PermanentPotionEffects {
    private static final NamespacedKey PERMANENT_EFFECTS = new NamespacedKey(ValhallaMMO.getInstance(), "permanent_potion_effects");
    private static final AdditionType additionType = Catch.catchOrElse(
            () -> AdditionType.valueOf(ValhallaMMO.getPluginConfig().getString("permanent_effect_stacking", "HIGHEST")),
            AdditionType.HIGHEST,
            "Invalid permanent_effect_stacking value given in config.yml"
    );

    private static final Collection<UUID> entitiesWithPermanentEffects = new HashSet<>();

    public static void initializeRunnable(){
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskTimer(ValhallaMMO.getInstance(), () -> {
            for (UUID uuid : new HashSet<>(entitiesWithPermanentEffects)){
                Entity e = ValhallaMMO.getInstance().getServer().getEntity(uuid);
                if (e == null || !e.isValid() || e.isDead() || !(e instanceof LivingEntity l) || (e instanceof Player p && !p.isOnline())) {
                    entitiesWithPermanentEffects.remove(uuid);
                    continue;
                }
                EntityProperties properties = EntityCache.getAndCacheProperties(l);
                List<PotionEffect> effects = properties.getPermanentPotionEffects();
                if (effects.isEmpty()){
                    entitiesWithPermanentEffects.remove(uuid);
                    continue;
                }
                for (PotionEffect effect : effects) l.addPotionEffect(effect);
            }
        }, 80L, 80L);
    }

    public static List<PotionEffect> fromString(String str){
        List<PotionEffect> effects = new ArrayList<>();
        if (StringUtils.isEmpty(str)) return effects;
        String[] effectStrings = str.split(";");
        for (String s : effectStrings){
            String[] args = s.split(":");
            if (args.length <= 1) continue;
            PotionEffectType type = PotionEffectType.getByName(args[0]);
            if (type == null) continue;
            int amplifier = Catch.catchOrElse(() -> Integer.parseInt(args[1]), -1);
            if (amplifier < 0) continue;
            effects.add(new PotionEffect(type, type == PotionEffectType.NIGHT_VISION ? 300 : 100, amplifier, false, false, false));
        }
        return effects;
    }

    public static List<PotionEffect> getPermanentPotionEffects(ItemMeta meta){
        return fromString(meta.getPersistentDataContainer().get(PERMANENT_EFFECTS, PersistentDataType.STRING));
    }

    public static void setPermanentPotionEffects(ItemMeta meta, List<PotionEffect> effects){
        meta.getPersistentDataContainer().set(PERMANENT_EFFECTS, PersistentDataType.STRING, effects.stream().map(p ->
                p.getType().getName() + ":" + p.getAmplifier()).collect(Collectors.joining(";"))
        );
    }

    public static List<PotionEffect> getCombinedEffects(List<List<PotionEffect>> effects){
        Map<PotionEffectType, Integer> totalEffects = new HashMap<>();
        for (List<PotionEffect> effectList : effects){
            for (PotionEffect effect : effectList){
                totalEffects.put(effect.getType(), additionType.get(totalEffects.getOrDefault(effect.getType(), -1), effect.getAmplifier() + 1) - 1);
            }
        }
        List<PotionEffect> finalEffects = new ArrayList<>();
        for (PotionEffectType type : totalEffects.keySet()){
            finalEffects.add(
                    new PotionEffect(type, type == PotionEffectType.NIGHT_VISION ? 300 : 100, totalEffects.get(type), false, false, false)
            );
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
        ADD(Integer::sum);
        Comparator compare;

        AdditionType(Comparator compare){
            this.compare = compare;
        }

        private int get(int i1, int i2){
            return compare.get(i1, i2);
        }
    }

    private interface Comparator{
        int get(int i1, int i2);
    }
}
