package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.item.ArmorSet;
import me.athlaeos.valhallammo.item.ArmorSetRegistry;
import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.CacheableStatSource;
import me.athlaeos.valhallammo.playerstats.StatCacheResetCause;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeSource implements CacheableStatSource {
    private static final int CACHE_DURATION_MS = 10000;
    private static final Map<UUID, Map<String, Map.Entry<Long, Double>>> CACHE = new ConcurrentHashMap<>();

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

    @Override
    public void reset(UUID uuid) {
        CACHE.remove(uuid);
    }

    @Override
    public double get(Entity entity) {
        Map<String, Map.Entry<Long, Double>> entries = CACHE.getOrDefault(entity.getUniqueId(), new ConcurrentHashMap<>());
        Map.Entry<Long, Double> entry = entries.get(this.attribute);
        if (entry != null && entry.getKey() + CACHE_DURATION_MS > System.currentTimeMillis()) return entry.getValue();
        entry = Map.entry(System.currentTimeMillis(), fetch(entity, false));
        entries.put(attribute, entry);
        CACHE.put(entity.getUniqueId(), entries);
        return entry.getValue();
    }

    @Override
    public void set(Entity entity, double value) {
        Map<String, Map.Entry<Long, Double>> entries = CACHE.getOrDefault(entity.getUniqueId(), new ConcurrentHashMap<>());
        Map.Entry<Long, Double> entry = Map.entry(System.currentTimeMillis(), fetch(entity, false));
        entries.put(attribute, entry);
        CACHE.put(entity.getUniqueId(), entries);
    }

    @Override
    public StatCacheResetCause getResetCause() {
        return StatCacheResetCause.EQUIPMENT_CHANGE;
    }
}
