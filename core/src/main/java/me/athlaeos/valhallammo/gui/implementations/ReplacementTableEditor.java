package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.loot.*;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class ReplacementTableEditor extends Menu {
    private static final NamespacedKey BUTTON_ACTION_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "button_action");
    private static final NamespacedKey BUTTON_DATA = new NamespacedKey(ValhallaMMO.getInstance(), "button_data");
    private static final int iconIndex = 4;
    private static final int poolDescriptionIndex = 16;
    private static final int previousPageIndex = 27;
    private static final int nextPageIndex = 35;
    private static final int deleteIndex = 45;
    private static final int backToMenuIndex = 53;
    private static final int[] poolIndexes = new int[]{
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private static final ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").get();

    private final ReplacementTable table;

    private final ItemStack iconLabel;
    private static final ItemStack poolDescriptionLabel = new ItemBuilder(getButtonData("editor_loottable_descriptionlabel", Material.PAPER))
            .name("&9What's a pool?")
            .lore("&fA replacement pool is a subdivision of a",
                    "&freplacement table.",
                    "&fReplacement tables can have as many pools",
                    "&fas you want, but only one will be selected",
                    "&ffor each potential loot item as long as",
                    "&fits filter conditions pass. ")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
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
    private static final ItemStack createNewButton = new ItemBuilder(getButtonData("editor_loottable_newpool", Material.LIME_DYE))
            .name("&b&lNew Pool")
            .stringTag(BUTTON_ACTION_KEY, "createNewButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();

    public ReplacementTableEditor(PlayerMenuUtility playerMenuUtility, ReplacementTable table) {
        super(playerMenuUtility);
        this.table = table;

        iconLabel = new ItemBuilder(table.getIcon())
                .lore("&fThe icon of the replacement table.",
                        "&fNot visible to players, it's",
                        "&fjust for your own organization",
                        "&fneeds.",
                        "&6Click with item to change icon")
                .name("&eReplacement Table Icon")
                .get();
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF310\uF80C\uF80A\uF808\uF802&8%table%" : TranslationManager.getTranslation("editormenu_loottables")).replace("%table%", table.getKey());
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

        ItemStack cursor = e.getCursor();
        ItemStack clicked = e.getCurrentItem();

        String action = ItemUtils.getPDCString(BUTTON_ACTION_KEY, clicked, "");
        if (!StringUtils.isEmpty(action)){
            switch (action){
                case "backToMenuButton" -> {
                    new LootTableOverviewMenu(playerMenuUtility).open();
                    LootTableRegistry.resetReplacementTableCache();
                    return;
                }
                case "deleteButton" -> {
                    confirmDeletion = true;
                    Utils.sendMessage(e.getWhoClicked(), "&cAre you sure you want to delete this loot table?");
                    setMenuItems();
                    return;
                }
                case "deleteConfirmButton" -> {
                    if (e.isRightClick()){
                        LootTableRegistry.getLootTables().remove(table.getKey());
                        new LootTableOverviewMenu(playerMenuUtility).open();
                        LootTableRegistry.resetReplacementTableCache();
                        return;
                    }
                }
                case "nextPageButton" -> page++;
                case "previousPageButton" -> page = Math.max(0, page - 1);
                case "createNewButton" -> {
                    playerMenuUtility.setPreviousMenu(this); // fallback in case player doesn't wanna
                    e.getWhoClicked().closeInventory();
                    Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                            new Question("&fWhat should the pool's key be? (type in chat, or 'cancel' to cancel)", s -> !table.getReplacementPools().containsKey(s), "&cPool with this key already exists! Try again")
                    ) {
                        @Override
                        public Action<Player> getOnFinish() {
                            if (getQuestions().isEmpty()) return super.getOnFinish();
                            Question question = getQuestions().get(0);
                            if (question.getAnswer() == null) return super.getOnFinish();
                            return (p) -> {
                                String answer = question.getAnswer().replaceAll(" ", "_").toLowerCase(java.util.Locale.US);
                                if (answer.contains("cancel")) playerMenuUtility.getPreviousMenu().open();
                                else if (table.getReplacementPools().containsKey(answer))
                                    Utils.sendMessage(getWho(), "&cPool key already exists!");
                                else {
                                    ReplacementPool pool = table.addPool(answer);
                                    LootTableRegistry.resetReplacementTableCache();
                                    new ReplacementPoolEditor(playerMenuUtility, pool).open();
                                }
                            };
                        }
                    };
                    Questionnaire.startQuestionnaire((Player) e.getWhoClicked(), questionnaire);
                }
            }
        }

        String data = ItemUtils.getPDCString(BUTTON_DATA, clicked, null);
        if (!StringUtils.isEmpty(data)){
            ReplacementPool pool = table.getReplacementPools().get(data);
            if (pool != null){
                new ReplacementPoolEditor(playerMenuUtility, pool).open();
                return;
            }
        }

        if (iconIndex == e.getRawSlot() && !ItemUtils.isEmpty(cursor)){
            table.setIcon(cursor.getType());
            iconLabel.setType(cursor.getType());
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
        inventory.setItem(iconIndex, iconLabel);

        List<ReplacementPool> pools = new ArrayList<>(table.getReplacementPools().values());
        pools.sort(Comparator.comparing(ReplacementPool::getKey));
        List<ItemStack> buttons = new ArrayList<>();
        pools.forEach(p -> {
            ItemBuilder builder = new ItemBuilder(Material.BOOK)
                    .name("&6" + p.getKey())
                    .stringTag(BUTTON_DATA, p.getKey());
            if (!p.getPredicates().isEmpty()) {
                builder.appendLore("&6" + (p.getPredicateSelection() == LootTable.PredicateSelection.ANY ? "Any" : "All") + "&e of the following conditions");
                builder.appendLore("&emust pass:");
                p.getPredicates().forEach(pr -> builder.appendLore(StringUtils.separateStringIntoLines("&f> " + pr.getActiveDescription(), 40)));
            }
            if (p.getEntries().isEmpty()) builder.appendLore("&cPool has no options");
            else builder.appendLore(String.format("&6Has %d potential options", p.getEntries().size()));
            buttons.add(builder.get());
        });
        buttons.add(createNewButton);
        Map<Integer, List<ItemStack>> pages = Utils.paginate(poolIndexes.length, buttons);

        page = Math.max(1, Math.min(page, pages.size()));

        if (!pages.isEmpty()){
            int index = 0;
            for (ItemStack i : pages.get(page - 1)){
                inventory.setItem(poolIndexes[index], i);
                index++;
            }
        }
        inventory.setItem(deleteIndex, confirmDeletion ? deleteConfirmButton : deleteButton);
        inventory.setItem(backToMenuIndex, backToMenuButton);
        inventory.setItem(poolDescriptionIndex, poolDescriptionLabel);
        if (page < pages.size()) inventory.setItem(nextPageIndex, nextPageButton);
        if (page > 1) inventory.setItem(previousPageIndex, previousPageButton);
    }
}
