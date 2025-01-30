package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.SetLootPredicatesMenu;
import me.athlaeos.valhallammo.gui.SetRecipeOptionMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.loot.*;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.loot.ReplacementEntry;
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

public class ReplacementPoolEditor extends Menu implements SetLootPredicatesMenu, SetRecipeOptionMenu {
    private static final NamespacedKey BUTTON_ACTION_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "button_action");
    private static final NamespacedKey BUTTON_DATA = new NamespacedKey(ValhallaMMO.getInstance(), "button_data");

    private static final int predicatesIndex = 1;
    private static final int predicateTypeIndex = 2;
    private static final int replacementItemIndex = 4;
    private static final int optionIndex = 5;
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

    private final ReplacementTable table;
    private final ReplacementPool pool;
    private boolean tinker = false;

    private static final ItemStack togglePredicateModeButton = new ItemBuilder(getButtonData("editor_loottable_predicatemode", Material.COMPARATOR))
            .name("&eFilter Mode")
            .stringTag(BUTTON_ACTION_KEY, "togglePredicateModeButton")
            .lore("&7Determines to what extent filters",
                    "&7should pass.",
                    "&7Should all filters pass",
                    "&7or should any filter pass?",
                    "&eClick to toggle")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack predicatesButton = new ItemBuilder(getButtonData("editor_loottable_pool_predicates", Material.HOPPER))
            .name("&bFilter")
            .stringTag(BUTTON_ACTION_KEY, "predicatesButton")
            .lore("&7The filter decides if this pool",
                    "&7is capable of dropping anything",
                    "&7based on conditions.",
                    "&eClick to open the menu",
                    "&8&m                <>                ",
                    "%predicates%").get();
    private static final ItemStack recipeOptionsButton = new ItemBuilder(getButtonData("editor_recipe_cooking_recipeoptions", Material.WRITABLE_BOOK))
            .name("&bMatcher Options")
            .stringTag(BUTTON_ACTION_KEY, "recipeOptionsButton")
            .lore("&7Matcher options are item matching",
                    "&7conditions. Only items matching the",
                    "&7selected condition will be used for",
                    "&7replacement.",
                    "&eClick to open the menu").get();
    private static final ItemStack nextPageButton = new ItemBuilder(getButtonData("editor_nextpage", Material.ARROW))
            .name("&7&lNext page")
            .stringTag(BUTTON_ACTION_KEY, "nextPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack previousPageButton = new ItemBuilder(getButtonData("editor_prevpage", Material.ARROW))
            .name("&7&lPrevious page")
            .stringTag(BUTTON_ACTION_KEY, "previousPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();

    private static final ItemStack deleteButton = new ItemBuilder(getButtonData("editor_delete", Material.BARRIER))
            .stringTag(BUTTON_ACTION_KEY, "deleteButton")
            .name("&cDelete Recipe").get();
    private static final ItemStack deleteConfirmButton = new ItemBuilder(getButtonData("editor_deleteconfirm", Material.BARRIER))
            .name("&cDelete Recipe")
            .stringTag(BUTTON_ACTION_KEY, "deleteConfirmButton")
            .enchant(EnchantmentMappings.UNBREAKING.getEnchantment(), 1)
            .lore("&aRight-click &7to confirm recipe deletion")
            .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS).wipeAttributes().get();
    private static final ItemStack backToMenuButton = new ItemBuilder(getButtonData("editor_backtomenu", Material.BOOK))
            .stringTag(BUTTON_ACTION_KEY, "backToMenuButton")
            .name("&fBack to Menu").get();
    private static final ItemStack createNewButton = new ItemBuilder(getButtonData("editor_loottable_newentry", Material.LIME_DYE))
            .name("&b&lNew Entry")
            .stringTag(BUTTON_ACTION_KEY, "createNewButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();

    public ReplacementPoolEditor(PlayerMenuUtility playerMenuUtility, ReplacementPool pool) {
        super(playerMenuUtility);
        this.pool = pool;
        this.table = LootTableRegistry.getReplacementTables().get(pool.getParentTable());
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
                    new ReplacementTableEditor(playerMenuUtility, table).open();
                    LootTableRegistry.resetReplacementTableCache();
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
                case "recipeOptionsButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new RecipeOptionMenu(playerMenuUtility, this).open();
                    return;
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
                        table.getReplacementPools().remove(pool.getKey());
                        new ReplacementTableEditor(playerMenuUtility, table).open();
                        LootTableRegistry.resetReplacementTableCache();
                        return;
                    }
                }
                case "nextPageButton" -> page++;
                case "previousPageButton" -> page = Math.max(0, page - 1);
                case "tinkerToggleButton" -> tinker = !tinker;
                case "createNewButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    ReplacementEntry entry = pool.addEntry(ItemUtils.isEmpty(e.getCursor()) ? new ItemBuilder(Material.GOLD_INGOT).name("&eReplace me!").lore("&fI'm just a placeholder drop!").get() : e.getCursor().clone());
                    new ReplacementEntryEditor(playerMenuUtility, pool, entry).open();
                    LootTableRegistry.resetReplacementTableCache();
                }
            }
        }

        if (replacementItemIndex == e.getRawSlot()){
            // clicked input slot
            if (!ItemUtils.isEmpty(e.getCursor())){
                pool.setToReplace(new SlotEntry(new ItemBuilder(e.getCursor().clone()).amount(1).get(), new MaterialChoice()));
            } else {
                if (pool.getToReplace() != null){
                    pool.getToReplace().setOption(null);
                }
            }
        }

        String data = ItemUtils.getPDCString(BUTTON_DATA, clicked, null);
        if (!StringUtils.isEmpty(data)){
            ReplacementEntry entry = pool.getEntries().get(UUID.fromString(data));
            if (entry != null){
                new ReplacementEntryEditor(playerMenuUtility, pool, entry).open();
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


        List<ReplacementEntry> entries = new ArrayList<>(pool.getEntries().values());
        entries.sort(Comparator.comparing(ReplacementEntry::getWeight));
        List<ItemStack> buttons = new ArrayList<>();
        entries.forEach(p -> {
            ItemBuilder builder = new ItemBuilder(p.tinker() ? pool.getToReplace().getItem() : p.getReplaceBy()).stringTag(BUTTON_DATA, p.getUuid().toString());
            builder.name("&e" + (p.tinker() ? "Tinkered " + pool.getToReplace().getOption().ingredientDescription(pool.getToReplace().getItem()) : ItemUtils.getItemName(ItemUtils.getItemMeta(p.getReplaceBy()))));

            if (!p.getPredicates().isEmpty()) {
                builder.appendLore("&6" + (p.getPredicateSelection() == LootTable.PredicateSelection.ANY ? "Any" : "All") + "&e of the following conditions");
                builder.appendLore("&emust pass:");
                p.getPredicates().forEach(pr -> builder.appendLore(StringUtils.separateStringIntoLines("&f> " + pr.getActiveDescription(), 40)));
            }
            double combinedWeight = pool.getEntries().values().stream().mapToDouble(ReplacementEntry::getWeight).sum();
            builder.appendLore("&eEntry weight: " + p.getWeight() + (p.getWeightBonusLooting() > 0 ? String.format(" (+%.1f/fortune)", p.getWeightBonusLooting()) : "") + (p.getWeightBonusLuck() > 0 ? String.format(" (+%.1f/luck)", p.getWeightBonusLuck()) : ""),
                    "&6With a combined weight of " + combinedWeight,
                    String.format("&6this entry has a %.1f%% chance", ((p.getWeight() / combinedWeight) * 100)),
                    "&6of dropping per roll (ignoring luck)");

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
        inventory.setItem(predicatesIndex, predicateIcon.get());
        inventory.setItem(optionIndex, recipeOptionsButton);

        ItemStack icon = new ItemBuilder(pool.getToReplace().getItem().clone()).appendLore(SlotEntry.getOptionLore(pool.getToReplace())).get();
        inventory.setItem(replacementItemIndex, icon);

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

    @Override
    public void setRecipeOption(RecipeOption option) {
        if (option == null) return;
        if (!option.isCompatible(pool.getToReplace().getItem()) || !option.isCompatibleWithInputItem(true)) {
            Utils.sendMessage(playerMenuUtility.getOwner(), "&cNot compatible with this item");
        } else {
            pool.getToReplace().setOption(option);
        }
    }
}
