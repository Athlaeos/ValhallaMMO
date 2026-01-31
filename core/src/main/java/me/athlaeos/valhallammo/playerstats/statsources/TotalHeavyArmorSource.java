package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class TotalHeavyArmorSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof LivingEntity v){
            double heavyArmor = AccumulativeStatManager.getCachedStats("HEAVY_ARMOR", v, 10000, true);

            double heavyArmorMultiplier = 1 + AccumulativeStatManager.getCachedStats("HEAVY_ARMOR_MULTIPLIER", v, 10000, true);
            double armorMultiplierBonus = 1 + AccumulativeStatManager.getCachedStats("ARMOR_MULTIPLIER_BONUS", v, 10000, true);

            double totalHeavyArmor = heavyArmor * heavyArmorMultiplier;

            return totalHeavyArmor * armorMultiplierBonus;
        }
        return 0;
    }

    @Override
    public double fetch(Entity victim, Entity a, boolean use) {
        if (victim instanceof LivingEntity v){
            double baseHeavyArmor = fetch(victim, use);
            double heavyArmorFlatPenetration = AccumulativeStatManager.getCachedRelationalStats("HEAVY_ARMOR_FLAT_IGNORED", v, a, 10000, true);
            double heavyArmorFractionPenetration = AccumulativeStatManager.getCachedRelationalStats("HEAVY_ARMOR_FRACTION_IGNORED", v, a, 10000, true);
            return (baseHeavyArmor * (1 - heavyArmorFractionPenetration)) - heavyArmorFlatPenetration;
        }
        return 0;
    }
}
