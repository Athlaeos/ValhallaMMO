package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.item.ArmorSet;
import me.athlaeos.valhallammo.item.ArmorSetRegistry;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;

public class SetBonusSource implements AccumulativeStatSource {
    private final String attribute;
    private final boolean negative;

    /**
     * It's important to note {@link AttributeSource} also includes set bonuses, this source is to be used for stats that
     * don't use AttributeSource to calculate stats (such as vanilla attribute stats)
     */
    public SetBonusSource(String attribute){
        this.attribute = attribute;
        this.negative = false;
    }
    /**
     * It's important to note {@link AttributeSource} also includes set bonuses, this source is to be used for stats that
     * don't use AttributeSource to calculate stats (such as vanilla attribute stats)
     */
    public SetBonusSource(String attribute, boolean negative){
        this.attribute = attribute;
        this.negative = negative;
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof LivingEntity l){
            double value = 0;

            Collection<ArmorSet> activeSets = ArmorSetRegistry.getActiveArmorSets(l);
            for (ArmorSet set : activeSets){
                value += (negative ? -1 : 1) * set.getSetBonus().getOrDefault(attribute, 0D);
            }
            return value;
        }
        return 0;
    }
}
