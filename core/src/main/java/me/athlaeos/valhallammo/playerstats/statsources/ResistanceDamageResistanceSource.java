package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ResistanceDamageResistanceSource implements AccumulativeStatSource {
    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof LivingEntity l){
            double resistancePerLevel = ValhallaMMO.getPluginConfig().getDouble("buff_resistance_reduction");
            PotionEffect effect = l.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            if (effect != null) return (effect.getAmplifier() + 1) * resistancePerLevel;
        }
        return 0;
    }
}
