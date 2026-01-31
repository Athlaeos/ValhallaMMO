package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.SetLootPredicatesMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.loot.predicates.PredicateRegistry;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.ConventionUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;
import java.util.stream.Collectors;

public class LootPredicateMenu extends Menu {
    private static final NamespacedKey KEY_ACTION = ValhallaMMO.key("key_action");
    private static final NamespacedKey KEY_PREDICATE_ID = ValhallaMMO.key("key_predicate_id");
    private static final NamespacedKey KEY_PREDICATE_BUTTON = ValhallaMMO.key("key_predicate_button");

    private static final List<Integer> predicateButtonIndexes = Arrays.asList(2, 3, 4, 5, 6, 11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42);
    private static final ItemStack confirmButton = new ItemBuilder(Material.STRUCTURE_VOID).stringTag(KEY_ACTION, "confirmButton").name("&b&lSave").get();
    private static final ItemStack createNewButton = new ItemBuilder(getButtonData("editor_newrecipe", Material.LIME_DYE))
            .name("&b&lAdd Condition")
            .stringTag(KEY_ACTION, "createNewButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack cancelButton = new ItemBuilder(Material.BARRIER).stringTag(KEY_ACTION, "cancelButton").name("&cDelete").get();
    private static final ItemStack nextPageButton = new ItemBuilder(Material.ARROW).stringTag(KEY_ACTION, "nextPageButton").name("&7&lNext page").get();
    private static final ItemStack previousPageButton = new ItemBuilder(Material.ARROW).stringTag(KEY_ACTION, "previousPageButton").name("&7&lPrevious page").get();

    private final Menu menu;
    private View view = View.VIEW_PREDICATES;
    private int currentPage = 0;

    private final Collection<LootPredicate> currentPredicates;
    private LootPredicate currentPredicate = null;

    public LootPredicateMenu(PlayerMenuUtility playerMenuUtility, Menu menu) {
        super(playerMenuUtility);
        this.menu = menu;

        if (menu instanceof SetLootPredicatesMenu m) this.currentPredicates = m.getPredicates();
        else this.currentPredicates = new ArrayList<>();
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF314\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_predicateselection"));
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
                case "confirmButton" -> {
                    if (view == View.NEW_PREDICATE){
                        if (currentPredicate != null){
                            currentPredicates.removeIf(m -> m.getKey().equalsIgnoreCase(currentPredicate.getKey()));
                            currentPredicates.add(currentPredicate);

                            currentPredicate = null;
                            view = View.VIEW_PREDICATES;
                        }
                    } else if (view == View.PICK_PREDICATES) view = View.VIEW_PREDICATES;
                    else if (view == View.VIEW_PREDICATES){
                        if (menu instanceof SetLootPredicatesMenu m){
                            m.setPredicates(currentPredicates);
                            menu.open();
                        }
                    }
                }
                case "createNewButton" -> view = View.PICK_PREDICATES;
                case "nextPageButton" -> currentPage++;
                case "previousPageButton" -> currentPage--;
                case "cancelButton" -> {
                    if (currentPredicate != null) {
                        if (currentPredicates.removeIf(m -> m.getKey().equalsIgnoreCase(currentPredicate.getKey()))) view = View.VIEW_PREDICATES;
                        else view = View.PICK_PREDICATES;
                    }
                }
            }
        }
        String clickedPredicate = ItemUtils.getPDCString(KEY_PREDICATE_ID, clickedItem, null);
        if (!StringUtils.isEmpty(clickedPredicate)){
            if (view == View.PICK_PREDICATES){
                currentPredicate = PredicateRegistry.createPredicate(clickedPredicate);

                view = View.NEW_PREDICATE;
            } else if (view == View.VIEW_PREDICATES){
                for (LootPredicate predicate : currentPredicates){
                    if (predicate.getKey().equals(clickedPredicate)){
                        currentPredicate = predicate;
                        view = View.NEW_PREDICATE;
                        break;
                    }
                }
            }
        }

        if (ItemUtils.getPDCInt(KEY_PREDICATE_BUTTON, clickedItem, 0) > 0 && currentPredicate != null){
            int buttonPressed = predicateButtonIndexes.indexOf(e.getRawSlot());
            currentPredicate.onButtonPress(e, buttonPressed);
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
        switch (view) {
            case VIEW_PREDICATES -> setViewPredicatesView();
            case PICK_PREDICATES -> setPickPredicatesView();
            case NEW_PREDICATE -> setNewPredicateView();
        }
    }

    private void setNewPredicateView(){
        if (currentPredicate != null){
            if (currentPredicate.getButtons() != null){
                for (Integer b : currentPredicate.getButtons().keySet()){
                    if (b >= predicateButtonIndexes.size()) {
                        throw new IllegalArgumentException("Loot Predicate " + currentPredicate.getKey() + " has button in invalid position " + b + ", must be between 0 and 25(non-inclusive)!");
                    }
                    ItemStack base = currentPredicate.getButtons().get(b);
                    if (ItemUtils.isEmpty(base)) throw new IllegalStateException("Loot Predicate " + currentPredicate.getKey() + " has empty button in slot " + b);
                    ItemStack button = new ItemBuilder(base).intTag(KEY_PREDICATE_BUTTON, 1).get();

                    inventory.setItem(predicateButtonIndexes.get(b), button);
                }
            }
        }
        inventory.setItem(53, confirmButton);
        inventory.setItem(45, cancelButton);
    }

    private void setPickPredicatesView(){
        for (int i = 0; i < 45; i++){
            inventory.setItem(i, null);
        }
        Map<String, LootPredicate> predicates = PredicateRegistry.getPredicates();
        Collection<String> currentStringPredicates = currentPredicates.stream().map(LootPredicate::getKey).collect(Collectors.toSet());
        List<LootPredicate> sortedPredicates = new ArrayList<>(predicates.values());
        sortedPredicates.sort(Comparator.comparing(LootPredicate::getKey));

        List<ItemStack> totalPredicateButtons = new ArrayList<>();
        for (LootPredicate predicate : sortedPredicates){
            if (currentStringPredicates.contains(predicate.getKey())) continue;

            ItemStack icon = new ItemBuilder(predicate.getIcon())
                    .lore(StringUtils.separateStringIntoLines(predicate.getDescription(), 40))
                    .name(predicate.getDisplayName())
                    .flag(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES, ConventionUtils.getHidePotionEffectsFlag(), ItemFlag.HIDE_ENCHANTS).wipeAttributes()
                    .stringTag(KEY_PREDICATE_ID, predicate.getKey()).get();

            totalPredicateButtons.add(icon);
        }
        Map<Integer, List<ItemStack>> pages = Utils.paginate(45, totalPredicateButtons);

        currentPage = Math.max(1, Math.min(currentPage, pages.size()));

        if (!pages.isEmpty()){
            for (ItemStack i : pages.get(currentPage - 1)){
                inventory.addItem(i);
            }
        }

        inventory.setItem(45, previousPageButton);
        inventory.setItem(53, nextPageButton);
        inventory.setItem(49, confirmButton);
    }

    private void setViewPredicatesView(){
        for (int i = 0; i < 45; i++){
            inventory.setItem(i, null);
        }
        List<LootPredicate> predicates = currentPredicates.stream().limit(45).toList();
        for (LootPredicate predicate : predicates){
            ItemStack icon = new ItemBuilder(predicate.getIcon())
                    .lore(StringUtils.separateStringIntoLines(predicate.getActiveDescription(), 40))
                    .name(predicate.getDisplayName())
                    .stringTag(KEY_PREDICATE_ID, predicate.getKey())
                    .flag(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES, ConventionUtils.getHidePotionEffectsFlag(), ItemFlag.HIDE_ENCHANTS).wipeAttributes()
                    .get();
            inventory.addItem(icon);
        }
        if (currentPredicates.size() <= 44){
            inventory.addItem(createNewButton);
        }
        inventory.setItem(49, confirmButton);
    }

    private enum View{
        PICK_PREDICATES,
        VIEW_PREDICATES,
        NEW_PREDICATE
    }
}
