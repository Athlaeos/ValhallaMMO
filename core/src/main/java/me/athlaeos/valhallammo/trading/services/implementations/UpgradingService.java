package me.athlaeos.valhallammo.trading.services.implementations;

import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.services.Service;
import me.athlaeos.valhallammo.trading.menu.ServiceMenu;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class UpgradingService extends Service {
    @Override
    public String getID() {
        return "UPGRADING";
    }

    @Override
    public void onSelect(InventoryClickEvent e, ServiceMenu menu, MerchantData data) {

    }

    @Override
    public ItemStack getButtonIcon(ServiceMenu serviceMenu, MerchantData data) {
        return new ItemBuilder(Material.LIME_DYE)
                .name("&aUpgrade")
                .lore("&8Upgrade your dumbass items")
                .data(serviceMenu.getServices().size() <= 3 ? 9199210 :
                        serviceMenu.getServices().size() <= 6 ? 9199211 :
                                9199212)
                .get();
    }
}
