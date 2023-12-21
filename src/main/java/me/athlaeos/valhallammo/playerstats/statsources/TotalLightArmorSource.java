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
            double lightArmor = Math.max(0, AccumulativeStatManager.getCachedStats("LIGHT_ARMOR", v, 10000, true));

            double lightArmorMultiplier = 1 + Math.max(0, AccumulativeStatManager.getCachedStats("LIGHT_ARMOR_MULTIPLIER", v, 10000, true));
            double armorMultiplierBonus = 1 + Math.max(0, AccumulativeStatManager.getCachedStats("ARMOR_MULTIPLIER_BONUS", v, 10000, true));

            double totalLightArmor = Math.max(0, lightArmor * lightArmorMultiplier);

            return totalLightArmor * armorMultiplierBonus;
        }
        return 0;
    }

    @Override
    public double fetch(Entity victim, Entity attackedBy, boolean use) {
        if (victim instanceof LivingEntity v && attackedBy instanceof LivingEntity a){
            double lightArmor = Math.max(0, AccumulativeStatManager.getCachedRelationalStats("LIGHT_ARMOR", v, a, 10000, true));
            double lightArmorFlatPenetration = AccumulativeStatManager.getCachedAttackerRelationalStats("LIGHT_ARMOR_FLAT_IGNORED", v, a, 10000, true);

            double lightArmorMultiplier = 1 + Math.max(0, AccumulativeStatManager.getCachedRelationalStats("LIGHT_ARMOR_MULTIPLIER", v, a, 10000, true));
            double lightArmorFractionPenetration = AccumulativeStatManager.getCachedAttackerRelationalStats("LIGHT_ARMOR_FRACTION_IGNORED", v, a, 10000, true);
            double armorMultiplierBonus = 1 + Math.max(0, AccumulativeStatManager.getCachedRelationalStats("ARMOR_MULTIPLIER_BONUS", v, a, 10000, true));
            double armorFractionPenetration = AccumulativeStatManager.getCachedAttackerRelationalStats("ARMOR_FRACTION_IGNORED", v, a, 10000, true);

            double totalLightArmor = (lightArmor * lightArmorMultiplier) * (1 - lightArmorFractionPenetration) - lightArmorFlatPenetration;

            return (totalLightArmor * armorMultiplierBonus) * (1 - armorFractionPenetration);
        }
        return 0;
    }
}
