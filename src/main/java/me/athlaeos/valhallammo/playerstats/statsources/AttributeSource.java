package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class AttributeSource implements AccumulativeStatSource {
    private final String attribute;
    private WeightClass weightClass = null;
    private String statPenalty = null;
    private final boolean negative;

    public AttributeSource(String attribute){
        this.attribute = attribute;
        this.negative = false;
    }
    public AttributeSource(String attribute, boolean negative){
        this.attribute = attribute;
        this.negative = negative;
    }
    public AttributeSource penalty(String statPenalty){
        this.statPenalty = statPenalty;
        return this;
    }
    public AttributeSource weight(WeightClass weightClass){
        this.weightClass = weightClass;
        return this;
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof LivingEntity l){
            return (negative ? -1 : 1) * EntityUtils.combinedAttributeValue(l, attribute, weightClass, statPenalty, false);
        }
        return 0;
    }
}
