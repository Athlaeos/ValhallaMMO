package me.athlaeos.valhallammo.platform.folia;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.athlaeos.valhallammo.platform.ValhallaPlatform;
import me.athlaeos.valhallammo.platform.scheduler.ValhallaScheduler;

public class FoliaPlatform implements ValhallaPlatform<ScheduledTask> {
    private FoliaValhallaScheduler scheduler = new FoliaValhallaScheduler();

    public static boolean usingFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public boolean supportsScoreboard() {
        return false;
    }

    @Override
    public boolean supportsPaper() {
        return true;
    }

    @Override
    public boolean supportsFolia() {
        return true;
    }

    @Override
    public ValhallaScheduler<ScheduledTask> getScheduler() {
        return scheduler;
    }
}
