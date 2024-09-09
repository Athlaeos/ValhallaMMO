package me.athlaeos.valhallammo.villagers;
import me.athlaeos.valhallammo.gui.MenuListener;
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

    public abstract void handleMenu(InventoryClickEvent e);

    public abstract void handleMenu(InventoryDragEvent e);

    public abstract void setMenuItems();

    public void open(){

        this.setMenuItems();
        MerchantListener.setActiveMenu(playerMenuUtility.getOwner(), this);

        playerMenuUtility.getOwner().openMerchant(merchant, true);
    }

    public Merchant getMerchant(){
        return merchant;
    }
}
