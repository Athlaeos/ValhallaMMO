package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.SetModifiersMenu;
import me.athlaeos.valhallammo.gui.SetLootPredicatesMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.loot.LootEntry;
import me.athlaeos.valhallammo.loot.LootPool;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
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

import java.util.*;

public class LootEntryEditor extends Menu implements SetModifiersMenu, SetLootPredicatesMenu {
    private static final NamespacedKey BUTTON_ACTION_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "button_action");

    private static final int predicatesIndex = 1;
    private static final int predicateTypeIndex = 2;
    private static final int dropIndex = 4;
    private static final int baseQuantityIndex = 6;
    private static final int fortuneQuantityIndex = 7;
    private static final int guaranteedPresentIndex = 13;
    private static final int chanceWeightBaseIndex3 = 19;
    private static final int chanceWeightBaseIndex2 = 20;
    private static final int chanceWeightBaseIndex1 = 21;
    private static final int chanceWeightLuckIndex1 = 23;
    private static final int chanceWeightLuckIndex2 = 24;
    private static final int chanceWeightLuckIndex3 = 25;
    private static final int modifierIndex = 40;
    private static final int deleteIndex = 45;
    private static final int backToMenuIndex = 53;

    private static final ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").get();

    private final LootTable table;
    private final LootPool pool;
    private final LootEntry entry;

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
    private static final ItemStack predicatesButton = new ItemBuilder(getButtonData("editor_loottable_entry_predicates", Material.WRITABLE_BOOK))
            .name("&bFilter")
            .stringTag(BUTTON_ACTION_KEY, "predicatesButton")
            .lore("&7The filter decides if this entry",
                    "&7is capable of dropping based on",
                    "&7conditions.",
                    "&eClick to open the menu",
                    "&8&m                <>                ",
                    "%predicates%").get();
    private static final ItemStack toggleGuaranteedDroppedButton = new ItemBuilder(getButtonData("editor_loottable_guaranteeddroptoggle", Material.COMPARATOR))
            .name("&eDrop Guaranteed")
            .stringTag(BUTTON_ACTION_KEY, "toggleGuaranteedDroppedButton")
            .lore("&7Determines if this drop should",
                    "&7always be dropped if it passed",
                    "&7the filters.",
                    "&7Chance/weight are irrelevant if",
                    "&7enabled",
                    "&eClick to toggle")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setWeightButton1 = new ItemBuilder(getButtonData("editor_loottable_setweight", Material.IRON_BLOCK))
            .name("&eWeight: ")
            .stringTag(BUTTON_ACTION_KEY, "setWeightButton1")
            .lore("&7Determines the weight for this",
                    "&7drop. The higher the weight, the",
                    "&7higher the drop chance. Drop chance",
                    "&7per roll is equal to ",
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
                    "&7drop. The higher the weight, the",
                    "&7higher the drop chance. Drop chance",
                    "&7per roll is equal to ",
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
                    "&7drop. The higher the weight, the",
                    "&7higher the drop chance. Drop chance",
                    "&7per roll is equal to ",
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
                    "&7this drop per luck point. ",
                    "&7The more of the luck stat the",
                    "&7player has, this more likely this",
                    "&7drop will be selected",
                    "&eClick to change by 1",
                    "&eShift-Click to change by 10")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setBonusWeightButton2 = new ItemBuilder(getButtonData("editor_loottable_setluckweight", Material.LAPIS_LAZULI))
            .name("&eBonus Luck Weight: ")
            .stringTag(BUTTON_ACTION_KEY, "setBonusWeightButton2")
            .lore("&7Determines the extra weight for",
                    "&7this drop per luck point. ",
                    "&7The more of the luck stat the",
                    "&7player has, this more likely this",
                    "&7drop will be selected",
                    "&eClick to change by 100",
                    "&eShift-Click to change by 1000")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setBonusWeightButton3 = new ItemBuilder(getButtonData("editor_loottable_setluckweight", Material.LAPIS_LAZULI))
            .name("&eBonus Luck Weight: ")
            .stringTag(BUTTON_ACTION_KEY, "setBonusWeightButton3")
            .lore("&7Determines the extra weight for",
                    "&7this drop per luck point. ",
                    "&7The more of the luck stat the",
                    "&7player has, this more likely this",
                    "&7drop will be selected",
                    "&eClick to change by 10000",
                    "&eShift-Click to change by 100000")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setChanceButton1 = new ItemBuilder(getButtonData("editor_loottable_setchance", Material.GOLD_BLOCK))
            .name("&eDrop Chance: ")
            .stringTag(BUTTON_ACTION_KEY, "setChanceButton1")
            .lore("&7Determines the drop chance.",
                    "&eClick to change by 10%",
                    "&eShift-Click to change by 1%")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setChanceButton2 = new ItemBuilder(getButtonData("editor_loottable_setchance", Material.GOLD_BLOCK))
            .name("&eDrop Chance: ")
            .stringTag(BUTTON_ACTION_KEY, "setChanceButton2")
            .lore("&7Determines the drop chance.",
                    "&eClick to change by 0.1%",
                    "&eShift-Click to change by 0.01%")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setChanceButton3 = new ItemBuilder(getButtonData("editor_loottable_setchance", Material.GOLD_BLOCK))
            .name("&eDrop Chance: ")
            .stringTag(BUTTON_ACTION_KEY, "setChanceButton3")
            .lore("&7Determines the drop chance.",
                    "&eClick to change by 0.001%",
                    "&eShift-Click to change by 0.0001%")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setBonusChanceButton1 = new ItemBuilder(getButtonData("editor_loottable_setluckchance", Material.LAPIS_LAZULI))
            .name("&eBonus Luck Chance: ")
            .stringTag(BUTTON_ACTION_KEY, "setBonusChanceButton1")
            .lore("&7Determines the additional drop",
                    "&7chance per luck point.",
                    "&7The more of the luck stat the",
                    "&7player has, the higher the drop",
                    "&7chance",
                    "&eClick to change by 10%",
                    "&eShift-Click to change by 1%")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setBonusChanceButton2 = new ItemBuilder(getButtonData("editor_loottable_setluckchance", Material.LAPIS_LAZULI))
            .name("&eBonus Luck Chance: ")
            .stringTag(BUTTON_ACTION_KEY, "setBonusChanceButton2")
            .lore("&7Determines the additional drop",
                    "&7chance per luck point.",
                    "&7The more of the luck stat the",
                    "&7player has, the higher the drop",
                    "&7chance",
                    "&eClick to change by 0.1%",
                    "&eShift-Click to change by 0.01%")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setBonusChanceButton3 = new ItemBuilder(getButtonData("editor_loottable_setluckchance", Material.LAPIS_LAZULI))
            .name("&eBonus Luck Chance: ")
            .stringTag(BUTTON_ACTION_KEY, "setBonusChanceButton3")
            .lore("&7Determines the additional drop",
                    "&7chance per luck point.",
                    "&7The more of the luck stat the",
                    "&7player has, the higher the drop",
                    "&7chance",
                    "&eClick to change by 0.001%",
                    "&eShift-Click to change by 0.0001%")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setBaseQuantityButton = new ItemBuilder(getButtonData("editor_loottable_basequantity", Material.PAPER))
            .name("&eBase Quantity")
            .stringTag(BUTTON_ACTION_KEY, "setBaseQuantityButton")
            .lore("&7How many items should this",
                    "&7entry drop? The amount dropped",
                    "&7will be a random amount between",
                    "&7your two given min and max values",
                    "&eClick to change &6min&e amount by 1",
                    "&eShift-Click to change &6max&e amount by 1")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setFortuneQuantityButton = new ItemBuilder(getButtonData("editor_loottable_luckquantity", Material.PAPER))
            .name("&eFortune Quantity")
            .stringTag(BUTTON_ACTION_KEY, "setFortuneQuantityButton")
            .lore("&7How many extra items should",
                    "&7this entry drop per level of",
                    "&7looting or fortune?",
                    "&7The base min/max are increased by",
                    "&7your given fortune min/max per level",
                    "&eClick to change &6min&e amount by 1",
                    "&eShift-Click to change &6max&e amount by 1")
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

    public LootEntryEditor(PlayerMenuUtility playerMenuUtility, LootPool parent, LootEntry entry) {
        super(playerMenuUtility);
        this.entry = entry;
        this.pool = parent;
        this.table = LootTableRegistry.getLootTables().get(pool.getParentTable());
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
            Utils.sendMessage(e.getWhoClicked(), "&cLoot Table has already been deleted");
            e.getWhoClicked().closeInventory();
            return;
        }

        ItemStack cursor = e.getCursor();
        ItemStack clicked = e.getCurrentItem();

        String action = ItemUtils.getPDCString(BUTTON_ACTION_KEY, clicked, "");
        if (!StringUtils.isEmpty(action)){
            switch (action){
                case "backToMenuButton" -> {
                    new LootPoolEditor(playerMenuUtility, pool).open();
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
                        new LootPoolEditor(playerMenuUtility, pool).open();
                        return;
                    }
                }
                case "nextPageButton" -> page++;
                case "previousPageButton" -> page = Math.max(0, page - 1);
                case "setWeightButton1" -> entry.setWeight(entry.getWeight() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
                case "setWeightButton2" -> entry.setWeight(entry.getWeight() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1000 : 100)));
                case "setWeightButton3" -> entry.setWeight(entry.getWeight() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 100000 : 10000)));
                case "setBonusWeightButton1" -> entry.setWeightQuality(entry.getWeightQuality() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
                case "setBonusWeightButton2" -> entry.setWeightQuality(entry.getWeightQuality() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1000 : 100)));
                case "setBonusWeightButton3" -> entry.setWeightQuality(entry.getWeightQuality() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 100000 : 10000)));
                case "setChanceButton1" -> entry.setChance(entry.getChance() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01)));
                case "setChanceButton2" -> entry.setChance(entry.getChance() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.001 : 0.0001)));
                case "setChanceButton3" -> entry.setChance(entry.getChance() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.00001 : 0.000001)));
                case "setBonusChanceButton1" -> entry.setChanceQuality(entry.getChanceQuality() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01)));
                case "setBonusChanceButton2" -> entry.setChanceQuality(entry.getChanceQuality() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.001 : 0.0001)));
                case "setBonusChanceButton3" -> entry.setChanceQuality(entry.getChanceQuality() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.00001 : 0.000001)));
                case "toggleGuaranteedDroppedButton" -> entry.setGuaranteedPresent(!entry.isGuaranteedPresent());
                case "setBaseQuantityButton" -> {
                    // shift click increases max base quantity, cannot go below min quantity or 0
                    if (e.isShiftClick()) entry.setBaseQuantityMax(Math.max(Math.max(0, entry.getBaseQuantityMin()), entry.getBaseQuantityMax() + (e.isLeftClick() ? 1 : -1)));
                    // regular click increases min base quantity, cannot go above max quantity or below 0
                    else entry.setBaseQuantityMin(Math.max(0, Math.min(entry.getBaseQuantityMax(), entry.getBaseQuantityMin() + (e.isLeftClick() ? 1 : -1))));
                }
                case "setFortuneQuantityButton" -> {
                    // shift click increases max fortune quantity, cannot go below min quantity or 0
                    if (e.isShiftClick()) entry.setQuantityMaxFortuneBase(Math.max(Math.max(0, entry.getQuantityMinFortuneBase()), entry.getQuantityMaxFortuneBase() + (e.isLeftClick() ? 0.1F : -0.1F)));
                    // regular click increases min fortune quantity, cannot go above max quantity or below 0
                    else entry.setQuantityMinFortuneBase(Math.max(0, Math.min(entry.getQuantityMaxFortuneBase(), entry.getQuantityMinFortuneBase() + (e.isLeftClick() ? 0.1F : -0.1F))));
                }
            }
        } else if (!ItemUtils.isEmpty(cursor) && e.getRawSlot() == dropIndex) entry.setDrop(cursor.clone());

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

        ItemBuilder dropIcon = new ItemBuilder(entry.getDrop());
        dropIcon.appendLore("&8&m                                   ",
                String.format("&9Quantity: %s %s",
                (entry.getBaseQuantityMin() != entry.getBaseQuantityMax() ? entry.getBaseQuantityMin() + "-" + entry.getBaseQuantityMax() : entry.getBaseQuantityMin()),
                (entry.getQuantityMinFortuneBase() > 0 ? (entry.getQuantityMinFortuneBase() != entry.getQuantityMaxFortuneBase() ? String.format("(+%.1f-%.1f/fortune)", entry.getQuantityMinFortuneBase(), entry.getQuantityMaxFortuneBase()) :
                        String.format("(+%.1f/fortune", entry.getQuantityMinFortuneBase())) : "")
        ));
        if (!entry.getPredicates().isEmpty()) {
            dropIcon.appendLore("&6" + (entry.getPredicateSelection() == LootTable.PredicateSelection.ANY ? "Any" : "All") + "&e of the following conditions");
            dropIcon.appendLore("&emust pass:");
            entry.getPredicates().forEach(pr -> dropIcon.appendLore(StringUtils.separateStringIntoLines("&f> " + pr.getActiveDescription(), 40)));
        }
        if (entry.isGuaranteedPresent()){
            dropIcon.appendLore("&aGuaranteed to drop if", "&aconditions succeed");
        } else {
            if (pool.isWeighted()){
                double combinedWeight = pool.getEntries().values().stream().mapToDouble(LootEntry::getWeight).sum();
                dropIcon.appendLore("&eWeight: " + entry.getWeight() + (entry.getWeightQuality() > 0 ? String.format(" (+%.1f/fortune)", entry.getWeightQuality()) : ""),
                        "&6With a combined weight of " + combinedWeight,
                        String.format("&6this has a %.1f%% chance of", ((entry.getWeight() / combinedWeight) * 100)),
                        "&6dropping per roll (ignoring luck)");
            } else {
                dropIcon.appendLore(String.format("&eDrop chance: %.1f %s", entry.getChance() * 100, (entry.getChanceQuality() > 0 ? String.format(" (+%.1f/luck)", entry.getChanceQuality() * 100) : "")));
            }
        }

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
        inventory.setItem(baseQuantityIndex, new ItemBuilder(setBaseQuantityButton).name("&eDrop Count: " + (entry.getBaseQuantityMin() + "-" + entry.getBaseQuantityMax())).get());
        inventory.setItem(fortuneQuantityIndex, new ItemBuilder(setFortuneQuantityButton).name(String.format("&eFortune Drop Count: %.1f-%.1f", entry.getQuantityMinFortuneBase(), entry.getQuantityMaxFortuneBase())).get());
        inventory.setItem(guaranteedPresentIndex, new ItemBuilder(toggleGuaranteedDroppedButton).name(entry.isGuaranteedPresent() ? "&eDrop guaranteed" : "&eDrop relies on " + (pool.isWeighted() ? "its weight" : "its drop chance")).get());
        if (!entry.isGuaranteedPresent()) inventory.setItem(chanceWeightBaseIndex1, pool.isWeighted() ? new ItemBuilder(setWeightButton1).name("&eWeight: " + entry.getWeight()).get() : new ItemBuilder(setChanceButton1).name(String.format("&eDrop Chance: %.4f%%", entry.getChance() * 100)).get());
        if (!entry.isGuaranteedPresent()) inventory.setItem(chanceWeightBaseIndex2, pool.isWeighted() ? new ItemBuilder(setWeightButton2).name("&eWeight: " + entry.getWeight()).get() : new ItemBuilder(setChanceButton2).name(String.format("&eDrop Chance: %.4f%%", entry.getChance() * 100)).get());
        if (!entry.isGuaranteedPresent()) inventory.setItem(chanceWeightBaseIndex3, pool.isWeighted() ? new ItemBuilder(setWeightButton3).name("&eWeight: " + entry.getWeight()).get() : new ItemBuilder(setChanceButton3).name(String.format("&eDrop Chance: %.4f%%", entry.getChance() * 100)).get());
        if (!entry.isGuaranteedPresent()) inventory.setItem(chanceWeightLuckIndex1, pool.isWeighted() ? new ItemBuilder(setBonusWeightButton1).name("&eBonus Weight: +" + entry.getWeightQuality() + "/luck").get() : new ItemBuilder(setBonusChanceButton1).name(String.format("&eBonus Drop Chance: +%.4f%%/luck", entry.getChanceQuality() * 100)).get());
        if (!entry.isGuaranteedPresent()) inventory.setItem(chanceWeightLuckIndex2, pool.isWeighted() ? new ItemBuilder(setBonusWeightButton2).name("&eBonus Weight: +" + entry.getWeightQuality() + "/luck").get() : new ItemBuilder(setBonusChanceButton2).name(String.format("&eBonus Drop Chance: +%.4f%%/luck", entry.getChanceQuality() * 100)).get());
        if (!entry.isGuaranteedPresent()) inventory.setItem(chanceWeightLuckIndex3, pool.isWeighted() ? new ItemBuilder(setBonusWeightButton3).name("&eBonus Weight: +" + entry.getWeightQuality() + "/luck").get() : new ItemBuilder(setBonusChanceButton3).name(String.format("&eBonus Drop Chance: +%.4f%%/luck", entry.getChanceQuality() * 100)).get());
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
