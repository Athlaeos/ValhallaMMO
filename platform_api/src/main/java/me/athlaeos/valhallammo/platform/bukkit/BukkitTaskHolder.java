package me.athlaeos.valhallammo.platform.bukkit;

import me.athlaeos.valhallammo.platform.scheduler.TaskHolder;
import org.bukkit.scheduler.BukkitTask;
import javax.annotation.Nonnull;

public class BukkitTaskHolder implements TaskHolder<BukkitTask> {

    private final BukkitTask parent;

    public BukkitTaskHolder(@Nonnull BukkitTask parent) {
        this.parent = parent;
    }

    @Override
    public BukkitTask getParent() {
        return parent;
    }

    @Override
    public void cancel() {
        parent.cancel();
    }

    @Override
    public boolean isCancelled() {
        return parent.isCancelled();
    }
}
