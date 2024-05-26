package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LootTableSelectionMenu extends Menu {
    private static final NamespacedKey KEY_ACTION = new NamespacedKey(ValhallaMMO.getInstance(), "key_action");
    private static final NamespacedKey BUTTON_DATA = new NamespacedKey(ValhallaMMO.getInstance(), "button_data");

    private static final ItemStack backToMenuButton = new ItemBuilder(getButtonData("editor_backtomenu", Material.BOOK))
            .stringTag(KEY_ACTION, "backToMenuButton")
            .name("&7Cancel Selection").get();
    private static final ItemStack createNewButton = new ItemBuilder(getButtonData("editor_newrecipe", Material.LIME_DYE))
            .name("&b&lNew")
            .stringTag(KEY_ACTION, "createNewButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").get();
    private static final ItemStack nextPageButton = new ItemBuilder(getButtonData("editor_nextpage", Material.ARROW))
            .name("&7&lNext page")
            .stringTag(KEY_ACTION, "nextPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack previousPageButton = new ItemBuilder(getButtonData("editor_prevpage", Material.ARROW))
            .name("&7&lPrevious page")
            .stringTag(KEY_ACTION, "previousPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();

    private final Menu openedFrom;
    private final Action<LootTable> tableAction;
    private int page = 0;

    public LootTableSelectionMenu(PlayerMenuUtility playerMenuUtility, Menu menu, Action<LootTable> tableAction) {
        super(playerMenuUtility);
        this.openedFrom = menu;
        this.tableAction = tableAction;
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF312\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_ingredientselection"));
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        e.setCancelled(true);
        if (ItemUtils.isEmpty(clickedItem)) return;
        String action = ItemUtils.getPDCString(KEY_ACTION, clickedItem, null);
        if (!StringUtils.isEmpty(action)) {
            switch (action){
                case "backToMenuButton"-> {
                    openedFrom.open();
                    return;
                }
                case "createNewButton" -> {
                    playerMenuUtility.setPreviousMenu(this); // fallback in case player doesn't wanna
                    e.getWhoClicked().closeInventory();
                    Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                            new Question("&fWhat should the loot table's key be? (type in chat, or 'cancel' to cancel)", s -> !LootTableRegistry.getLootTables().containsKey(s), "&cLoot table with this key already exists! Try again")
                    ) {
                        @Override
                        public Action<Player> getOnFinish() {
                            if (getQuestions().isEmpty()) return super.getOnFinish();
                            Question question = getQuestions().get(0);
                            if (question.getAnswer() == null) return super.getOnFinish();
                            return (p) -> {
                                String answer = question.getAnswer().replaceAll(" ", "_").toLowerCase();
                                if (answer.contains("cancel")) playerMenuUtility.getPreviousMenu().open();
                                else if (LootTableRegistry.getLootTables().containsKey(answer))
                                    Utils.sendMessage(getWho(), "&cLoot table key already exists!");
                                else {
                                    LootTable newTable = new LootTable(answer);
                                    LootTableRegistry.registerLootTable(newTable);
                                    if (tableAction != null) tableAction.act(newTable);
                                }
                            };
                        }
                    };
                    Questionnaire.startQuestionnaire((Player) e.getWhoClicked(), questionnaire);
                    return;
                }
                case "nextPageButton" -> page++;
                case "previousPageButton" -> page = Math.max(0, page - 1);
            }
        }
        String storedValue = ItemUtils.getPDCString(BUTTON_DATA, clickedItem, null);
        if (StringUtils.isEmpty(storedValue)) return;
        LootTable table = LootTableRegistry.getLootTables().get(storedValue);
        if (table == null) return;
        if (tableAction == null || e.isShiftClick())
            new LootTableEditor(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()), table).open();
        else tableAction.act(table);

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
        if (!ValhallaMMO.getPluginConfig().getBoolean("admin_gui_filler_removal")) for (int i = 45; i < 54; i++) inventory.setItem(i, filler);
        List<ItemStack> buttons = new ArrayList<>();
        for (LootTable table : LootTableRegistry.getLootTables().values()){
            ItemBuilder icon = new ItemBuilder(table.getIcon()).name("&e" + table.getKey()).stringTag(BUTTON_DATA, table.getKey());

            icon.lore(switch (table.getVanillaLootPreservationType()){
                case CLEAR -> "&fVanilla loot removed";
                case KEEP -> "&fTable loot added to vanilla loot";
                case CLEAR_UNLESS_EMPTY -> "&fVanilla loot overwritten";
            });
            if (table.getPools().isEmpty()) icon.appendLore("&cNo loot pools set, drops nothing");
            else icon.appendLore("&aSelecting loot from " + table.getPools().size() + " pools");

            if (tableAction != null) icon.appendLore("&6Click to select, ", "&6Shift-Click to edit");
            else icon.appendLore("&6Click to edit");
            buttons.add(icon.get());
        }
        buttons.add(createNewButton);

        Map<Integer, List<ItemStack>> pages = Utils.paginate(45, buttons);

        page = Math.max(1, Math.min(page, pages.size()));

        if (!pages.isEmpty()){
            pages.get(page - 1).forEach(inventory::addItem);
        }

        inventory.setItem(45, previousPageButton);
        inventory.setItem(49, backToMenuButton);
        inventory.setItem(53, nextPageButton);
    }
}
