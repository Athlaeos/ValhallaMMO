package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.blockvalidations.ValidationRegistry;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.SetValidationsMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.ConventionUtils;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class ValidationSelectionMenu extends Menu {
    private static final NamespacedKey KEY_ACTION = new NamespacedKey(ValhallaMMO.getInstance(), "key_action");
    public static final NamespacedKey KEY_VALIDATION = new NamespacedKey(ValhallaMMO.getInstance(), "key_validation");

    private final Menu menu;
    private final String targetBlock;

    private Collection<String> currentValidations = new HashSet<>();

    private static final ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").get();
    private static final ItemStack nextPageButton = new ItemBuilder(getButtonData("editor_nextpage", Material.ARROW))
            .name("&7&lNext page")
            .stringTag(KEY_ACTION, "nextPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack previousPageButton = new ItemBuilder(getButtonData("editor_prevpage", Material.ARROW))
            .name("&7&lPrevious page")
            .stringTag(KEY_ACTION, "previousPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack cancelButton = new ItemBuilder(getButtonData("editor_validation_selection_return", Material.BOOK))
            .stringTag(KEY_ACTION, "cancelButton")
            .name("&7Return to menu without saving").get();
    private static final ItemStack confirmButton = new ItemBuilder(getButtonData("editor_validation_selection_save", Material.STRUCTURE_VOID))
            .name("&aConfirm")
            .stringTag(KEY_ACTION, "confirmButton")
            .lore("&aRight-click &7to save changes").get();

    private int currentPage = 0;

    public ValidationSelectionMenu(PlayerMenuUtility playerMenuUtility, Menu menu, String block) {
        super(playerMenuUtility);
        this.menu = menu;
        this.targetBlock = block;

        if (menu instanceof SetValidationsMenu m) this.currentValidations = m.getValidations();
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF30F\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_validationselection"));
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
                    if (menu instanceof SetValidationsMenu m){
                        m.setValidations(currentValidations);
                        menu.open();
                        return;
                    }
                }
                case "cancelButton" -> menu.open();
                case "nextPageButton" -> currentPage++;
                case "previousPageButton" -> currentPage--;
            }
        }
        String clickedValidation = ItemUtils.getPDCString(KEY_VALIDATION, clickedItem, null);
        if (!StringUtils.isEmpty(clickedValidation)){
            if (currentValidations.contains(clickedValidation)) currentValidations.remove(clickedValidation);
            else currentValidations.add(clickedValidation);
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
        if (!ValhallaMMO.getPluginConfig().getBoolean("admin_gui_filler_removal")) for (int i = 45; i < 54; i++) inventory.setItem(i, filler);
        List<ItemStack> icons = new ArrayList<>();
        for (Validation validation : ValidationRegistry.getValidations()){
            if (targetBlock != null && !validation.isCompatible(targetBlock)) continue;
            ItemBuilder builder = new ItemBuilder(validation.icon())
                    .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ConventionUtils.getHidePotionEffectsFlag(), ItemFlag.HIDE_DYE).wipeAttributes()
                    .stringTag(KEY_VALIDATION, validation.id());
            if (currentValidations.contains(validation.id())){
                builder.appendLore("&8&m                <>                ", "&2[&aEnabled&2]");
                builder.enchant(EnchantmentMappings.UNBREAKING.getEnchantment(), 1);
            } else {
                builder.appendLore("&8&m                <>                ", "&4[&cDisabled&4]");
            }
            icons.add(builder.get());
        }
        icons.sort(Comparator.comparing(ItemStack::getType).thenComparing(item -> ChatColor.stripColor(ItemUtils.getItemName(new ItemBuilder(item)))));
        Map<Integer, List<ItemStack>> pages = Utils.paginate(45, icons);

        currentPage = Math.max(1, Math.min(currentPage, pages.size()));

        if (!pages.isEmpty()){
            pages.get(currentPage - 1).forEach(inventory::addItem);
        }

        inventory.setItem(45, previousPageButton);
        inventory.setItem(47, cancelButton);
        inventory.setItem(51, confirmButton);
        inventory.setItem(53, nextPageButton);
    }
}
