package me.athlaeos.valhallammo.playerstats.profiles;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class ProfileCache {
    private static final Map<UUID, Map<Class<? extends Profile>, CacheEntry>> cache = new HashMap<>();
    private static long cacheDuration = 10000L;

    public static void resetCacheDuration(){
        cacheDuration = ValhallaMMO.getPluginConfig().getLong("profile_caching", 10000L);
    }

    /**
     * Used to reset the cache of a player. Use this in case you make changes to a player's profile so that the cached
     * profile can be invalidated.
     * @param player the player to reset their cached profiles for
     */
    public static void resetCache(Player player) {
        cache.remove(player.getUniqueId());
    }

    /**
     * Fetches a cached profile, or caches the player's merged profile if the previous is expired or doesn't exist
     * @param player the player to fetch the cached merged profile from
     * @param type the type of profile to fetch
     * @return the cached profile
     */
    @SuppressWarnings("unchecked")
    public static <T extends Profile> T getOrCache(Player player, Class<T> type){
        Map<Class<? extends Profile>, CacheEntry> profiles = cache.getOrDefault(player.getUniqueId(), new HashMap<>());
        CacheEntry entry = profiles.get(type);
        if (entry == null || entry.getCacheUntil() < System.currentTimeMillis()) {
            entry = new CacheEntry(ProfileRegistry.getMergedProfile(player, type), cacheDuration);
            profiles.put(type, entry);
            cache.put(player.getUniqueId(), profiles);
        }
        return (T) entry.getCachedProfile();
    }

    /**
     * Used to clean the cache off any offline players, to prevent memory buildup
     */
    public static void cleanCache(){
        for (UUID uuid : new HashSet<>(cache.keySet())){
            Player p = ValhallaMMO.getInstance().getServer().getPlayer(uuid);
            if (p == null || !p.isOnline()) cache.remove(uuid);
        }
    }

    private static class CacheEntry {
        private final long cacheUntil;
        private final Profile cachedProfile;

        public CacheEntry(Profile profile, long cacheFor) {
            this.cachedProfile = profile;
            this.cacheUntil = System.currentTimeMillis() + cacheFor;
        }

        public Profile getCachedProfile() {
            return cachedProfile;
        }

        public long getCacheUntil() {
            return cacheUntil;
        }
    }
}
