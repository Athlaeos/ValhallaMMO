package me.athlaeos.valhallammo.platform.bukkit;

import me.athlaeos.valhallammo.platform.scheduler.TaskHolder;
import me.athlaeos.valhallammo.platform.scheduler.ValhallaScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;

public class BukkitValhallaScheduler implements ValhallaScheduler<BukkitTask> {

    private BukkitTaskHolder of(@Nonnull BukkitTask task) {
        return new BukkitTaskHolder(task);
    }

    @Override
    public TaskHolder<BukkitTask> runTask(Plugin plugin, Runnable toRun) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                toRun.run();
            }
        }.runTask(plugin);
        return of(task);
    }

    @Override
    public TaskHolder<BukkitTask> runTaskLater(Plugin plugin, long delay, Runnable toRun) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                toRun.run();
            }
        }.runTaskLater(plugin, delay);
        return of(task);
    }

    @Override
    public TaskHolder<BukkitTask> runTaskTimer(Plugin plugin, long delay, long repeat, Runnable toRun) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                toRun.run();
            }
        }.runTaskTimer(plugin, delay, repeat);
        return of(task);
    }

    @Override
    public TaskHolder<BukkitTask> runTaskAsync(Plugin plugin, Runnable toRun) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                toRun.run();
            }
        }.runTaskAsynchronously(plugin);
        return of(task);
    }

    @Override
    public TaskHolder<BukkitTask> runTaskLaterAsync(Plugin plugin, long delay, Runnable toRun) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                toRun.run();
            }
        }.runTaskLaterAsynchronously(plugin, delay);
        return of(task);
    }

    @Override
    public TaskHolder<BukkitTask> runTaskTimerAsync(Plugin plugin, long delay, long repeat, Runnable toRun) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                toRun.run();
            }
        }.runTaskTimerAsynchronously(plugin, delay, repeat);
        return of(task);
    }

    @Override
    public TaskHolder<BukkitTask> runEntityTask(Plugin plugin, Entity entity, long delay, Runnable toRun) {
        return this.runTaskLater(plugin, delay, toRun);
    }

    @Override
    public void runEntityTask(Plugin plugin, Entity entity, Runnable toRun) {
        this.runTask(plugin, toRun);
    }

    @Override
    public TaskHolder<BukkitTask> runLocationTask(Plugin plugin, Location location, long delay, Runnable toRun) {
        return this.runTaskLater(plugin, delay, toRun);
    }

    @Override
    public void runLocationTask(Plugin plugin, Location location, Runnable toRun) {
        this.runTask(plugin, toRun);
    }

    @Override
    public TaskHolder<BukkitTask> runEntityTaskAsync(Plugin plugin, Entity entity, long delay, Runnable toRun) {
        return this.runTaskLaterAsync(plugin, delay, toRun);
    }

    @Override
    public TaskHolder<BukkitTask> runEntityTaskAsync(Plugin plugin, Entity entity, long delay, long period, Runnable toRun) {
        return this.runTaskTimerAsync(plugin, delay, period, toRun);
    }

    @Override
    public void runEntityTaskAsync(Plugin plugin, Entity entity, Runnable toRun) {
        this.runTaskAsync(plugin, toRun);
    }

    @Override
    public TaskHolder<BukkitTask> runLocationTaskAsync(Plugin plugin, Location location, long delay, Runnable toRun) {
        return this.runTaskLaterAsync(plugin, delay, toRun);
    }

    @Override
    public void runLocationTaskAsync(Plugin plugin, Location location, Runnable toRun) {
        this.runTaskAsync(plugin, toRun);
    }
}
