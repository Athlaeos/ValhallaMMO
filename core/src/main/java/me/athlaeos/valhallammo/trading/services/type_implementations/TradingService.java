package me.athlaeos.valhallammo.trading.services.type_implementations;

import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantConfiguration;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.menu.MerchantServicesMenu;
import me.athlaeos.valhallammo.trading.menu.ServiceMenu;
import me.athlaeos.valhallammo.trading.merchants.VirtualMerchant;
import me.athlaeos.valhallammo.trading.merchants.implementations.SimpleMerchant;
import me.athlaeos.valhallammo.trading.services.Service;
import me.athlaeos.valhallammo.trading.services.ServiceRegistry;
import me.athlaeos.valhallammo.trading.services.ServiceType;
import org.bukkit.Material;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.List;

public class TradingService extends ServiceType {
    private final DynamicButton button = Catch.catchOrElse(() -> new DynamicButton(CustomMerchantManager.getTradingConfig().getString("service_button_type_trading", "")), null);

    @Override
    public String getID() {
        return "TRADING";
    }

    @Override
    public void onServiceSelect(InventoryClickEvent e, ServiceMenu menu, Service service, MerchantData data) {
        AbstractVillager v = data.getVillager();
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
    public ItemStack getButtonIcon(ServiceMenu menu, Service service, MerchantData data) {
        return button.get(ButtonSize.defaultFromButtonCount(menu.getServices().size()))
                .name(TranslationManager.translatePlaceholders(CustomMerchantManager.getTradingConfig().getString("service_button_name_trading")))
                .lore(TranslationManager.translateListPlaceholders(
                                        CustomMerchantManager.getTradingConfig().getStringList("service_button_description_trading"))
                ).get();
    }

    @Override
    public void onTypeConfigurationSelect(InventoryClickEvent e, Service service, MerchantServicesMenu menu) {
        // do nothing, trading services are not configurable
        if (service == null) menu.getType().getServices().add(ServiceRegistry.SERVICE_TRADING.getID());
    }

    @Override
    public ItemStack getDefaultButton() {
        return new ItemBuilder(Material.EMERALD)
                .name("&aTrading")
                .lore("&7Allows trading with a merchant")
                .get();
    }
}
