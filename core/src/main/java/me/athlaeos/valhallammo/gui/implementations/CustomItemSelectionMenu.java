package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.item.CustomItem;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CustomItemSelectionMenu extends Menu {
    private static final NamespacedKey KEY_ACTION = ValhallaMMO.key("key_action");
    private static final NamespacedKey BUTTON_DATA = ValhallaMMO.key("button_data");

    private static final ItemStack createNewButton = new ItemBuilder(getButtonData("editor_newrecipe", Material.LIME_DYE))
            .name("&b&lNew")
            .lore("&fClick with item to register it")
            .stringTag(KEY_ACTION, "createNewButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack nextPageButton = new ItemBuilder(getButtonData("editor_nextpage", Material.ARROW))
            .name("&7&lNext page")
            .stringTag(KEY_ACTION, "nextPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack previousPageButton = new ItemBuilder(getButtonData("editor_prevpage", Material.ARROW))
            .name("&7&lPrevious page")
            .stringTag(KEY_ACTION, "previousPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();

    private int page = 0;
    private final Consumer<CustomItem> onItemSelection;

    public CustomItemSelectionMenu(PlayerMenuUtility playerMenuUtility, Consumer<CustomItem> onItemSelection) {
        super(playerMenuUtility);
        this.onItemSelection = onItemSelection;
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF31B\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_customitems"));
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(!(e.getClickedInventory() instanceof PlayerInventory));
        ItemStack clickedItem = e.getCurrentItem();
        if (ItemUtils.isEmpty(clickedItem)) return;
        String action = ItemUtils.getPDCString(KEY_ACTION, clickedItem, null);
        ItemStack cursor = e.getCursor();
        if (!ItemUtils.isEmpty(cursor)) cursor = cursor.clone();
        if (!StringUtils.isEmpty(action)) {
            switch (action){
                case "createNewButton" -> {
                    if (ItemUtils.isEmpty(e.getCursor())) return;
                    ItemStack finalCursor = e.getCursor().clone();
                    playerMenuUtility.setPreviousMenu(this); // fallback in case player doesn't wanna
                    e.getWhoClicked().closeInventory();
                    Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                            new Question("&fWhat should the item's id be? (type in chat, or 'cancel' to cancel)", s -> !CustomItemRegistry.getItems().containsKey(s), "&cItem with this id already exists! Try again")
                    ) {
                        @Override
                        public Action<Player> getOnFinish() {
                            if (getQuestions().isEmpty()) return super.getOnFinish();
                            Question question = getQuestions().get(0);
                            if (question.getAnswer() == null) return super.getOnFinish();
                            return (p) -> {
                                String answer = question.getAnswer().replaceAll(" ", "_").toLowerCase(java.util.Locale.US);
                                if (answer.contains("cancel")) playerMenuUtility.getPreviousMenu().open();
                                else if (CustomItemRegistry.getItems().containsKey(answer)) {
                                    Utils.sendMessage(getWho(), "&cItem id already exists!");
                                    playerMenuUtility.getPreviousMenu().open();
                                } else {
                                    CustomItemRegistry.register(answer, finalCursor);
                                    playerMenuUtility.getPreviousMenu().open();
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
            setMenuItems();
            return;
        }
        String storedValue = ItemUtils.getPDCString(BUTTON_DATA, clickedItem, null);
        if (StringUtils.isEmpty(storedValue)) return;
        CustomItem clicked = CustomItemRegistry.getItem(storedValue);
        if (clicked != null){
            if (e.isLeftClick()){
                onItemSelection.accept(clicked);
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
        List<ItemStack> buttons = new ArrayList<>();
        List<String> itemIDs = new ArrayList<>(CustomItemRegistry.getItems().keySet());
        itemIDs.sort(Comparator.comparing(s -> s));

        for (String item : itemIDs){
            ItemBuilder icon = new ItemBuilder(CustomItemRegistry.getItem(item).getItem()).name("&f" + item)
                    .lore("&fClick to select")
                    .stringTag(BUTTON_DATA, item);
            buttons.add(icon.get());
        }
        buttons.add(createNewButton);

        Map<Integer, List<ItemStack>> pages = Utils.paginate(45, buttons);

        page = Math.max(1, Math.min(page, pages.size()));

        if (!pages.isEmpty()){
            pages.get(page - 1).forEach(inventory::addItem);
        }

        inventory.setItem(45, previousPageButton);
        inventory.setItem(53, nextPageButton);
    }
}
