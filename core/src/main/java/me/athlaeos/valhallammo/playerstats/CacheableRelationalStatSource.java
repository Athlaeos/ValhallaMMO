package me.athlaeos.valhallammo.playerstats;

import org.bukkit.entity.Entity;

import java.util.UUID;

public interface CacheableRelationalStatSource extends EvEAccumulativeStatSource, CacheableStatSource {
    double get(Entity primaryEntity, Entity secondaryEntity);

    void set(Entity primaryEntity, Entity secondaryEntity, double value);
}
