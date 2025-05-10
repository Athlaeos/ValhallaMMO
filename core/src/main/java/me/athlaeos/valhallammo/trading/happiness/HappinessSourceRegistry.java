package me.athlaeos.valhallammo.trading.happiness;

import me.athlaeos.valhallammo.trading.happiness.sources.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HappinessSourceRegistry {
    private static final Map<String, HappinessSource> sources = new HashMap<>();
    private static final Map<UUID, CacheEntry> cache = new HashMap<>();
    private static final Map<UUID, Map<UUID, CacheEntry>> relationalCache = new HashMap<>();
    private static final long CACHE_DURATION = 10000;

    static {
        registerSource(new Brightness());
        registerSource(new Comfort());
        registerSource(new Company());
        registerSource(new Health());
        registerSource(new PlayerRelation());
        registerSource(new Security());
        registerSource(new Space());
        registerSource(new Stress());
        registerSource(new Trust());
    }

    public static float getHappiness(Player interactingPlayer, Entity entity){
        CacheEntry cachedEntry;
        if (interactingPlayer != null) cachedEntry = relationalCache.getOrDefault(entity.getUniqueId(), new HashMap<>()).get(interactingPlayer.getUniqueId());
        else cachedEntry = cache.get(entity.getUniqueId());
        if (cachedEntry != null && cachedEntry.time + CACHE_DURATION > System.currentTimeMillis()) return cachedEntry.value;

        float happiness = 0;
        for (HappinessSource source : sources.values()){
            if (source.appliesTo(entity)) happiness += source.get(interactingPlayer, entity);
        }
        cachedEntry = new CacheEntry(entity.getUniqueId(), System.currentTimeMillis(), happiness);
        if (interactingPlayer != null){
            Map<UUID, CacheEntry> entries = relationalCache.getOrDefault(entity.getUniqueId(), new HashMap<>());
            entries.put(interactingPlayer.getUniqueId(), cachedEntry);
            relationalCache.put(entity.getUniqueId(), entries);
        } else cache.put(entity.getUniqueId(), cachedEntry);
        return happiness;
    }

    public static void registerSource(HappinessSource source){
        sources.put(source.id(), source);
    }

    public static Map<String, HappinessSource> getSources(){
        return new HashMap<>(sources);
    }

    private record CacheEntry(UUID uuid, Long time, float value){}
}
