package me.athlaeos.valhallammo.platform.bukkit;

import me.athlaeos.valhallammo.platform.ValhallaPlatform;
import me.athlaeos.valhallammo.platform.scheduler.ValhallaScheduler;
import org.bukkit.scheduler.BukkitTask;

public class BukkitPlatform implements ValhallaPlatform<BukkitTask> {
    private final BukkitValhallaScheduler scheduler = new BukkitValhallaScheduler();

    @Override
    public boolean supportsScoreboard() {
        return true;
    }

    @Override
    public boolean supportsPaper() {
        return false;
    }

    @Override
    public boolean supportsFolia() {
        return false;
    }

    @Override
    public ValhallaScheduler<BukkitTask> getScheduler() {
        return scheduler;
    }
}
