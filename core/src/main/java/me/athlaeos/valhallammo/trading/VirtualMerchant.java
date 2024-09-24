package me.athlaeos.valhallammo.trading;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.List;

public abstract class VirtualMerchant {
    protected Merchant merchant;
    protected Villager villager;
    protected PlayerMenuUtility playerMenuUtility;

    public VirtualMerchant(PlayerMenuUtility utility, Villager villager, List<MerchantRecipe> recipes){
        this.playerMenuUtility = utility;
        this.villager = villager;
        this.merchant = Bukkit.createMerchant(getMenuName());
        this.merchant.setRecipes(recipes);
    }

    public abstract void onClose();
    public abstract void onOpen();

    public abstract String getMenuName();

    public void open(){
        playerMenuUtility.getOwner().openMerchant(merchant, true);
    }

    public Merchant getMerchant(){
        return merchant;
    }
}
