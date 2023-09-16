package me.athlaeos.valhallammo.event;

import me.athlaeos.valhallammo.crafting.recipetypes.DynamicBrewingRecipe;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerCustomBrewEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled = false;
    private final Player brewer;
    private final DynamicBrewingRecipe recipe;
    private ItemStack result;
    private final BrewingStand stand;
    private final boolean success;

    public PlayerCustomBrewEvent(Player brewer, DynamicBrewingRecipe recipe, ItemStack result, BrewingStand stand, boolean success){
        this.brewer = brewer;
        this.recipe = recipe;
        this.result = result;
        this.stand = stand;
        this.success = success;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public Player getBrewer() {
        return brewer;
    }

    public DynamicBrewingRecipe getRecipe() {
        return recipe;
    }

    public BrewingStand getStand() {
        return stand;
    }

    /**
     * The result of the custom brewing event. If null is returned, the itemstack in
     * the brewing stand is not changed
     * @return the item result of the brewing event
     */
    public ItemStack getResult() {
        return result;
    }

    /**
     * Sets the result of the custom brewing event. The item in the brewing stand will be
     * replaced with this new result unless it is null.
     * @param result the new result of the recipe
     */
    public void setResult(ItemStack result) {
        this.result = result;
    }

    /**
     * Returns if the recipe went successfully or not. If false, one of the dynamic item modifiers failed and returned null.
     * If true, recipe was successful and result was modified;
     * @return true if successful, false otherwise.
     */
    public boolean isSuccessful() {
        return success;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}