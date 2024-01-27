package me.athlaeos.valhallammo.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerLeaveCombatEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private long when;
    private final long left;

    public PlayerLeaveCombatEvent(Player who, long playerEnterCombatTime, long playerLeftCombatTime) {
        super(who);
        this.when = playerEnterCombatTime;
        this.left = playerLeftCombatTime;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * returns the time between the starting time and the alternativeStartingTime
     * @param alternativeLeaveCombatTime the supposed time the player entered combat in
     * @return the time between where the player entered combat and when they left it
     */
    public long getTimeInCombat(long alternativeLeaveCombatTime){
        return alternativeLeaveCombatTime - when;
    }

    public long getTimeInCombat() { return left - when; }

    /**
     * returns the time in milliseconds when the player supposedly left combat
     * @return the time in milliseconds when the player left combat
     */
    public long getWhen() {
        return when;
    }

    /**
     * sets the time where the player supposedly left combat. increasing the time increases the time the player
     * spent in combat, decreasing it decreases the time the player spent in combat
     * @param when the time at which the player left combat
     */
    public void setWhen(long when) {
        this.when = when;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
