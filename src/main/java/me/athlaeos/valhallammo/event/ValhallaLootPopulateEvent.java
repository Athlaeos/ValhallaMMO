package me.athlaeos.valhallammo.event;

import me.athlaeos.valhallammo.loot.LootTable;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ValhallaLootPopulateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final LootTable table;
    private final LootContext context;
    private final List<ItemStack> drops;
    private boolean cancelled = false;
    private LootTable.VanillaLootPreservationType preservationType;
    public ValhallaLootPopulateEvent(LootTable table, LootContext context, List<ItemStack> drops) {
        this.table = table;
        this.context = context;
        this.drops = drops;
        preservationType = table.getVanillaLootPreservationType();
    }

    public LootTable.VanillaLootPreservationType getPreservationType() {
        return preservationType;
    }

    public void setPreservationType(LootTable.VanillaLootPreservationType preservationType) {
        this.preservationType = preservationType;
    }

    public LootTable getTable() {
        return table;
    }

    public List<ItemStack> getDrops() {
        return drops;
    }

    public LootContext getContext() {
        return context;
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
}
