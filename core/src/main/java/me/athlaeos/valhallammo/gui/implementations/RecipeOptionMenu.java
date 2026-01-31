package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOptionRegistry;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.SetRecipeOptionMenu;
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

public class RecipeOptionMenu extends Menu {
    private static final NamespacedKey KEY_ACTION = ValhallaMMO.key("key_action");
    public static final NamespacedKey KEY_OPTION_ID = ValhallaMMO.key("key_option_id");

    private static final ItemStack cancelButton = new ItemBuilder(Material.BARRIER).stringTag(KEY_ACTION, "cancelButton").name("&fCancel").get();
    private static final ItemStack nextPageButton = new ItemBuilder(Material.ARROW).stringTag(KEY_ACTION, "nextPageButton").name("&7&lNext page").get();
    private static final ItemStack previousPageButton = new ItemBuilder(Material.ARROW).stringTag(KEY_ACTION, "previousPageButton").name("&7&lPrevious page").get();

    private final Menu menu;
    private int currentPage = 0;

    private RecipeOption currentOption = null;

    public RecipeOptionMenu(PlayerMenuUtility playerMenuUtility, Menu menu) {
        super(playerMenuUtility);
        this.menu = menu;
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF302\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_ingredientoptionselection"));
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (ItemUtils.isEmpty(clickedItem)) return;
        e.setCancelled(!(e.getClickedInventory() instanceof PlayerInventory));
        String action = ItemUtils.getPDCString(KEY_ACTION, clickedItem, null);
        if (!StringUtils.isEmpty(action)){
            switch (action){
                case "nextPageButton" -> currentPage++;
                case "previousPageButton" -> currentPage--;
                case "cancelButton" -> {
                    if (currentOption != null && menu instanceof SetRecipeOptionMenu m) m.setRecipeOption(null);
                    menu.open();
                    menu.setMenuItems();
                    return;
                }
            }
        }
        String clickedOption = ItemUtils.getPDCString(KEY_OPTION_ID, clickedItem, null);
        if (!StringUtils.isEmpty(clickedOption)){
            e.setCancelled(true);
            currentOption = playerMenuUtility.getOptions().getOrDefault(clickedOption, RecipeOptionRegistry.createOption(clickedOption));
            currentOption.onClick(e);
            if (!e.isCancelled()) {
                if (menu instanceof SetRecipeOptionMenu m) m.setRecipeOption(currentOption.getNew());
                menu.open();
                menu.setMenuItems();
                return;
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
            inventory.setItem(i, null);
        }
        Map<String, RecipeOption> options = RecipeOptionRegistry.getOptions();
        List<ItemStack> totalOptionButtons = new ArrayList<>();
        for (String optionName : options.keySet()){
            RecipeOption option = playerMenuUtility.getOptions().getOrDefault(optionName, options.get(optionName));
            if (option == null) continue;
            option = option.getNew();

            ItemStack icon = new ItemBuilder(option.getIcon()).stringTag(KEY_OPTION_ID, option.getName()).get();
            totalOptionButtons.add(icon);
        }
        totalOptionButtons.sort(Comparator.comparing(ItemStack::getType));
        Map<Integer, List<ItemStack>> pages = Utils.paginate(45, totalOptionButtons);

        currentPage = Math.max(1, Math.min(currentPage, pages.size()));

        if (!pages.isEmpty()){
            for (ItemStack i : pages.get(currentPage - 1)){
                inventory.addItem(i);
            }
        }

        inventory.setItem(45, previousPageButton);
        inventory.setItem(49, cancelButton);
        inventory.setItem(53, nextPageButton);
    }
}
