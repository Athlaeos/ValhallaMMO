package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.ItemSkillRequirements;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

public class AttributeAttackerSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    private final String attribute;
    private WeightClass weightClass = null;
    private String statPenalty = null;
    private final boolean negative;
    private boolean ignoreProjectiles = true;

    public AttributeAttackerSource(String attribute){
        this.attribute = attribute;
        this.negative = false;
    }
    public AttributeAttackerSource(String attribute, boolean negative){
        this.attribute = attribute;
        this.negative = negative;
    }
    /**
     * Heavily decreases the attributes gotten if the player fails to meet the item's skill requirements. The statPenalties to choose from are defined in config.yml
     */
    public AttributeAttackerSource penalty(String statPenalty){
        this.statPenalty = statPenalty;
        return this;
    }
    /**
     * Causes the source to only consider equipment if it also matches the appropriate weight class (excluding held items)
     */
    public AttributeAttackerSource weight(WeightClass weightClass){
        this.weightClass = weightClass;
        return this;
    }
    /**
     * Causes the source to also scan a projectile's attributes (if any)
     */
    public AttributeAttackerSource proj(){
        this.ignoreProjectiles = false;
        return this;
    }

    @Override
    public double fetch(Entity victim, Entity attackedBy, boolean use) {
        double value = 0;
        LivingEntity trueAttacker = attackedBy instanceof LivingEntity a ? a : (attackedBy instanceof Projectile p && p.getShooter() instanceof LivingEntity l ? l : null);
        if (trueAttacker == null) return value;

        // gather stats from real attacker, and include only main hand if it was a melee attack (not a projectile)
        value += (negative ? -1 : 1) * EntityUtils.combinedAttributeValue(trueAttacker, attribute, weightClass, statPenalty, !(attackedBy instanceof Projectile));

        if (!ignoreProjectiles && attackedBy instanceof Projectile p){
            ItemBuilder item = ItemUtils.getStoredItem(p);
            if (item == null) return value;
            AttributeWrapper wrapper = ItemAttributesRegistry.getAnyAttribute(item.getMeta(), attribute);
            if (wrapper == null) return value;
            double multiplier = 1;
            if (attribute != null && trueAttacker instanceof Player player) multiplier += ItemSkillRequirements.getPenalty(player, item.getMeta(), attribute);
            value += wrapper.getValue() * multiplier;
        }
        return value;
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        return 0;
    }
}
