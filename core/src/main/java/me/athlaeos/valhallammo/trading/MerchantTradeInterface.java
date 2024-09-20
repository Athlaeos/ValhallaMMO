package me.athlaeos.valhallammo.trading;

import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import org.bukkit.inventory.MerchantRecipe;

import java.util.List;

public class MerchantTradeInterface extends VirtualMerchant {
    public MerchantTradeInterface(PlayerMenuUtility utility, List<MerchantRecipe> recipes) {
        super(utility, recipes);
    }

    @Override
    public void onClose() {

    }

    @Override
    public String getMenuName() {
        return "test";
    }
}
