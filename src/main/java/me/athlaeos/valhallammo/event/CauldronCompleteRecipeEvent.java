package me.athlaeos.valhallammo.event;

import me.athlaeos.valhallammo.crafting.recipetypes.DynamicCauldronRecipe;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CauldronCompleteRecipeEvent extends BlockEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Player crafter;
    private final DynamicCauldronRecipe recipe;
    private ItemStack result;

    public CauldronCompleteRecipeEvent(@NotNull Block theBlock, DynamicCauldronRecipe recipe, Player thrower, ItemStack result) {
        super(theBlock);
        this.recipe = recipe;
        this.crafter = thrower;
        this.result = result;
    }

    public Player getCrafter() {
        return crafter;
    }

    public DynamicCauldronRecipe getRecipe() {
        return recipe;
    }

    public ItemStack getResult() {
        return result;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}