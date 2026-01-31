package me.athlaeos.valhallammo.playerstats;

import org.bukkit.entity.Entity;

import java.util.UUID;

public interface CacheableStatSource extends AccumulativeStatSource {
    void reset(UUID uuid);

    double get(Entity entity);

    void set(Entity entity, double value);

    StatCacheResetCause getResetCause();
}
