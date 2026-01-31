package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.item.ArmorSet;
import me.athlaeos.valhallammo.item.ArmorSetRegistry;
import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.playerstats.CacheableRelationalStatSource;
import me.athlaeos.valhallammo.playerstats.StatCacheResetCause;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeDefenderSource implements CacheableRelationalStatSource {
    private static final int CACHE_DURATION_MS = 10000;
    private static final Map<UUID, Map<UUID, Map<String, Map.Entry<Long, Double>>>> RELATIONAL_CACHE = new ConcurrentHashMap<>();
    // primary|secondary|attribute|cache time|cached value
    private static final Map<UUID, Map<String, Map.Entry<Long, Double>>> NON_RELATIONAL_CACHE = new ConcurrentHashMap<>();

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
        return fetch(statPossessor, null, use);
    }

    @Override
    public double get(Entity primaryEntity, Entity secondaryEntity) {
        if (secondaryEntity == null) return get(primaryEntity);
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
        NON_RELATIONAL_CACHE.remove(uuid);
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
