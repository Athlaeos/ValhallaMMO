package me.athlaeos.valhallammo.trading.services.type_implementations;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.dom.MerchantTrade;
import me.athlaeos.valhallammo.trading.menu.MerchantServicesMenu;
import me.athlaeos.valhallammo.trading.menu.OrderingServiceConfigurationMenu;
import me.athlaeos.valhallammo.trading.menu.ServiceMenu;
import me.athlaeos.valhallammo.trading.menu.ServiceOrderingMenu;
import me.athlaeos.valhallammo.trading.services.ServiceType;
import me.athlaeos.valhallammo.trading.services.service_implementations.OrderService;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OrderingService extends ServiceType<OrderService> {
    private final DynamicButton button = Catch.catchOrElse(() -> new DynamicButton(CustomMerchantManager.getTradingConfig().getString("service_button_type_ordering", "")), null);
    @Override
    public String getID() {
        return "ORDERING";
    }

    @Override
    public void onServiceSelect(InventoryClickEvent e, ServiceMenu menu, OrderService service, MerchantData data) {
        MerchantData.OrderData pendingOrder = data.getPendingOrders().get(menu.getPlayerMenuUtility().getOwner().getUniqueId());
        if (pendingOrder != null) {
            if (pendingOrder.shouldReceive()) {
                List<ItemStack> toReceive = new ArrayList<>();

                for (String order : pendingOrder.getOrder().keySet()){
                    int quantity = pendingOrder.getOrder().get(order);
                    MerchantTrade trade = CustomMerchantManager.getTrade(order);
                    if (trade == null) continue;
                    for (int i = 0; i < quantity; i++){
                        ItemBuilder result = new ItemBuilder(trade.getResult());
                        DynamicItemModifier.modify(ModifierContext.builder(result).setOtherType(data).entity(data.getVillager()).crafter(menu.getPlayerMenuUtility().getOwner()).executeUsageMechanics().validate().get(), trade.getModifiers());
                        if (ItemUtils.isEmpty(result.getItem()) || CustomFlag.hasFlag(result.getMeta(), CustomFlag.UNCRAFTABLE)) continue;
                        toReceive.add(result.get());
                    }
                }
                for (ItemStack item : toReceive) {
                    ItemUtils.addItem(menu.getPlayerMenuUtility().getOwner(), item, true);
                }

                menu.getPlayerMenuUtility().getOwner().closeInventory();
                data.getPendingOrders().remove(menu.getPlayerMenuUtility().getOwner().getUniqueId());
            } else if (e.isShiftClick()) {
                ServiceOrderingMenu m = new ServiceOrderingMenu(menu.getPlayerMenuUtility(), data);
                if (m.getOrderableTrades().isEmpty()) {
                    if (data.getVillager() instanceof Villager villager) villager.shakeHead();
                } else m.open();
            }
        } else {
            ServiceOrderingMenu m = new ServiceOrderingMenu(menu.getPlayerMenuUtility(), data);
            if (m.getOrderableTrades().isEmpty()) {
                if (data.getVillager() instanceof Villager villager) villager.shakeHead();
            } else m.open();
        }
    }

    @Override
    public ItemStack getButtonIcon(ServiceMenu menu, OrderService service, MerchantData data) {
        MerchantData.OrderData pendingOrder = data.getPendingOrders().get(menu.getPlayerMenuUtility().getOwner().getUniqueId());
        List<String> value = pendingOrder == null ? CustomMerchantManager.getTradingConfig().getStringList("service_button_description_ordering") :
                (pendingOrder.shouldReceive() ? CustomMerchantManager.getTradingConfig().getStringList("service_button_description_ordering_ready") :
                        CustomMerchantManager.getTradingConfig().getStringList("service_button_description_ordering_pending"));

        return button.get(ButtonSize.defaultFromButtonCount(menu.getServices().size()))
                .name(TranslationManager.translatePlaceholders(CustomMerchantManager.getTradingConfig().getString("service_button_name_ordering")))
                .lore(TranslationManager.translateListPlaceholders(
                        value
                ))
                .placeholderLore("%time%", pendingOrder == null ? "" : ServiceOrderingMenu.timeFormat(pendingOrder.getRemainingTime()))
                .get();
    }

    @Override
    public void onTypeConfigurationSelect(InventoryClickEvent e, OrderService service, MerchantServicesMenu menu) {
        new OrderingServiceConfigurationMenu(menu.getPlayerMenuUtility(), menu, service).open();
    }
}
