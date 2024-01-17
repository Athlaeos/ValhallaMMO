package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.item.ArmorSet;
import me.athlaeos.valhallammo.item.ArmorSetRegistry;
import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;

public class VanillaAttributeDefenderSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    private final Attribute attribute;
    private final AttributeModifier.Operation operation;
    private WeightClass weightClass = null;
    private String statPenalty = null;

    public VanillaAttributeDefenderSource(Attribute attribute, AttributeModifier.Operation operation){
        this.attribute = attribute;
        this.operation = operation;
    }
    public VanillaAttributeDefenderSource penalty(String statPenalty){
        this.statPenalty = statPenalty;
        return this;
    }
    public VanillaAttributeDefenderSource weight(WeightClass weightClass){
        this.weightClass = weightClass;
        return this;
    }

    @Override
    public double fetch(Entity victim, Entity attackedBy, boolean use) {
        if (victim instanceof LivingEntity l){
            double value = EntityUtils.combinedAttributeValue(l, attribute, operation, weightClass, statPenalty, false);

            Collection<ArmorSet> activeSets = ArmorSetRegistry.getActiveArmorSets(l);
            for (ArmorSet set : activeSets){
                value += set.getSetBonus().getOrDefault(attribute.toString(), 0D);
            }
            return value;
        }
        return 0;
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof LivingEntity l){
            double value = EntityUtils.combinedAttributeValue(l, attribute, operation, weightClass, statPenalty, false);

            Collection<ArmorSet> activeSets = ArmorSetRegistry.getActiveArmorSets(l);
            for (ArmorSet set : activeSets){
                value += set.getSetBonus().getOrDefault(attribute.toString(), 0D);
            }
            return value;
        }
        return 0;
    }
}
