package me.athlaeos.valhallammo.platform;

import me.athlaeos.valhallammo.platform.scheduler.ValhallaScheduler;

/**
 * Represents a platform specific functions implementation and feature flags
 * @param <T> The task type used by this playform
 */
public interface ValhallaPlatform<T> {
    /**
     * @return If the default bukkit scoreboard is supported
     */
    public boolean supportsScoreboard();

    /**
     * @return If this platform supports paper
     */
    public boolean supportsPaper();


    /**
     * @return If this platform supports folia
     */
    public boolean supportsFolia();

    /**
     * @return Gets the platform specific scheduler
     */
    public ValhallaScheduler<T> getScheduler();

}
