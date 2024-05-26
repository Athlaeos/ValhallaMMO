package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.item.ArmorSet;
import me.athlaeos.valhallammo.item.ArmorSetRegistry;
import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;

public class AttributeDefenderSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    private final String attribute;
    private WeightClass weightClass = null;
    private String statPenalty = null;
    private boolean negative = false;

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
    public AttributeDefenderSource negative(){
        this.negative = true;
        return this;
    }

    @Override
    public double fetch(Entity victim, Entity attackedBy, boolean use) {
        if (victim instanceof LivingEntity l){
            double value = EntityUtils.combinedAttributeValue(l, attribute, weightClass, statPenalty, false);

            Collection<ArmorSet> activeSets = ArmorSetRegistry.getActiveArmorSets(l);
            for (ArmorSet set : activeSets){
                value += set.getSetBonus().getOrDefault(attribute, 0D);
            }
            return (negative ? -1 : 1) * value;
        }
        return 0;
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof LivingEntity l){
            double value = EntityUtils.combinedAttributeValue(l, attribute, weightClass, statPenalty, false);

            Collection<ArmorSet> activeSets = ArmorSetRegistry.getActiveArmorSets(l);
            for (ArmorSet set : activeSets){
                value += set.getSetBonus().getOrDefault(attribute, 0D);
            }
            return (negative ? -1 : 1) * value;
        }
        return 0;
    }
}
