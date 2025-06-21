package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LayoutConfigurationMenu extends Menu {
    private ItemStack button = null;
    private final List<Integer> secondaryButtonIndexes;
    private int primaryButtonIndex = -1;
    private int rows = 6;
    private boolean offsetRow = false;
    private final Menu previousMenu;

    public LayoutConfigurationMenu(PlayerMenuUtility playerMenuUtility, Menu previousMenu, int rows, ItemStack button, int primaryButtonIndex, List<Integer> secondaryButtonIndexes) {
        super(playerMenuUtility);
        this.previousMenu = previousMenu;
        this.rows = rows;
        this.button = button;
        this.primaryButtonIndex = primaryButtonIndex;
        this.secondaryButtonIndexes = secondaryButtonIndexes;
    }

    @Override
    public String getMenuName() {
        return Utils.chat("&8Configure Layout");
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getRawSlot() == 45) rows = Math.max(1, Math.min(6, rows + (e.isLeftClick() ? 1 : -1)));
        else if (e.getRawSlot() == 46 && rows > 5) offsetRow = !offsetRow;
        else if (e.getRawSlot() == 53) {
            if (previousMenu instanceof LayoutConfigurable lc) {
                if (primaryButtonIndex < 0 || ItemUtils.isEmpty(button) || primaryButtonIndex >= 9 * rows) {
                    Utils.sendMessage(e.getWhoClicked(), "&cA primary button is required!");
                    return;
                }
                lc.setConfiguration(rows, primaryButtonIndex, button, secondaryButtonIndexes);
            }
            previousMenu.open();
            return;
        } else if (e.getRawSlot() < Math.min(45, 9 * rows)) {
            int clickedGridIndex = e.getRawSlot() + (offsetRow ? 9 : 0);
              ItemStack cursor = e.getCursor();
              if (ItemUtils.isEmpty(cursor)) {
                  if (e.isLeftClick()) secondaryButtonIndexes.remove(clickedGridIndex);
                  else secondaryButtonIndexes.add(clickedGridIndex);
              } else {
                  primaryButtonIndex = clickedGridIndex;
                  button = cursor.clone();
              }
        }
        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        e.setCancelled(true);
        if (e.getRawSlots().size() == 1){
            ClickType type = e.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT;
            InventoryAction action = e.getType() == DragType.EVEN ? InventoryAction.DROP_ALL_CURSOR : InventoryAction.DROP_ONE_CURSOR;
            handleMenu(new InventoryClickEvent(e.getView(), InventoryType.SlotType.CONTAINER, new ArrayList<>(e.getRawSlots()).get(0), type, action));
        }
    }

    @Override
    public void setMenuItems() {
        inventory.clear();
        if (rows <= 4) offsetRow = false;

        int primaryIndex = primaryButtonIndex + (offsetRow ? -9 : 0);
        if (!ItemUtils.isEmpty(button) && primaryIndex >= 0) inventory.setItem(primaryIndex, button.clone());
        for (int i : secondaryButtonIndexes) {
            if (!offsetRow && i >= 45) continue;
            int secondaryIndex = i + (offsetRow ? -9 : 0);
            if (secondaryIndex < 0) continue;
            inventory.setItem(i, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).name("").get());
        }
        if (rows < 5) {
            for (int i = 44; i >= 9 * rows; i--){
                inventory.setItem(i, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).name("").get());
            }
        }

        inventory.setItem(53, backToMenuButton);
        inventory.setItem(49, new ItemBuilder(Material.KNOWLEDGE_BOOK).name("&fInfo")
                .lore(
                        "&fThis menu allows you to configure",
                        "&fthe layout of the menu",
                        "",
                        "&aA placed item will serve as the primary",
                        "&abutton for display purposes. Clicking",
                        "&athis button will activate whatever service",
                        "&athis menu is configuring. ",
                        "",
                        "&aRight-Clicking without item onto the grid",
                        "&amarks the slot as a secondary button.",
                        "&aSecondary buttons may also be clicked to",
                        "&aactivate the service, but they're invisible",
                        "&cLeft-Click such slots to undo them",
                        "",
                        "&fThe whole purpose for this is so that you may",
                        "&fplace buttons bigger than 1 item slot in the menu"
                ).get());
        inventory.setItem(45, new ItemBuilder(Material.CHEST).name("&fMenu Size")
                .lore(
                        "&aCurrently set to " + rows,
                        "&fDetermines the amount of rows the menu has",
                        "",
                        "&6Click to add/decrease"
                ).get());
        if (rows > 5) inventory.setItem(46, new ItemBuilder(Material.REDSTONE_TORCH).name("&fRow Selection")
                .lore(
                        "&fSince more rows are involved than this menu",
                        "&fcan show, currently displaying rows &a" + (offsetRow ? "2-6" : "1-5"),
                        "",
                        "&6Click to toggle"
                ).get());
    }

    private static final ItemStack backToMenuButton = new ItemBuilder(getButtonData("editor_backtomenu", Material.BOOK))
            .name("&fSave Changes").get();
}
