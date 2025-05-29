package me.athlaeos.valhallammo.trading.services.type_implementations;

import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.menu.MerchantServicesMenu;
import me.athlaeos.valhallammo.trading.menu.ServiceMenu;
import me.athlaeos.valhallammo.trading.services.Service;
import me.athlaeos.valhallammo.trading.services.ServiceType;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class UpgradingService extends ServiceType {
    private final DynamicButton button = Catch.catchOrElse(() -> new DynamicButton(CustomMerchantManager.getTradingConfig().getString("service_button_type_upgrading", "")), null);

    @Override
    public String getID() {
        return "UPGRADING";
    }

    @Override
    public void onServiceSelect(InventoryClickEvent e, ServiceMenu menu, Service service, MerchantData data) {

    }

    @Override
    public ItemStack getButtonIcon(ServiceMenu menu, Service service, MerchantData data) {
        return button.get(ButtonSize.defaultFromButtonCount(menu.getServices().size()))
                .name(TranslationManager.translatePlaceholders(CustomMerchantManager.getTradingConfig().getString("service_button_name_upgrading")))
                .lore(TranslationManager.translateListPlaceholders(
                        CustomMerchantManager.getTradingConfig().getStringList("service_button_description_upgrading"))
                ).get();
    }

    @Override
    public void onTypeConfigurationSelect(InventoryClickEvent e, Service service, MerchantServicesMenu menu) {

    }

    @Override
    public ItemStack getDefaultButton() {
        return new ItemBuilder(Material.SMITHING_TABLE)
                .name("&eUpgrading")
                .lore("&7Allows players to upgrade or modify",
                        "&7their items in specific ways",
                        "&7in exchange for items")
                .get();
    }
}
