package me.athlaeos.valhallammo.trading;

import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;

public class PlayerTradeItemEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Villager villager;
    private final Merchant merchant;
    private final MerchantRecipe recipeTraded;
    private final int timesTraded;
    private ItemStack result;
    private GossipTypeWrapper reputationInfluence;
    private boolean cancelled = false;

    public PlayerTradeItemEvent(@NotNull Player who, Villager villager, Merchant merchant, MerchantRecipe recipeTraded, ItemStack result, int timesTraded, GossipTypeWrapper reputationInfluence) {
        super(who);
        this.merchant = merchant;
        this.villager = villager;
        this.recipeTraded = recipeTraded;
        this.timesTraded = timesTraded;
        this.result = result;
        this.reputationInfluence = reputationInfluence;
    }

    /**
     * @return The merchant involved in this event
     */
    public Merchant getMerchant() { return merchant; }

    /**
     * @return The Villager associated with the Merchant. May be null, as Merchant does not need a Villager
     */
    public Villager getVillager() { return villager; }

    /**
     * @return The recipe traded
     */
    public MerchantRecipe getRecipeTraded() { return recipeTraded; }

    /**
     * @return The amount of times the MerchantRecipe was traded. Modifying this will change how many times the player receives the item traded
     */
    public int getTimesTraded() { return timesTraded; }
    public GossipTypeWrapper getReputationInfluence() { return reputationInfluence; }
    public ItemStack getResult() { return result; }
    public void setReputationInfluence(GossipTypeWrapper reputationInfluence) { this.reputationInfluence = reputationInfluence; }
    public void setResult(ItemStack result) { this.result = result; }

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
}
