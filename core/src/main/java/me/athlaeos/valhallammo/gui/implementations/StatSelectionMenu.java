package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.SetStatsMenu;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.localization.TranslationManager;
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

public class StatSelectionMenu extends Menu {
    private static final NamespacedKey KEY_ACTION = new NamespacedKey(ValhallaMMO.getInstance(), "key_action");
    private static final NamespacedKey KEY_ATTRIBUTE_ID = new NamespacedKey(ValhallaMMO.getInstance(), "key_attribute_id");

    private static final ItemStack confirmButton = new ItemBuilder(Material.STRUCTURE_VOID).stringTag(KEY_ACTION, "confirmButton").name("&b&lSave").get();
    private static final ItemStack createNewButton = new ItemBuilder(getButtonData("editor_newrecipe", Material.LIME_DYE))
            .name("&b&lAdd Stat")
            .stringTag(KEY_ACTION, "createNewButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack cancelButton = new ItemBuilder(Material.BARRIER).stringTag(KEY_ACTION, "cancelButton").name("&cDelete").get();
    private static final ItemStack nextPageButton = new ItemBuilder(Material.ARROW).stringTag(KEY_ACTION, "nextPageButton").name("&7&lNext page").get();
    private static final ItemStack previousPageButton = new ItemBuilder(Material.ARROW).stringTag(KEY_ACTION, "previousPageButton").name("&7&lPrevious page").get();
    private static final ItemStack valueButton = new ItemBuilder(Material.GOLDEN_SWORD).stringTag(KEY_ACTION, "valueButton").name("&fValue: &e").get();

    private final Menu menu;
    private View view = View.VIEW_STATS;
    private int currentPage = 0;

    private final Map<String, Double> currentAttributes;
    private String currentAttribute = null;
    private double value = 0;

    public StatSelectionMenu(PlayerMenuUtility playerMenuUtility, Menu menu) {
        super(playerMenuUtility);
        this.menu = menu;

        if (menu instanceof SetStatsMenu m) this.currentAttributes = m.getStats();
        else this.currentAttributes = new HashMap<>();
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF314\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_statselection"));
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
                    if (view == View.NEW_STAT){
                        if (currentAttribute != null){
                            currentAttributes.put(currentAttribute, value);

                            currentAttribute = null;
                            value = 0;
                            view = View.VIEW_STATS;
                        }
                    } else if (view == View.PICK_STAT) view = View.VIEW_STATS;
                    else if (view == View.VIEW_STATS){
                        if (menu instanceof SetStatsMenu m){
                            m.setStats(currentAttributes);
                            menu.open();
                        }
                    }
                }
                case "createNewButton" -> view = View.PICK_STAT;
                case "nextPageButton" -> currentPage++;
                case "previousPageButton" -> currentPage--;
                case "cancelButton" -> {
                    if (currentAttribute != null) {
                        if (currentAttributes.containsKey(currentAttribute)) {
                            view = View.VIEW_STATS;
                            currentAttributes.remove(currentAttribute);
                        } else view = View.PICK_STAT;
                    }
                }
                case "valueButton" -> {
                    AttributeWrapper wrapper = ItemAttributesRegistry.getCopy(currentAttribute);
                    if (wrapper != null) value += ((e.isRightClick() ? -1 : 1) * (e.isShiftClick() ? wrapper.getBigIncrement() : wrapper.getSmallIncrement()));
                }
            }
        }
        String clickedAttribute = ItemUtils.getPDCString(KEY_ATTRIBUTE_ID, clickedItem, null);
        if (!StringUtils.isEmpty(clickedAttribute)){
            if (view == View.PICK_STAT){
                currentAttribute = clickedAttribute;

                view = View.NEW_STAT;
            } else if (view == View.VIEW_STATS){
                for (String stat : currentAttributes.keySet()){
                    if (stat.equals(clickedAttribute)){
                        currentAttribute = stat;
                        value = currentAttributes.get(stat);
                        view = View.NEW_STAT;
                        break;
                    }
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
        switch (view) {
            case VIEW_STATS -> setViewStatsView();
            case PICK_STAT -> setPickStatsView();
            case NEW_STAT -> setNewStatView();
        }
    }

    private void setNewStatView(){
        if (currentAttribute != null){
            AttributeWrapper wrapper = ItemAttributesRegistry.getCopy(currentAttribute);
            if (wrapper != null){
                wrapper.setValue(value);
                inventory.setItem(22, new ItemBuilder(valueButton)
                        .name("&fSet to: &e" + wrapper.getFormat().format(value))
                        .lore("&7Click to increase/decrease by " + wrapper.getFormat().format(wrapper.getSmallIncrement()),
                                "&7Shift-Click to do so by " + wrapper.getFormat().format(wrapper.getBigIncrement()))
                        .get()
                );
            }
        }
        inventory.setItem(53, confirmButton);
        inventory.setItem(45, cancelButton);
    }

    private void setPickStatsView(){
        for (int i = 0; i < 45; i++){
            inventory.setItem(i, null);
        }
        Map<String, AttributeWrapper> stats = ItemAttributesRegistry.getRegisteredAttributes();
        Collection<String> currentStringStats = currentAttributes.keySet();
        List<AttributeWrapper> sortedStats = new ArrayList<>(stats.values());
        sortedStats.sort(Comparator.comparing(AttributeWrapper::getAttribute));

        List<ItemStack> totalStatButtons = new ArrayList<>();
        for (AttributeWrapper stat : sortedStats){
            if (currentStringStats.contains(stat.getAttribute()) || stat.getAttributeName() == null) continue;

            ItemStack icon = new ItemBuilder(stat.getIcon() == null ? Material.NAME_TAG : stat.getIcon())
                    .name("&f" + stat.getAttributeName().replace("%icon%", "").replace("%value%", "").trim())
                    .flag(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES, ConventionUtils.getHidePotionEffectsFlag(), ItemFlag.HIDE_ENCHANTS).wipeAttributes()
                    .stringTag(KEY_ATTRIBUTE_ID, stat.getAttribute()).get();

            totalStatButtons.add(icon);
        }
        Map<Integer, List<ItemStack>> pages = Utils.paginate(45, totalStatButtons);

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

    private void setViewStatsView(){
        for (int i = 0; i < 45; i++){
            inventory.setItem(i, null);
        }
        List<String> stats = currentAttributes.keySet().stream().limit(45).toList();
        for (String stat : stats){
            AttributeWrapper wrapper = ItemAttributesRegistry.getCopy(stat);
            if (wrapper == null || wrapper.getAttributeName() == null) continue;
            wrapper.setValue(currentAttributes.get(stat));
            ItemStack icon = new ItemBuilder(wrapper.getIcon() == null ? Material.NAME_TAG : wrapper.getIcon())
                    .lore(wrapper.getLoreDisplay())
                    .name("&f" + wrapper.getAttributeName().replace("%icon%", "").replace("%value%", "").trim())
                    .stringTag(KEY_ATTRIBUTE_ID, wrapper.getAttribute())
                    .flag(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES, ConventionUtils.getHidePotionEffectsFlag(), ItemFlag.HIDE_ENCHANTS).wipeAttributes()
                    .get();
            inventory.addItem(icon);
        }
        if (currentAttributes.size() <= 44){
            inventory.addItem(createNewButton);
        }
        inventory.setItem(49, confirmButton);
    }

    private enum View{
        PICK_STAT,
        VIEW_STATS,
        NEW_STAT
    }
}
