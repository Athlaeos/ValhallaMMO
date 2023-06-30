package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Timer {

    private static final Map<String, Map<UUID, Long>> allCooldowns = new HashMap<>();
    private static final Map<String, Map<UUID, Long>> allTimers = new HashMap<>();
    private static final Map<String, Map<UUID, Long>> allTimersNanos = new HashMap<>();

    public static void setCooldown(UUID entity, int timems, String cooldownKey){
        allCooldowns.putIfAbsent(cooldownKey, new HashMap<>());
        Map<UUID, Long> cooldowns = allCooldowns.get(cooldownKey);
        cooldowns.put(entity, System.currentTimeMillis() + timems);
        allCooldowns.put(cooldownKey, cooldowns);
    }

    /**
     * Ability cooldowns are regular cooldowns that get ignored if the player has the valhalla.ignorecooldowns permission.
     * The cooldown applied is also further influenced by the COOLDOWN_REDUCTION stat
     * @param entity the entity to set their cooldown for
     * @param timems the time in milliseconds to set the cooldown for
     * @param cooldownKey the identifier of the cooldown ability
     */
    public static void setAbilityCooldown(Entity entity, int timems, String cooldownKey){
        if (!entity.hasPermission("valhalla.ignorecooldowns")){
            double cooldownReduction = AccumulativeStatManager.getStats("COOLDOWN_REDUCTION", entity, true);
            int newCooldown = Math.max(0, (int) (timems * (1D - cooldownReduction)));
            setCooldown(entity.getUniqueId(), newCooldown, cooldownKey);
        }
    }

    public static long getCooldown(UUID entity, String cooldownKey){
        long time = System.currentTimeMillis();
        Map<UUID, Long> cooldowns = allCooldowns.getOrDefault(cooldownKey, new HashMap<>());
        return cooldowns.getOrDefault(entity, time) - time;
    }

    public static boolean isCooldownPassed(UUID entity, String cooldownKey){
        return getCooldown(entity, cooldownKey) <= 0;
    }

    public static void startTimer(UUID entity, String timerKey){
        allTimers.putIfAbsent(timerKey, new HashMap<>());
        Map<UUID, Long> timers = allTimers.get(timerKey);
        timers.put(entity, System.currentTimeMillis());
        allTimers.put(timerKey, timers);
    }

    public static long getTimerResult(UUID entity, String timerKey){
        long time = System.currentTimeMillis();
        Map<UUID, Long> timers = allTimers.getOrDefault(timerKey, new HashMap<>());
        return time - timers.getOrDefault(entity, time);
    }

    public static void stopTimer(UUID entity, String timerKey){
        allTimers.putIfAbsent(timerKey, new HashMap<>());
        Map<UUID, Long> timers = allTimers.get(timerKey);
        timers.remove(entity);
        allTimers.put(timerKey, timers);
    }


    public static void startTimerNanos(UUID entity, String timerKey){
        allTimersNanos.putIfAbsent(timerKey, new HashMap<>());
        Map<UUID, Long> timers = allTimersNanos.get(timerKey);
        timers.put(entity, System.nanoTime());
        allTimersNanos.put(timerKey, timers);
    }

    public static long getTimerResultNanos(UUID entity, String timerKey){
        long time = System.nanoTime();
        Map<UUID, Long> timers = allTimersNanos.getOrDefault(timerKey, new HashMap<>());
        return time - timers.getOrDefault(entity, time);
    }

    public static void stopTimerNanos(UUID entity, String timerKey){
        allTimersNanos.putIfAbsent(timerKey, new HashMap<>());
        Map<UUID, Long> timers = allTimersNanos.get(timerKey);
        timers.remove(entity);
        allTimersNanos.put(timerKey, timers);
    }
}
