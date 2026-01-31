package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class TotalArmorSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof LivingEntity v){
            double lightArmor = Math.max(0, AccumulativeStatManager.getCachedStats("LIGHT_ARMOR", v, 10000, true));
            double heavyArmor = Math.max(0, AccumulativeStatManager.getCachedStats("HEAVY_ARMOR", v, 10000, true));
            double nonEquipmentArmor = Math.max(0, AccumulativeStatManager.getCachedStats("WEIGHTLESS_ARMOR", v, 10000, true));

            double lightArmorMultiplier = Math.max(0, AccumulativeStatManager.getCachedStats("LIGHT_ARMOR_MULTIPLIER", v, 10000, true));
            double heavyArmorMultiplier = Math.max(0, AccumulativeStatManager.getCachedStats("HEAVY_ARMOR_MULTIPLIER", v, 10000, true));
            double armorMultiplierBonus = Math.max(0, AccumulativeStatManager.getCachedStats("ARMOR_MULTIPLIER_BONUS", v, 10000, true));

            double totalLightArmor = Math.max(0, lightArmor * lightArmorMultiplier);
            double totalHeavyArmor = Math.max(0, heavyArmor * heavyArmorMultiplier);

            return Math.max(0, (totalLightArmor + totalHeavyArmor + nonEquipmentArmor) * (1 + armorMultiplierBonus));
        }
        return 0;
    }

    @Override
    public double fetch(Entity victim, Entity attackedBy, boolean use) {
        if (victim instanceof LivingEntity v && attackedBy instanceof LivingEntity a){
            double lightArmor = Math.max(0, AccumulativeStatManager.getCachedRelationalStats("LIGHT_ARMOR", v, a, 10000, true));
            double lightArmorFlatPenetration = AccumulativeStatManager.getCachedRelationalStats("LIGHT_ARMOR_FLAT_IGNORED", v, a, 10000, true);
            double heavyArmor = Math.max(0, AccumulativeStatManager.getCachedRelationalStats("HEAVY_ARMOR", v, a, 10000, true));
            double heavyArmorFlatPenetration = AccumulativeStatManager.getCachedRelationalStats("HEAVY_ARMOR_FLAT_IGNORED", v, a, 10000, true);
            double nonEquipmentArmor = Math.max(0, AccumulativeStatManager.getCachedRelationalStats("WEIGHTLESS_ARMOR", v, a, 10000, true));
            double armorFlatPenetration = AccumulativeStatManager.getCachedRelationalStats("ARMOR_FLAT_IGNORED", v, a, 10000, true);

            double lightArmorMultiplier = Math.max(0, AccumulativeStatManager.getCachedRelationalStats("LIGHT_ARMOR_MULTIPLIER", v, a, 10000, true));
            double lightArmorFractionPenetration = AccumulativeStatManager.getCachedRelationalStats("LIGHT_ARMOR_FRACTION_IGNORED", v, a, 10000, true);
            double heavyArmorMultiplier = Math.max(0, AccumulativeStatManager.getCachedRelationalStats("HEAVY_ARMOR_MULTIPLIER", v, a, 10000, true));
            double heavyArmorFractionPenetration = AccumulativeStatManager.getCachedRelationalStats("HEAVY_ARMOR_FRACTION_IGNORED", v, a, 10000, true);
            double armorMultiplierBonus = Math.max(0, AccumulativeStatManager.getCachedRelationalStats("ARMOR_MULTIPLIER_BONUS", v, a, 10000, true));
            double armorFractionPenetration = AccumulativeStatManager.getCachedRelationalStats("ARMOR_FRACTION_IGNORED", v, a, 10000, true);

            double totalLightArmor = Math.max(0, (lightArmor * lightArmorMultiplier) * (1 - lightArmorFractionPenetration) - lightArmorFlatPenetration);
            double totalHeavyArmor = Math.max(0, (heavyArmor * heavyArmorMultiplier) * (1 - heavyArmorFractionPenetration) - heavyArmorFlatPenetration);

            return Math.max(0, ((totalLightArmor + totalHeavyArmor + nonEquipmentArmor) * (1 + armorMultiplierBonus)) * (1 - armorFractionPenetration) - armorFlatPenetration);
        }
        return 0;
    }
}
