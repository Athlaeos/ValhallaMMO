package me.athlaeos.valhallammo.playerstats;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.Scheduling;
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
        if (lastCacheRefreshMap.getOrDefault(entity.getUniqueId(), 0L) + CACHE_REFRESH_DELAY <= System.currentTimeMillis()){
            // delay expired, cache properties
            cachedProperties.put(entity.getUniqueId(), EntityUtils.getEntityProperties(entity, true, true, true));
            lastCacheRefreshMap.put(entity.getUniqueId(), System.currentTimeMillis());
        }
        return cachedProperties.getOrDefault(entity.getUniqueId(), EntityUtils.getEntityProperties(entity, true, true, true));
    }

    public static void resetHands(LivingEntity entity){
        cachedProperties.put(entity.getUniqueId(), EntityUtils.updateProperties(cachedProperties.getOrDefault(entity.getUniqueId(), getAndCacheProperties(entity)),
                entity, false, true, false));
    }

    public static void resetEquipment(LivingEntity entity){
        cachedProperties.put(entity.getUniqueId(), EntityUtils.updateProperties(cachedProperties.getOrDefault(entity.getUniqueId(), getAndCacheProperties(entity)),
                entity, true, false, false));
    }

    public static void resetPotionEffects(LivingEntity entity){
        cachedProperties.put(entity.getUniqueId(), EntityUtils.updateProperties(cachedProperties.getOrDefault(entity.getUniqueId(), getAndCacheProperties(entity)),
                entity, false, false, true));
    }

    public static void removeProperties(LivingEntity entity){
        cachedProperties.remove(entity.getUniqueId());
    }

    public static void attemptCacheCleanup(){
        Scheduling.runTask(ValhallaMMO.getInstance(), () -> {
            if (lastCacheCleanup + CACHE_CLEANUP_DELAY < System.currentTimeMillis()){
                Collection<UUID> uuids = new HashSet<>(cachedProperties.keySet());
                uuids.forEach(u -> {
                    Entity entity = ValhallaMMO.getInstance().getServer().getEntity(u);
                    if (entity == null || !entity.isValid()){
                        cachedProperties.remove(u);
                        lastCacheRefreshMap.remove(u);
                    }
                });
                lastCacheCleanup = System.currentTimeMillis();
            }
        });
    }
}
