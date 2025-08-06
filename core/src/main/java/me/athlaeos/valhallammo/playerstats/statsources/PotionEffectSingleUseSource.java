package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;

public class PotionEffectSingleUseSource implements AccumulativeStatSource {
    private final String potionEffect;
    private final boolean negative;

    public PotionEffectSingleUseSource(String potionEffect){
        this.potionEffect = potionEffect;
        this.negative = false;
    }
    public PotionEffectSingleUseSource(String potionEffect, boolean negative){
        this.potionEffect = potionEffect;
        this.negative = negative;
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof LivingEntity l){
            EntityProperties properties = EntityCache.getAndCacheProperties(l);
            CustomPotionEffect effect = properties.getActivePotionEffects().get(potionEffect);
            if (effect == null || (effect.getEffectiveUntil() != -1 && effect.getEffectiveUntil() <= System.currentTimeMillis())) return 0;
            double amplifier = negative ? -effect.getAmplifier() : effect.getAmplifier();
            if (use) {
                effect.setEffectiveUntil(0);
                PotionEffectRegistry.addEffect(l, null, effect, true, 1, EntityPotionEffectEvent.Cause.EXPIRATION, EntityPotionEffectEvent.Action.REMOVED);
            }
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> AccumulativeStatManager.uncache(l), 1L);
            return amplifier;
        }
        return 0;
    }
}
