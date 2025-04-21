package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class TotalLightArmorSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof LivingEntity v){
            double lightArmor = AccumulativeStatManager.getCachedStats("LIGHT_ARMOR", v, 10000, true);

            double lightArmorMultiplier = 1 + AccumulativeStatManager.getCachedStats("LIGHT_ARMOR_MULTIPLIER", v, 10000, true);
            double armorMultiplierBonus = 1 + AccumulativeStatManager.getCachedStats("ARMOR_MULTIPLIER_BONUS", v, 10000, true);

            double totalLightArmor = lightArmor * lightArmorMultiplier;

            return totalLightArmor * armorMultiplierBonus;
        }
        return 0;
    }

    @Override
    public double fetch(Entity victim, Entity a, boolean use) {
        if (victim instanceof LivingEntity v){
            double baseLightArmor = fetch(victim, use);
            double lightArmorFlatPenetration = AccumulativeStatManager.getCachedAttackerRelationalStats("LIGHT_ARMOR_FLAT_IGNORED", v, a, 10000, true);
            double lightArmorFractionPenetration = AccumulativeStatManager.getCachedAttackerRelationalStats("LIGHT_ARMOR_FRACTION_IGNORED", v, a, 10000, true);
            return (baseLightArmor * (1 - lightArmorFractionPenetration)) - lightArmorFlatPenetration;
        }
        return 0;
    }
}
