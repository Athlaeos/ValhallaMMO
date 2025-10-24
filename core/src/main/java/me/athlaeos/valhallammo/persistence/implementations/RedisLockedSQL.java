package me.athlaeos.valhallammo.persistence.implementations;

import com.google.common.collect.ClassToInstanceMap;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.listeners.JoinLeaveListener;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RedisLockedSQL extends SQL {
    private static final long LOCK_TIMEOUT = 5000L;
    private static final String EDITION_LOCK = "vmmo:player:lock";

    protected final Queue<List<Profile>> saveQueue = new ConcurrentLinkedQueue<>();
    private JedisPool pool;

    @Override
    public int minimumProfileThreadCount() {
        // 1 thread for loading, 1 thread for saving, 1 thread for queue, 1 for pinging
        return 4;
    }

    public boolean acquireLock(String editionKey, String lockValue) {
        try(Jedis jedis = pool.getResource()) {
            SetParams setParams = new SetParams().nx().px(LOCK_TIMEOUT);
            String result = jedis.set(EDITION_LOCK + ":" + editionKey, lockValue, setParams);
            if (!result.equals("OK")) {
                return false;
            }
            String value = jedis.get(EDITION_LOCK + ":" + editionKey);
            return lockValue.equals(value);
        }
    }

    public boolean hasLock(String editionKey) {
        try(Jedis jedis = pool.getResource()) {
            String currentLockValue = jedis.get(EDITION_LOCK + ":" + editionKey);
            return currentLockValue != null;
        }
    }

    public void releaseLock(String editionKey, String lockValue) {
        try(Jedis jedis = pool.getResource()) {
            String currentLockValue = jedis.get(EDITION_LOCK + ":" + editionKey);
            if (currentLockValue.equals(lockValue)) {
                jedis.del(EDITION_LOCK + ":" + editionKey);
            }
        }
    }

    public void startSaveQueue() {
        profileThreads.execute(() -> {
            while (!ValhallaMMO.disabling() || !saveQueue.isEmpty()) {
                List<Profile> profiles = saveQueue.poll();
                if (profiles == null || profiles.isEmpty()) return;
                UUID owner = profiles.get(0).getOwner();
                String key = owner.toString();
                String lock = UUID.randomUUID().toString();
                if (!acquireLock(key, lock)) {
                    saveQueue.add(profiles);
                    return;
                }
                try {
                    for (Profile profile : profiles) {
                        if (profile.getOwner() == null) continue;
                        insertOrUpdateProfile(profile.getOwner(), profile);
                    }
                } finally {
                    saving.remove(owner);
                    releaseLock(key, lock);
                    Player player = Bukkit.getPlayer(owner);
                    if (player == null || !player.isOnline()) uncacheProfile(owner);
                }
            }
        });
    }

    @Override
    public void saveProfile(UUID uuid, boolean localLock) {
        CompletableFuture<ClassToInstanceMap<Profile>> future = persistentProfiles.getIfPresent(uuid);
        if (future == null || !future.isDone()) return;
        else if (localLock && !saving.add(uuid)) return;

        ClassToInstanceMap<Profile> profiles = future.join();
        String lockKey = uuid.toString();
        String lock = UUID.randomUUID().toString();
        if (!acquireLock(lockKey, lock)) {
            // Queue saving
            saveQueue.add(new ArrayList<>(profiles.values()));
            return;
        }

        profileThreads.execute(() -> {
            try {
                if (!JoinLeaveListener.getLoadedProfiles().contains(uuid)) {
                    releaseLock(lockKey, lock);
                    return;
                }
                for (Profile profile : profiles.values()) {
                    insertOrUpdateProfile(uuid, profile);
                }
            } finally {
                if (localLock) {
                    saving.remove(uuid);
                }
                releaseLock(lockKey, lock);
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) uncacheProfile(uuid);
            }
        });
    }

    public JedisPool getPool() {
        if (pool != null) return pool;

        try {
            this.pool = new JedisPool(ValhallaMMO.getPluginConfig().getString("redis_url", "redis://127.0.0.1:6379"));
        } catch(Exception ex) {
            pool = null;
            ValhallaMMO.getInstance().getLogger().severe("Failed to initialize Jedis connection");
            ex.printStackTrace();
        }
        return pool;
    }
}
