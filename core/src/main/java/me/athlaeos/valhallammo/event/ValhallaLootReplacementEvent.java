package me.athlaeos.valhallammo.event;

import me.athlaeos.valhallammo.loot.ReplacementTable;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.NotNull;

public class ValhallaLootReplacementEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final ReplacementTable table;
    private final LootContext context;
    private boolean cancelled = false;
    private boolean executeGlobal = true;
    public ValhallaLootReplacementEvent(ReplacementTable table, LootContext context) {
        this.table = table;
        this.context = context;
    }

    public ReplacementTable getTable() {
        return table;
    }

    public LootContext getContext() {
        return context;
    }

    public void setExecuteGlobal(boolean executeGlobal) {
        this.executeGlobal = executeGlobal;
    }

    public boolean executeGlobal() {
        return executeGlobal;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
