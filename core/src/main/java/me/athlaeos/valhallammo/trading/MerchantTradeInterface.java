package me.athlaeos.valhallammo.trading;

import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;

import java.util.List;

public class MerchantTradeInterface extends VirtualMerchant {
    public MerchantTradeInterface(PlayerMenuUtility utility, Villager villager, List<MerchantRecipe> recipes) {
        super(utility, villager, recipes);
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onOpen() {

    }

    @Override
    public String getMenuName() {
        return "test";
    }
}
