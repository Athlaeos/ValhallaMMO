package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class ResistanceArmorWeightClassSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    private final String key;

    public ResistanceArmorWeightClassSource(String key){
        this.key = key;
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof LivingEntity l){
            double heavyResistance = ValhallaMMO.getPluginConfig().getDouble("heavy_" + key);
            double lightResistance = ValhallaMMO.getPluginConfig().getDouble("light_" + key);
            double weightlessResistance = ValhallaMMO.getPluginConfig().getDouble("weightless_" + key);
            EntityProperties properties = EntityCache.getAndCacheProperties(l);
            return heavyResistance * properties.getHeavyArmorCount() +
                    lightResistance * properties.getLightArmorCount() +
                    weightlessResistance * properties.getWeightlessArmorCount();
        }
        return 0;
    }

    @Override
    public double fetch(Entity victim, Entity attackedBy, boolean use) {
        return fetch(victim, use);
    }
}
