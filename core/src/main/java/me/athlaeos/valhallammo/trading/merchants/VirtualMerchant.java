package me.athlaeos.valhallammo.trading.merchants;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.dom.MerchantTrade;
import me.athlaeos.valhallammo.trading.listeners.MerchantListener;
import me.athlaeos.valhallammo.trading.menu.MerchantMenu;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.*;

public abstract class VirtualMerchant implements MerchantMenu {
    private final Merchant merchant;
    private final UUID merchantID;
    private final MerchantData data;
    private final PlayerMenuUtility playerMenuUtility;
    private List<Pair<MerchantTrade, MerchantRecipe>> recipes = new ArrayList<>();
    private int expToGrant = 0;
    private final Map<String, Integer> maxTimesTradeable = new HashMap<>();

    private boolean changeRecipes = true;

    public VirtualMerchant(PlayerMenuUtility utility, UUID merchantID, MerchantData data, List<Pair<MerchantTrade, MerchantRecipe>> recipes){
        this.data = data;
        this.playerMenuUtility = utility;
        this.merchantID = merchantID;
        this.merchant = Bukkit.createMerchant(getMenuName());
        this.recipes = recipes;
    }

    @Override
    public UUID getMerchantID() {
        return merchantID;
    }

    @Override
    public MerchantData getData() {
        return data;
    }

    public void setExpToGrant(int expToGrant) {
        this.expToGrant = Math.max(0, expToGrant);
    }

    public int getExpToGrant() {
        return expToGrant;
    }

    public Map<String, Integer> getMaxTimesTradeable() {
        return maxTimesTradeable;
    }

    public void setMaxTimesTradeable(String trade, int quantity){
        maxTimesTradeable.put(trade, quantity);
    }

    public int getMaxTimesTradeable(String trade){
        return maxTimesTradeable.getOrDefault(trade, 0);
    }

    public List<Pair<MerchantTrade, MerchantRecipe>> getRecipes() {
        if (!changeRecipes) return new ArrayList<>(recipes);
        return recipes;
    }

    public void setRecipes(List<Pair<MerchantTrade, MerchantRecipe>> recipes) throws IllegalAccessException {
        if (!changeRecipes) throw new IllegalAccessException("You are not allowed to change the merchant's recipes at this time!");
        this.recipes = recipes;
    }

    public abstract String getMenuName();

    public void open(){
        changeRecipes = false;
        this.merchant.setRecipes(recipes.stream().map(Pair::getTwo).toList());
        MerchantMenu oldMenu = MerchantListener.getCurrentActiveVirtualMerchant(playerMenuUtility.getOwner());
        if (oldMenu instanceof VirtualMerchant virtualMerchant) MerchantListener.virtualMerchantClose(playerMenuUtility.getOwner(), virtualMerchant);
        MerchantListener.setActiveTradingMenu(playerMenuUtility.getOwner(), this);
        if (merchantID != null) MerchantListener.getTradingMerchants().add(merchantID);
        playerMenuUtility.getOwner().openMerchant(merchant, true);
        onOpen();
    }

    public Merchant getMerchant(){
        return merchant;
    }
}
