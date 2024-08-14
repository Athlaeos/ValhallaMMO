package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.gui.*;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.loot.*;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ReplacementEntryEditor extends Menu implements SetModifiersMenu, SetLootPredicatesMenu {
    private static final NamespacedKey BUTTON_ACTION_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "button_action");

    private static final int predicatesIndex = 1;
    private static final int predicateTypeIndex = 2;
    private static final int dropIndex = 4;
    private static final int tinkerIndex = 6;
    private static final int chanceWeightBaseIndex3 = 19;
    private static final int chanceWeightBaseIndex2 = 20;
    private static final int chanceWeightBaseIndex1 = 21;
    private static final int chanceWeightLuckIndex1 = 23;
    private static final int chanceWeightLuckIndex2 = 24;
    private static final int chanceWeightLuckIndex3 = 25;
    private static final int chanceWeightLootingIndex1 = 14;
    private static final int chanceWeightLootingIndex2 = 15;
    private static final int chanceWeightLootingIndex3 = 16;
    private static final int modifierIndex = 40;
    private static final int deleteIndex = 45;
    private static final int backToMenuIndex = 53;

    private static final ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").get();

    private final ReplacementTable table;
    private final ReplacementPool pool;
    private final ReplacementEntry entry;

    private static final ItemStack togglePredicateModeButton = new ItemBuilder(getButtonData("editor_loottable_predicatemode", Material.COMPARATOR))
            .name("&eFilter Mode")
            .stringTag(BUTTON_ACTION_KEY, "togglePredicateModeButton")
            .lore("&7Determines to what extent filters",
                    "&7should pass.",
                    "&7Should all filters pass",
                    "&7or should any filter pass?",
                    "&eClick to toggle")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack modifierButton = new ItemBuilder(getButtonData("editor_loottable_entry_modifiers", Material.WRITABLE_BOOK))
            .name("&dDynamic Item Modifiers")
            .stringTag(BUTTON_ACTION_KEY, "modifierButton")
            .lore("&7Modifiers are functions to edit",
                    "&7the output item based on player",
                    "&7stats.",
                    "&eClick to open the menu",
                    "&8&m                <>                ",
                    "%modifiers%").get();
    private static final ItemStack tinkerToggleButton = new ItemBuilder(getButtonData("editor_loottable_toggletinker", Material.ANVIL))
            .name("&eAnvil")
            .stringTag(BUTTON_ACTION_KEY, "tinkerToggleButton")
            .lore("&7If tinkering is enabled, the",
                    "&7replacement table will instead of",
                    "&7replacing loot with another item",
                    "&7tinker said items without replacing").get();
    private static final ItemStack predicatesButton = new ItemBuilder(getButtonData("editor_loottable_entry_predicates", Material.WRITABLE_BOOK))
            .name("&bFilter")
            .stringTag(BUTTON_ACTION_KEY, "predicatesButton")
            .lore("&7The filter decides if this entry",
                    "&7is capable of dropping based on",
                    "&7conditions.",
                    "&eClick to open the menu",
                    "&8&m                <>                ",
                    "%predicates%").get();
    private static final ItemStack setWeightButton1 = new ItemBuilder(getButtonData("editor_loottable_setweight", Material.IRON_BLOCK))
            .name("&eWeight: ")
            .stringTag(BUTTON_ACTION_KEY, "setWeightButton1")
            .lore("&7Determines the weight for this",
                    "&7entry. The higher the weight, the",
                    "&7higher the chance for it to be used. ",
                    "&7Drop chance per roll is equal to ",
                    "&7dropWeight/totalWeight, ",
                    "&7where totalWeight is the combined",
                    "&7weight of all possible drops.",
                    "&eClick to change by 1",
                    "&eShift-Click to change by 10")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setWeightButton2 = new ItemBuilder(getButtonData("editor_loottable_setweight", Material.IRON_BLOCK))
            .name("&eWeight: ")
            .stringTag(BUTTON_ACTION_KEY, "setWeightButton2")
            .lore("&7Determines the weight for this",
                    "&7entry. The higher the weight, the",
                    "&7higher the chance for it to be used. ",
                    "&7Drop chance per roll is equal to ",
                    "&7dropWeight/totalWeight, ",
                    "&7where totalWeight is the combined",
                    "&7weight of all possible drops.",
                    "&eClick to change by 100",
                    "&eShift-Click to change by 1000")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setWeightButton3 = new ItemBuilder(getButtonData("editor_loottable_setweight", Material.IRON_BLOCK))
            .name("&eWeight: ")
            .stringTag(BUTTON_ACTION_KEY, "setWeightButton3")
            .lore("&7Determines the weight for this",
                    "&7entry. The higher the weight, the",
                    "&7higher the chance for it to be used. ",
                    "&7Drop chance per roll is equal to ",
                    "&7dropWeight/totalWeight, ",
                    "&7where totalWeight is the combined",
                    "&7weight of all possible drops.",
                    "&eClick to change by 10000",
                    "&eShift-Click to change by 100000")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setBonusWeightButton1 = new ItemBuilder(getButtonData("editor_loottable_setluckweight", Material.LAPIS_LAZULI))
            .name("&eBonus Luck Weight: ")
            .stringTag(BUTTON_ACTION_KEY, "setBonusWeightButton1")
            .lore("&7Determines the extra weight for",
                    "&7this entry per luck point. ",
                    "&7The more of the luck stat the",
                    "&7player has, this more likely this",
                    "&7entry will be selected",
                    "&eClick to change by 1",
                    "&eShift-Click to change by 10")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setBonusWeightButton2 = new ItemBuilder(getButtonData("editor_loottable_setluckweight", Material.LAPIS_LAZULI))
            .name("&eBonus Luck Weight: ")
            .stringTag(BUTTON_ACTION_KEY, "setBonusWeightButton2")
            .lore("&7Determines the extra weight for",
                    "&7this entry per luck point. ",
                    "&7The more of the luck stat the",
                    "&7player has, this more likely this",
                    "&7entry will be selected",
                    "&eClick to change by 100",
                    "&eShift-Click to change by 1000")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setBonusWeightButton3 = new ItemBuilder(getButtonData("editor_loottable_setluckweight", Material.LAPIS_LAZULI))
            .name("&eBonus Luck Weight: ")
            .stringTag(BUTTON_ACTION_KEY, "setBonusWeightButton3")
            .lore("&7Determines the extra weight for",
                    "&7this entry per luck point. ",
                    "&7The more of the luck stat the",
                    "&7player has, this more likely this",
                    "&7entry will be selected",
                    "&eClick to change by 10000",
                    "&eShift-Click to change by 100000")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setLootingWeightButton1 = new ItemBuilder(getButtonData("editor_loottable_setluckweight", Material.LAPIS_LAZULI))
            .name("&eBonus Looting Weight: ")
            .stringTag(BUTTON_ACTION_KEY, "setLootingWeightButton1")
            .lore("&7Determines the extra weight for",
                    "&7this entry per level of looting",
                    "&7or fortune. The more of these enchant levels",
                    "&7the player has, this more likely this",
                    "&7entry will be selected",
                    "&eClick to change by 1",
                    "&eShift-Click to change by 10")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setLootingWeightButton2 = new ItemBuilder(getButtonData("editor_loottable_setluckweight", Material.LAPIS_LAZULI))
            .name("&eBonus Looting Weight: ")
            .stringTag(BUTTON_ACTION_KEY, "setLootingWeightButton2")
            .lore("&7Determines the extra weight for",
                    "&7this entry per level of looting",
                    "&7or fortune. The more of these enchant levels",
                    "&7the player has, this more likely this",
                    "&7entry will be selected",
                    "&eClick to change by 100",
                    "&eShift-Click to change by 1000")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setLootingWeightButton3 = new ItemBuilder(getButtonData("editor_loottable_setluckweight", Material.LAPIS_LAZULI))
            .name("&eBonus Looting Weight: ")
            .stringTag(BUTTON_ACTION_KEY, "setLootingWeightButton3")
            .lore("&7Determines the extra weight for",
                    "&7this entry per level of looting",
                    "&7or fortune. The more of these enchant levels",
                    "&7the player has, this more likely this",
                    "&7entry will be selected",
                    "&eClick to change by 10000",
                    "&eShift-Click to change by 100000")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();

    private static final ItemStack deleteButton = new ItemBuilder(getButtonData("editor_delete", Material.BARRIER))
            .stringTag(BUTTON_ACTION_KEY, "deleteButton")
            .name("&cDelete Recipe").get();
    private static final ItemStack deleteConfirmButton = new ItemBuilder(getButtonData("editor_deleteconfirm", Material.BARRIER))
            .name("&cDelete Recipe")
            .stringTag(BUTTON_ACTION_KEY, "deleteConfirmButton")
            .enchant(EnchantmentMappings.UNBREAKING.getEnchantment(), 1)
            .lore("&aRight-click &7to confirm recipe deletion")
            .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS).get();
    private static final ItemStack backToMenuButton = new ItemBuilder(getButtonData("editor_backtomenu", Material.BOOK))
            .stringTag(BUTTON_ACTION_KEY, "backToMenuButton")
            .name("&fBack to Menu").get();

    public ReplacementEntryEditor(PlayerMenuUtility playerMenuUtility, ReplacementPool parent, ReplacementEntry entry) {
        super(playerMenuUtility);
        this.entry = entry;
        this.pool = parent;
        this.table = LootTableRegistry.getReplacementTables().get(pool.getParentTable());
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF319" : TranslationManager.getTranslation("editormenu_lootentry"));
    }

    @Override
    public int getSlots() {
        return 54;
    }

    private boolean confirmDeletion = false;
    private int page = 0;

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(!(e.getClickedInventory() instanceof PlayerInventory));
        if (table == null){
            Utils.sendMessage(e.getWhoClicked(), "&cReplacement Table has already been deleted");
            e.getWhoClicked().closeInventory();
            return;
        }

        ItemStack cursor = e.getCursor();
        ItemStack clicked = e.getCurrentItem();

        String action = ItemUtils.getPDCString(BUTTON_ACTION_KEY, clicked, "");
        if (!StringUtils.isEmpty(action)){
            switch (action){
                case "backToMenuButton" -> {
                    new ReplacementPoolEditor(playerMenuUtility, pool).open();
                    LootTableRegistry.resetReplacementTableCache();
                    return;
                }
                case "modifierButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new DynamicModifierMenu(playerMenuUtility, this).open();
                    return;
                }
                case "predicatesButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new LootPredicateMenu(playerMenuUtility, this).open();
                    return;
                }
                case "togglePredicateModeButton" -> {
                    int currentMode = Arrays.asList(LootTable.PredicateSelection.values()).indexOf(entry.getPredicateSelection());
                    if (e.getClick().isLeftClick()){
                        if (currentMode + 1 >= LootTable.PredicateSelection.values().length) currentMode = 0;
                        else currentMode += 1;
                    } else {
                        if (currentMode - 1 < 0) currentMode = LootTable.PredicateSelection.values().length - 1;
                        else currentMode -= 1;
                    }
                    entry.setPredicateSelection(LootTable.PredicateSelection.values()[currentMode]);
                }
                case "deleteButton" -> {
                    confirmDeletion = true;
                    Utils.sendMessage(e.getWhoClicked(), "&cAre you sure you want to delete this loot pool?");
                    setMenuItems();
                    return;
                }
                case "deleteConfirmButton" -> {
                    if (e.isRightClick()){
                        pool.getEntries().remove(entry.getUuid());
                        new ReplacementPoolEditor(playerMenuUtility, pool).open();
                        LootTableRegistry.resetReplacementTableCache();
                        return;
                    }
                }
                case "tinkerToggleButton" -> entry.setTinker(!entry.tinker());
                case "nextPageButton" -> page++;
                case "previousPageButton" -> page = Math.max(0, page - 1);
                case "setWeightButton1" -> entry.setWeight(entry.getWeight() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
                case "setWeightButton2" -> entry.setWeight(entry.getWeight() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1000 : 100)));
                case "setWeightButton3" -> entry.setWeight(entry.getWeight() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 100000 : 10000)));
                case "setBonusWeightButton1" -> entry.setWeightBonusLuck(entry.getWeightBonusLuck() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
                case "setBonusWeightButton2" -> entry.setWeightBonusLuck(entry.getWeightBonusLuck() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1000 : 100)));
                case "setBonusWeightButton3" -> entry.setWeightBonusLuck(entry.getWeightBonusLuck() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 100000 : 10000)));
                case "setLootingWeightButton1" -> entry.setWeightBonusLooting(entry.getWeightBonusLooting() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
                case "setLootingWeightButton2" -> entry.setWeightBonusLooting(entry.getWeightBonusLooting() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1000 : 100)));
                case "setLootingWeightButton3" -> entry.setWeightBonusLooting(entry.getWeightBonusLooting() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 100000 : 10000)));
            }
        } else if (!ItemUtils.isEmpty(cursor) && e.getRawSlot() == dropIndex) entry.setReplaceBy(cursor.clone());

        confirmDeletion = false;
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
        if (!ValhallaMMO.getPluginConfig().getBoolean("admin_gui_filler_removal")) for (int i = 0; i < 54; i++) inventory.setItem(i, filler);

        ItemBuilder dropIcon = new ItemBuilder(entry.tinker() ? pool.getToReplace().getItem() : entry.getReplaceBy());
        if (entry.tinker()) dropIcon.name(pool.getToReplace().getOption().ingredientDescription(pool.getToReplace().getItem()));
        if (!entry.getPredicates().isEmpty()) {
            dropIcon.appendLore("&6" + (entry.getPredicateSelection() == LootTable.PredicateSelection.ANY ? "Any" : "All") + "&e of the following conditions");
            dropIcon.appendLore("&emust pass:");
            entry.getPredicates().forEach(pr -> dropIcon.appendLore(StringUtils.separateStringIntoLines("&f> " + pr.getActiveDescription(), 40)));
        }
        double combinedWeight = pool.getEntries().values().stream().mapToDouble(ReplacementEntry::getWeight).sum();
        dropIcon.appendLore("&eWeight: " + entry.getWeight() + (entry.getWeightBonusLooting() > 0 ? String.format(" (+%.1f/fortune)", entry.getWeightBonusLooting()) : "") + (entry.getWeightBonusLuck() > 0 ? String.format(" (+%.1f/luck)", entry.getWeightBonusLuck()) : ""),
                "&6With a combined weight of " + combinedWeight,
                String.format("&6this has a %.1f%% chance of", ((entry.getWeight() / combinedWeight) * 100)),
                "&6dropping per roll (ignoring luck and fortune)");

        List<String> predicateLore = new ArrayList<>();
        pool.getPredicates().forEach(p -> predicateLore.addAll(StringUtils.separateStringIntoLines("&d> " + p.getActiveDescription(), 40)));
        ItemBuilder predicateIcon = new ItemBuilder(predicatesButton).placeholderLore("%predicates%", predicateLore);

        List<String> modifierLore = new ArrayList<>();
        entry.getModifiers().forEach(m -> modifierLore.addAll(StringUtils.separateStringIntoLines("&d> " + m.getActiveDescription(), 40)));
        ItemBuilder modifierIcon = new ItemBuilder(modifierButton).placeholderLore("%modifiers%", modifierLore);

        inventory.setItem(predicateTypeIndex, new ItemBuilder(togglePredicateModeButton).name("&eFilter Mode: " +
                    switch (pool.getPredicateSelection()){
                        case ALL -> "&aALL conditions must pass";
                        case ANY -> "&aANY condition must pass";
                    }
                ).get());
        inventory.setItem(deleteIndex, confirmDeletion ? deleteConfirmButton : deleteButton);
        inventory.setItem(backToMenuIndex, backToMenuButton);
        inventory.setItem(predicatesIndex, predicateIcon.get());
        inventory.setItem(modifierIndex, modifierIcon.get());
        inventory.setItem(dropIndex, dropIcon.get());
        inventory.setItem(tinkerIndex, new ItemBuilder(tinkerToggleButton).name("&eTinker drop: " + (entry.tinker() ? "&6Yes" : "&7No")).get());

        inventory.setItem(chanceWeightBaseIndex1, new ItemBuilder(setWeightButton1).name("&eWeight: " + entry.getWeight()).get());
        inventory.setItem(chanceWeightBaseIndex2, new ItemBuilder(setWeightButton2).name("&eWeight: " + entry.getWeight()).get());
        inventory.setItem(chanceWeightBaseIndex3, new ItemBuilder(setWeightButton3).name("&eWeight: " + entry.getWeight()).get());
        inventory.setItem(chanceWeightLuckIndex1, new ItemBuilder(setBonusWeightButton1).name("&eBonus Weight: +" + entry.getWeightBonusLuck() + "/luck").get());
        inventory.setItem(chanceWeightLuckIndex2, new ItemBuilder(setBonusWeightButton2).name("&eBonus Weight: +" + entry.getWeightBonusLuck() + "/luck").get());
        inventory.setItem(chanceWeightLuckIndex3, new ItemBuilder(setBonusWeightButton3).name("&eBonus Weight: +" + entry.getWeightBonusLuck() + "/luck").get());
        inventory.setItem(chanceWeightLootingIndex1, new ItemBuilder(setLootingWeightButton1).name("&eBonus Weight: +" + entry.getWeightBonusLooting() + "/looting").get());
        inventory.setItem(chanceWeightLootingIndex2, new ItemBuilder(setLootingWeightButton2).name("&eBonus Weight: +" + entry.getWeightBonusLooting() + "/looting").get());
        inventory.setItem(chanceWeightLootingIndex3, new ItemBuilder(setLootingWeightButton3).name("&eBonus Weight: +" + entry.getWeightBonusLooting() + "/looting").get());
    }

    @Override
    public void setResultModifiers(List<DynamicItemModifier> resultModifiers) {
        entry.setModifiers(resultModifiers);
    }

    @Override
    public List<DynamicItemModifier> getResultModifiers() {
        return entry.getModifiers();
    }

    @Override
    public void setPredicates(Collection<LootPredicate> predicates) {

    }

    @Override
    public Collection<LootPredicate> getPredicates() {
        return entry.getPredicates();
    }
}
