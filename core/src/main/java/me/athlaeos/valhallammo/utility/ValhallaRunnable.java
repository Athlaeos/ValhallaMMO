package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.platform.scheduler.TaskHolder;
import me.athlaeos.valhallammo.platform.scheduler.ValhallaScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public abstract class ValhallaRunnable implements Runnable {
    private TaskHolder<?> task = null;

    public void cancel() {
        if (task == null) return;
        task.cancel();
    }

    public boolean isCanceled() {
        if (task == null) return true;
        return task.isCancelled();
    }

    private static ValhallaScheduler<?> getScheduler() {
        return ValhallaMMO.getPlatform().getScheduler();
    }

    /**
     * Run a task on the main thread
     */
    public void runTask(Plugin plugin) {
        this.cancel();
        this.task = Scheduling.runTask(plugin, this);
    }

    /**
     * Run a task after [delay] of ticks
     */
    public void runTaskLater(Plugin plugin, long delay) {
        this.cancel();
        this.task = Scheduling.runTaskLater(plugin, delay, this);
    }

    /**
     * Repeat a task every [repeat] ticks after [delay] ticks
     */
    public void runTaskTimer(Plugin plugin, long delay, long repeat) {
        this.cancel();
        this.task = Scheduling.runTaskTimer(plugin, delay, repeat, this);
    }

    /**
     * Repeat a task every [repeat] ticks after [delay] ticks
     */
    public void runLocation(Plugin plugin, Location location, long delay, long repeat) {
        this.cancel();
        this.task = Scheduling.runLocationTask(plugin, location, delay, repeat, this);
    }

    public void runEntity(Plugin plugin, Entity entity, long delay) {
        this.cancel();
        this.task = Scheduling.runEntityTask(plugin, entity, delay, this);
    }

    public void runEntity(Plugin plugin, Entity entity, long delay, long repeat) {
        this.cancel();
        this.task = Scheduling.runEntityTask(plugin, entity, delay, repeat, this);
    }

    /**
     * Run a task on the async thread
     */
    public void runTaskAsync(Plugin plugin) {
        this.cancel();
        this.task = Scheduling.runTaskAsync(plugin, this);
    }

    /**
     * Run a task after [delay] ticks on the async thread
     */
    public void runTaskLaterAsync(Plugin plugin, long delay) {
        this.cancel();
        this.task = Scheduling.runTaskLaterAsync(plugin, delay, this);
    }

    /**
     * Repeat a task every [repeat] ticks after [delay] ticks on the async thread
     */
    public void runTaskTimerAsync(Plugin plugin, long delay, long repeat) {
        this.cancel();
        this.task = Scheduling.runTaskTimerAsync(plugin, delay, repeat, this);
    }

}
