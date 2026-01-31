package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.implementations.loottablecategories.*;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootTableOverviewMenu extends Menu {
    private static final NamespacedKey KEY_ACTION = ValhallaMMO.key("key_action");
    public static final NamespacedKey KEY_TABLE_CATEGORY = ValhallaMMO.key("key_table_category");
    public static final NamespacedKey KEY_TABLE = ValhallaMMO.key("key_table");

    public static final TableCategory BLOCKS_LOOT = new BlockLootTables(12);
    public static final TableCategory ENTITIES_LOOT = new EntityLootTables(21);
    public static final TableCategory CONTAINERS_LOOT = new ContainerLootTables(30);
    public static final TableCategory FISHING_LOOT = new FishingLootTable(39);
    public static final TableCategory GLOBAL_REPLACEMENT = new GlobalReplacementTable(5);
    public static final TableCategory BLOCKS_REPLACEMENT = new BlockReplacementTables(14);
    public static final TableCategory ENTITIES_REPLACEMENT = new EntityReplacementTables(23);
    public static final TableCategory CONTAINERS_REPLACEMENT = new ContainerReplacementTables(32);
    public static final TableCategory FISHING_REPLACEMENT = new FishingReplacementTable(41);
    private static final Map<String, TableCategory> categories = new HashMap<>();
    static {
        registerCategory(BLOCKS_LOOT);
        registerCategory(ENTITIES_LOOT);
        registerCategory(CONTAINERS_LOOT);
        registerCategory(FISHING_LOOT);
        registerCategory(GLOBAL_REPLACEMENT);
        registerCategory(BLOCKS_REPLACEMENT);
        registerCategory(ENTITIES_REPLACEMENT);
        registerCategory(CONTAINERS_REPLACEMENT);
        registerCategory(FISHING_REPLACEMENT);
    }
    public static void registerCategory(TableCategory category){ categories.put(category.getId(), category); }
    public static Map<String, TableCategory> getCategories() { return new HashMap<>(categories); }

    private static final ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").get();
    private static final ItemStack nextPageButton = new ItemBuilder(getButtonData("editor_nextpage", Material.ARROW))
            .name("&7&lNext page")
            .stringTag(KEY_ACTION, "nextPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack previousPageButton = new ItemBuilder(getButtonData("editor_prevpage", Material.ARROW))
            .name("&7&lPrevious page")
            .stringTag(KEY_ACTION, "previousPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack backToMenuButton = new ItemBuilder(getButtonData("editor_backtomenu", Material.BOOK))
            .name("&fBack to Menu")
            .stringTag(KEY_ACTION, "backToMenuButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack editLootTablesButton = new ItemBuilder(getButtonData("editor_loottable_edit", Material.BOOK))
            .name("&fEdit Loot Tables")
            .stringTag(KEY_ACTION, "editLootTablesButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack editReplacementTablesButton = new ItemBuilder(getButtonData("editor_replacementtable_edit", Material.KNOWLEDGE_BOOK))
            .name("&fEdit Replacement Tables")
            .stringTag(KEY_ACTION, "editReplacementTablesButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();

    private int currentPage = 0;
    private TableCategory currentCategory = null;

    public LootTableOverviewMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    public LootTableOverviewMenu(PlayerMenuUtility playerMenuUtility, String category) {
        super(playerMenuUtility);
        this.currentCategory = categories.get(category);
    }

    @Override
    public String getMenuName() {
        return currentCategory == null ?
                Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF311\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_loottableoverview")) :
                Utils.chat(currentCategory.getTitle());
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
        String clickedCategory = ItemUtils.getPDCString(KEY_TABLE_CATEGORY, clickedItem, "");
        String clickedTable = ItemUtils.getPDCString(KEY_TABLE, clickedItem, null);
        if (!StringUtils.isEmpty(action)){
            switch (action){
                case "backToMenuButton" -> {
                    new LootTableOverviewMenu(playerMenuUtility).open();
                    return;
                }
                case "editLootTablesButton" -> {
                    new LootTableSelectionMenu(playerMenuUtility, this, null).open();
                    return;
                }
                case "editReplacementTablesButton" -> {
                    new ReplacementTableSelectionMenu(playerMenuUtility, this, null).open();
                    return;
                }
                case "nextPageButton" -> currentPage++;
                case "previousPageButton" -> currentPage--;
            }
        } else if (!StringUtils.isEmpty(clickedCategory)){
            currentCategory = categories.get(clickedCategory);
            if (currentCategory != null){
                if (currentCategory.getCategoryOptions().isEmpty()) currentCategory.onButtonClick(e, null, this);
                else new LootTableOverviewMenu(playerMenuUtility, currentCategory.getId()).open();
                return;
            }
        } else if (!StringUtils.isEmpty(clickedTable)){
            if (currentCategory != null) {
                currentCategory.onButtonClick(e, clickedTable, this);
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
        if (currentCategory != null) setPickOptionView();
        else setViewCategoriesView();
    }

    private void setPickOptionView(){
        if (!ValhallaMMO.getPluginConfig().getBoolean("admin_gui_filler_removal")) for (int i = 45; i < 54; i++) inventory.setItem(i, filler);
        List<ItemStack> recipes = currentCategory.getCategoryOptions();
        Map<Integer, List<ItemStack>> pages = Utils.paginate(45, recipes);

        currentPage = Math.max(1, Math.min(currentPage, pages.size()));

        if (!pages.isEmpty()){
            pages.get(currentPage - 1).forEach(inventory::addItem);
        }

        inventory.setItem(45, previousPageButton);
        inventory.setItem(49, backToMenuButton);
        inventory.setItem(53, nextPageButton);
    }

    private void setViewCategoriesView(){
        if (!ValhallaMMO.getPluginConfig().getBoolean("admin_gui_filler_removal")) for (int i = 0; i < 54; i++) inventory.setItem(i, filler);

        for (TableCategory category : categories.values()){
            inventory.setItem(category.getPosition(), new ItemBuilder(category.getIcon()).stringTag(KEY_TABLE_CATEGORY, category.getId()).get());
        }
        inventory.setItem(48, editLootTablesButton);
        inventory.setItem(50, editReplacementTablesButton);
    }
}
