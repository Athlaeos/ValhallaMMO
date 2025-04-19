package me.athlaeos.valhallammo.platform.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public interface ValhallaScheduler<T> {

    public TaskHolder<T> runTask(Plugin plugin, Runnable toRun);

    public TaskHolder<T> runTaskLater(Plugin plugin, long delay, Runnable toRun);

    public TaskHolder<T> runTaskTimer(Plugin plugin, long delay, long repeat, Runnable toRun);

    public TaskHolder<T> runTaskAsync(Plugin plugin, Runnable toRun);

    public TaskHolder<T> runTaskLaterAsync(Plugin plugin, long delay, Runnable toRun);

    public TaskHolder<T> runTaskTimerAsync(Plugin plugin, long delay, long repeat, Runnable toRun);

    public TaskHolder<T> runEntityTask(Plugin plugin, Entity entity, long delay, Runnable toRun);

    public TaskHolder<T> runEntityTask(Plugin plugin, Entity entity, long delay, long period, Runnable toRun);

    public void runEntityTask(Plugin plugin, Entity entity, Runnable toRun);

    public TaskHolder<T> runLocationTask(Plugin plugin, Location location, long delay, Runnable toRun);

    public TaskHolder<T> runLocationTask(Plugin plugin, Location location, long delay, long period, Runnable toRun);

    public void runLocationTask(Plugin plugin, Location location, Runnable toRun);

    public TaskHolder<T> runEntityTaskAsync(Plugin plugin, Entity entity, long delay, Runnable toRun);

    public TaskHolder<T> runEntityTaskAsync(Plugin plugin, Entity entity, long delay, long period, Runnable toRun);

    public void runEntityTaskAsync(Plugin plugin, Entity entity, Runnable toRun);

    public TaskHolder<T> runLocationTaskAsync(Plugin plugin, Location location, long delay, Runnable toRun);

    public void runLocationTaskAsync(Plugin plugin, Location location, Runnable toRun);
}
