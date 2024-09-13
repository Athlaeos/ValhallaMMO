package me.athlaeos.valhallammo.trading;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.List;

public abstract class VirtualMerchant {
    protected Merchant merchant;
    protected PlayerMenuUtility playerMenuUtility;

    public VirtualMerchant(PlayerMenuUtility utility, List<MerchantRecipe> recipes){
        this.playerMenuUtility = utility;
        this.merchant = Bukkit.createMerchant(getMenuName());
        this.merchant.setRecipes(recipes);
    }

    public abstract String getMenuName();

    public void open(){
        playerMenuUtility.getOwner().openMerchant(merchant, true);
    }

    public Merchant getMerchant(){
        return merchant;
    }
}
