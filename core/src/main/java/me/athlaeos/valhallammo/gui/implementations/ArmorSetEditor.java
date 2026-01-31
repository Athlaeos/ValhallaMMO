package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.SetStatsMenu;
import me.athlaeos.valhallammo.item.ArmorSet;
import me.athlaeos.valhallammo.item.ArmorSetRegistry;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArmorSetEditor extends Menu implements SetStatsMenu {
    private static final NamespacedKey BUTTON_ACTION_KEY = ValhallaMMO.key("button_action");
    private static final NamespacedKey BUTTON_DATA = ValhallaMMO.key("button_data");
    private static final int backToMenuButtonIndex = 53;
    private static final int deleteButtonIndex = 45;
    private static final int nextPageButtonIndex = 53;
    private static final int previousPageButtonIndex = 45;
    private static final int statsButtonIndex = 31;
    private static final int nameButtonIndex = 12;
    private static final int loreButtonIndex = 13;
    private static final int amountRequiredButtonIndex = 14;

    private static final ItemStack createNewButton = new ItemBuilder(getButtonData("editor_armorset_newentry", Material.LIME_DYE))
            .name("&b&lNew Entry")
            .stringTag(BUTTON_ACTION_KEY, "createNewButton")
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
    private static final ItemStack nextPageButton = new ItemBuilder(getButtonData("editor_nextpage", Material.ARROW))
            .name("&7&lNext page")
            .stringTag(BUTTON_ACTION_KEY, "nextPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack previousPageButton = new ItemBuilder(getButtonData("editor_prevpage", Material.ARROW))
            .name("&7&lPrevious page")
            .stringTag(BUTTON_ACTION_KEY, "previousPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack backToMenuButton = new ItemBuilder(getButtonData("editor_backtomenu", Material.BOOK))
            .stringTag(BUTTON_ACTION_KEY, "backToMenuButton")
            .name("&fBack to Menu").get();
    private static final ItemStack setAmountRequiredButton = new ItemBuilder(getButtonData("editor_armorset_setrequired", Material.DIAMOND_CHESTPLATE))
            .name("&dSet Size")
            .lore("&fDetermines how many pieces", "&fof this set the player has", "&fto wear to get the set bonus.")
            .stringTag(BUTTON_ACTION_KEY, "setAmountRequiredButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setNameButton = new ItemBuilder(getButtonData("editor_armorset_setname", Material.NAME_TAG))
            .name("&dSet Name")
            .lore("&fDetermines the set name displayed", "&fon the set items.")
            .stringTag(BUTTON_ACTION_KEY, "setNameButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setLoreButton = new ItemBuilder(getButtonData("editor_armorset_setlore", Material.WRITABLE_BOOK))
            .name("&dSet Lore")
            .lore("&fDetermines the lore displayed", "&fon the set items when wearing", "&fa full set")
            .stringTag(BUTTON_ACTION_KEY, "setLoreButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack setStatsButton = new ItemBuilder(getButtonData("editor_armorset_setstats", Material.PAPER))
            .name("&dSet Stats")
            .lore("&fDetermines the stats given", "&fto players wearing a complete", "&fset.")
            .stringTag(BUTTON_ACTION_KEY, "setStatsButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();

    private int page = 0;
    private ArmorSet currentSet = null;

    public ArmorSetEditor(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF31A" : TranslationManager.getTranslation("editormenu_armorsets"));
    }

    @Override
    public int getSlots() {
        return 54;
    }

    boolean confirmDeletion = false;

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);


        ItemStack clicked = e.getCurrentItem();

        String action = ItemUtils.getPDCString(BUTTON_ACTION_KEY, clicked, "");
        if (!StringUtils.isEmpty(action)){
            switch (action){
                case "backToMenuButton" -> currentSet = null;
                case "deleteButton" -> {
                    confirmDeletion = true;
                    Utils.sendMessage(e.getWhoClicked(), "&cAre you sure you want to delete this armor set?");
                    setMenuItems();
                    return;
                }
                case "deleteConfirmButton" -> {
                    if (e.isRightClick() && currentSet != null){
                        ArmorSetRegistry.getRegisteredSets().remove(currentSet.getId());
                        currentSet = null;
                        return;
                    }
                }
                case "setAmountRequiredButton" -> currentSet.setPiecesRequired(Math.max(0, currentSet.getPiecesRequired() + (e.isRightClick() ? -1 : 1)));
                case "nextPageButton" -> page++;
                case "previousPageButton" -> page = Math.max(0, page - 1);
                case "setStatsButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new StatSelectionMenu(playerMenuUtility, this).open();
                    return;
                }
                case "createNewButton" -> {
                    playerMenuUtility.setPreviousMenu(this); // fallback in case player doesn't wanna
                    e.getWhoClicked().closeInventory();
                    Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                            new Question("&fWhat should the set's id be? (type in chat, or 'cancel' to cancel)", s -> !ArmorSetRegistry.getRegisteredSets().containsKey(s), "&cArmor set with this key already exists! Try again")
                    ) {
                        @Override
                        public Action<Player> getOnFinish() {
                            if (getQuestions().isEmpty()) return super.getOnFinish();
                            Question question = getQuestions().get(0);
                            if (question.getAnswer() == null) return super.getOnFinish();
                            return (p) -> {
                                String answer = question.getAnswer().replaceAll(" ", "_").toLowerCase(java.util.Locale.US);
                                if (answer.contains("cancel")) playerMenuUtility.getPreviousMenu().open();
                                else if (ArmorSetRegistry.getRegisteredSets().containsKey(answer))
                                    Utils.sendMessage(getWho(), "&cArmor set id already exists!");
                                else {
                                    ArmorSetRegistry.register(new ArmorSet(answer));
                                    playerMenuUtility.getPreviousMenu().open();
                                }
                            };
                        }
                    };
                    Questionnaire.startQuestionnaire((Player) e.getWhoClicked(), questionnaire);
                }
                case "setNameButton" -> {
                    playerMenuUtility.setPreviousMenu(this); // fallback in case player doesn't wanna
                    e.getWhoClicked().closeInventory();
                    Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                            new Question("&fWhat should the set's name be? (type in chat, 'cancel' to cancel, or 'clear' to clear)", null, null)
                    ) {
                        @Override
                        public Action<Player> getOnFinish() {
                            if (getQuestions().isEmpty()) return super.getOnFinish();
                            Question question = getQuestions().get(0);
                            if (question.getAnswer() == null) return super.getOnFinish();
                            return (p) -> {
                                String answer = question.getAnswer();
                                if (!answer.contains("cancel")) {
                                    if (answer.equalsIgnoreCase("clear")) answer = null;
                                    currentSet.setName(answer);
                                }
                                playerMenuUtility.getPreviousMenu().open();
                            };
                        }
                    };
                    Questionnaire.startQuestionnaire((Player) e.getWhoClicked(), questionnaire);
                }
                case "setLoreButton" -> {
                    playerMenuUtility.setPreviousMenu(this); // fallback in case player doesn't wanna
                    e.getWhoClicked().closeInventory();
                    Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                            new Question("&fWhat should the set's lore be? (type in chat, 'cancel' to cancel, or 'clear' to clear. Use /n for new lines)", null, null)
                    ) {
                        @Override
                        public Action<Player> getOnFinish() {
                            if (getQuestions().isEmpty()) return super.getOnFinish();
                            Question question = getQuestions().get(0);
                            if (question.getAnswer() == null) return super.getOnFinish();
                            return (p) -> {
                                String answer = question.getAnswer();
                                if (!answer.contains("cancel")) {
                                    if (answer.equalsIgnoreCase("clear")) answer = null;
                                    currentSet.setLore(answer == null ? new ArrayList<>() : new ArrayList<>(List.of(answer.split("/n"))));
                                }
                                playerMenuUtility.getPreviousMenu().open();
                            };
                        }
                    };
                    Questionnaire.startQuestionnaire((Player) e.getWhoClicked(), questionnaire);
                }
            }
        }

        String data = ItemUtils.getPDCString(BUTTON_DATA, clicked, null);
        if (!StringUtils.isEmpty(data)){
            ArmorSet set = ArmorSetRegistry.getRegisteredSets().get(data);
            if (set != null){
                currentSet = set;
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

        if (currentSet != null){
            inventory.setItem(nameButtonIndex, new ItemBuilder(setNameButton).name("&eSet Name: " + (currentSet.getName() == null ? "Nothing" : currentSet.getName())).get());
            inventory.setItem(loreButtonIndex, new ItemBuilder(setLoreButton).name("&eSet Lore").prependLore("&8&m                          ").prependLore(currentSet.getLore()).prependLore(currentSet.getLore().isEmpty() ? "&fNo lore set" : "&fSet to").get());
            inventory.setItem(amountRequiredButtonIndex, new ItemBuilder(setAmountRequiredButton).name("&eAmount Required: " + currentSet.getPiecesRequired()).get());
            inventory.setItem(statsButtonIndex, new ItemBuilder(setStatsButton).prependLore("&8&m                          ").prependLore(currentSet.getSetBonus().isEmpty() ? List.of("&cNo stats set") : currentSet.getSetBonus().keySet().stream().map(a -> {
                AttributeWrapper wrapper = ItemAttributesRegistry.getCopy(a);
                if (wrapper == null) return null;
                wrapper.setValue(currentSet.getSetBonus().get(a));
                return wrapper.getLoreDisplay();
            }).toList()).get());
            inventory.setItem(backToMenuButtonIndex, backToMenuButton);
            inventory.setItem(deleteButtonIndex, confirmDeletion ? deleteConfirmButton : deleteButton);
        } else {
            List<ItemStack> icons = new ArrayList<>();
            for (ArmorSet set : ArmorSetRegistry.getRegisteredSets().values()){
                ItemBuilder icon = new ItemBuilder(Material.NAME_TAG)
                        .stringTag(BUTTON_DATA, set.getId())
                        .name("&f" + (set.getName() == null ? set.getId() : set.getName()))
                        .lore(set.getLore().isEmpty() ? List.of("&7No lore") : set.getLore())
                        .appendLore("&8&m                          ")
                        .appendLore(set.getSetBonus().isEmpty() ? List.of("&cNo stats") : set.getSetBonus().keySet().stream().map(a -> {
                            AttributeWrapper wrapper = ItemAttributesRegistry.getCopy(a);
                            if (wrapper == null) return null;
                            wrapper.setValue(set.getSetBonus().get(a));
                            return wrapper.getLoreDisplay();
                        }).toList());
                icons.add(icon.get());
            }
            icons.add(createNewButton);
            Map<Integer, List<ItemStack>> pages = Utils.paginate(45, icons);

            page = Math.max(1, Math.min(page, pages.size()));

            if (!pages.isEmpty()){
                int index = 0;
                for (ItemStack i : pages.get(page - 1)){
                    inventory.setItem(index, i);
                    index++;
                }
            }

            if (page > 0 && page < pages.size()) inventory.setItem(previousPageButtonIndex, previousPageButton);
            if (page < pages.size()) inventory.setItem(nextPageButtonIndex, nextPageButton);
        }
    }

    @Override
    public void setStats(Map<String, Double> stats) {
        currentSet.setSetBonus(stats);
    }

    @Override
    public Map<String, Double> getStats() {
        return currentSet.getSetBonus();
    }
}
