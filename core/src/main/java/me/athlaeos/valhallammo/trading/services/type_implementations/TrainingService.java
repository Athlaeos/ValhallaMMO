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

public class TrainingService extends ServiceType {
    private final DynamicButton button = Catch.catchOrElse(() -> new DynamicButton(CustomMerchantManager.getTradingConfig().getString("service_button_type_training", "")), null);

    @Override
    public String getID() {
        return "TRAINING";
    }

    @Override
    public void onServiceSelect(InventoryClickEvent e, ServiceMenu menu, Service service, MerchantData data) {

    }

    @Override
    public ItemStack getButtonIcon(ServiceMenu menu, Service service, MerchantData data) {
        return button.get(ButtonSize.defaultFromButtonCount(menu.getServices().size()))
                .name(TranslationManager.translatePlaceholders(CustomMerchantManager.getTradingConfig().getString("service_button_name_training")))
                .lore(TranslationManager.translateListPlaceholders(
                        CustomMerchantManager.getTradingConfig().getStringList("service_button_description_training"))
                ).get();
    }

    @Override
    public void onTypeConfigurationSelect(InventoryClickEvent e, Service service, MerchantServicesMenu menu) {

    }

    @Override
    public ItemStack getDefaultButton() {
        return new ItemBuilder(Material.EMERALD)
                .name("&eTraining")
                .lore("&7Allows players to purchase",
                        "&7skill levels or experience",
                        "&7in exchange for items")
                .get();
    }
}
