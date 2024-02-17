package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class TotalWeightlessArmorSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof LivingEntity v){
            double nonEquipmentArmor = AccumulativeStatManager.getCachedStats("WEIGHTLESS_ARMOR", v, 10000, true);

            double armorMultiplierBonus = 1 + AccumulativeStatManager.getCachedStats("ARMOR_MULTIPLIER_BONUS", v, 10000, true);

            return nonEquipmentArmor * armorMultiplierBonus;
        }
        return 0;
    }

    @Override
    public double fetch(Entity victim, Entity attackedBy, boolean use) {
        if (victim instanceof LivingEntity v && attackedBy instanceof LivingEntity a){
            double nonEquipmentArmor = Math.max(0, AccumulativeStatManager.getCachedRelationalStats("WEIGHTLESS_ARMOR", v, a, 10000, true));
            double armorFlatPenetration = AccumulativeStatManager.getCachedAttackerRelationalStats("ARMOR_FLAT_IGNORED", v, a, 10000, true);

            double armorMultiplierBonus = 1 + Math.max(0, AccumulativeStatManager.getCachedRelationalStats("ARMOR_MULTIPLIER_BONUS", v, a, 10000, true));
            double armorFractionPenetration = AccumulativeStatManager.getCachedAttackerRelationalStats("ARMOR_FRACTION_IGNORED", v, a, 10000, true);

            return (nonEquipmentArmor * armorMultiplierBonus) * (1 - armorFractionPenetration) - armorFlatPenetration;
        }
        return 0;
    }
}
