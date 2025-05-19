package me.athlaeos.valhallammo.trading.services.implementations;

import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.services.Service;
import me.athlaeos.valhallammo.trading.menu.ServiceMenu;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class TrainingService extends Service {
    @Override
    public String getID() {
        return "TRAINING";
    }

    @Override
    public void onSelect(InventoryClickEvent e, ServiceMenu menu, MerchantData data) {

    }

    @Override
    public ItemStack getButtonIcon(ServiceMenu serviceMenu, MerchantData data) {
        return new ItemBuilder(Material.LIME_DYE)
                .name("&aTrain")
                .lore("&8Train your skills for money")
                .data(serviceMenu.getServices().size() <= 3 ? 9199207 :
                        serviceMenu.getServices().size() <= 6 ? 9199208 :
                                9199209)
                .get();
    }
}
