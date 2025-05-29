package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.trading.services.service_implementations.OrderService;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class OrderingServiceConfigurationMenu extends SimpleConfigurationMenu<OrderService> {
    public OrderingServiceConfigurationMenu(PlayerMenuUtility playerMenuUtility, Menu previousMenu, OrderService thingyToConfigure) {
        super(playerMenuUtility, previousMenu, thingyToConfigure, new ArrayList<>());

        addButton(new Button(Material.CLOCK, 13, () -> "&fOrder Time", () -> List.of(
                "&7Currently " + thingyToConfigure.getBaseOrderTime(),
                "&8(1000 being 1 in-game hour, 24000 being a day)",
                "&8Determines the base amount of time until",
                "&8an order can be picked up. Typically increases",
                "&8the more items and trades are ordered",
                "&6Click to increase/decrease by 1 hour",
                "&6Shift-Click to do so by 1 day"
        ), (service, event) -> service.setBaseOrderTime(Math.max(0, service.getBaseOrderTime() + ((event.isShiftClick() ? 24000 : 1000) * (event.isLeftClick() ? 1 : -1))))));

        addButton(new Button(Material.CLOCK, 21, () -> "&fExtra Time per Order", () -> List.of(
                "&7Currently " + thingyToConfigure.getOrderTimeBonusPerItem(),
                "&8(1000 being 1 in-game hour, 24000 being a day)",
                "&8Every time a multiple of a trade is ordered,",
                "&8this amount of time is added to the delivery",
                "&8time. 5 orders of 1 trade would add " + (thingyToConfigure.getOrderTimeBonusPerItem() * 5),
                "&8to the delivery time",
                "&6Click to increase/decrease by 0.1 hours",
                "&6Shift-Click to do so by 1 hour"
        ), (service, event) -> service.setOrderTimeBonusPerItem(service.getOrderTimeBonusPerItem() + ((event.isShiftClick() ? 1000 : 100) * (event.isLeftClick() ? 1 : -1)))));

        addButton(new Button(Material.CLOCK, 23, () -> "&fExtra Time per Trade", () -> List.of(
                "&7Currently " + thingyToConfigure.getOrderTimeBonusPerTrade(),
                "&8(1000 being 1 in-game hour, 24000 being a day)",
                "&8For every different trade ordered this",
                "&8amount of time is added to the delivery",
                "&8time. 1 order of 3 different trades would ",
                "&8add " + (thingyToConfigure.getOrderTimeBonusPerTrade() * 3),
                "&6Click to increase/decrease by 1 hour",
                "&6Shift-Click to do so by 1 day"
        ), (service, event) -> service.setOrderTimeBonusPerTrade(Math.max(0, service.getOrderTimeBonusPerTrade() + ((event.isShiftClick() ? 24000 : 1000) * (event.isLeftClick() ? 1 : -1))))));

        addButton(new Button(Material.BARREL, 39, () -> "&fBulk Discount Minimum", () -> List.of(
                "&7Currently " + thingyToConfigure.getBulkMinimumOrdersForDiscount(),
                "&8Determines the minimum amount of orders until",
                "&8the trade becomes legible for bulk discounts.",
                "&8Bulk discounts only affect the one trade.",
                "&6Click to increase/decrease by 1",
                "&6Shift-Click to do so by 8"
        ), (service, event) -> service.setBulkMinimumOrdersForDiscount(Math.max(0, service.getBulkMinimumOrdersForDiscount() + ((event.isShiftClick() ? 8 : 1) * (event.isLeftClick() ? 1 : -1))))));

        addButton(new Button(Material.BARREL, 40, () -> "&fBulk Discount per Order", () -> List.of(
                String.format("&7Currently %.1f/order", thingyToConfigure.getBulkDiscountPerItem() * 100),
                "&8Determines the discount per order",
                "&8above the required bulk discount minimum.",
                "&8With a discount of 1%, a minimum of 10,",
                "&8and with 20 orders you would get a 10%",
                "&8discount",
                "&6Click to increase/decrease by 0.1%",
                "&6Shift-Click to do so by 2.5%"
        ), (service, event) -> service.setBulkDiscountPerItem(service.getBulkDiscountPerItem() + ((event.isShiftClick() ? 0.025F : 0.001F) * (event.isLeftClick() ? 1 : -1)))));

        addButton(new Button(Material.BARREL, 41, () -> "&fMax Bulk Discount", () -> List.of(
                String.format("&7Currently %.1f", thingyToConfigure.getBulkMaxDiscount() * 100),
                "&8Determines the maximum discount",
                "&8attainable with a bulk order.",
                "&6Click to increase/decrease by 0.1%",
                "&6Shift-Click to do so by 2.5%"
        ), (service, event) -> service.setBulkMaxDiscount(Math.max(0, service.getBulkMaxDiscount() + ((event.isShiftClick() ? 0.025F : 0.001F) * (event.isLeftClick() ? 1 : -1))))));
    }

    @Override
    public String getMenuName() {
        return Utils.chat("&8Configure Ordering Service " + thingyToConfigure.getID()); // TODO data driven
    }
}
