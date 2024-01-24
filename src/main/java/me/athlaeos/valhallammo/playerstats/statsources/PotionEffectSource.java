package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class PotionEffectSource implements AccumulativeStatSource {
    private final String potionEffect;
    private final boolean negative;

    public PotionEffectSource(String potionEffect, boolean negative){
        this.potionEffect = potionEffect;
        this.negative = negative;
    }

    public PotionEffectSource(String potionEffect){
        this.potionEffect = potionEffect;
        this.negative = false;
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof LivingEntity l){
            EntityProperties properties = EntityCache.getAndCacheProperties(l);
            CustomPotionEffect effect = properties.getActivePotionEffects().get(potionEffect);
            return effect == null ? 0 : (negative ? -effect.getAmplifier() : effect.getAmplifier());
        }
        return 0;
    }
}
