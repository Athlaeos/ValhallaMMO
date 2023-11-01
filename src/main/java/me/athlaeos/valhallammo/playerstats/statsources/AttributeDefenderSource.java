package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class AttributeDefenderSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    private final String attribute;
    private WeightClass weightClass = null;
    private String statPenalty = null;

    public AttributeDefenderSource(String attribute){
        this.attribute = attribute;
    }
    public AttributeDefenderSource penalty(String statPenalty){
        this.statPenalty = statPenalty;
        return this;
    }
    public AttributeDefenderSource weight(WeightClass weightClass){
        this.weightClass = weightClass;
        return this;
    }

    @Override
    public double fetch(Entity victim, Entity attackedBy, boolean use) {
        if (victim instanceof LivingEntity l){
            return EntityUtils.combinedAttributeValue(l, attribute, weightClass, statPenalty, false);
        }
        return 0;
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof LivingEntity l){
            return EntityUtils.combinedAttributeValue(l, attribute, weightClass, statPenalty, false);
        }
        return 0;
    }
}
