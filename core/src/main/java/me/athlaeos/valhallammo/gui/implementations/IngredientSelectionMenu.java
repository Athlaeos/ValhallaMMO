package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.SetIngredientsMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;
import java.util.stream.Collectors;

public class IngredientSelectionMenu extends Menu {
    private static final NamespacedKey KEY_ACTION = new NamespacedKey(ValhallaMMO.getInstance(), "key_action");

    private static final ItemStack confirmButton = new ItemBuilder(getButtonData("editor_ingredient_selection_save", Material.STRUCTURE_VOID))
            .name("&aConfirm")
            .stringTag(KEY_ACTION, "confirmButton")
            .lore("&aRight-click &7to save changes").get();
    private static final ItemStack cancelButton = new ItemBuilder(getButtonData("editor_ingredient_selection_return", Material.BOOK))
            .stringTag(KEY_ACTION, "cancelButton")
            .name("&7Return to menu without saving").get();

    private final Menu menu;

    private Map<ItemStack, Integer> currentIngredients = new HashMap<>();
    private List<ItemStack> listedIngredients = new ArrayList<>();

    private int stackCap = 45;

    public IngredientSelectionMenu(PlayerMenuUtility playerMenuUtility, Menu menu) {
        super(playerMenuUtility);
        this.menu = menu;

        if (menu instanceof SetIngredientsMenu m) {
            this.currentIngredients = m.getIngredients();
            this.listedIngredients = ItemUtils.decompressStacks(m.getIngredients());
        }
    }

    public IngredientSelectionMenu(PlayerMenuUtility playerMenuUtility, Menu menu, int stackCap) {
        super(playerMenuUtility);
        this.menu = menu;
        this.stackCap = Math.min(stackCap, 45);

        if (menu instanceof SetIngredientsMenu m) {
            this.currentIngredients = m.getIngredients();
            this.listedIngredients = ItemUtils.decompressStacks(m.getIngredients());
        }
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF300\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_ingredientselection"));
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        e.setCancelled(!(e.getClickedInventory() instanceof PlayerInventory));
        if (e.getRawSlot() < stackCap) e.setCancelled(false);

        ItemStack[] originalContents = Arrays.copyOfRange(inventory.getContents(), 0, stackCap);
        List<ItemStack> listedContents = new ArrayList<>(Arrays.asList(originalContents));
        listedContents.remove(confirmButton);

        Map<ItemStack, Integer> contents = ItemUtils.compressStacks(listedContents);

        listedIngredients = ItemUtils.decompressStacks(contents);
        currentIngredients = new HashMap<>(contents);

        if (!ItemUtils.isEmpty(clickedItem)) {
            String action = ItemUtils.getPDCString(KEY_ACTION, clickedItem, null);
            if (!StringUtils.isEmpty(action)){
                switch (action){
                    case "confirmButton" -> {
                        if (menu instanceof SetIngredientsMenu m){
                            m.setIngredients(currentIngredients);
                            menu.open();
                            return;
                        }
                    }
                    case "cancelButton" -> menu.open();
                }
            }
        }

        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        if (e.getRawSlots().size() == 1){
            e.setCancelled(true);
            ClickType type = e.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT;
            InventoryAction action = e.getType() == DragType.EVEN ? InventoryAction.DROP_ALL_CURSOR : InventoryAction.DROP_ONE_CURSOR;
            handleMenu(new InventoryClickEvent(e.getView(), InventoryType.SlotType.CONTAINER, new ArrayList<>(e.getRawSlots()).get(0), type, action));
        }
    }

    @Override
    public void setMenuItems() {
        inventory.clear();

        for (int i = 0; i < 45; i++){
            if (i < stackCap) inventory.setItem(i, null);
            else inventory.setItem(i, new ItemStack(Material.RED_STAINED_GLASS_PANE));
        }
        for (ItemStack ingredient : listedIngredients.stream().limit(stackCap).toList()){
            inventory.addItem(ingredient);
        }
        inventory.setItem(45, cancelButton);
        inventory.setItem(53, confirmButton);
    }
}
