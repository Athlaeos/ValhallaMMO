package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.platform.scheduler.TaskHolder;
import me.athlaeos.valhallammo.platform.scheduler.ValhallaScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class Scheduling {

    private static ValhallaScheduler<?> getScheduler() {
        return ValhallaMMO.getPlatform().getScheduler();
    }

    /**
     * Run a task on the main thread
     */
    public static TaskHolder<?> runTask(Plugin plugin, Runnable toRun) {
        return getScheduler().runTask(plugin, toRun);
    }

    /**
     * Run a task after [delay] of ticks
     */
    public static TaskHolder<?> runTaskLater(Plugin plugin, long delay, Runnable toRun) {
        return getScheduler().runTaskLater(plugin, delay, toRun);
    }

    /**
     * Repeat a task every [repeat] ticks after [delay] ticks
     */
    public static TaskHolder<?> runTaskTimer(Plugin plugin, long delay, long repeat, Runnable toRun) {
        return getScheduler().runTaskTimer(plugin, delay, repeat, toRun);
    }

    /**
     * Run a task on the async thread
     */
    public static TaskHolder<?> runTaskAsync(Plugin plugin, Runnable toRun) {
        return getScheduler().runTaskAsync(plugin, toRun);
    }

    /**
     * Run a task after [delay] ticks on the async thread
     */
    public static TaskHolder<?> runTaskLaterAsync(Plugin plugin, long delay, Runnable toRun) {
        return getScheduler().runTaskLaterAsync(plugin, delay, toRun);
    }

    /**
     * Repeat a task every [repeat] ticks after [delay] ticks on the async thread
     */
    public static TaskHolder<?> runTaskTimerAsync(Plugin plugin, long delay, long repeat, Runnable toRun) {
        return getScheduler().runTaskTimerAsync(plugin, delay, repeat, toRun);
    }

    public static TaskHolder<?> runEntityTask(Plugin plugin, Entity entity, long delay, Runnable toRun) {
        return getScheduler().runEntityTask(plugin, entity, delay, toRun);
    }

    public static void runEntityTask(Plugin plugin, Entity entity, Runnable toRun) {
        getScheduler().runEntityTask(plugin, entity, toRun);
    }

    public static TaskHolder<?> runLocationTask(Plugin plugin, Location location, long delay, Runnable toRun) {
        return getScheduler().runLocationTask(plugin, location, delay, toRun);
    }

    public static void runLocationTask(Plugin plugin, Location location, Runnable toRun) {
        getScheduler().runLocationTask(plugin, location, toRun);
    }

    public static TaskHolder<?> runEntityTaskAsync(Plugin plugin, Entity entity, long delay, Runnable toRun) {
        return getScheduler().runEntityTaskAsync(plugin, entity, delay, toRun);
    }

    public static void runEntityTaskAsync(Plugin plugin, Entity entity, Runnable toRun) {
        getScheduler().runEntityTaskAsync(plugin, entity, toRun);
    }

    public static TaskHolder<?> runEntityTaskAsync(Plugin plugin, Entity entity, long delay, long period, Runnable toRun) {
        return getScheduler().runEntityTaskAsync(plugin, entity, delay, period, toRun);
    }

    public static TaskHolder<?> runLocationTaskAsync(Plugin plugin, Location location, long delay, Runnable toRun) {
        return getScheduler().runLocationTaskAsync(plugin, location, delay, toRun);
    }

    public static void runLocationTaskAsync(Plugin plugin, Location location, Runnable toRun) {
        getScheduler().runLocationTaskAsync(plugin, location, toRun);
    }

}
