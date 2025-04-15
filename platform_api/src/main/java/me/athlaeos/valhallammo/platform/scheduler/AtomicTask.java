package me.athlaeos.valhallammo.platform.scheduler;

public class AtomicTask<T> implements TaskHolder<TaskHolder<T>> {
    private TaskHolder<T> parent;

    public void setParent(TaskHolder<?> task) {
        if (task == null) return;
        parent = (TaskHolder<T>) task;
    }

    @Override
    public TaskHolder<T> getParent() {
        return parent;
    }

    @Override
    public void cancel() {
        if (parent == null) return;
        parent.cancel();
    }

    @Override
    public boolean isCancelled() {
        if (parent == null) return true;
        return parent.isCancelled();
    }
}
