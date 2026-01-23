package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.item.*;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.playerstats.CacheableRelationalStatSource;
import me.athlaeos.valhallammo.playerstats.StatCacheResetCause;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeAttackerSource implements CacheableRelationalStatSource {
    private static final int CACHE_DURATION_MS = 10000;
    private static final Map<UUID, Map<UUID, Map<String, Map.Entry<Long, Double>>>> RELATIONAL_CACHE = new ConcurrentHashMap<>();
    // primary|secondary|attribute|cache time|cached value
    private static final Map<UUID, Map<String, Map.Entry<Long, Double>>> NON_RELATIONAL_CACHE = new ConcurrentHashMap<>();

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
        value += (negative ? -1 : 1) * (!(trueAttacker instanceof Projectile) ?
                EntityUtils.combinedAttackerAttributeValue(trueAttacker, attribute, weightClass, statPenalty, true) :
                EntityUtils.combinedAttributeValue(trueAttacker, attribute, weightClass, statPenalty, false));

        if (!ignoreProjectiles && attackedBy instanceof Projectile p){
            ItemBuilder item = ItemUtils.getStoredItem(p);
            if (item == null) return value;
            AttributeWrapper wrapper = ItemAttributesRegistry.getAnyAttribute(item.getMeta(), attribute);
            if (wrapper == null) return value;
            double multiplier = 1;
            if (attribute != null && trueAttacker instanceof Player player) multiplier += ItemSkillRequirements.getPenalty(player, item, attribute);
            value += wrapper.getValue() * multiplier;
        }

        Collection<ArmorSet> activeSets = ArmorSetRegistry.getActiveArmorSets(trueAttacker);
        for (ArmorSet set : activeSets){
            value += (negative ? -1 : 1) * set.getSetBonus().getOrDefault(attribute, 0D);
        }
        return value;
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        return fetch(null, statPossessor, use);
    }

    @Override
    public double get(Entity primaryEntity, Entity secondaryEntity) {
        if (primaryEntity == null) return get(secondaryEntity);
        Map<UUID, Map<String, Map.Entry<Long, Double>>> attackerEntries = RELATIONAL_CACHE.getOrDefault(primaryEntity.getUniqueId(), new ConcurrentHashMap<>());
        Map<String, Map.Entry<Long, Double>> statEntries = attackerEntries.getOrDefault(secondaryEntity.getUniqueId(), new ConcurrentHashMap<>());
        Map.Entry<Long, Double> entry = statEntries.get(this.attribute);
        if (entry != null && entry.getKey() + CACHE_DURATION_MS > System.currentTimeMillis()) return entry.getValue();
        entry = Map.entry(System.currentTimeMillis(), fetch(primaryEntity, secondaryEntity, false));
        statEntries.put(attribute, entry);
        attackerEntries.put(secondaryEntity.getUniqueId(), statEntries);
        RELATIONAL_CACHE.put(primaryEntity.getUniqueId(), attackerEntries);
        return entry.getValue();
    }

    @Override
    public void set(Entity primaryEntity, Entity secondaryEntity, double value) {
        Map<UUID, Map<String, Map.Entry<Long, Double>>> attackerEntries = RELATIONAL_CACHE.getOrDefault(primaryEntity.getUniqueId(), new ConcurrentHashMap<>());
        Map<String, Map.Entry<Long, Double>> statEntries = attackerEntries.getOrDefault(secondaryEntity.getUniqueId(), new ConcurrentHashMap<>());
        Map.Entry<Long, Double> entry = Map.entry(System.currentTimeMillis(), fetch(primaryEntity, secondaryEntity, false));
        statEntries.put(attribute, entry);
        attackerEntries.put(secondaryEntity.getUniqueId(), statEntries);
        RELATIONAL_CACHE.put(primaryEntity.getUniqueId(), attackerEntries);
    }

    @Override
    public void reset(UUID uuid) {
        // removing all references to the uuid from all other entries in the cache
        for (UUID id : RELATIONAL_CACHE.keySet()){
            Map<UUID, Map<String, Map.Entry<Long, Double>>> attackerEntries = RELATIONAL_CACHE.getOrDefault(id, new ConcurrentHashMap<>());
            attackerEntries.remove(uuid);
            RELATIONAL_CACHE.put(id, attackerEntries);
        }
        // removing the uuid mapping itself from the cache
        RELATIONAL_CACHE.remove(uuid);
    }

    @Override
    public double get(Entity entity) {
        Map<String, Map.Entry<Long, Double>> entries = NON_RELATIONAL_CACHE.getOrDefault(entity.getUniqueId(), new ConcurrentHashMap<>());
        Map.Entry<Long, Double> entry = entries.get(this.attribute);
        if (entry != null && entry.getKey() + CACHE_DURATION_MS > System.currentTimeMillis()) return entry.getValue();
        entry = Map.entry(System.currentTimeMillis(), fetch(entity, false));
        entries.put(attribute, entry);
        NON_RELATIONAL_CACHE.put(entity.getUniqueId(), entries);
        return entry.getValue();
    }

    @Override
    public void set(Entity entity, double value) {
        Map<String, Map.Entry<Long, Double>> entries = NON_RELATIONAL_CACHE.getOrDefault(entity.getUniqueId(), new ConcurrentHashMap<>());
        Map.Entry<Long, Double> entry = Map.entry(System.currentTimeMillis(), fetch(entity, false));
        entries.put(attribute, entry);
        NON_RELATIONAL_CACHE.put(entity.getUniqueId(), entries);
    }

    @Override
    public StatCacheResetCause getResetCause() {
        return StatCacheResetCause.EQUIPMENT_CHANGE;
    }
}
