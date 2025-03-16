package me.athlaeos.valhallammo.platform.paper;

import me.athlaeos.valhallammo.platform.ValhallaPlatform;
import me.athlaeos.valhallammo.platform.bukkit.BukkitValhallaScheduler;
import me.athlaeos.valhallammo.platform.scheduler.ValhallaScheduler;
import org.bukkit.scheduler.BukkitTask;

public class PaperPlatform implements ValhallaPlatform<BukkitTask> {
    private final BukkitValhallaScheduler scheduler = new BukkitValhallaScheduler();

    public static boolean usingPaper() {
        try {
            Class.forName("com.destroystokyo.paper.loottable.LootableInventory");
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public boolean supportsScoreboard() {
        return true;
    }

    @Override
    public boolean supportsPaper() {
        return true;
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
