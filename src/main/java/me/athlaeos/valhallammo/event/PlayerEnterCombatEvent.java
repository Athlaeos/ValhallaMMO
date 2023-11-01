package me.athlaeos.valhallammo.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerEnterCombatEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled = false;
    private long when;

    public PlayerEnterCombatEvent(Player who) {
        super(who);
        when = System.currentTimeMillis();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * returns the time in milliseconds when the player supposedly entered combat
     * @return the time in milliseconds when the player entered combat
     */
    public long getWhen() {
        return when;
    }

    /**
     * sets the time where the player supposedly entered combat. increasing the time decreasing the time the player
     * spent in combat, decreasing it increases the time the player spent in combat
     * @param when the time at which the player entered combat
     */
    public void setWhen(long when) {
        this.when = when;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
