package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.loot.LootEntry;
import me.athlaeos.valhallammo.loot.LootPool;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;

import java.util.*;

public class LootFreeSelectionMenu extends Menu {
    private static final NamespacedKey BUTTON_ACTION_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "button_action");
    private static final NamespacedKey BUTTON_DATA = new NamespacedKey(ValhallaMMO.getInstance(), "button_data");
    private static final boolean selectableLootModifiers = ValhallaMMO.getPluginConfig().getBoolean("selectable_loot_modifiers");
    private static final int nextGuaranteedDropPageIndex = 6;
    private static final int previousGuaranteedDropPageIndex = 2;
    private static final int nextDropPageIndex = 26;
    private static final int previousDropPageIndex = 18;
    private static final int confirmIndex = 44;

    private static final int[] entryGuaranteedLayoutPriority = new int[]{4, 13, 3, 5, 12, 14};
    private static final int[] entrySelectLayoutPriority = new int[]{31, 30, 32, 29, 33, 28, 34};

    private final LootTable table;
    private final boolean allowRepeatedSelection;
    private final Action<Map<LootEntry, Integer>> onFinish;
    private final List<LootEntry> guaranteedDrops = new ArrayList<>();
    private final List<ItemBuilder> guaranteedDropsItems = new ArrayList<>();
    private final Map<UUID, Integer> selection = new HashMap<>();
    private final Map<UUID, ItemBuilder> generatedItems = new HashMap<>();
    private final Map<UUID, LootEntry> entryMapping = new HashMap<>();
    private int allowedPicks;

    private int guaranteedDropsPage = 0;
    private int dropsPage = 0;

    private static final ItemStack confirmSelectionButton = new ItemBuilder(getButtonData("lootselection_confirm", Material.STRUCTURE_VOID))
            .name(TranslationManager.getTranslation("menu_loottablefreeselection_confirmselection"))
            .stringTag(BUTTON_ACTION_KEY, "confirmSelectionButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack nextPageButtonGuaranteed = new ItemBuilder(getButtonData("editor_nextpage", Material.ARROW))
            .name(TranslationManager.getTranslation("translation_next_page"))
            .stringTag(BUTTON_ACTION_KEY, "nextPageButtonGuaranteed")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack previousPageButtonGuaranteed = new ItemBuilder(getButtonData("editor_prevpage", Material.ARROW))
            .name(TranslationManager.getTranslation("translation_previous_page"))
            .stringTag(BUTTON_ACTION_KEY, "previousPageButtonGuaranteed")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack nextPageButtonSelection = new ItemBuilder(getButtonData("editor_nextpage", Material.ARROW))
            .name(TranslationManager.getTranslation("translation_next_page"))
            .stringTag(BUTTON_ACTION_KEY, "nextPageButtonSelection")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack previousPageButtonSelection = new ItemBuilder(getButtonData("editor_prevpage", Material.ARROW))
            .name(TranslationManager.getTranslation("translation_previous_page"))
            .stringTag(BUTTON_ACTION_KEY, "previousPageButtonSelection")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack unselectedDropBase = new ItemBuilder(getButtonData("lootselection_unpicked", Material.RED_DYE)).name("&r").get();
    private static final ItemStack selectedDropBase = new ItemBuilder(getButtonData("lootselection_picked", Material.LIME_DYE)).name("&r").get();

    public LootFreeSelectionMenu(PlayerMenuUtility playerMenuUtility, LootTable table, LootContext context, boolean allowRepeatedSelection, Action<Map<LootEntry, Integer>> onFinish) {
        super(playerMenuUtility);
        this.table = table;
        this.allowRepeatedSelection = allowRepeatedSelection;
        this.onFinish = onFinish;

        for (LootPool pool : table.getPools().values()){
            if (allowedPicks <= 0) allowedPicks = pool.getRolls(context);
            for (LootEntry entry : pool.getEntries().values()){
                entryMapping.put(entry.getUuid(), entry);
                ItemBuilder drop = new ItemBuilder(entry.getDrop());
                if (selectableLootModifiers) DynamicItemModifier.modify(ModifierContext.builder(drop).crafter(playerMenuUtility.getOwner()).validate().get(), entry.getModifiers());
                if (CustomFlag.hasFlag(drop.getMeta(), CustomFlag.UNCRAFTABLE)) continue;
                if (entry.isGuaranteedPresent()) {
                    guaranteedDropsItems.add(drop);
                    guaranteedDrops.add(entry);
                } else generatedItems.put(entry.getUuid(), drop.stringTag(BUTTON_DATA, entry.getUuid().toString()));
            }
        }
    }

    @Override
    public String getMenuName() {
        return Utils.chat((ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF100\uF80C\uF809\uF808" : "") + TranslationManager.getTranslation("menu_loottablefreeselection"));
    }

    @Override
    public int getSlots() {
        return 45;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (ItemUtils.isEmpty(clicked)) return;

        ItemMeta clickedMeta = clicked.getItemMeta();
        if (clickedMeta == null) return;
        String action = ItemUtils.getPDCString(BUTTON_ACTION_KEY, clickedMeta, null);
        if (action != null){
            switch (action){
                case "confirmSelectionButton" -> {
                    playerMenuUtility.getOwner().closeInventory();
                    Map<LootEntry, Integer> rewards = new HashMap<>();
                    guaranteedDrops.forEach(d -> rewards.put(d, 1));
                    for (LootPool pool : table.getPools().values()){
                        for (UUID uuid : selection.keySet()) {
                            LootEntry entry = pool.getEntries().get(uuid);
                            int quantity = selection.getOrDefault(uuid, 0);
                            if (entry == null || quantity <= 0) continue;
                            rewards.put(entry, quantity);
                        }
                    }
                    onFinish.act(rewards);
                    return;
                }
                case "previousPageButtonGuaranteed" -> guaranteedDropsPage = Math.max(0, guaranteedDropsPage - 1);
                case "nextPageButtonGuaranteed" -> guaranteedDropsPage = Math.min((int) Math.ceil(guaranteedDrops.size() / (double) entryGuaranteedLayoutPriority.length), guaranteedDropsPage + 1);
                case "previousPageButtonSelection" -> dropsPage = Math.max(0, dropsPage - 1);
                case "nextPageButtonSelection" -> dropsPage = Math.min((int) Math.ceil(generatedItems.size() / (double) entrySelectLayoutPriority.length), dropsPage + 1);
            }
        } else {
            String data = ItemUtils.getPDCString(BUTTON_DATA, clickedMeta, null);
            if (data == null) return;
            UUID uuid = UUID.fromString(data);
            if (e.isLeftClick() && allowedPicks > 0){
                if (selection.containsKey(uuid) && allowRepeatedSelection) {
                    int quantity = selection.get(uuid);
                    selection.put(uuid, quantity + 1);
                    allowedPicks--;
                } else if (!selection.containsKey(uuid)) {
                    selection.put(uuid, 1);
                    allowedPicks--;
                }
            } else if (e.isRightClick() && selection.containsKey(uuid)){
                int quantity = selection.get(uuid) - 1;
                if (quantity <= 0) selection.remove(uuid);
                else selection.put(uuid, quantity);
                allowedPicks++;
            }
        }

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

        Map<Integer, List<ItemBuilder>> guaranteedDropsPages = Utils.paginate(entryGuaranteedLayoutPriority.length, guaranteedDropsItems);
        guaranteedDropsPage = Math.max(1, Math.min(guaranteedDropsPage, guaranteedDropsPages.size()));
        if (!guaranteedDropsPages.isEmpty()){
            int index = 0;
            for (ItemBuilder i : guaranteedDropsPages.get(guaranteedDropsPage - 1)){
                i = new ItemBuilder(i.getItem().clone()).setMeta(i.getMeta().clone());
                inventory.setItem(entryGuaranteedLayoutPriority[index], i.prependLore(TranslationManager.getTranslation("menu_loottablefreeselection_guaranteedincluded")).translate().get());
                index++;
            }
        }

        Map<Integer, List<ItemBuilder>> dropsPages = Utils.paginate(entrySelectLayoutPriority.length, new ArrayList<>(generatedItems.values()));
        dropsPage = Math.max(1, Math.min(dropsPage, dropsPages.size()));
        if (!dropsPages.isEmpty()){
            int index = 0;
            for (ItemBuilder i : dropsPages.get(dropsPage - 1)){
                i = new ItemBuilder(i.getItem().clone()).setMeta(i.getMeta().clone());

                String uuidString = ItemUtils.getPDCString(BUTTON_DATA, i.getMeta(), null);
                if (uuidString != null){
                    UUID uuid = UUID.fromString(uuidString);
                    LootEntry entry = entryMapping.get(uuid);
                    if (entry == null) continue;

                    DynamicItemModifier.modify(ModifierContext.builder(i).crafter(playerMenuUtility.getOwner()).validate().get(), entry.getModifiers());

                    int existingAmount = Math.max(1, selection.getOrDefault(uuid, 1));
                    String quantity = (entry.getBaseQuantityMin() != entry.getBaseQuantityMax() ? (entry.getBaseQuantityMin() * existingAmount) + "-" + (entry.getBaseQuantityMax() * existingAmount) : String.valueOf(entry.getBaseQuantityMin() * existingAmount)) + "x ";

                    inventory.setItem(entrySelectLayoutPriority[index], i
                            .name(TranslationManager.getTranslation("menu_loottablefreeselection_nameformat").replace("%quantity%", selection.containsKey(uuid) ? quantity : "").replace("%item%", ItemUtils.getItemName(i.getMeta())))
                            .prependLore(ItemUtils.setListPlaceholder(TranslationManager.getListTranslation("loot_selection_prefix"), "%rolls%", (allowedPicks > 0 ? "&a" : "&c") + allowedPicks))
                            .appendLore(ItemUtils.setListPlaceholder(TranslationManager.getListTranslation("loot_selection_suffix"), "%rolls%", (allowedPicks > 0 ? "&a" : "&c") + allowedPicks))
                            .translate().get()
                    );

                    inventory.setItem(entrySelectLayoutPriority[index] + 9,
                            new ItemBuilder(selection.containsKey(uuid) ? selectedDropBase : unselectedDropBase)
                                    .name(ItemUtils.getItemName(i.getMeta()))
                                    .prependLore(ItemUtils.setListPlaceholder(TranslationManager.getListTranslation("loot_selection_prefix"), "%rolls%", (allowedPicks > 0 ? "&a" : "&c") + allowedPicks))
                                    .appendLore(ItemUtils.setListPlaceholder(TranslationManager.getListTranslation("loot_selection_suffix"), "%rolls%", (allowedPicks > 0 ? "&a" : "&c") + allowedPicks))
                                    .stringTag(BUTTON_DATA, uuidString)
                                    .amount(Math.max(1, selection.getOrDefault(uuid, 1)))
                                    .translate().get()
                    );
                }
                index++;
            }
        }

        if (allowedPicks == 0 || (!allowRepeatedSelection && selection.size() >= generatedItems.size())) inventory.setItem(confirmIndex, confirmSelectionButton);
        if (guaranteedDropsPage > 1) inventory.setItem(previousGuaranteedDropPageIndex, previousPageButtonGuaranteed);
        if (dropsPage > 1) inventory.setItem(previousDropPageIndex, previousPageButtonSelection);
        if (guaranteedDropsPage <= guaranteedDropsPages.size()) inventory.setItem(nextGuaranteedDropPageIndex, nextPageButtonGuaranteed);
        if (dropsPage <= dropsPages.size()) inventory.setItem(nextDropPageIndex, nextPageButtonSelection);
    }
}
