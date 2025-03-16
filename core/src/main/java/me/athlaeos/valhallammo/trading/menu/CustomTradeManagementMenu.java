package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class CustomTradeManagementMenu extends Menu {
    private View view = View.PROFESSIONS;
    public CustomTradeManagementMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return "&8Manage Trades"; // TODO custom menu
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {

    }

    @Override
    public void handleMenu(InventoryDragEvent e) {

    }

    @Override
    public void setMenuItems() {

    }

    private enum View{
        PROFESSIONS,
        SUBTYPES,
        SUBTYPE,
        TRADES,
        TRADE
    }
}
