package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.SetLootPredicatesMenu;
import me.athlaeos.valhallammo.gui.SetModifiersMenu;
import me.athlaeos.valhallammo.gui.implementations.DynamicModifierMenu;
import me.athlaeos.valhallammo.gui.implementations.LootPredicateMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantLevel;
import me.athlaeos.valhallammo.trading.dom.MerchantTrade;
import me.athlaeos.valhallammo.trading.dom.MerchantType;
import me.athlaeos.valhallammo.trading.services.ServiceRegistry;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class CustomTradeManagementMenu extends Menu implements SetModifiersMenu, SetLootPredicatesMenu {
    private static final NamespacedKey KEY_PROFESSION = new NamespacedKey(ValhallaMMO.getInstance(), "button_profession");
    private static final NamespacedKey KEY_SUBTYPE = new NamespacedKey(ValhallaMMO.getInstance(), "button_subtype");
    private static final NamespacedKey KEY_BUTTON = new NamespacedKey(ValhallaMMO.getInstance(), "button_functionality");
    private static final NamespacedKey KEY_TRADE = new NamespacedKey(ValhallaMMO.getInstance(), "button_trade");

    private static final List<Integer> noviceTradeIndexes = List.of(4, 5, 6, 7);
    private static final List<Integer> apprenticeTradeIndexes = List.of(13, 14, 15, 16);
    private static final List<Integer> journeymanTradeIndexes = List.of(22, 23, 24, 25);
    private static final List<Integer> expertTradeIndexes = List.of(31, 32, 33, 34);
    private static final List<Integer> masterTradeIndexes = List.of(40, 41, 42, 43);

    private View view = View.PROFESSIONS;
    private int subTypePage = 0;
    private Villager.Profession currentProfession = null;
    private boolean travelingMerchant = false;
    private MerchantType currentSubType = null;
    private MerchantTrade currentTrade = null;
    private boolean confirmDeletion = false;

    private int tradesNovicePage = 0;
    private int tradesApprenticePage = 0;
    private int tradesJourneymanPage = 0;
    private int tradesExpertPage = 0;
    private int tradesMasterPage = 0;

    public CustomTradeManagementMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return Utils.chat(switch(view) {
            case TRADE -> "&8Manage Trade " + currentTrade.getID();
            case TRADES -> "&8" + currentSubType.getType() + "'s Trades";
            case SUBTYPE -> "&8" + currentSubType.getType();
            case SUBTYPES -> "&8Subtypes of " + (travelingMerchant ? "Traveling Merchant" : StringUtils.toPascalCase(currentProfession.toString().replace("_", " ")));
            case PROFESSIONS -> "&8Profession Overview";
        }); // TODO custom menu
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(!(e.getClickedInventory() instanceof PlayerInventory));

        ItemBuilder clickedItem = ItemUtils.isEmpty(e.getCurrentItem()) ? null : new ItemBuilder(e.getCurrentItem());
        ItemBuilder cursor = ItemUtils.isEmpty(e.getCursor()) ? null : new ItemBuilder(e.getCursor());
        String buttonFunction = clickedItem == null ? null : clickedItem.getMeta().getPersistentDataContainer().get(KEY_BUTTON, PersistentDataType.STRING);
        if (view == View.PROFESSIONS){
            String clickedData = clickedItem == null ? null : clickedItem.getMeta().getPersistentDataContainer().get(KEY_PROFESSION, PersistentDataType.STRING);
            Villager.Profession clickedProfession = Catch.catchOrElse(() -> Villager.Profession.valueOf(clickedData), null);
            travelingMerchant = clickedData != null && clickedData.equals("TRAVELING");
            if (clickedProfession == null && !travelingMerchant) return;
            currentProfession = clickedProfession;
            switchView(View.SUBTYPES);
        } else if (view == View.SUBTYPES){
            if (buttonFunction != null && buttonFunction.equals("createNewButton")) {
                playerMenuUtility.setPreviousMenu(this); // fallback in case player doesn't wanna
                e.getWhoClicked().closeInventory();
                Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                        new Question("&fWhat should the type's ID be? (type in chat, or 'cancel' to cancel)", s -> CustomMerchantManager.getMerchantType(s.replaceAll(" ", "_").toLowerCase(java.util.Locale.US)) == null, "&cType with this key already exists! Try again")
                ) {
                    @Override
                    public Action<Player> getOnFinish() {
                        if (getQuestions().isEmpty()) return super.getOnFinish();
                        Question question = getQuestions().get(0);
                        if (question.getAnswer() == null) return super.getOnFinish();
                        return (p) -> {
                            String answer = question.getAnswer().replaceAll(" ", "_").toLowerCase(java.util.Locale.US);
                            if (answer.contains("cancel")) playerMenuUtility.getPreviousMenu().open();
                            else if (CustomMerchantManager.getMerchantType(answer) != null)
                                Utils.sendMessage(getWho(), "&cThe given type already exists!");
                            else {
                                MerchantType type = new MerchantType(answer);
                                currentSubType = type;
                                CustomMerchantManager.registerMerchantType(type);
                                if (travelingMerchant) CustomMerchantManager.getTravelingMerchantConfiguration().getMerchantTypes().add(type.getType());
                                else CustomMerchantManager.getMerchantConfiguration(currentProfession).getMerchantTypes().add(type.getType());
                                switchView(View.SUBTYPE);
                            }
                        };
                    }
                };
                Questionnaire.startQuestionnaire((Player) e.getWhoClicked(), questionnaire);
            } else if (buttonFunction != null && buttonFunction.equals("backToMenuButton")) {
                currentProfession = null;
                travelingMerchant = false;
                switchView(View.PROFESSIONS);
            } else {
                String clickedSubtype = clickedItem == null ? null : clickedItem.getMeta().getPersistentDataContainer().get(KEY_SUBTYPE, PersistentDataType.STRING);
                MerchantType type = clickedSubtype == null || clickedSubtype.isEmpty() ? null : CustomMerchantManager.getMerchantType(clickedSubtype);
                if (type == null) return;
                if (e.isRightClick() && e.isShiftClick()) {
                    confirmDeletion = true;
                    setMenuItems();
                    return;
                } else if (e.isLeftClick() && e.isShiftClick()) {
                    if (confirmDeletion) CustomMerchantManager.removeType(type);
                } else {
                    currentSubType = type;
                    switchView(View.SUBTYPE);
                }
            }
        } else if (view == View.SUBTYPE){
            if (buttonFunction != null){
                switch (buttonFunction) {
                    case "subtypeNameButton" -> {
                        playerMenuUtility.setPreviousMenu(this); // fallback in case player doesn't wanna
                        e.getWhoClicked().closeInventory();
                        Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                                new Question("&fWhat should the type's display name be? (type in chat, or 'cancel' to cancel, or 'reset' to remove)", s -> true, "")
                        ) {
                            @Override
                            public Action<Player> getOnFinish() {
                                if (getQuestions().isEmpty()) return super.getOnFinish();
                                Question question = getQuestions().get(0);
                                if (question.getAnswer() == null) return super.getOnFinish();
                                return (p) -> {
                                    String answer = question.getAnswer();
                                    if (answer.contains("cancel")) {
                                        playerMenuUtility.getPreviousMenu().open();
                                        return;
                                    } else if (answer.equals("reset")) currentSubType.setName(null);
                                    else currentSubType.setName(answer);
                                    playerMenuUtility.getPreviousMenu().open();
                                };
                            }
                        };
                        Questionnaire.startQuestionnaire((Player) e.getWhoClicked(), questionnaire);
                    }
                    case "subtypeServicesButton" -> {
                        new MerchantServicesMenu(playerMenuUtility, this, currentSubType).open();
                        return;
                    }
                    case "subtypeVersionButton" -> currentSubType.setVersion(Math.max(0, currentSubType.getVersion() + ((e.isShiftClick() ? 5 : 1) * (e.isLeftClick() ? 1 : -1))));
                    case "subtypeRealisticButton" -> currentSubType.setResetTradesDaily(!currentSubType.resetsTradesDaily());
                    case "subtypeProfessionLossButton" -> currentSubType.setCanLoseProfession(!currentSubType.canLoseProfession());
                    case "subtypePerPlayerStockButton" -> currentSubType.setPerPlayerStock(!currentSubType.isPerPlayerStock());
                    case "subtypeWeightButton" -> currentSubType.setWeight(currentSubType.getWeight() + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.1 : -0.1)));
                    case "subtypeTradesButton" -> switchView(View.TRADES);
                    case "backToMenuButton" -> {
                        currentSubType = null;
                        switchView(View.SUBTYPES);
                    }
                }
            }
        } else if (view == View.TRADES){
            String trade = clickedItem == null ? null : clickedItem.getMeta().getPersistentDataContainer().get(KEY_TRADE, PersistentDataType.STRING);
            MerchantTrade clickedTrade = trade == null ? null : CustomMerchantManager.getTrade(trade);
            if (clickedTrade != null){
                if (e.isRightClick() && e.isShiftClick()) {
                    confirmDeletion = true;
                    setMenuItems();
                    return;
                } else if (e.isLeftClick() && e.isShiftClick()) {
                    if (confirmDeletion) CustomMerchantManager.removeTrade(clickedTrade);
                } else {
                    currentTrade = clickedTrade;
                    switchView(View.TRADE);
                }
            } else {
                String clickedFunction = clickedItem == null ? null : clickedItem.getMeta().getPersistentDataContainer().get(KEY_BUTTON, PersistentDataType.STRING);
                if (clickedFunction == null) return;
                switch (clickedFunction) {
                    case "createNewButton" -> {
                        MerchantLevel clickedLevel = noviceTradeIndexes.contains(e.getRawSlot()) ? MerchantLevel.NOVICE :
                                apprenticeTradeIndexes.contains(e.getRawSlot()) ? MerchantLevel.APPRENTICE :
                                        journeymanTradeIndexes.contains(e.getRawSlot()) ? MerchantLevel.JOURNEYMAN :
                                                expertTradeIndexes.contains(e.getRawSlot()) ? MerchantLevel.EXPERT :
                                                        MerchantLevel.MASTER;
                        playerMenuUtility.setPreviousMenu(this); // fallback in case player doesn't wanna
                        e.getWhoClicked().closeInventory();
                        Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                                new Question("&fWhat should the trade's ID be? (type in chat, or 'cancel' to cancel)", s -> CustomMerchantManager.getTrade(s.replaceAll(" ", "_").toLowerCase(java.util.Locale.US)) == null, "&cTrade with this key already exists! Try again")
                        ) {
                            @Override
                            public Action<Player> getOnFinish() {
                                if (getQuestions().isEmpty()) return super.getOnFinish();
                                Question question = getQuestions().get(0);
                                if (question.getAnswer() == null) return super.getOnFinish();
                                return (p) -> {
                                    String answer = question.getAnswer().replaceAll(" ", "_").toLowerCase(java.util.Locale.US);
                                    if (answer.contains("cancel")) playerMenuUtility.getPreviousMenu().open();
                                    else if (CustomMerchantManager.getTrade(answer) != null)
                                        Utils.sendMessage(getWho(), "&cThe given trade already exists!");
                                    else {
                                        MerchantTrade trade = new MerchantTrade(answer);
                                        currentTrade = trade;
                                        CustomMerchantManager.registerTrade(trade);
                                        currentSubType.getTrades(clickedLevel).getTrades().add(trade.getID());
                                        switchView(View.TRADE);
                                    }
                                };
                            }
                        };
                        Questionnaire.startQuestionnaire((Player) e.getWhoClicked(), questionnaire);
                    }
                    case "pageForwardButton" -> {
                        if (e.getRawSlot() == noviceTradeIndexes.getLast() + 1) tradesNovicePage++;
                        if (e.getRawSlot() == apprenticeTradeIndexes.getLast() + 1) tradesApprenticePage++;
                        if (e.getRawSlot() == journeymanTradeIndexes.getLast() + 1) tradesJourneymanPage++;
                        if (e.getRawSlot() == expertTradeIndexes.getLast() + 1) tradesExpertPage++;
                        if (e.getRawSlot() == masterTradeIndexes.getLast() + 1) tradesMasterPage++;
                    }
                    case "pageBackButton" -> {
                        if (e.getRawSlot() == noviceTradeIndexes.getFirst() - 1) tradesNovicePage--;
                        if (e.getRawSlot() == apprenticeTradeIndexes.getFirst() - 1) tradesApprenticePage--;
                        if (e.getRawSlot() == journeymanTradeIndexes.getFirst() - 1) tradesJourneymanPage--;
                        if (e.getRawSlot() == expertTradeIndexes.getFirst() - 1) tradesExpertPage--;
                        if (e.getRawSlot() == masterTradeIndexes.getFirst() - 1) tradesMasterPage--;
                    }
                    case "tradesNoviceRollsButton" -> currentSubType.setRolls(MerchantLevel.NOVICE, currentSubType.getRolls(MerchantLevel.NOVICE) + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.01 : -0.01)));
                    case "tradesNoviceRollQualityButton" -> currentSubType.setRollQuality(MerchantLevel.NOVICE, currentSubType.getRollQuality(MerchantLevel.NOVICE) + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.01 : -0.01)));
                    case "tradesApprenticeRollsButton" -> currentSubType.setRolls(MerchantLevel.APPRENTICE, currentSubType.getRolls(MerchantLevel.APPRENTICE) + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.01 : -0.01)));
                    case "tradesApprenticeRollQualityButton" -> currentSubType.setRollQuality(MerchantLevel.APPRENTICE, currentSubType.getRollQuality(MerchantLevel.APPRENTICE) + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.01 : -0.01)));
                    case "tradesApprenticeExpRequirementButton" -> currentSubType.setExpRequirement(MerchantLevel.APPRENTICE, Math.max(0, currentSubType.getRawExpRequirement(MerchantLevel.APPRENTICE) + ((e.isShiftClick() ? 10 : 1) * (e.isLeftClick() ? 1 : -1))));
                    case "tradesJourneymanRollsButton" -> currentSubType.setRolls(MerchantLevel.JOURNEYMAN, currentSubType.getRolls(MerchantLevel.JOURNEYMAN) + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.01 : -0.01)));
                    case "tradesJourneymanRollQualityButton" -> currentSubType.setRollQuality(MerchantLevel.JOURNEYMAN, currentSubType.getRollQuality(MerchantLevel.JOURNEYMAN) + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.01 : -0.01)));
                    case "tradesJourneymanExpRequirementButton" -> currentSubType.setExpRequirement(MerchantLevel.JOURNEYMAN, Math.max(0, currentSubType.getRawExpRequirement(MerchantLevel.JOURNEYMAN) + ((e.isShiftClick() ? 10 : 1) * (e.isLeftClick() ? 1 : -1))));
                    case "tradesExpertRollsButton" -> currentSubType.setRolls(MerchantLevel.EXPERT, currentSubType.getRolls(MerchantLevel.EXPERT) + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.01 : -0.01)));
                    case "tradesExpertRollQualityButton" -> currentSubType.setRollQuality(MerchantLevel.EXPERT, currentSubType.getRollQuality(MerchantLevel.EXPERT) + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.01 : -0.01)));
                    case "tradesExpertExpRequirementButton" -> currentSubType.setExpRequirement(MerchantLevel.EXPERT, Math.max(0, currentSubType.getRawExpRequirement(MerchantLevel.EXPERT) + ((e.isShiftClick() ? 10 : 1) * (e.isLeftClick() ? 1 : -1))));
                    case "tradesMasterRollsButton" -> currentSubType.setRolls(MerchantLevel.MASTER, currentSubType.getRolls(MerchantLevel.MASTER) + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.01 : -0.01)));
                    case "tradesMasterRollQualityButton" -> currentSubType.setRollQuality(MerchantLevel.MASTER, currentSubType.getRollQuality(MerchantLevel.MASTER) + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.01 : -0.01)));
                    case "tradesMasterExpRequirementButton" -> currentSubType.setExpRequirement(MerchantLevel.MASTER, Math.max(0, currentSubType.getRawExpRequirement(MerchantLevel.MASTER) + ((e.isShiftClick() ? 10 : 1) * (e.isLeftClick() ? 1 : -1))));
                    case "backToMenuButton" -> switchView(View.SUBTYPE);
                }
            }
        } else if (view == View.TRADE){
            String clickedFunction = clickedItem == null ? null : clickedItem.getMeta().getPersistentDataContainer().get(KEY_BUTTON, PersistentDataType.STRING);
            if (clickedFunction == null) {
                switch (e.getRawSlot()) {
                    case 20 -> {
                        if (cursor == null) return;
                        currentTrade.setScalingCostItem(cursor.get());
                    }
                    case 21 -> {
                        if (cursor == null) currentTrade.setOptionalCostItem(null);
                        else currentTrade.setOptionalCostItem(cursor.get());
                    }
                    case 23 -> {
                        if (cursor == null) return;
                        currentTrade.setResult(cursor.get());
                    }
                }
                setMenuItems();
                return;
            }
            switch (clickedFunction) {
                case "tradePredicatesButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new LootPredicateMenu(playerMenuUtility, this).open();
                    return;
                }
                case "tradePredicateModeButton" -> {
                    int currentMode = Arrays.asList(LootTable.PredicateSelection.values()).indexOf(currentTrade.getPredicateSelection());
                    if (e.getClick().isLeftClick()){
                        if (currentMode + 1 >= LootTable.PredicateSelection.values().length) currentMode = 0;
                        else currentMode += 1;
                    } else {
                        if (currentMode - 1 < 0) currentMode = LootTable.PredicateSelection.values().length - 1;
                        else currentMode -= 1;
                    }
                    currentTrade.setPredicateSelection(LootTable.PredicateSelection.values()[currentMode]);
                }
                case "tradeWeightButton" -> currentTrade.setWeight((float) (currentTrade.getWeight() + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.1F : -0.1F))));
                case "tradeWeightQualityButton" -> currentTrade.setWeightQuality(currentTrade.getWeightQuality() + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.1F : -0.1F)));
                case "tradeWeightDemandModButton" -> currentTrade.setDemandWeightModifier(currentTrade.getDemandWeightModifier() + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.01F : -0.01F)));
                case "tradeWeightDemandMaxButton" -> currentTrade.setDemandWeightMaxQuantity(currentTrade.getDemandWeightMaxQuantity() + ((e.isShiftClick() ? 10 : 1) * (e.isLeftClick() ? 1 : -1)));
                case "tradeUsesButton" -> currentTrade.setMaxUses(currentTrade.getMaxUses() + ((e.isShiftClick() ? 8 : 1) * (e.isLeftClick() ? 1 : -1)));
                case "tradeUsesDemandModButton" -> currentTrade.setDemandMaxUsesModifier(currentTrade.getDemandMaxUsesModifier() + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.01F : -0.01F)));
                case "tradeUsesDemandMaxButton" -> currentTrade.setDemandMaxUsesMaxQuantity(currentTrade.getDemandMaxUsesMaxQuantity() + ((e.isShiftClick() ? 10 : 1) * (e.isLeftClick() ? 1 : -1)));
                case "tradeReputationPositiveModButton" -> currentTrade.setPositiveReputationMultiplier(currentTrade.getPositiveReputationMultiplier() + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.01F : -0.01F)));
                case "tradePriceDemandModButton" -> currentTrade.setDemandPriceMultiplier(currentTrade.getDemandPriceMultiplier() + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.01F : -0.01F)));
                case "tradeVillagerExperienceButton" -> currentTrade.setVillagerExperience(currentTrade.getVillagerExperience() + ((e.isShiftClick() ? 10 : 1) * (e.isLeftClick() ? 1 : -1)));
                case "tradeModifierButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new DynamicModifierMenu(playerMenuUtility, this).open();
                    return;
                }
                case "tradeReputationNegativeModButton" -> currentTrade.setNegativeReputationMultiplier(currentTrade.getNegativeReputationMultiplier() + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.01F : -0.01F)));
                case "tradePriceDemandMaxButton" -> currentTrade.setDemandPriceMax(currentTrade.getDemandPriceMax() + ((e.isShiftClick() ? 10 : 1) * (e.isLeftClick() ? 1 : -1)));
                case "tradeEnchantingExperienceButton" -> currentTrade.setEnchantingExperience(currentTrade.getEnchantingExperience() + ((e.isShiftClick() ? 10 : 1) * (e.isLeftClick() ? 0.1F : -0.1F)));
                case "tradeFixedUsesButton" -> currentTrade.setFixedUseCount(!currentTrade.hasFixedUseCount());
                case "tradeExclusiveButton" -> currentTrade.setExclusive(!currentTrade.isExclusive());
                case "tradeRestockDelayButton" -> currentTrade.setRestockDelay(Math.max(-1000, currentTrade.getRestockDelay() + ((e.isShiftClick() ? 24 : 1) * (e.isLeftClick() ? 1000 : -1000))));
                case "tradePriceRandomizerButton" -> {
                    if (e.isShiftClick()) currentTrade.setPriceRandomPositiveOffset(Math.max(currentTrade.getPriceRandomNegativeOffset(), currentTrade.getPriceRandomPositiveOffset() + (e.isLeftClick() ? 1 : -1)));
                    else currentTrade.setPriceRandomNegativeOffset(Math.min(currentTrade.getPriceRandomPositiveOffset(), currentTrade.getPriceRandomNegativeOffset() + (e.isLeftClick() ? 1 : -1)));
                }
                case "tradeTradeableButton" -> currentTrade.setTradeable(!currentTrade.isTradeable());
                case "backToMenuButton" -> switchView(View.TRADES);
                case "tradeRefreshesButton" -> currentTrade.setRefreshes(!currentTrade.refreshes());
                case "tradeOrderableButton" -> currentTrade.setMaxOrderCount(currentTrade.getMaxOrderCount() + ((e.isShiftClick() ? 8 : 1) * (e.isLeftClick() ? 1 : -1)));
                case "tradeGiftWeightButton" -> currentTrade.setGiftWeight((currentTrade.getGiftWeight() + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.1F : -0.1F))));
                case "tradeSkillExperienceButton" -> currentTrade.setSkillExp(currentTrade.getSkillExp() + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 1 : -1)));
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
        switch (view){
            case PROFESSIONS -> {
                inventory.setItem(13, professionButton(null));
                inventory.setItem(20, professionButton(Villager.Profession.CARTOGRAPHER));
                inventory.setItem(21, professionButton(Villager.Profession.LIBRARIAN));
                inventory.setItem(22, professionButton(Villager.Profession.CLERIC));
                inventory.setItem(24, professionButton(Villager.Profession.MASON));
                inventory.setItem(28, professionButton(Villager.Profession.NONE));
                inventory.setItem(29, professionButton(Villager.Profession.NITWIT));
                inventory.setItem(31, professionButton(Villager.Profession.FLETCHER));
                inventory.setItem(32, professionButton(Villager.Profession.ARMORER));
                inventory.setItem(33, professionButton(Villager.Profession.TOOLSMITH));
                inventory.setItem(34, professionButton(Villager.Profession.WEAPONSMITH));
                inventory.setItem(38, professionButton(Villager.Profession.LEATHERWORKER));
                inventory.setItem(39, professionButton(Villager.Profession.SHEPHERD));
                inventory.setItem(40, professionButton(Villager.Profession.FARMER));
                inventory.setItem(41, professionButton(Villager.Profession.FISHERMAN));
                inventory.setItem(42, professionButton(Villager.Profession.BUTCHER));
            }
            case SUBTYPES -> {
                if (currentProfession != null || travelingMerchant){
                    List<MerchantType> types = new ArrayList<>((travelingMerchant ? CustomMerchantManager.getTravelingMerchantConfiguration() : CustomMerchantManager.getMerchantConfiguration(currentProfession)).getMerchantTypes().stream().map(CustomMerchantManager::getMerchantType).filter(Objects::nonNull).toList());
                    types.sort(Comparator.comparing(MerchantType::getType));
                    List<ItemStack> buttons = new ArrayList<>();

                    double totalWeight = types.stream().map(MerchantType::getWeight).mapToDouble(d -> d).sum();
                    for (MerchantType type : types){
                        ItemStack icon = new ItemBuilder(Material.PAPER)
                                .name("&e" + (type.getName() == null ? type.getType() : TranslationManager.translatePlaceholders(type.getName())) + " &7v" + type.getVersion())
                                .lore(
                                        String.format("&7Chance of occurrence: &e%.1f%% &7(&e%.1f&7)", Math.max(0, Math.min(100, (type.getWeight() / totalWeight) * 100)), type.getWeight()),
                                        "&7Trades:",
                                        "    &7Novice:        &e" + type.getTrades().get(MerchantLevel.NOVICE).getTrades().size(),
                                        "    &7Apprentice:   &b" + type.getTrades().get(MerchantLevel.APPRENTICE).getTrades().size(),
                                        "    &7Journeyman: &a" + type.getTrades().get(MerchantLevel.JOURNEYMAN).getTrades().size(),
                                        "    &7Expert:        &c" + type.getTrades().get(MerchantLevel.EXPERT).getTrades().size(),
                                        "    &7Master:        &d" + type.getTrades().get(MerchantLevel.MASTER).getTrades().size(),
                                        "",
                                        "&6Click to edit",
                                        "&cShift-Right-Click to delete")
                                .stringTag(KEY_SUBTYPE, type.getType())
                                .get();
                        buttons.add(icon);
                    }
                    if (types.isEmpty()){
                        ItemStack icon = new ItemBuilder(Material.BARRIER)
                                .name("&cNo subtypes registered")
                                .lore("&7No custom trades assigned to", "&7this profession type")
                                .get();
                        buttons.add(icon);
                    }
                    buttons.add(Buttons.createNewButton);

                    Map<Integer, List<ItemStack>> pages = Utils.paginate(45, buttons);
                    subTypePage = Math.max(0, Math.min(pages.size() - 1, subTypePage));
                    for (ItemStack i : pages.get(subTypePage)){
                        inventory.addItem(i);
                    }

                    if (subTypePage < pages.size() - 1) inventory.setItem(53, Buttons.pageForwardButton);
                    if (subTypePage > 0) inventory.setItem(45, Buttons.pageBackButton);
                }
                inventory.setItem(49, Buttons.backToMenuButton);
            }
            case SUBTYPE -> {
                if (currentSubType != null){
                    List<MerchantType> types = new ArrayList<>((travelingMerchant ? CustomMerchantManager.getTravelingMerchantConfiguration() : CustomMerchantManager.getMerchantConfiguration(currentProfession)).getMerchantTypes().stream().map(CustomMerchantManager::getMerchantType).filter(Objects::nonNull).toList());
                    double totalWeight = types.stream().map(MerchantType::getWeight).mapToDouble(d -> d).sum();
                    inventory.setItem(0, new ItemBuilder(Buttons.subtypeNameButton).name(currentSubType.getName() == null ? "&7No name" : currentSubType.getName()).get());
                    inventory.setItem(8, new ItemBuilder(Buttons.subtypeVersionButton).name("&fVersion: " + currentSubType.getVersion()).get());
                    inventory.setItem(11, new ItemBuilder(Buttons.subtypeRealisticButton).name("&fDaily Resets: " + (currentSubType.resetsTradesDaily() ? "Yes" : "No")).get());
                    inventory.setItem(13, new ItemBuilder(Buttons.subtypeProfessionLossButton).name("&fPermanent Profession: " + (currentSubType.canLoseProfession() ? "No" : "Yes")).get());
                    inventory.setItem(15, new ItemBuilder(Buttons.subtypePerPlayerStockButton).name("&fPer Player Stock: " + (currentSubType.isPerPlayerStock() ? "Yes" : "No")).get());
                    List<String> validServices = new ArrayList<>();
                    for (String service : currentSubType.getServices())
                        if (ServiceRegistry.getService(service) != null) validServices.add(service);
                    inventory.setItem(29, new ItemBuilder(Buttons.subtypeServicesButton).prependLore("&eCurrently has " + validServices.size() + " services", "").get());
                    inventory.setItem(31, new ItemBuilder(Buttons.subtypeWeightButton)
                            .name("&fWeight: " + currentSubType.getWeight())
                            .prependLore(
                                    String.format("&7Chance of occurrence: &e%.1f%% &7(&e%.1f&7)", Math.max(0, Math.min(100, (currentSubType.getWeight() / totalWeight) * 100)), currentSubType.getWeight())
                            ).get());
                    inventory.setItem(33, new ItemBuilder(Buttons.subtypeTradesButton)
                            .prependLore(
                                    "&7Trades:",
                                    "    &7Novice:        &e" + currentSubType.getTrades().get(MerchantLevel.NOVICE).getTrades().size(),
                                    "    &7Apprentice:   &b" + currentSubType.getTrades().get(MerchantLevel.APPRENTICE).getTrades().size(),
                                    "    &7Journeyman: &a" + currentSubType.getTrades().get(MerchantLevel.JOURNEYMAN).getTrades().size(),
                                    "    &7Expert:        &c" + currentSubType.getTrades().get(MerchantLevel.EXPERT).getTrades().size(),
                                    "    &7Master:        &d" + currentSubType.getTrades().get(MerchantLevel.MASTER).getTrades().size()
                            ).get());
                }
                inventory.setItem(49, Buttons.backToMenuButton);
            }
            case TRADES -> {
                if (currentSubType != null){
                    List<ItemStack> noviceTradesButtons = new ArrayList<>();
                    List<ItemStack> apprenticeTradesButtons = new ArrayList<>();
                    List<ItemStack> journeymanTradesButtons = new ArrayList<>();
                    List<ItemStack> expertTradesButtons = new ArrayList<>();
                    List<ItemStack> masterTradesButtons = new ArrayList<>();

                    List<MerchantTrade> noviceTrades = currentSubType.getTrades(MerchantLevel.NOVICE).getTrades().stream().map(CustomMerchantManager::getTrade).filter(Objects::nonNull).toList();
                    List<MerchantTrade> apprenticeTrades = currentSubType.getTrades(MerchantLevel.APPRENTICE).getTrades().stream().map(CustomMerchantManager::getTrade).filter(Objects::nonNull).toList();
                    List<MerchantTrade> journeymanTrades = currentSubType.getTrades(MerchantLevel.JOURNEYMAN).getTrades().stream().map(CustomMerchantManager::getTrade).filter(Objects::nonNull).toList();
                    List<MerchantTrade> expertTrades = currentSubType.getTrades(MerchantLevel.EXPERT).getTrades().stream().map(CustomMerchantManager::getTrade).filter(Objects::nonNull).toList();
                    List<MerchantTrade> masterTrades = currentSubType.getTrades(MerchantLevel.MASTER).getTrades().stream().map(CustomMerchantManager::getTrade).filter(Objects::nonNull).toList();

                    for (MerchantTrade trade : noviceTrades) noviceTradesButtons.add(fromTrade(trade));
                    for (MerchantTrade trade : apprenticeTrades) apprenticeTradesButtons.add(fromTrade(trade));
                    for (MerchantTrade trade : journeymanTrades) journeymanTradesButtons.add(fromTrade(trade));
                    for (MerchantTrade trade : expertTrades) expertTradesButtons.add(fromTrade(trade));
                    for (MerchantTrade trade : masterTrades) masterTradesButtons.add(fromTrade(trade));
                    noviceTradesButtons.add(Buttons.createNewButton);
                    apprenticeTradesButtons.add(Buttons.createNewButton);
                    journeymanTradesButtons.add(Buttons.createNewButton);
                    expertTradesButtons.add(Buttons.createNewButton);
                    masterTradesButtons.add(Buttons.createNewButton);

                    Map<Integer, List<ItemStack>> novicePages = Utils.paginate(4, noviceTradesButtons);
                    Map<Integer, List<ItemStack>> apprenticePages = Utils.paginate(4, apprenticeTradesButtons);
                    Map<Integer, List<ItemStack>> journeymanPages = Utils.paginate(4, journeymanTradesButtons);
                    Map<Integer, List<ItemStack>> expertPages = Utils.paginate(4, expertTradesButtons);
                    Map<Integer, List<ItemStack>> masterPages = Utils.paginate(4, masterTradesButtons);

                    tradesNovicePage = Math.max(0, Math.min(novicePages.size() - 1, tradesNovicePage));
                    tradesApprenticePage = Math.max(0, Math.min(apprenticePages.size() - 1, tradesApprenticePage));
                    tradesJourneymanPage = Math.max(0, Math.min(journeymanPages.size() - 1, tradesJourneymanPage));
                    tradesExpertPage = Math.max(0, Math.min(expertPages.size() - 1, tradesExpertPage));
                    tradesMasterPage = Math.max(0, Math.min(masterPages.size() - 1, tradesMasterPage));

                    inventory.setItem(0, new ItemBuilder(Buttons.tradesNoviceRollsButton).prependLore(String.format("&7Currently: &e%.2f", currentSubType.getRolls(MerchantLevel.NOVICE)), "").get());
                    inventory.setItem(1, new ItemBuilder(Buttons.tradesNoviceRollQualityButton).prependLore(String.format("&7Currently: &e%.2f", currentSubType.getRollQuality(MerchantLevel.NOVICE)), "").get());
                    inventory.setItem(2, new ItemBuilder(Buttons.tradesNoviceExpRequirementButton).get());
                    inventory.setItem(3, Buttons.pageBackButton);
                    for (int i = 0; i < novicePages.get(tradesNovicePage).size(); i++) inventory.setItem(noviceTradeIndexes.get(i), novicePages.get(tradesNovicePage).get(i));
                    inventory.setItem(8, Buttons.pageForwardButton);
                    inventory.setItem(9, new ItemBuilder(Buttons.tradesApprenticeRollsButton).prependLore(String.format("&7Currently: &e%.2f", currentSubType.getRolls(MerchantLevel.APPRENTICE)), "").get());
                    inventory.setItem(10, new ItemBuilder(Buttons.tradesApprenticeRollQualityButton).prependLore(String.format("&7Currently: &e%.2f", currentSubType.getRollQuality(MerchantLevel.APPRENTICE)), "").get());
                    inventory.setItem(11, new ItemBuilder(Buttons.tradesApprenticeExpRequirementButton).prependLore(String.format("&7Currently: &e%d &8(total &6%d&8)", currentSubType.getTrades(MerchantLevel.APPRENTICE).getExpRequirement(), currentSubType.getExpRequirement(MerchantLevel.APPRENTICE))).get());
                    inventory.setItem(12, Buttons.pageBackButton);
                    for (int i = 0; i < apprenticePages.get(tradesApprenticePage).size(); i++) inventory.setItem(apprenticeTradeIndexes.get(i), apprenticePages.get(tradesApprenticePage).get(i));
                    inventory.setItem(17, Buttons.pageForwardButton);
                    inventory.setItem(18, new ItemBuilder(Buttons.tradesJourneymanRollsButton).prependLore(String.format("&7Currently: &e%.2f", currentSubType.getRolls(MerchantLevel.JOURNEYMAN)), "").get());
                    inventory.setItem(19, new ItemBuilder(Buttons.tradesJourneymanRollQualityButton).prependLore(String.format("&7Currently: &e%.2f", currentSubType.getRollQuality(MerchantLevel.JOURNEYMAN)), "").get());
                    inventory.setItem(20, new ItemBuilder(Buttons.tradesJourneymanExpRequirementButton).prependLore(String.format("&7Currently: &e%d &8(total &6%d&8)", currentSubType.getTrades(MerchantLevel.JOURNEYMAN).getExpRequirement(), currentSubType.getExpRequirement(MerchantLevel.JOURNEYMAN))).get());
                    inventory.setItem(21, Buttons.pageBackButton);
                    for (int i = 0; i < journeymanPages.get(tradesJourneymanPage).size(); i++) inventory.setItem(journeymanTradeIndexes.get(i), journeymanPages.get(tradesJourneymanPage).get(i));
                    inventory.setItem(26, Buttons.pageForwardButton);
                    inventory.setItem(27, new ItemBuilder(Buttons.tradesExpertRollsButton).prependLore(String.format("&7Currently: &e%.2f", currentSubType.getRolls(MerchantLevel.EXPERT)), "").get());
                    inventory.setItem(28, new ItemBuilder(Buttons.tradesExpertRollQualityButton).prependLore(String.format("&7Currently: &e%.2f", currentSubType.getRollQuality(MerchantLevel.EXPERT)), "").get());
                    inventory.setItem(29, new ItemBuilder(Buttons.tradesExpertExpRequirementButton).prependLore(String.format("&7Currently: &e%d &8(total &6%d&8)", currentSubType.getTrades(MerchantLevel.EXPERT).getExpRequirement(), currentSubType.getExpRequirement(MerchantLevel.EXPERT))).get());
                    inventory.setItem(30, Buttons.pageBackButton);
                    for (int i = 0; i < expertPages.get(tradesExpertPage).size(); i++) inventory.setItem(expertTradeIndexes.get(i), expertPages.get(tradesExpertPage).get(i));
                    inventory.setItem(35, Buttons.pageForwardButton);
                    inventory.setItem(36, new ItemBuilder(Buttons.tradesMasterRollsButton).prependLore(String.format("&7Currently: &e%.2f", currentSubType.getRolls(MerchantLevel.MASTER)), "").get());
                    inventory.setItem(37, new ItemBuilder(Buttons.tradesMasterRollQualityButton).prependLore(String.format("&7Currently: &e%.2f", currentSubType.getRollQuality(MerchantLevel.MASTER)), "").get());
                    inventory.setItem(38, new ItemBuilder(Buttons.tradesMasterExpRequirementButton).prependLore(String.format("&7Currently: &e%d &8(total &6%d&8)", currentSubType.getTrades(MerchantLevel.MASTER).getExpRequirement(), currentSubType.getExpRequirement(MerchantLevel.MASTER))).get());
                    inventory.setItem(39, Buttons.pageBackButton);
                    for (int i = 0; i < masterPages.get(tradesMasterPage).size(); i++) inventory.setItem(masterTradeIndexes.get(i), masterPages.get(tradesMasterPage).get(i));
                    inventory.setItem(44, Buttons.pageForwardButton);
                }
                inventory.setItem(49, Buttons.backToMenuButton);
            }
            case TRADE -> {
                if (currentTrade != null){
                    List<String> predicateLore = new ArrayList<>();
                    currentTrade.getPredicates().forEach(p -> predicateLore.addAll(StringUtils.separateStringIntoLines("&d> " + p.getActiveDescription(), 40)));
                    inventory.setItem(0, new ItemBuilder(Buttons.tradePredicatesButton).placeholderLore("%predicates%", predicateLore).get());
                    inventory.setItem(1, new ItemBuilder(Buttons.tradePredicateModeButton).prependLore("&e" + currentTrade.getPredicateSelection()).get());
                    String weight = String.format("&8%.1f + %.2f/demand, up to %d, +%.1f/luck", currentTrade.getWeight(), currentTrade.getDemandWeightModifier(), currentTrade.getDemandWeightMaxQuantity(), currentTrade.getWeightQuality());
                    inventory.setItem(2, new ItemBuilder(Buttons.tradeWeightButton).prependLore(String.format("&7Currently &e%.1f", currentTrade.getWeight()), weight).get());
                    inventory.setItem(3, new ItemBuilder(Buttons.tradeWeightQualityButton).prependLore(String.format("&7Currently &e%.1f", currentTrade.getWeightQuality()), weight).get());

                    if (currentTrade.isTradeable()) {
                        if (currentSubType.resetsTradesDaily()){
                            inventory.setItem(4, new ItemBuilder(Buttons.tradeWeightDemandModButton).prependLore(String.format("&7Currently &e%.2f", currentTrade.getDemandWeightModifier()), weight).get());
                            inventory.setItem(5, new ItemBuilder(Buttons.tradeWeightDemandMaxButton).prependLore(String.format("&7Currently &e%d", currentTrade.getDemandWeightMaxQuantity()), weight).get());
                        } else {
                            inventory.setItem(5, new ItemBuilder(Buttons.tradeRefreshesButton).prependLore("&7Currently &e" + (currentTrade.refreshes() ? "On" : "Off")).get());
                        }

                        String uses = String.format("&8%d + %.2f/demand, up to %d", currentTrade.getMaxUses(), currentTrade.getDemandMaxUsesModifier(), currentTrade.getDemandMaxUsesMaxQuantity());
                        inventory.setItem(6, new ItemBuilder(Buttons.tradeUsesButton).prependLore(String.format("&7Currently &e%d", currentTrade.getMaxUses()), uses).get());
                        inventory.setItem(7, new ItemBuilder(Buttons.tradeUsesDemandModButton).prependLore(String.format("&7Currently &e%.2f", currentTrade.getDemandMaxUsesModifier()), uses).get());
                        inventory.setItem(8, new ItemBuilder(Buttons.tradeUsesDemandMaxButton).prependLore(String.format("&7Currently &e%d", currentTrade.getDemandMaxUsesMaxQuantity()), uses).get());
                        inventory.setItem(9, new ItemBuilder(Buttons.tradeReputationPositiveModButton).prependLore(String.format("&7Currently &ex%.1f", currentTrade.getPositiveReputationMultiplier())).get());
                        String price = String.format("&8%d + %.0f%%/demand, up to %s", currentTrade.getScalingCostItem().getAmount(), currentTrade.getDemandPriceMultiplier() * 100, (currentTrade.getDemandPriceMax() > 0 ? "+" : "") + currentTrade.getDemandPriceMax());
                        inventory.setItem(11, new ItemBuilder(Buttons.tradePriceDemandModButton).prependLore(String.format("&7Currently &e%.2f", currentTrade.getDemandPriceMultiplier()), price).get());
                        inventory.setItem(13, new ItemBuilder(Buttons.tradeVillagerExperienceButton).prependLore(String.format("&7Currently &e%d", currentTrade.getVillagerExperience())).get());

                        inventory.setItem(20, currentTrade.getScalingCostItem());
                        inventory.setItem(21, currentTrade.getOptionalCostItem());
                        inventory.setItem(27, new ItemBuilder(Buttons.tradeReputationNegativeModButton).prependLore(String.format("&7Currently &ex%.1f", currentTrade.getNegativeReputationMultiplier())).get());
                        inventory.setItem(29, new ItemBuilder(Buttons.tradePriceDemandMaxButton).prependLore(String.format("&7Currently &e%d", currentTrade.getDemandPriceMax()), price).get());
                        inventory.setItem(31, new ItemBuilder(Buttons.tradeEnchantingExperienceButton).prependLore(String.format("&7Currently &e%.1f", currentTrade.getEnchantingExperience())).get());
                        inventory.setItem(37, new ItemBuilder(Buttons.tradeFixedUsesButton).prependLore("&7Currently &e" + (currentTrade.hasFixedUseCount() ? "On" : "Off")).get());
                        inventory.setItem(35, new ItemBuilder(Buttons.tradeSkillExperienceButton).prependLore(String.format("&7Currently &e%d", (int) currentTrade.getSkillExp())).get());
                        inventory.setItem(41, new ItemBuilder(Buttons.tradeRestockDelayButton).prependLore("&7Currently &e" + (currentTrade.getRestockDelay() < 0 ? "Does not restock" : timeToString(currentTrade.getRestockDelay()))).get());
                        inventory.setItem(39, new ItemBuilder(Buttons.tradePriceRandomizerButton).prependLore(String.format("&7Price is &e%d-%d", currentTrade.getScalingCostItem().getAmount() + currentTrade.getPriceRandomNegativeOffset(), currentTrade.getScalingCostItem().getAmount() + currentTrade.getPriceRandomPositiveOffset())).get());
                    }
                    inventory.setItem(18, new ItemBuilder(Buttons.tradeTradeableButton).prependLore("&7Currently &e" + (currentTrade.isTradeable() ? "On" : "Off")).get());
                    inventory.setItem(23, currentTrade.getResult());
                    List<String> modifierLore = new ArrayList<>();
                    currentTrade.getModifiers().forEach(m -> modifierLore.addAll(StringUtils.separateStringIntoLines("&d> " + m.getActiveDescription(), 40)));
                    inventory.setItem(24, new ItemBuilder(Buttons.tradeModifierButton).placeholderLore("%modifiers%", modifierLore).get());
                    inventory.setItem(43, new ItemBuilder(Buttons.tradeExclusiveButton).prependLore("&7Currently &e" + (currentTrade.isExclusive() ? "On" : "Off")).get());
                    inventory.setItem(26, new ItemBuilder(Buttons.tradeOrderableButton).prependLore(String.format("&7Currently &e%d", currentTrade.getMaxOrderCount())).get());
                    String giftWeight = currentTrade.getGiftWeight() < 0 ? "&cCan only be gifted once per player" : currentTrade.getGiftWeight() == 0 ? "&eCannot be gifted" : "&aMay be gifted indefinitely";
                    inventory.setItem(17, new ItemBuilder(Buttons.tradeGiftWeightButton).prependLore(String.format("&7Currently %s%.1f", currentTrade.getGiftWeight() < 0 ? "&c" : currentTrade.getGiftWeight() == 0 ? "&7" : "&a", Math.abs(currentTrade.getWeight())), giftWeight).get());
                }
                inventory.setItem(49, Buttons.backToMenuButton);
            }
        }
    }

    private String timeToString(long delay){
        if (delay < 24000) return String.format("%d ingame hours", delay/1000);
        else if (delay % 24000 == 0) return String.format("%d ingame days", (int) (delay / 24000D));
        else return String.format("%d ingame days and %d hours", (int) (delay / 24000D), (int) ((delay % 24000) / 1000D));
    }

    private ItemStack fromTrade(MerchantTrade trade){
        return new ItemBuilder(trade.getResult())
                .name("&f" + trade.getID())
                .lore(
                        "&7Weight: " + trade.getWeight() + " (+" + trade.getDemandWeightModifier() + "/demand, up to " + trade.getDemandWeightMaxQuantity() + ")",
                        "",
                        "&7Costs &e" + (trade.getScalingCostItem() == null ? "nothing" : trade.getScalingCostItem().getAmount() + "x " + ItemUtils.getItemName(ItemUtils.getItemMeta(trade.getScalingCostItem()))),
                        trade.getOptionalCostItem() == null ? "&7and has no optional cost" : ("&7and " + trade.getOptionalCostItem().getAmount() + "x " + ItemUtils.getItemName(ItemUtils.getItemMeta(trade.getOptionalCostItem()))),
                        "",
                        "&7Can be traded &e" + trade.getMaxUses() + "&7 times (+" + trade.getDemandMaxUsesModifier() + "/demand, up to " + trade.getDemandMaxUsesMaxQuantity() + ")",
                        "&7Progresses the merchant by " + trade.getVillagerExperience() + " experience"
                ).stringTag(KEY_TRADE, trade.getID())
                .get();
    }

    private void switchView(View view){
        this.view = view;
        playerMenuUtility.getOwner().closeInventory();
        this.open();
    }

    @Override
    public void setPredicates(Collection<LootPredicate> predicates) {
        currentTrade.setPredicates(predicates);
    }

    @Override
    public Collection<LootPredicate> getPredicates() {
        return currentTrade.getPredicates();
    }

    @Override
    public void setResultModifiers(List<DynamicItemModifier> resultModifiers) {
        currentTrade.setModifiers(resultModifiers);
    }

    @Override
    public List<DynamicItemModifier> getResultModifiers() {
        return currentTrade.getModifiers();
    }

    private enum View{
        PROFESSIONS,
        SUBTYPES,
        SUBTYPE,
        TRADES,
        TRADE
    }

    private ItemStack professionButton(Villager.Profession profession){
        if (profession == null) return new ItemBuilder(Buttons.professionTraveling).appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getTravelingMerchantConfiguration().getMerchantTypes().size())).get();
        return switch (profession){
            case NONE -> new ItemBuilder(Buttons.professionNone).appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.NONE).getMerchantTypes().size())).get();
            case NITWIT -> new ItemBuilder(Buttons.professionNitwit).appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.NITWIT).getMerchantTypes().size())).get();
            case ARMORER -> new ItemBuilder(Buttons.professionArmorer).appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.ARMORER).getMerchantTypes().size())).get();
            case TOOLSMITH -> new ItemBuilder(Buttons.professionToolsmith).appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.TOOLSMITH).getMerchantTypes().size())).get();
            case WEAPONSMITH -> new ItemBuilder(Buttons.professionWeaponsmith).appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.WEAPONSMITH).getMerchantTypes().size())).get();
            case FLETCHER -> new ItemBuilder(Buttons.professionFletcher).appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.FLETCHER).getMerchantTypes().size())).get();
            case CARTOGRAPHER -> new ItemBuilder(Buttons.professionCartographer).appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.CARTOGRAPHER).getMerchantTypes().size())).get();
            case LIBRARIAN -> new ItemBuilder(Buttons.professionLibrarian).appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.LIBRARIAN).getMerchantTypes().size())).get();
            case CLERIC -> new ItemBuilder(Buttons.professionCleric).appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.CLERIC).getMerchantTypes().size())).get();
            case MASON -> new ItemBuilder(Buttons.professionMason).appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.MASON).getMerchantTypes().size())).get();
            case LEATHERWORKER -> new ItemBuilder(Buttons.professionLeatherworker).appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.LEATHERWORKER).getMerchantTypes().size())).get();
            case SHEPHERD -> new ItemBuilder(Buttons.professionShepherd).appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.SHEPHERD).getMerchantTypes().size())).get();
            case FARMER -> new ItemBuilder(Buttons.professionFarmer).appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.FARMER).getMerchantTypes().size())).get();
            case FISHERMAN -> new ItemBuilder(Buttons.professionFisherman).appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.FISHERMAN).getMerchantTypes().size())).get();
            case BUTCHER -> new ItemBuilder(Buttons.professionButcher).appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.BUTCHER).getMerchantTypes().size())).get();
        };
    }

    private static class Buttons{
        private static final ItemStack backToMenuButton =
                new ItemBuilder(getButtonData("editor_backtomenu", Material.LIME_DYE))
                        .name("&fBack")
                        .stringTag(KEY_BUTTON, "backToMenuButton")
                        .get();
        private static final ItemStack pageBackButton =
                new ItemBuilder(getButtonData("editor_prevpage", Material.ARROW))
                        .name("&fPrevious Page")
                        .stringTag(KEY_BUTTON, "pageBackButton")
                        .get();
        private static final ItemStack pageForwardButton =
                new ItemBuilder(getButtonData("editor_nextpage", Material.ARROW))
                        .name("&fNext Page")
                        .stringTag(KEY_BUTTON, "pageForwardButton")
                        .get();
        private static final ItemStack createNewButton = new ItemBuilder(getButtonData("editor_newrecipe", Material.LIME_DYE))
                .name("&b&lNew Entry")
                .stringTag(KEY_BUTTON, "createNewButton")
                .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();

        private static final ItemStack professionArmorer =
                new ItemBuilder(getButtonData("editor_trading_profession_armorer", Material.BLAST_FURNACE))
                        .name("&fArmorer")
                        .lore("&7Subtypes assigned to the Armorer profession")
                        .stringTag(KEY_PROFESSION, "ARMORER")
                        .get();
        private static final ItemStack professionButcher =
                new ItemBuilder(getButtonData("editor_trading_profession_butcher", Material.PORKCHOP))
                        .name("&fButcher")
                        .lore("&7Subtypes assigned to the Butcher profession")
                        .stringTag(KEY_PROFESSION, "BUTCHER")
                        .get();
        private static final ItemStack professionCartographer =
                new ItemBuilder(getButtonData("editor_trading_profession_cartographer", Material.MAP))
                        .name("&fCartographer")
                        .lore("&7Subtypes assigned to the Cartographer profession")
                        .stringTag(KEY_PROFESSION, "CARTOGRAPHER")
                        .get();
        private static final ItemStack professionCleric =
                new ItemBuilder(getButtonData("editor_trading_profession_cleric", Material.BREWING_STAND))
                        .name("&fCleric")
                        .lore("&7Subtypes assigned to the Cleric profession")
                        .stringTag(KEY_PROFESSION, "CLERIC")
                        .get();
        private static final ItemStack professionFarmer =
                new ItemBuilder(getButtonData("editor_trading_profession_farmer", Material.WHEAT))
                        .name("&fFarmer")
                        .lore("&7Subtypes assigned to the Farmer profession")
                        .stringTag(KEY_PROFESSION, "FARMER")
                        .get();
        private static final ItemStack professionFisherman =
                new ItemBuilder(getButtonData("editor_trading_profession_fisherman", Material.COD))
                        .name("&fFisherman")
                        .lore("&7Subtypes assigned to the Fisherman profession")
                        .stringTag(KEY_PROFESSION, "FISHERMAN")
                        .get();
        private static final ItemStack professionFletcher =
                new ItemBuilder(getButtonData("editor_trading_profession_fletcher", Material.BOW))
                        .name("&fFletcher")
                        .lore("&7Subtypes assigned to the Fletcher profession")
                        .stringTag(KEY_PROFESSION, "FLETCHER")
                        .get();
        private static final ItemStack professionLeatherworker =
                new ItemBuilder(getButtonData("editor_trading_profession_leatherworker", Material.LEATHER))
                        .name("&fLeatherworker")
                        .lore("&7Subtypes assigned to the Leatherworker profession")
                        .stringTag(KEY_PROFESSION, "LEATHERWORKER")
                        .get();
        private static final ItemStack professionLibrarian =
                new ItemBuilder(getButtonData("editor_trading_profession_librarian", Material.BOOKSHELF))
                        .name("&fLibrarian")
                        .lore("&7Subtypes assigned to the Librarian profession")
                        .stringTag(KEY_PROFESSION, "LIBRARIAN")
                        .get();
        private static final ItemStack professionMason =
                new ItemBuilder(getButtonData("editor_trading_profession_mason", Material.STONECUTTER))
                        .name("&fMason")
                        .lore("&7Subtypes assigned to the Mason profession")
                        .stringTag(KEY_PROFESSION, "MASON")
                        .get();
        private static final ItemStack professionNitwit =
                new ItemBuilder(getButtonData("editor_trading_profession_nitwit", Material.GREEN_WOOL))
                        .name("&fNitwit")
                        .lore("&7Subtypes assigned to the Nitwit profession")
                        .stringTag(KEY_PROFESSION, "NITWIT")
                        .get();
        private static final ItemStack professionShepherd =
                new ItemBuilder(getButtonData("editor_trading_profession_shepherd", Material.SHEARS))
                        .name("&fShepherd")
                        .lore("&7Subtypes assigned to the Shepherd profession")
                        .stringTag(KEY_PROFESSION, "SHEPHERD")
                        .get();
        private static final ItemStack professionToolsmith =
                new ItemBuilder(getButtonData("editor_trading_profession_toolsmith", Material.SMITHING_TABLE))
                        .name("&fToolsmith")
                        .lore("&7Subtypes assigned to the Toolsmith profession")
                        .stringTag(KEY_PROFESSION, "TOOLSMITH")
                        .get();
        private static final ItemStack professionWeaponsmith =
                new ItemBuilder(getButtonData("editor_trading_profession_weaponsmith", Material.GRINDSTONE))
                        .name("&fWeaponsmith")
                        .lore("&7Subtypes assigned to the Weaponsmith profession")
                        .stringTag(KEY_PROFESSION, "WEAPONSMITH")
                        .get();
        private static final ItemStack professionNone =
                new ItemBuilder(getButtonData("editor_trading_profession_none", Material.BROWN_WOOL))
                        .name("&fNone")
                        .lore("&7Subtypes assigned to villagers without profession")
                        .stringTag(KEY_PROFESSION, "NONE")
                        .get();
        private static final ItemStack professionTraveling =
                new ItemBuilder(getButtonData("editor_trading_profession_traveling", Material.BLUE_WOOL))
                        .name("&fTraveling Merchant")
                        .lore("&7Subtypes assigned to traveling merchants")
                        .stringTag(KEY_PROFESSION, "TRAVELING")
                        .get();

        private static final ItemStack subtypeNameButton =
                new ItemBuilder(getButtonData("editor_trading_subtype_name", Material.WRITABLE_BOOK))
                        .lore("&7This is what the trading",
                                "&7inventory title will display",
                                "&7during trading, if the villager",
                                "&7has no display name of their own",
                                "",
                                "&6Click to edit")
                        .stringTag(KEY_BUTTON, "subtypeNameButton")
                        .get();
        private static final ItemStack subtypeServicesButton =
                new ItemBuilder(getButtonData("editor_trading_subtype_services", Material.IRON_PICKAXE))
                        .name("&fServices")
                        .lore("&7The merchant's available services.",
                                "&7If the player only has 1 unlocked",
                                "&7service, it will open automatically.",
                                "&7If the player has several, a different",
                                "&7menu showing all of them will open",
                                "&7of which the player needs to select one",
                                "",
                                "&6Click to edit")
                        .stringTag(KEY_BUTTON, "subtypeServicesButton")
                        .get();
        private static final ItemStack subtypeVersionButton =
                new ItemBuilder(getButtonData("editor_trading_subtype_version", Material.REDSTONE_TORCH))
                        .lore("&7Determines the version of this",
                                "&7merchant type. ",
                                "&7If the version of existing villagers",
                                "&7does not match this version, ",
                                "&7(i.e. when the version number is updated)",
                                "&7their trades are reset",
                                "",
                                "&6Click to add/subtract 1")
                        .stringTag(KEY_BUTTON, "subtypeVersionButton")
                        .get();
        private static final ItemStack subtypeProfessionLossButton =
                new ItemBuilder(getButtonData("editor_trading_subtype_professionloseable", Material.BARRIER))
                        .lore("&7If enabled, villagers of this type",
                                "&7will be able to lose their profession",
                                "&7if their work station becomes unavailable",
                                "&7to them (like in vanilla).",
                                "&7If not, this profession is permanent",
                                "&7when obtained",
                                "",
                                "&6Click to toggle")
                        .stringTag(KEY_BUTTON, "subtypeProfessionLossButton")
                        .get();
        private static final ItemStack subtypeTradesButton =
                new ItemBuilder(getButtonData("editor_trading_subtype_trades", Material.EMERALD))
                        .name("&fTrades")
                        .lore("",
                                "&6Click to edit")
                        .stringTag(KEY_BUTTON, "subtypeTradesButton")
                        .get();
        private static final ItemStack subtypeWeightButton =
                new ItemBuilder(getButtonData("editor_trading_subtype_weight", Material.DIAMOND))
                        .name("&fWeight")
                        .lore("",
                                "&7Determines how rare this villager",
                                "&7subtype is.",
                                "",
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 25")
                        .stringTag(KEY_BUTTON, "subtypeWeightButton")
                        .get();
        private static final ItemStack subtypePerPlayerStockButton =
                new ItemBuilder(getButtonData("editor_trading_subtype_perplayerstock", Material.ENDER_CHEST))
                        .lore("&7If enabled, each villager has a",
                                "&7separate stock of items for each",
                                "&7player. ",
                                "&7If disabled, a villager keeps a single",
                                "&7stock of goods for every player to",
                                "&7have to share.",
                                "",
                                "&6Click to toggle")
                        .stringTag(KEY_BUTTON, "subtypePerPlayerStockButton")
                        .get();
        private static final ItemStack subtypeRealisticButton =
                new ItemBuilder(getButtonData("editor_trading_subtype_realistic", Material.FLINT_AND_STEEL))
                        .lore("&7If enabled, a villager's trade selection",
                                "&7will reset and change daily.",
                                "&7This also features a demand mechanic,",
                                "&7where frequently purchased items also",
                                "&7increase the likelihood for it to",
                                "&7re-occur.",
                                "&7If disabled, their selection of trades",
                                "&7remains the same (vanilla)",
                                "",
                                "&6Click to toggle")
                        .stringTag(KEY_BUTTON, "subtypeRealisticButton")
                        .get();
        private static final ItemStack tradesNoviceRollsButton =
                new ItemBuilder(getButtonData("editor_trading_trades_novice_rolls", Material.COPPER_INGOT))
                        .name("&fRolls (Novice)")
                        .lore("&7Determines how many trades are",
                                "&7selected from this level.",
                                "&7For example, if 6 trades are",
                                "&7possible, with 'rolls' set to 2",
                                "&7there will be 2 of those 6 trades",
                                "&7randomly selected.",
                                "&7Fractions of numbers are averaged",
                                "&7out. (2.3 would be 30% 3 and 70% 2)",
                                "&6Click to add/subtract 0.1",
                                "&6Shift-click to add/subtract 1")
                        .stringTag(KEY_BUTTON, "tradesNoviceRollsButton")
                        .get();
        private static final ItemStack tradesNoviceRollQualityButton =
                new ItemBuilder(getButtonData("editor_trading_trades_novice_rollquality", Material.LAPIS_LAZULI))
                        .name("&fLuck Rolls Modifier (Novice)")
                        .lore("&7Adds/subtracts to the 'rolls'",
                                "&7of this level based on the trading",
                                "&7luck of the player.",
                                "&7With a positive luck modifier,",
                                "&7more trading luck = more possible",
                                "&7trades.",
                                "&6Click to add/subtract 0.01",
                                "&6Shift-click to add/subtract 0.25")
                        .stringTag(KEY_BUTTON, "tradesNoviceRollQualityButton")
                        .get();
        private static final ItemStack tradesNoviceExpRequirementButton =
                new ItemBuilder(getButtonData("editor_trading_trades_novice_exprequirement", Material.EXPERIENCE_BOTTLE))
                        .name("&fEXP Required (Novice)")
                        .lore("&7The amount of exp the villager",
                                "&7needs to progress to this level.",
                                "&cFor novice this is always 0")
                        .stringTag(KEY_BUTTON, "tradesNoviceExpRequirementButton")
                        .get();
        private static final ItemStack tradesApprenticeRollsButton =
                new ItemBuilder(getButtonData("editor_trading_trades_apprentice_rolls", Material.IRON_INGOT))
                        .name("&fRolls (Apprentice)")
                        .lore("&7Determines how many trades are",
                                "&7selected from this level.",
                                "&7For example, if 6 trades are",
                                "&7possible, with 'rolls' set to 2",
                                "&7there will be 2 of those 6 trades",
                                "&7randomly selected.",
                                "&7Fractions of numbers are averaged",
                                "&7out. (2.3 would be 30% 3 and 70% 2)",
                                "&6Click to add/subtract 0.1",
                                "&6Shift-click to add/subtract 1")
                        .stringTag(KEY_BUTTON, "tradesApprenticeRollsButton")
                        .get();
        private static final ItemStack tradesApprenticeRollQualityButton =
                new ItemBuilder(getButtonData("editor_trading_trades_apprentice_rollquality", Material.LAPIS_LAZULI))
                        .name("&fLuck Rolls Modifier (Apprentice)")
                        .lore("&7Adds/subtracts to the 'rolls'",
                                "&7of this level based on the trading",
                                "&7luck of the player.",
                                "&7With a positive luck modifier,",
                                "&7more trading luck = more possible",
                                "&7trades.",
                                "&6Click to add/subtract 0.01",
                                "&6Shift-click to add/subtract 0.25")
                        .stringTag(KEY_BUTTON, "tradesApprenticeRollQualityButton")
                        .get();
        private static final ItemStack tradesApprenticeExpRequirementButton =
                new ItemBuilder(getButtonData("editor_trading_trades_apprentice_exprequirement", Material.EXPERIENCE_BOTTLE))
                        .name("&fEXP Required (Apprentice)")
                        .lore("&7The amount of exp the villager",
                                "&7needs to progress to this level.",
                                "&6Click to add/subtract 1",
                                "&6Shift-click to add/subtract 10")
                        .stringTag(KEY_BUTTON, "tradesApprenticeExpRequirementButton")
                        .get();
        private static final ItemStack tradesJourneymanRollsButton =
                new ItemBuilder(getButtonData("editor_trading_trades_journeyman_rolls", Material.GOLD_INGOT))
                        .name("&fRolls (Journeyman)")
                        .lore("&7Determines how many trades are",
                                "&7selected from this level.",
                                "&7For example, if 6 trades are",
                                "&7possible, with 'rolls' set to 2",
                                "&7there will be 2 of those 6 trades",
                                "&7randomly selected.",
                                "&7Fractions of numbers are averaged",
                                "&7out. (2.3 would be 30% 3 and 70% 2)",
                                "&6Click to add/subtract 0.1",
                                "&6Shift-click to add/subtract 1")
                        .stringTag(KEY_BUTTON, "tradesJourneymanRollsButton")
                        .get();
        private static final ItemStack tradesJourneymanRollQualityButton =
                new ItemBuilder(getButtonData("editor_trading_trades_journeyman_rollquality", Material.LAPIS_LAZULI))
                        .name("&fLuck Rolls Modifier (Journeyman)")
                        .lore("&7Adds/subtracts to the 'rolls'",
                                "&7of this level based on the trading",
                                "&7luck of the player.",
                                "&7With a positive luck modifier,",
                                "&7more trading luck = more possible",
                                "&7trades.",
                                "&6Click to add/subtract 0.01",
                                "&6Shift-click to add/subtract 0.25")
                        .stringTag(KEY_BUTTON, "tradesJourneymanRollQualityButton")
                        .get();
        private static final ItemStack tradesJourneymanExpRequirementButton =
                new ItemBuilder(getButtonData("editor_trading_trades_journeyman_exprequirement", Material.EXPERIENCE_BOTTLE))
                        .name("&fEXP Required (Journeyman)")
                        .lore("&7The amount of exp the villager",
                                "&7needs to progress to this level.",
                                "&6Click to add/subtract 1",
                                "&6Shift-click to add/subtract 10")
                        .stringTag(KEY_BUTTON, "tradesJourneymanExpRequirementButton")
                        .get();
        private static final ItemStack tradesExpertRollsButton =
                new ItemBuilder(getButtonData("editor_trading_trades_expert_rolls", Material.DIAMOND))
                        .name("&fRolls (Expert)")
                        .lore("&7Determines how many trades are",
                                "&7selected from this level.",
                                "&7For example, if 6 trades are",
                                "&7possible, with 'rolls' set to 2",
                                "&7there will be 2 of those 6 trades",
                                "&7randomly selected.",
                                "&7Fractions of numbers are averaged",
                                "&7out. (2.3 would be 30% 3 and 70% 2)",
                                "&6Click to add/subtract 0.1",
                                "&6Shift-click to add/subtract 1")
                        .stringTag(KEY_BUTTON, "tradesExpertRollsButton")
                        .get();
        private static final ItemStack tradesExpertRollQualityButton =
                new ItemBuilder(getButtonData("editor_trading_trades_expert_rollquality", Material.LAPIS_LAZULI))
                        .name("&fLuck Rolls Modifier (Expert)")
                        .lore("&7Adds/subtracts to the 'rolls'",
                                "&7of this level based on the trading",
                                "&7luck of the player.",
                                "&7With a positive luck modifier,",
                                "&7more trading luck = more possible",
                                "&7trades.",
                                "&6Click to add/subtract 0.01",
                                "&6Shift-click to add/subtract 0.25")
                        .stringTag(KEY_BUTTON, "tradesExpertRollQualityButton")
                        .get();
        private static final ItemStack tradesExpertExpRequirementButton =
                new ItemBuilder(getButtonData("editor_trading_trades_expert_exprequirement", Material.EXPERIENCE_BOTTLE))
                        .name("&fEXP Required (Expert)")
                        .lore("&7The amount of exp the villager",
                                "&7needs to progress to this level.",
                                "&6Click to add/subtract 1",
                                "&6Shift-click to add/subtract 10")
                        .stringTag(KEY_BUTTON, "tradesExpertExpRequirementButton")
                        .get();
        private static final ItemStack tradesMasterRollsButton =
                new ItemBuilder(getButtonData("editor_trading_trades_master_rolls", Material.EMERALD))
                        .name("&fRolls (Master)")
                        .lore("&7Determines how many trades are",
                                "&7selected from this level.",
                                "&7For example, if 6 trades are",
                                "&7possible, with 'rolls' set to 2",
                                "&7there will be 2 of those 6 trades",
                                "&7randomly selected.",
                                "&7Fractions of numbers are averaged",
                                "&7out. (2.3 would be 30% 3 and 70% 2)",
                                "&6Click to add/subtract 0.1",
                                "&6Shift-click to add/subtract 1")
                        .stringTag(KEY_BUTTON, "tradesMasterRollsButton")
                        .get();
        private static final ItemStack tradesMasterRollQualityButton =
                new ItemBuilder(getButtonData("editor_trading_trades_master_rollquality", Material.LAPIS_LAZULI))
                        .name("&fLuck Rolls Modifier (Master)")
                        .lore("&7Adds/subtracts to the 'rolls'",
                                "&7of this level based on the trading",
                                "&7luck of the player.",
                                "&7With a positive luck modifier,",
                                "&7more trading luck = more possible",
                                "&7trades.",
                                "&6Click to add/subtract 0.01",
                                "&6Shift-click to add/subtract 0.25")
                        .stringTag(KEY_BUTTON, "tradesMasterRollQualityButton")
                        .get();
        private static final ItemStack tradesMasterExpRequirementButton =
                new ItemBuilder(getButtonData("editor_trading_trades_master_exprequirement", Material.EXPERIENCE_BOTTLE))
                        .name("&fEXP Required (Master)")
                        .lore("&7The amount of exp the villager",
                                "&7needs to progress to this level.",
                                "&6Click to add/subtract 1",
                                "&6Shift-click to add/subtract 10")
                        .stringTag(KEY_BUTTON, "tradesMasterExpRequirementButton")
                        .get();
        private static final ItemStack tradePredicateModeButton = new ItemBuilder(getButtonData("editor_trading_trade_predicatemode", Material.COMPARATOR))
                .name("&eFilter Mode")
                .stringTag(KEY_BUTTON, "tradePredicateModeButton")
                .lore("",
                        "&7Determines to what extent filters",
                        "&7should pass.",
                        "&7Should all filters pass",
                        "&7or should any filter pass?",
                        "&eClick to toggle")
                .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
        private static final ItemStack tradePredicatesButton = new ItemBuilder(getButtonData("editor_trading_trade_predicates", Material.WRITABLE_BOOK))
                .name("&bFilter")
                .stringTag(KEY_BUTTON, "tradePredicatesButton")
                .lore("&7The filter decides if this trade",
                        "&7can be in the trade list based on",
                        "&7conditions.",
                        "&eClick to open the menu",
                        "&8&m                <>                ",
                        "%predicates%")
                .get();
        private static final ItemStack tradeModifierButton = new ItemBuilder(getButtonData("editor_trading_trade_modifiers", Material.WRITABLE_BOOK))
                .name("&dDynamic Item Modifiers")
                .stringTag(KEY_BUTTON, "tradeModifierButton")
                .lore("",
                        "&7Modifiers are functions to edit",
                        "&7the output item based on player",
                        "&7stats.",
                        "&eClick to open the menu",
                        "&8&m                <>                ")
                .get();
        private static final ItemStack tradeWeightButton =
                new ItemBuilder(getButtonData("editor_trading_trade_weight", Material.IRON_BLOCK))
                        .name("&fWeight")
                        .lore("",
                                "&7Determines how rare the trade is.",
                                "&7Higher weight means higher likelihood",
                                "&7of occurring")
                        .stringTag(KEY_BUTTON, "tradeWeightButton")
                        .get();
        private static final ItemStack tradeWeightQualityButton =
                new ItemBuilder(getButtonData("editor_trading_trade_weightqual", Material.IRON_INGOT))
                        .name("&fWeight Luck Modifier")
                        .lore("",
                                "&7Modifies the weight of this trade",
                                "&7based on the luck of the first",
                                "&7player interacting with the villager")
                        .stringTag(KEY_BUTTON, "tradeWeightQualityButton")
                        .get();
        private static final ItemStack tradeWeightDemandModButton =
                new ItemBuilder(getButtonData("editor_trading_trade_weightdemandmod", Material.IRON_INGOT))
                        .name("&fWeight Demand Modifier")
                        .lore("",
                                "&7Modifies the weight of this trade",
                                "&7based on its demand")
                        .stringTag(KEY_BUTTON, "tradeWeightDemandModButton")
                        .get();
        private static final ItemStack tradeWeightDemandMaxButton =
                new ItemBuilder(getButtonData("editor_trading_trade_weightdemandmax", Material.IRON_INGOT))
                        .name("&fWeight Demand Cap")
                        .lore("",
                                "&7The max amount the demand modifier",
                                "&7is allowed to modify weight by")
                        .stringTag(KEY_BUTTON, "tradeWeightDemandMaxButton")
                        .get();
        private static final ItemStack tradeUsesButton =
                new ItemBuilder(getButtonData("editor_trading_trade_uses", Material.IRON_INGOT))
                        .name("&fTimes Tradeable")
                        .lore("",
                                "&7The base number of times this",
                                "&7trade can be traded before needing",
                                "&7to be restocked")
                        .stringTag(KEY_BUTTON, "tradeUsesButton")
                        .get();
        private static final ItemStack tradeUsesDemandModButton =
                new ItemBuilder(getButtonData("editor_trading_trade_usesdemandmod", Material.IRON_INGOT))
                        .name("&fTimes Tradeable Demand Modifier")
                        .lore("",
                                "&7Modifies the amount of times",
                                "&7this trade may be traded based",
                                "&7on its demand")
                        .stringTag(KEY_BUTTON, "tradeUsesDemandModButton")
                        .get();
        private static final ItemStack tradeUsesDemandMaxButton =
                new ItemBuilder(getButtonData("editor_trading_trade_usesdemandmax", Material.IRON_INGOT))
                        .name("&fTimes Tradeable Demand Cap")
                        .lore("",
                                "&7The max amount the demand modifier",
                                "&7is allowed to increase/decrease",
                                "&7the trade quantity")
                        .stringTag(KEY_BUTTON, "tradeUsesDemandMaxButton")
                        .get();
        private static final ItemStack tradeVillagerExperienceButton =
                new ItemBuilder(getButtonData("editor_trading_trade_villagerexperience", Material.IRON_INGOT))
                        .name("&fVillager Experience")
                        .lore("",
                                "&7The amount of experience the",
                                "&7villager gets each time this trade",
                                "&7is traded. Not to be confused with",
                                "&7the experience orbs the player gets",
                                "&7upon trading")
                        .stringTag(KEY_BUTTON, "tradeVillagerExperienceButton")
                        .get();
        private static final ItemStack tradeEnchantingExperienceButton =
                new ItemBuilder(getButtonData("editor_trading_trade_enchantingexperience", Material.IRON_INGOT))
                        .name("&fEnchanting Experience")
                        .lore("",
                                "&7The amount of enchanting experience",
                                "&7rewarded every time this trade is",
                                "&7made")
                        .stringTag(KEY_BUTTON, "tradeEnchantingExperienceButton")
                        .get();
        private static final ItemStack tradePriceDemandModButton =
                new ItemBuilder(getButtonData("editor_trading_trade_pricedemandmod", Material.IRON_INGOT))
                        .name("&fPrice Demand Modifier")
                        .lore("",
                                "&7Modifies the price of the trade",
                                "&7based on its demand")
                        .stringTag(KEY_BUTTON, "tradePriceDemandModButton")
                        .get();
        private static final ItemStack tradePriceDemandMaxButton =
                new ItemBuilder(getButtonData("editor_trading_trade_pricedemandmax", Material.IRON_INGOT))
                        .name("&fPrice Demand Cap")
                        .lore("",
                                "&7The max amount the demand modifier",
                                "&7is allowed to increase/decrease",
                                "&7the trade price")
                        .stringTag(KEY_BUTTON, "tradePriceDemandMaxButton")
                        .get();
        private static final ItemStack tradeReputationPositiveModButton =
                new ItemBuilder(getButtonData("editor_trading_trade_reputationposmod", Material.IRON_INGOT))
                        .name("&fReputation Multiplier (positive)")
                        .lore("",
                                "&7If the player's reputation is positive,",
                                "&7it is multiplied by the given number",
                                "&7before affecting the pricing",
                                "&8(higher numbers means positive reputation",
                                "&8affects the price more)")
                        .stringTag(KEY_BUTTON, "tradeReputationPositiveModButton")
                        .get();
        private static final ItemStack tradeReputationNegativeModButton =
                new ItemBuilder(getButtonData("editor_trading_trade_reputationnegmod", Material.IRON_INGOT))
                        .name("&fReputation Multiplier (negative)")
                        .lore("",
                                "&7If the player's reputation is negative,",
                                "&7it is multiplied by the given number",
                                "&7before affecting the pricing",
                                "&8(higher numbers means negative reputation",
                                "&8affects the price more)")
                        .stringTag(KEY_BUTTON, "tradeReputationNegativeModButton")
                        .get();
        private static final ItemStack tradeFixedUsesButton =
                new ItemBuilder(getButtonData("editor_trading_trade_fixeduses", Material.IRON_INGOT))
                        .name("&fFixed Uses")
                        .lore("",
                                "&7If enabled, the amount of trades",
                                "&7cannot be influenced by player stats")
                        .stringTag(KEY_BUTTON, "tradeFixedUsesButton")
                        .get();
        private static final ItemStack tradeExclusiveButton =
                new ItemBuilder(getButtonData("editor_trading_trade_exclusive", Material.IRON_INGOT))
                        .name("&fExclusive")
                        .lore("",
                                "&7If enabled, only players who are",
                                "&7specifically granted access to this",
                                "&7trade can see it")
                        .stringTag(KEY_BUTTON, "tradeExclusiveButton")
                        .get();
        private static final ItemStack tradeRestockDelayButton =
                new ItemBuilder(getButtonData("editor_trading_trade_restockdelay", Material.CLOCK))
                        .name("&fRestock Cooldown")
                        .lore("",
                                "&7Determines the duration in which",
                                "&7this trade cannot restock after",
                                "&7restocking.",
                                "&7Keep in mind that when the duration",
                                "&7ends the villager is not immediately",
                                "&7prompted to restock")
                        .stringTag(KEY_BUTTON, "tradeRestockDelayButton")
                        .get();
        private static final ItemStack tradePriceRandomizerButton =
                new ItemBuilder(getButtonData("editor_trading_trade_pricerandomizer", Material.DIAMOND))
                        .name("&fPrice Randomizer")
                        .lore("",
                                "&7Randomizes the base price amount",
                                "&eClick to increase/decrease the minimum bound",
                                "&eShift-click to do so with the maximum bound")
                        .stringTag(KEY_BUTTON, "tradePriceRandomizerButton")
                        .get();
        private static final ItemStack tradeGiftWeightButton =
                new ItemBuilder(getButtonData("editor_trading_trade_giftable", Material.DIAMOND))
                        .name("&fGift Rarity")
                        .lore("",
                                "&7Determines how rare the trade should",
                                "&7be in the context of gifting.",
                                "&cIf negative, the value is made positive,",
                                "&cbut the trade may also be gifted just once",
                                "&cper player.",
                                "&7If 0, the trade cannot be gifted",
                                "&eClick to increase/decrease by 0.1",
                                "&eShift-Click to do so by 2.5")
                        .stringTag(KEY_BUTTON, "tradeGiftWeightButton")
                        .get();
        private static final ItemStack tradeRefreshesButton =
                new ItemBuilder(getButtonData("editor_trading_trade_refreshes", Material.DIAMOND))
                        .name("&fDaily Refresh")
                        .lore("",
                                "&7If enabled, this trade refreshes daily.",
                                "&7A refresh means the trade stays the same,",
                                "&7but the output is reprocessed by the",
                                "&7trade modifiers.",
                                "&eClick to toggle")
                        .stringTag(KEY_BUTTON, "tradeRefreshesButton")
                        .get();
        private static final ItemStack tradeSkillExperienceButton =
                new ItemBuilder(getButtonData("editor_trading_trade_skillexperience", Material.DIAMOND))
                        .name("&fSkill Experience")
                        .lore("",
                                "&7The trading skill experience rewarded",
                                "&7every time this trade is traded",
                                "&eClick to increase/decrease by 1",
                                "&eShift-click to increase/decrease by 25")
                        .stringTag(KEY_BUTTON, "tradeSkillExperienceButton")
                        .get();
        private static final ItemStack tradeTradeableButton =
                new ItemBuilder(getButtonData("editor_trading_trade_tradeable", Material.REDSTONE_TORCH))
                        .name("&fTradeable")
                        .lore("",
                                "&7If enabled, this entry can be",
                                "&7obtained through trading.",
                                "&7Can be used to make an item only",
                                "&7obtainable through gifting or ",
                                "&7ordering",
                                "&eClick to toggle")
                        .stringTag(KEY_BUTTON, "tradeTradeableButton")
                        .get();
        private static final ItemStack tradeOrderableButton =
                new ItemBuilder(getButtonData("editor_trading_trade_orderable", Material.FILLED_MAP))
                        .name("&fMax Order Count")
                        .lore("",
                                "&7The max amount of times this",
                                "&7trade may be ordered.",
                                "&7If 0, this trade can't be ordered")
                        .stringTag(KEY_BUTTON, "tradeOrderableButton")
                        .get();
    }
}
