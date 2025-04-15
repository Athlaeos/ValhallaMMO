package me.athlaeos.valhallammo.platform.scheduler;

/**
 * Represents a holder for a platforms task
 * @param <T> The scheduled task for this platform
 */
public interface TaskHolder<T> {
    /**
     * @return the parent task, for this platform
     */
    T getParent();

    /**
     * Cancel this task
     */
    void cancel();

    /**
     * Check if this task is canceled
     * @return If the task has been canceled/ completed
     */
    boolean isCancelled();
}
