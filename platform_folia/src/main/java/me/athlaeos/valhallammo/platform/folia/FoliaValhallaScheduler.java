package me.athlaeos.valhallammo.platform.folia;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.athlaeos.valhallammo.platform.scheduler.TaskHolder;
import me.athlaeos.valhallammo.platform.scheduler.ValhallaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class FoliaValhallaScheduler implements ValhallaScheduler<ScheduledTask> {
    private final AsyncScheduler async = Bukkit.getServer().getAsyncScheduler();
    private final GlobalRegionScheduler global = Bukkit.getServer().getGlobalRegionScheduler();
    private final RegionScheduler region = Bukkit.getServer().getRegionScheduler();

    private FoliaTaskHolder holder(ScheduledTask task) {
        return new FoliaTaskHolder(task);
    }

    @Override
    public TaskHolder<ScheduledTask> runTask(Plugin plugin, Runnable toRun) {
        return holder(global.run(plugin, scheduledTask -> toRun.run()));
    }

    @Override
    public TaskHolder<ScheduledTask> runTaskLater(Plugin plugin, long delay, Runnable toRun) {
        return holder(global.runDelayed(plugin, scheduledTask -> toRun.run(), delay));
    }

    @Override
    public TaskHolder<ScheduledTask> runTaskTimer(Plugin plugin, long delay, long repeat, Runnable toRun) {
        return holder(global.runAtFixedRate(plugin, scheduledTask -> toRun.run(), minDelay(delay), repeat));
    }

    @Override
    public TaskHolder<ScheduledTask> runTaskAsync(Plugin plugin, Runnable toRun) {
        return holder(async.runNow(plugin, scheduledTask -> toRun.run()));
    }

    @Override
    public TaskHolder<ScheduledTask> runTaskLaterAsync(Plugin plugin, long delay, Runnable toRun) {
        return holder(async.runDelayed(plugin, scheduledTask -> toRun.run(), delay * 20, TimeUnit.MILLISECONDS));
    }

    @Override
    public TaskHolder<ScheduledTask> runTaskTimerAsync(Plugin plugin, long delay, long repeat, Runnable toRun) {
        return holder(async.runAtFixedRate(plugin, scheduledTask -> toRun.run(), delay * 20, repeat * 20, TimeUnit.MILLISECONDS));
    }

    @Override
    public TaskHolder<ScheduledTask> runEntityTask(Plugin plugin, Entity entity, long delay, Runnable toRun) {
        return holder(entity.getScheduler().runDelayed(plugin, scheduledTask -> toRun.run(), null, delay));
    }

    @Override
    public TaskHolder<ScheduledTask> runEntityTask(Plugin plugin, Entity entity, long delay, long period, Runnable toRun) {
        return holder(entity.getScheduler().runAtFixedRate(plugin, scheduledTask -> toRun.run(), null, delay, period));
    }

    @Override
    public void runEntityTask(Plugin plugin, Entity entity, Runnable toRun) {
        entity.getScheduler().run(plugin, scheduledTask -> toRun.run(), null);
    }

    @Override
    public TaskHolder<ScheduledTask> runLocationTask(Plugin plugin, Location location, long delay, Runnable toRun) {
        return holder(region.runDelayed(plugin, location, scheduledTask -> toRun.run(), delay));
    }

    @Override
    public TaskHolder<ScheduledTask> runLocationTask(Plugin plugin, Location location, long delay, long period, Runnable toRun) {
        return holder(region.runAtFixedRate(plugin, location, scheduledTask -> toRun.run(), delay, period));
    }

    @Override
    public void runLocationTask(Plugin plugin, Location location, Runnable toRun) {
        region.run(plugin, location, scheduledTask -> toRun.run());
    }

    @Override
    public TaskHolder<ScheduledTask> runEntityTaskAsync(Plugin plugin, Entity entity, long delay, Runnable toRun) {
        return runEntityTask(plugin, entity, delay, toRun);
    }

    @Override
    public TaskHolder<ScheduledTask> runEntityTaskAsync(Plugin plugin, Entity entity, long delay, long period, Runnable toRun) {
        return holder(entity.getScheduler().runAtFixedRate(plugin, scheduledTask -> toRun.run(), null, minDelay(delay), period));
    }

    @Override
    public void runEntityTaskAsync(Plugin plugin, Entity entity, Runnable toRun) {
        runEntityTask(plugin, entity, toRun);
    }

    @Override
    public TaskHolder<ScheduledTask> runLocationTaskAsync(Plugin plugin, Location location, long delay, Runnable toRun) {
        return runLocationTask(plugin, location, delay, toRun);
    }

    @Override
    public void runLocationTaskAsync(Plugin plugin, Location location, Runnable toRun) {
        runLocationTask(plugin, location, toRun);
    }

    private long minDelay(long delay) {
        return Math.max(1, delay);
    }
}
