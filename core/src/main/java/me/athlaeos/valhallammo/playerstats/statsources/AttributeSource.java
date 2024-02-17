package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.item.ArmorSet;
import me.athlaeos.valhallammo.item.ArmorSetRegistry;
import me.athlaeos.valhallammo.item.ItemSkillRequirements;
import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;

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
            double value = (negative ? -1 : 1) * EntityUtils.combinedAttributeValue(l, attribute, weightClass, statPenalty, false);

            Collection<ArmorSet> activeSets = ArmorSetRegistry.getActiveArmorSets(l);
            for (ArmorSet set : activeSets){
                value += (negative ? -1 : 1) * set.getSetBonus().getOrDefault(attribute, 0D);
            }

            return value;
        }
        return 0;
    }
}
