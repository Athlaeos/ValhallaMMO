package me.athlaeos.valhallammo.trading.services.implementations;

import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantConfiguration;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.merchants.VirtualMerchant;
import me.athlaeos.valhallammo.trading.merchants.implementations.SimpleMerchant;
import me.athlaeos.valhallammo.trading.services.Service;
import me.athlaeos.valhallammo.trading.menu.ServiceMenu;
import org.bukkit.Material;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.List;

public class TradingService extends Service {
    private final Villager.Profession profession;

    public TradingService(Villager.Profession profession){
        this.profession = profession;
    }

    @Override
    public String getID() {
        return "TRADING_" + (profession == null ? "TRAVELING" : profession);
    }

    @Override
    public void onSelect(InventoryClickEvent e, ServiceMenu menu, MerchantData data) {
        AbstractVillager v = data.getVillager();
        if (v instanceof Villager villager && villager.getProfession() != profession) return;
        else if (!(v instanceof Villager) && profession != null) return;
        MerchantConfiguration configuration = v instanceof Villager villager ? CustomMerchantManager.getMerchantConfigurations().get(villager.getProfession()) : CustomMerchantManager.getTravelingMerchantConfiguration();
        if (configuration == null || configuration.getMerchantTypes().isEmpty()) return;
        List<MerchantRecipe> recipes = CustomMerchantManager.recipesFromData(data, menu.getPlayerMenuUtility().getOwner());
        if (recipes != null) {
            VirtualMerchant merchant = new SimpleMerchant(PlayerMenuUtilManager.getPlayerMenuUtility(menu.getPlayerMenuUtility().getOwner()), v.getUniqueId(), data, recipes);
            if (merchant.getRecipes().isEmpty()) {
                if (v instanceof Villager villager) {
                    villager.shakeHead();
                    menu.getPlayerMenuUtility().getOwner().closeInventory();
                }
            } else merchant.open();
        } else if (v instanceof Villager villager) {
            villager.shakeHead();
            menu.getPlayerMenuUtility().getOwner().closeInventory();
        }
    }

    @Override
    public ItemStack getButtonIcon(ServiceMenu serviceMenu, MerchantData data) {
        return new ItemBuilder(Material.LIME_DYE)
                .name("&aTrade")
                .lore("&8Trade with this man")
                .data(serviceMenu.getServices().size() <= 3 ? 9199201 :
                        serviceMenu.getServices().size() <= 6 ? 9199202 :
                                9199203)
                .get();
    }
}
