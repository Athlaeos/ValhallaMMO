package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Timer {
    private static final Map<String, Map<UUID, Long>> allCooldowns = new HashMap<>();
    private static final Map<String, Map<UUID, Long>> allTimers = new HashMap<>();
    private static final Map<String, Map<UUID, Long>> allTimersNanos = new HashMap<>();

    /**
     * Sets a cooldown of a given duration.
     * @param entity the entity to set the cooldown to
     * @param timems the time (in milliseconds) for the cooldown
     * @param cooldownKey the cooldown key, should be unique per cooldown timer
     */
    public static void setCooldown(UUID entity, int timems, String cooldownKey){
        if (!allCooldowns.containsKey(cooldownKey)) allCooldowns.put(cooldownKey, new HashMap<>());
        allCooldowns.get(cooldownKey).put(entity, System.currentTimeMillis() + timems);
    }

    /**
     * Sets a cooldown of a given duration. Cooldown is ignored by the entity's COOLDOWN_REDUCTION stat, and is ignored
     * completely if the player has the valhalla.ignorecooldowns permission.
     * @param entity the entity to set the cooldown to
     * @param timems the time (in milliseconds) for the cooldown
     * @param cooldownKey the cooldown key, should be unique per cooldown timer
     */
    public static void setCooldownIgnoreIfPermission(Entity entity, int timems, String cooldownKey){
        if (!entity.hasPermission("valhalla.ignorecooldowns")){
            double cooldownReduction = AccumulativeStatManager.getCachedStats("COOLDOWN_REDUCTION", entity, 10000, true);
            int newCooldown = Math.max(0, (int) (timems * (1D - cooldownReduction)));
            setCooldown(entity.getUniqueId(), newCooldown, cooldownKey);
        }
    }

    public static boolean sendIfNotPassed(Player player, String cooldownKey, String type){
        if (!isCooldownPassed(player.getUniqueId(), cooldownKey)){
            sendCooldownStatus(player, cooldownKey, type);
            return true;
        }
        return false;
    }

    private static final Map<UUID, BukkitRunnable> cooldownStatusRunnables = new HashMap<>();

    public static void sendCooldownStatus(Player player, String cooldownKey, String type){
        int ticks = ConfigManager.getConfig("config.yml").get().getInt("cooldown_status_duration", 0);
        if (ticks > 0){
            BukkitRunnable existingRunnable = cooldownStatusRunnables.get(player.getUniqueId());
            if (existingRunnable != null) existingRunnable.cancel();
            existingRunnable = new BukkitRunnable() {
                int ticksRemaining = ticks;
                @Override
                public void run() {
                    long remainingCooldown = getCooldown(player.getUniqueId(), cooldownKey);

                    Utils.sendActionBar(player, TranslationManager.getTranslation("status_cooldown")
                            .replace("%type%", type)
                            .replace("%timestamp%", StringUtils.toTimeStamp(remainingCooldown, 1000))
                            .replace("%timestamp2%", StringUtils.toTimeStamp2(remainingCooldown, 1000, true))
                    );
                    if (ticksRemaining < 0 || remainingCooldown <= 0 || !player.isOnline()) {
                        cancel();
                        cooldownStatusRunnables.remove(player.getUniqueId());
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                    }
                    ticksRemaining--;
                }
            };
            existingRunnable.runTaskTimer(ValhallaMMO.getInstance(), 1L, 1L);
            cooldownStatusRunnables.put(player.getUniqueId(), existingRunnable);
        } else if (ticks == 0) {
            long remainingCooldown = getCooldown(player.getUniqueId(), cooldownKey);

            Utils.sendActionBar(player, TranslationManager.getTranslation("status_cooldown")
                    .replace("%type%", type)
                    .replace("%timestamp%", StringUtils.toTimeStamp(remainingCooldown, 1000))
                    .replace("%timestamp2%", StringUtils.toTimeStamp2(remainingCooldown, 1000, true))
            );
        }
    }

    /**
     * Returns the remaining cooldown of the entity.
     * @param entity the entity to return the cooldown of
     * @param cooldownKey the key of the cooldown to return
     * @return the remaining cooldown in milliseconds, or 0 if passed
     */
    public static long getCooldown(UUID entity, String cooldownKey){
        if (!allCooldowns.containsKey(cooldownKey)) allCooldowns.put(cooldownKey, new HashMap<>());
        if (allCooldowns.get(cooldownKey).containsKey(entity)){
            return allCooldowns.get(cooldownKey).get(entity) - System.currentTimeMillis();
        }
        return 0;
    }

    /**
     * Returns true if the cooldown has passed
     * @param entity the entity to check their cooldown
     * @param cooldownKey the cooldown key to check
     * @return true if passed, false if not
     */
    public static boolean isCooldownPassed(UUID entity, String cooldownKey){
        allCooldowns.computeIfAbsent(cooldownKey, k -> new HashMap<>());
        if (allCooldowns.getOrDefault(cooldownKey, new HashMap<>()).containsKey(entity)){
            return allCooldowns.getOrDefault(cooldownKey, new HashMap<>()).get(entity) <= System.currentTimeMillis();
        }
        return true;
    }

    /**
     * Starts a timer at this time.
     * @param entity the entity to start the timer for
     * @param timerKey the key for the timer, should be unique
     */
    public static void startTimer(UUID entity, String timerKey){
        if (!allTimers.containsKey(timerKey)) allTimers.put(timerKey, new HashMap<>());
        allTimers.get(timerKey).put(entity, System.currentTimeMillis());
    }

    /**
     * Returns the time since the timer has been started.
     * @param entity the entity to return the timer for
     * @param timerKey the key for the timer to return
     * @return the time in milliseconds since the timer has started, or 0 if it didn't exist
     */
    public static long getTimerResult(UUID entity, String timerKey){
        if (!allTimers.containsKey(timerKey)) allTimers.put(timerKey, new HashMap<>());
        if (allTimers.get(timerKey).containsKey(entity)){
            return System.currentTimeMillis() - allTimers.get(timerKey).get(entity);
        }
        return 0;
    }

    /**
     * Removes the timer
     * @param entity the entity to remove the timer for
     * @param timerKey the timer key to remove
     */
    public static void stopTimer(UUID entity, String timerKey){
        if (!allTimers.containsKey(timerKey)) allTimers.put(timerKey, new HashMap<>());
        allTimers.get(timerKey).remove(entity);
    }

    /**
     * Does what {@link Timer#startTimer(UUID, String)} does, but in nanosecond precision
     * @param entity the entity to start the timer for
     * @param timerKey the key for the timer
     */
    public static void startTimerNanos(UUID entity, String timerKey){
        if (!allTimersNanos.containsKey(timerKey)) allTimersNanos.put(timerKey, new HashMap<>());
        allTimersNanos.get(timerKey).put(entity, System.nanoTime());
    }

    /**
     * Does what {@link Timer#getTimerResult(UUID, String)} does, but in nanosecond precision
     * @param entity the entity to return the timer for
     * @param timerKey the key for the timer
     */
    public static long getTimerResultNanos(UUID entity, String timerKey){
        if (!allTimersNanos.containsKey(timerKey)) allTimersNanos.put(timerKey, new HashMap<>());
        if (allTimersNanos.get(timerKey).containsKey(entity)){
            return System.nanoTime() - allTimersNanos.get(timerKey).get(entity);
        }
        return 0;
    }

    /**
     * Stops a nanosecond precision timer
     * @param entity the entity to stop the timer for
     * @param timerKey the key for the timer
     */
    public static void stopTimerNanos(UUID entity, String timerKey){
        if (!allTimersNanos.containsKey(timerKey)) allTimersNanos.put(timerKey, new HashMap<>());
        allTimersNanos.get(timerKey).remove(entity);
    }
}