package me.athlaeos.valhallammo.trading.merchants;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.listeners.MerchantListener;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class VirtualMerchant {
    private final Merchant merchant;
    private final UUID merchantID;
    private final MerchantData data;
    private final PlayerMenuUtility playerMenuUtility;
    private List<MerchantRecipe> recipes = new ArrayList<>();
    private int expToGrant = 0;

    private boolean changeRecipes = true;

    public VirtualMerchant(PlayerMenuUtility utility, UUID merchantID, MerchantData data, List<MerchantRecipe> recipes){
        this.data = data;
        this.playerMenuUtility = utility;
        this.merchantID = merchantID;
        this.merchant = Bukkit.createMerchant(getMenuName());
        this.recipes = recipes;
    }

    public UUID getMerchantID() {
        return merchantID;
    }

    public MerchantData getData() {
        return data;
    }

    public void setExpToGrant(int expToGrant) {
        this.expToGrant = Math.max(0, expToGrant);
    }

    public int getExpToGrant() {
        return expToGrant;
    }

    public List<MerchantRecipe> getRecipes() {
        if (!changeRecipes) return new ArrayList<>(recipes);
        return recipes;
    }

    public void setRecipes(List<MerchantRecipe> recipes) throws IllegalAccessException {
        if (!changeRecipes) throw new IllegalAccessException("You are not allowed to change the merchant's recipes at this time!");
        this.recipes = recipes;
    }

    public abstract void onClose();
    public abstract void onOpen();

    public abstract String getMenuName();

    public void open(){
        changeRecipes = false;
        this.merchant.setRecipes(recipes);
        VirtualMerchant oldMenu = MerchantListener.getCurrentActiveVirtualMerchant(playerMenuUtility.getOwner());
        if (oldMenu != null) MerchantListener.virtualMerchantClose(playerMenuUtility.getOwner(), oldMenu);
        MerchantListener.setActiveTradingMenu(playerMenuUtility.getOwner(), this);
        if (merchantID != null) MerchantListener.getTradingMerchants().add(merchantID);
        playerMenuUtility.getOwner().openMerchant(merchant, true);
        onOpen();
    }

    public Merchant getMerchant(){
        return merchant;
    }
}
