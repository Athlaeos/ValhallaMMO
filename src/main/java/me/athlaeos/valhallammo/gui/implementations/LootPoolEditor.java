package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
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
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class LootPoolEditor extends Menu implements SetLootPredicatesMenu {
    private static final NamespacedKey BUTTON_ACTION_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "button_action");
    private static final NamespacedKey BUTTON_DATA = new NamespacedKey(ValhallaMMO.getInstance(), "button_data");

    private static final int predicatesIndex = 1;
    private static final int predicateTypeIndex = 2;
    private static final int toggleWeightedIndex = 5;
    private static final int weightedRollsBaseIndex = 6;
    private static final int weightedRollsLuckBonusIndex = 7;
    private static final int previousPageIndex = 27;
    private static final int nextPageIndex = 35;
    private static final int deleteIndex = 45;
    private static final int backToMenuIndex = 53;
    private static final int[] entryIndexes = new int[]{
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private static final ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").get();

    private final LootTable table;
    private final LootPool pool;

    private static final ItemStack togglePredicateModeButton = new ItemBuilder(getButtonData("editor_loottable_predicatemode", Material.COMPARATOR))
            .name("&eFilter Mode")
            .stringTag(BUTTON_ACTION_KEY, "togglePredicateModeButton")
            .lore("&7Determines to what extent filters",
                    "&7should pass.",
                    "&7Should all filters pass",
                    "&7or should any filter pass?",
                    "&eClick to toggle")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack predicatesButton = new ItemBuilder(getButtonData("editor_loottable_pool_predicates", Material.WRITABLE_BOOK))
            .name("&bFilter")
            .stringTag(BUTTON_ACTION_KEY, "predicatesButton")
            .lore("&7The filter decides if this pool",
                    "&7is capable of dropping anything",
                    "&7based on conditions.",
                    "&eClick to open the menu",
                    "&8&m                <>                ",
                    "%predicates%").get();
    private static final ItemStack toggleWeightedButton = new ItemBuilder(getButtonData("editor_loottable_toggleweighted", Material.IRON_BLOCK))
            .name("&eWeighted")
            .stringTag(BUTTON_ACTION_KEY, "toggleWeightedButton")
            .lore("&7Determines if the loot pool",
                    "&7should have weighted entries",
                    "&7or chanced entries",
                    "&7Weighted pools can only drop",
                    "&7a limited amount of things",
                    "&7while chanced pools can drop",
                    "&7any amount as long as the entry's",
                    "&7chance 'procs'",
                    "&eClick to toggle")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack setBaseRollsButton = new ItemBuilder(getButtonData("editor_loottable_weightedbaserolls", Material.PAPER))
            .name("&eWeighted Rolls")
            .stringTag(BUTTON_ACTION_KEY, "setBaseRollsButton")
            .lore("&7How many items should this",
                    "&7loot pool attempt to drop?",
                    "&eClick to change by 1",
                    "&eShift-Click to change by 5")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack setBonusLuckRollsButton = new ItemBuilder(getButtonData("editor_loottable_weightedbonusrolls", Material.PAPER))
            .name("&eBonus Luck Rolls")
            .stringTag(BUTTON_ACTION_KEY, "setBonusLuckRollsButton")
            .lore("&7How many extra rolls should ",
                    "&7occur based on player luck?",
                    "&eClick to change by 0.1",
                    "&eShift-Click to change by 1")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack nextPageButton = new ItemBuilder(getButtonData("editor_nextpage", Material.ARROW))
            .name("&7&lNext page")
            .stringTag(BUTTON_ACTION_KEY, "nextPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack previousPageButton = new ItemBuilder(getButtonData("editor_prevpage", Material.ARROW))
            .name("&7&lPrevious page")
            .stringTag(BUTTON_ACTION_KEY, "previousPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();

    private static final ItemStack deleteButton = new ItemBuilder(getButtonData("editor_delete", Material.BARRIER))
            .stringTag(BUTTON_ACTION_KEY, "deleteButton")
            .name("&cDelete Recipe").get();
    private static final ItemStack deleteConfirmButton = new ItemBuilder(getButtonData("editor_deleteconfirm", Material.BARRIER))
            .name("&cDelete Recipe")
            .stringTag(BUTTON_ACTION_KEY, "deleteConfirmButton")
            .enchant(Enchantment.DURABILITY, 1)
            .lore("&aRight-click &7to confirm recipe deletion")
            .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS).get();
    private static final ItemStack backToMenuButton = new ItemBuilder(getButtonData("editor_backtomenu", Material.BOOK))
            .stringTag(BUTTON_ACTION_KEY, "backToMenuButton")
            .name("&fBack to Menu").get();
    private static final ItemStack createNewButton = new ItemBuilder(getButtonData("editor_loottable_newentry", Material.LIME_DYE))
            .name("&b&lNew Entry")
            .stringTag(BUTTON_ACTION_KEY, "createNewButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();

    public LootPoolEditor(PlayerMenuUtility playerMenuUtility, LootPool pool) {
        super(playerMenuUtility);
        this.pool = pool;
        this.table = LootTableRegistry.getLootTables().get(pool.getParentTable());
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF313\uF80C\uF80A\uF808\uF802&8%pool%" : TranslationManager.getTranslation("editormenu_lootpool")).replace("%pool%", table.getKey());
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

        ItemStack clicked = e.getCurrentItem();

        String action = ItemUtils.getPDCString(BUTTON_ACTION_KEY, clicked, "");
        if (!StringUtils.isEmpty(action)){
            switch (action){
                case "backToMenuButton" -> {
                    new LootTableEditor(playerMenuUtility, table).open();
                    return;
                }
                case "togglePredicateModeButton" -> {
                    int currentMode = Arrays.asList(LootTable.PredicateSelection.values()).indexOf(pool.getPredicateSelection());
                    if (e.getClick().isLeftClick()){
                        if (currentMode + 1 >= LootTable.PredicateSelection.values().length) currentMode = 0;
                        else currentMode += 1;
                    } else {
                        if (currentMode - 1 < 0) currentMode = LootTable.PredicateSelection.values().length - 1;
                        else currentMode -= 1;
                    }
                    pool.setPredicateSelection(LootTable.PredicateSelection.values()[currentMode]);
                }
                case "predicatesButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new LootPredicateMenu(playerMenuUtility, this).open();
                    return;
                }
                case "deleteButton" -> {
                    confirmDeletion = true;
                    Utils.sendMessage(e.getWhoClicked(), "&cAre you sure you want to delete this loot pool?");
                    setMenuItems();
                    return;
                }
                case "deleteConfirmButton" -> {
                    if (e.isRightClick()){
                        table.getPools().remove(pool.getKey());
                        new LootTableEditor(playerMenuUtility, table).open();
                        return;
                    }
                }
                case "nextPageButton" -> page++;
                case "previousPageButton" -> page = Math.max(0, page - 1);
                case "toggleWeightedButton" -> pool.setWeighted(!pool.isWeighted());
                case "setBaseRollsButton" -> pool.setWeightedRolls(pool.getWeightedRolls() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 5 : 1)));
                case "setBonusLuckRollsButton" -> pool.setBonusLuckRolls(pool.getBonusLuckRolls() + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1 : 0.1)));
                case "createNewButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    LootEntry entry = pool.addEntry(ItemUtils.isEmpty(e.getCursor()) ? new ItemBuilder(Material.GOLD_INGOT).name("&eReplace me!").lore("&fI'm just a placeholder drop!").get() : e.getCursor().clone());
                    new LootEntryEditor(playerMenuUtility, pool, entry).open();
                }
            }
        }

        String data = ItemUtils.getPDCString(BUTTON_DATA, clicked, null);
        if (!StringUtils.isEmpty(data)){
            LootEntry entry = pool.getEntries().get(UUID.fromString(data));
            if (entry != null){
                new LootEntryEditor(playerMenuUtility, pool, entry).open();
                return;
            }
        }

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


        List<LootEntry> entries = new ArrayList<>(pool.getEntries().values());
        entries.sort(Comparator.comparing(LootEntry::getWeight));
        List<ItemStack> buttons = new ArrayList<>();
        entries.forEach(p -> {
            ItemBuilder builder = new ItemBuilder(p.getDrop()).stringTag(BUTTON_DATA, p.getUuid().toString());
            builder.name("&e" + ItemUtils.getItemName(builder.getMeta()));
            builder.lore(String.format("&9Quantity: %s %s",
                    (p.getBaseQuantityMin() != p.getBaseQuantityMax() ? p.getBaseQuantityMin() + "-" + p.getBaseQuantityMax() : p.getBaseQuantityMin()),
                    (p.getQuantityMinFortuneBase() > 0 ? (p.getQuantityMinFortuneBase() != p.getQuantityMaxFortuneBase() ? String.format("(+%.1f-%.1f/fortune)", p.getQuantityMinFortuneBase(), p.getQuantityMaxFortuneBase()) :
                            String.format("(+%.1f/fortune", p.getQuantityMinFortuneBase())) : "")
            ));
            if (!p.getPredicates().isEmpty()) {
                builder.appendLore("&6" + (p.getPredicateSelection() == LootTable.PredicateSelection.ANY ? "Any" : "All") + "&e of the following conditions");
                builder.appendLore("&emust pass:");
                p.getPredicates().forEach(pr -> builder.appendLore(StringUtils.separateStringIntoLines("&f> " + pr.getActiveDescription(), 40)));
            }
            if (p.isGuaranteedPresent()){
                builder.appendLore("&aEntry is always present once if", "&aconditions succeed");
            } else {
                if (pool.isWeighted()){
                    double combinedWeight = pool.getEntries().values().stream().mapToDouble(LootEntry::getWeight).sum();
                    builder.appendLore("&eEntry weight: " + p.getWeight() + (p.getWeightQuality() > 0 ? String.format(" (+%.1f/luck)", p.getWeightQuality()) : ""),
                            "&6With a combined weight of " + combinedWeight,
                            String.format("&6this entry has a %.1f%% chance", ((p.getWeight() / combinedWeight) * 100)),
                            "&6of dropping per roll (ignoring luck)");
                } else {
                    builder.appendLore(String.format("&eEntry chance: %.1f %s", p.getChance() * 100, (p.getChanceQuality() > 0 ? String.format(" (+%.1f/luck)", p.getChanceQuality() * 100) : "")));
                }
            }

            buttons.add(builder.get());
        });
        buttons.add(createNewButton);
        Map<Integer, List<ItemStack>> pages = Utils.paginate(entryIndexes.length, buttons);

        page = Math.max(1, Math.min(page, pages.size()));

        if (!pages.isEmpty()){
            int index = 0;
            for (ItemStack i : pages.get(page - 1)){
                inventory.setItem(entryIndexes[index], i);
                index++;
            }
        }

        List<String> predicateLore = new ArrayList<>();
        pool.getPredicates().forEach(p -> predicateLore.addAll(StringUtils.separateStringIntoLines("&d> " + p.getActiveDescription(), 40)));
        ItemBuilder predicateIcon = new ItemBuilder(predicatesButton).placeholderLore("%predicates%", predicateLore);

        inventory.setItem(predicateTypeIndex, new ItemBuilder(togglePredicateModeButton).name("&eFilter Mode: " +
                    switch (pool.getPredicateSelection()){
                        case ALL -> "&aALL conditions must pass";
                        case ANY -> "&aANY condition must pass";
                    }
                ).get());
        inventory.setItem(deleteIndex, confirmDeletion ? deleteConfirmButton : deleteButton);
        inventory.setItem(backToMenuIndex, backToMenuButton);
        inventory.setItem(toggleWeightedIndex, new ItemBuilder(toggleWeightedButton).name("&eIs Weighted: " + (pool.isWeighted() ? "Yes" : "No")).get());
        inventory.setItem(predicatesIndex, predicateIcon.get());
        if (pool.isWeighted()) {
            inventory.setItem(weightedRollsBaseIndex, new ItemBuilder(setBaseRollsButton).name("&eBase Rolls: " + pool.getWeightedRolls()).get());
            inventory.setItem(weightedRollsLuckBonusIndex, new ItemBuilder(setBonusLuckRollsButton).name("&eBonus Luck Rolls: " + pool.getBonusLuckRolls()).get());
        }
        if (page < pages.size()) inventory.setItem(nextPageIndex, nextPageButton);
        if (page > 1) inventory.setItem(previousPageIndex, previousPageButton);
    }

    @Override
    public void setPredicates(Collection<LootPredicate> predicates) {

    }

    @Override
    public Collection<LootPredicate> getPredicates() {
        return pool.getPredicates();
    }
}
