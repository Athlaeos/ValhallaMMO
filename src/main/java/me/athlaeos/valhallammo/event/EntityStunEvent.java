package me.athlaeos.valhallammo.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class EntityStunEvent extends EntityEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private int duration;
    private final Entity causedBy;

    public EntityStunEvent(Entity who, Entity causedBy, int duration) {
        super(who);
        this.causedBy = causedBy;
        this.duration = duration;
    }

    @Override public @NotNull HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
    @Override public boolean isCancelled() { return cancelled; }
    public Entity getCausedBy() { return causedBy; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
}