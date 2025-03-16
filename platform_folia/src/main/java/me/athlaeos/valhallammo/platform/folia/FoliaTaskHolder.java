package me.athlaeos.valhallammo.platform.folia;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.athlaeos.valhallammo.platform.scheduler.TaskHolder;
import org.checkerframework.checker.nullness.qual.NonNull;

public class FoliaTaskHolder implements TaskHolder<ScheduledTask> {

    private final ScheduledTask parent;

    public FoliaTaskHolder(@NonNull ScheduledTask parent) {
        this.parent = parent;
    }

    @Override
    public ScheduledTask getParent() {
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
