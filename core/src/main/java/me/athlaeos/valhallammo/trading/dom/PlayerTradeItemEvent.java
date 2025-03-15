package me.athlaeos.valhallammo.trading.dom;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerTradeItemEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final UUID merchant;
    private final Merchant merchantInventory;
    private final MerchantRecipe recipeTraded;
    private final MerchantTrade customTrade;
    private final MerchantData merchantData;
    private final int timesTraded;
    private ItemStack result;
    private boolean cancelled = false;

    public PlayerTradeItemEvent(@NotNull Player who, UUID id, MerchantData merchantData, Merchant merchant, MerchantRecipe recipeTraded, MerchantTrade trade, ItemStack result, int timesTraded) {
        super(who);
        this.merchantInventory = merchant;
        this.merchant = id;
        this.recipeTraded = recipeTraded;
        this.customTrade = trade;
        this.timesTraded = timesTraded;
        this.merchantData = merchantData;
        this.result = result;
    }

    /**
     * @return The merchant involved in this event
     */
    public Merchant getMerchantInventory() { return merchantInventory; }

    /**
     * @return The ID associated with the custom merchant
     */
    public UUID getMerchant() { return merchant; }

    /**
     * @return The recipe traded
     */
    public MerchantRecipe getRecipeTraded() { return recipeTraded; }

    /**
     * @return The amount of times the MerchantRecipe was traded. Modifying this will change how many times the player receives the item traded
     */
    public int getTimesTraded() { return timesTraded; }
    public ItemStack getResult() { return result; }
    public void setResult(ItemStack result) { this.result = result; }
    public MerchantTrade getCustomTrade() { return customTrade; }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public MerchantData getMerchantData() {
        return merchantData;
    }
}
