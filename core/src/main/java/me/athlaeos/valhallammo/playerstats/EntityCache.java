package me.athlaeos.valhallammo.playerstats;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public class EntityCache {
    private static final long CACHE_REFRESH_DELAY = 10000;
    private static final long CACHE_CLEANUP_DELAY = 600000;
    private static final Map<UUID, EntityProperties> cachedProperties = new HashMap<>();
    private static final Map<UUID, Long> lastCacheRefreshMap = new HashMap<>();
    private static long lastCacheCleanup = System.currentTimeMillis();

    public static EntityProperties getAndCacheProperties(LivingEntity entity){
        attemptCacheCleanup();
        Long lastCached = lastCacheRefreshMap.get(entity.getUniqueId());
        EntityProperties cached = cachedProperties.get(entity.getUniqueId());
        if (lastCached == null || cached == null || lastCached + CACHE_REFRESH_DELAY <= System.currentTimeMillis()) {
            lastCacheRefreshMap.put(entity.getUniqueId(), System.currentTimeMillis());
            cachedProperties.put(entity.getUniqueId(), EntityUtils.getEntityProperties(entity, true, true, true));
        }
        return cachedProperties.get(entity.getUniqueId());
//        return cachedProperties.compute(entity.getUniqueId(), (uuid, cached) -> {
//            Long lastCache = lastCacheRefreshMap.get(uuid);
//            if (lastCache == null || lastCache + CACHE_REFRESH_DELAY <= System.currentTimeMillis() || cached == null) {
//                lastCacheRefreshMap.put(uuid, System.currentTimeMillis());
//                return EntityUtils.getEntityProperties(entity, true, true, true);
//            }
//            return cached;
//        });
    }

    public static void resetHands(LivingEntity entity){
        cachedProperties.computeIfPresent(entity.getUniqueId(), (uuid, cached) ->
                EntityUtils.updateProperties(cached, entity, false, true, false));
    }

    public static void resetEquipment(LivingEntity entity){
        cachedProperties.computeIfPresent(entity.getUniqueId(), (uuid, cached) ->
                EntityUtils.updateProperties(cached, entity, true, false, false));
    }

    public static void resetPotionEffects(LivingEntity entity){
        cachedProperties.computeIfPresent(entity.getUniqueId(), (uuid, cached) ->
                EntityUtils.updateProperties(cached, entity, false, false, true));
    }

    public static void removeProperties(LivingEntity entity){
        cachedProperties.remove(entity.getUniqueId());
    }

    public static void attemptCacheCleanup(){
        Bukkit.getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
            if (lastCacheCleanup + CACHE_CLEANUP_DELAY < System.currentTimeMillis()){
                for (UUID uuid : new HashSet<>(cachedProperties.keySet())) {
                    Entity entity = Bukkit.getEntity(uuid);
                    if (entity == null || !entity.isValid()){
                        cachedProperties.remove(uuid);
                        lastCacheRefreshMap.remove(uuid);
                    }
                }
                lastCacheCleanup = System.currentTimeMillis();
            }
        });
    }
}
