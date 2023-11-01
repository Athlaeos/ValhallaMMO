package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class PotionEffectAttackerSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    private final String potionEffect;
    private final boolean negative;

    public PotionEffectAttackerSource(String potionEffect, boolean negative){
        this.potionEffect = potionEffect;
        this.negative = negative;
    }

    public PotionEffectAttackerSource(String potionEffect){
        this.potionEffect = potionEffect;
        this.negative = false;
    }

    @Override
    public double fetch(Entity victim, Entity attackedBy, boolean use) {
        if (attackedBy instanceof LivingEntity l){
            EntityProperties properties = EntityCache.getAndCacheProperties(l);
            CustomPotionEffect effect = properties.getActivePotionEffects().get(potionEffect);
            return effect == null ? 0 : (negative ? -effect.getAmplifier() : effect.getAmplifier());
        }
        return 0;
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        return 0;
    }
}
