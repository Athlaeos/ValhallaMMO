package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantLevel;
import me.athlaeos.valhallammo.trading.dom.MerchantTrade;
import me.athlaeos.valhallammo.trading.dom.MerchantType;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CustomTradeManagementMenu extends Menu {
    private static final NamespacedKey KEY_PROFESSION = new NamespacedKey(ValhallaMMO.getInstance(), "button_profession");
    private static final NamespacedKey KEY_SUBTYPE = new NamespacedKey(ValhallaMMO.getInstance(), "button_subtype");
    private static final NamespacedKey KEY_BUTTON = new NamespacedKey(ValhallaMMO.getInstance(), "button_functionality");

    private static final int[] noviceTradeIndexes = new int[]{4, 5, 6, 7};
    private static final int[] apprenticeTradeIndexes = new int[]{13, 14, 15, 16};
    private static final int[] journeymanTradeIndexes = new int[]{22, 23, 24, 25};
    private static final int[] expertTradeIndexes = new int[]{31, 32, 33, 34};
    private static final int[] masterTradeIndexes = new int[]{40, 41, 42, 43};

    private View view = View.PROFESSIONS;
    private int subTypePage = 0;
    private Villager.Profession currentProfession = null;
    private MerchantType currentSubType = null;
    private MerchantTrade currentTrade = null;

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
        return "&8Manage Trades"; // TODO custom menu
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {

    }

    @Override
    public void handleMenu(InventoryDragEvent e) {

    }

    @Override
    public void setMenuItems() {
        inventory.clear();
        switch (view){
            case PROFESSIONS -> {
                inventory.setItem(11, professionButton(Villager.Profession.CARTOGRAPHER));
                inventory.setItem(12, professionButton(Villager.Profession.LIBRARIAN));
                inventory.setItem(13, professionButton(Villager.Profession.CLERIC));
                inventory.setItem(15, professionButton(Villager.Profession.MASON));
                inventory.setItem(19, professionButton(Villager.Profession.NONE));
                inventory.setItem(20, professionButton(Villager.Profession.NITWIT));
                inventory.setItem(22, professionButton(Villager.Profession.FLETCHER));
                inventory.setItem(23, professionButton(Villager.Profession.ARMORER));
                inventory.setItem(24, professionButton(Villager.Profession.TOOLSMITH));
                inventory.setItem(25, professionButton(Villager.Profession.WEAPONSMITH));
                inventory.setItem(29, professionButton(Villager.Profession.LEATHERWORKER));
                inventory.setItem(30, professionButton(Villager.Profession.SHEPHERD));
                inventory.setItem(31, professionButton(Villager.Profession.FARMER));
                inventory.setItem(32, professionButton(Villager.Profession.FISHERMAN));
                inventory.setItem(33, professionButton(Villager.Profession.BUTCHER));
            }
            case SUBTYPES -> {
                if (currentProfession != null){
                    List<MerchantType> types = new ArrayList<>(CustomMerchantManager.getMerchantConfiguration(currentProfession).getMerchantTypes().stream().map(CustomMerchantManager::getMerchantType).filter(Objects::nonNull).toList());
                    types.sort(Comparator.comparing(MerchantType::getType));
                    List<ItemStack> buttons = new ArrayList<>();

                    double totalWeight = types.stream().map(MerchantType::getWeight).mapToDouble(d -> d).sum();
                    for (MerchantType type : types){
                        ItemStack icon = new ItemBuilder(Material.PAPER)
                                .name("&e" + TranslationManager.translatePlaceholders(type.getName()) + " &7v" + type.getVersion())
                                .lore(
                                        String.format("&7Chance of occurrence: &e%.1f%% &7(&e%.1f&7)", Math.max(0, Math.min(100, (type.getWeight() / totalWeight) * 100)), type.getWeight()),
                                        "&7Trades:",
                                        "    Novice:     &e" + type.getTrades().get(MerchantLevel.NOVICE).getTrades().size(),
                                        "    Apprentice: &b" + type.getTrades().get(MerchantLevel.APPRENTICE).getTrades().size(),
                                        "    Journeyman: &a" + type.getTrades().get(MerchantLevel.JOURNEYMAN).getTrades().size(),
                                        "    Expert:     &c" + type.getTrades().get(MerchantLevel.EXPERT).getTrades().size(),
                                        "    Master:     &d" + type.getTrades().get(MerchantLevel.MASTER).getTrades().size(),
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
                    List<MerchantType> types = new ArrayList<>(CustomMerchantManager.getMerchantConfiguration(currentProfession).getMerchantTypes().stream().map(CustomMerchantManager::getMerchantType).filter(Objects::nonNull).toList());
                    double totalWeight = types.stream().map(MerchantType::getWeight).mapToDouble(d -> d).sum();
                    inventory.setItem(0, new ItemBuilder(Buttons.subtypeNameButton).name(currentSubType.getName() == null ? "&7No name" : currentSubType.getName()).get());
                    inventory.setItem(8, new ItemBuilder(Buttons.subtypeVersionButton).name("&fVersion: " + currentSubType.getVersion()).get());
                    inventory.setItem(11, new ItemBuilder(Buttons.subtypeRealisticButton).name("&fRestocking Resets: " + (currentSubType.isResetTradesOnRestock() ? "Yes" : "No")).get());
                    inventory.setItem(13, new ItemBuilder(Buttons.subtypeProfessionLossButton).name("&fPermanent Profession: " + (currentSubType.canLoseProfession() ? "No" : "Yes")).get());
                    inventory.setItem(15, new ItemBuilder(Buttons.subtypePerPlayerStockButton).name("&fPer Player Stock: " + (currentSubType.isPerPlayerStock() ? "Yes" : "No")).get());
                    inventory.setItem(21, new ItemBuilder(Buttons.subtypeWeightButton)
                            .name("&fWeight: " + currentSubType.getWeight())
                            .prependLore(
                                    String.format("&7Chance of occurrence: &e%.1f%% &7(&e%.1f&7)", Math.max(0, Math.min(100, (currentSubType.getWeight() / totalWeight) * 100)), currentSubType.getWeight())
                            ).get());
                    inventory.setItem(23, new ItemBuilder(Buttons.subtypeTradesButton)
                            .prependLore(
                                    "&7Trades:",
                                    "    Novice:     &e" + currentSubType.getTrades().get(MerchantLevel.NOVICE).getTrades().size(),
                                    "    Apprentice: &b" + currentSubType.getTrades().get(MerchantLevel.APPRENTICE).getTrades().size(),
                                    "    Journeyman: &a" + currentSubType.getTrades().get(MerchantLevel.JOURNEYMAN).getTrades().size(),
                                    "    Expert:     &c" + currentSubType.getTrades().get(MerchantLevel.EXPERT).getTrades().size(),
                                    "    Master:     &d" + currentSubType.getTrades().get(MerchantLevel.MASTER).getTrades().size()
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

                    inventory.setItem(0, new ItemBuilder(Buttons.tradesNoviceRollsButton).prependLore("&7Currently: &e" + currentSubType.getRolls(MerchantLevel.NOVICE), "").get());
                    inventory.setItem(1, new ItemBuilder(Buttons.tradesNoviceRollQualityButton).prependLore("&7Currently: &e" + currentSubType.getRollQuality(MerchantLevel.NOVICE), "").get());
                    inventory.setItem(2, new ItemBuilder(Buttons.tradesNoviceExpRequirementButton).get());
                    inventory.setItem(3, Buttons.pageBackButton);
                    for (int i = 0; i < novicePages.get(tradesNovicePage).size(); i++) inventory.setItem(noviceTradeIndexes[i], novicePages.get(tradesNovicePage).get(i));
                    inventory.setItem(8, Buttons.pageForwardButton);
                    inventory.setItem(9, new ItemBuilder(Buttons.tradesApprenticeRollsButton).prependLore("&7Currently: &e" + currentSubType.getRolls(MerchantLevel.APPRENTICE), "").get());
                    inventory.setItem(10, new ItemBuilder(Buttons.tradesApprenticeRollQualityButton).prependLore("&7Currently: &e" + currentSubType.getRollQuality(MerchantLevel.APPRENTICE), "").get());
                    inventory.setItem(11, new ItemBuilder(Buttons.tradesApprenticeExpRequirementButton).prependLore("&7Currently: &e" + currentSubType.getTrades(MerchantLevel.APPRENTICE).getExpRequirement() + " &8(total &6" + currentSubType.getExpRequirement(MerchantLevel.APPRENTICE) + "&8)").get());
                    inventory.setItem(12, Buttons.pageBackButton);
                    for (int i = 0; i < apprenticePages.get(tradesApprenticePage).size(); i++) inventory.setItem(apprenticeTradeIndexes[i], apprenticePages.get(tradesApprenticePage).get(i));
                    inventory.setItem(17, Buttons.pageForwardButton);
                    inventory.setItem(18, new ItemBuilder(Buttons.tradesJourneymanRollsButton).prependLore("&7Currently: &e" + currentSubType.getRolls(MerchantLevel.JOURNEYMAN), "").get());
                    inventory.setItem(19, new ItemBuilder(Buttons.tradesJourneymanRollQualityButton).prependLore("&7Currently: &e" + currentSubType.getRollQuality(MerchantLevel.JOURNEYMAN), "").get());
                    inventory.setItem(20, new ItemBuilder(Buttons.tradesJourneymanExpRequirementButton).prependLore("&7Currently: &e" + currentSubType.getTrades(MerchantLevel.JOURNEYMAN).getExpRequirement() + " &8(total &6" + currentSubType.getExpRequirement(MerchantLevel.JOURNEYMAN) + "&8)").get());
                    inventory.setItem(21, Buttons.pageBackButton);
                    for (int i = 0; i < journeymanPages.get(tradesJourneymanPage).size(); i++) inventory.setItem(journeymanTradeIndexes[i], journeymanPages.get(tradesJourneymanPage).get(i));
                    inventory.setItem(26, Buttons.pageForwardButton);
                    inventory.setItem(27, new ItemBuilder(Buttons.tradesExpertRollsButton).prependLore("&7Currently: &e" + currentSubType.getRolls(MerchantLevel.EXPERT), "").get());
                    inventory.setItem(28, new ItemBuilder(Buttons.tradesExpertRollQualityButton).prependLore("&7Currently: &e" + currentSubType.getRollQuality(MerchantLevel.EXPERT), "").get());
                    inventory.setItem(29, new ItemBuilder(Buttons.tradesExpertExpRequirementButton).prependLore("&7Currently: &e" + currentSubType.getTrades(MerchantLevel.EXPERT).getExpRequirement() + " &8(total &6" + currentSubType.getExpRequirement(MerchantLevel.EXPERT) + "&8)").get());
                    inventory.setItem(30, Buttons.pageBackButton);
                    for (int i = 0; i < expertPages.get(tradesExpertPage).size(); i++) inventory.setItem(expertTradeIndexes[i], expertPages.get(tradesExpertPage).get(i));
                    inventory.setItem(35, Buttons.pageForwardButton);
                    inventory.setItem(36, new ItemBuilder(Buttons.tradesMasterRollsButton).prependLore("&7Currently: &e" + currentSubType.getRolls(MerchantLevel.MASTER), "").get());
                    inventory.setItem(37, new ItemBuilder(Buttons.tradesMasterRollQualityButton).prependLore("&7Currently: &e" + currentSubType.getRollQuality(MerchantLevel.MASTER), "").get());
                    inventory.setItem(38, new ItemBuilder(Buttons.tradesMasterExpRequirementButton).prependLore("&7Currently: &e" + currentSubType.getTrades(MerchantLevel.MASTER).getExpRequirement() + " &8(total &6" + currentSubType.getExpRequirement(MerchantLevel.MASTER) + "&8)").get());
                    inventory.setItem(39, Buttons.pageBackButton);
                    for (int i = 0; i < masterPages.get(tradesMasterPage).size(); i++) inventory.setItem(masterTradeIndexes[i], masterPages.get(tradesMasterPage).get(i));
                    inventory.setItem(44, Buttons.pageForwardButton);
                }
                inventory.setItem(49, Buttons.backToMenuButton);
            }
            case TRADE -> {
                if (currentTrade != null){

                }
                inventory.setItem(49, Buttons.backToMenuButton);
            }
        }
    }

    private ItemStack fromTrade(MerchantTrade trade){
        return new ItemBuilder(trade.getResult())
                .name(trade.getId())
                .lore(
                        "&7Weight: " + trade.getWeight() + " (+" + trade.getDemandWeightModifier() + "/demand, up to " + trade.getDemandWeightMaxQuantity() + ")",
                        "",
                        "&7Costs &e" + (trade.getScalingCostItem() == null ? "nothing" : trade.getScalingCostItem().getAmount() + "x " + ItemUtils.getItemName(ItemUtils.getItemMeta(trade.getScalingCostItem()))),
                        trade.getOptionalCostItem() == null ? "&7and has no optional cost" : ("&7and " + trade.getOptionalCostItem().getAmount() + "x " + ItemUtils.getItemName(ItemUtils.getItemMeta(trade.getOptionalCostItem()))),
                        "",
                        "&7Can be traded &e" + trade.getMaxUses() + "&7 times (+" + trade.getDemandMaxUsesModifier() + "/demand, up to " + trade.getDemandMaxUsesMaxQuantity() + ")",
                        "&7Progresses the merchant by " + trade.getVillagerExperience() + " experience"
                ).get();
    }

    private void switchView(View view){
        this.view = view;
        playerMenuUtility.getOwner().closeInventory();
        this.open();
    }

    private enum View{
        PROFESSIONS,
        SUBTYPES,
        SUBTYPE,
        TRADES,
        TRADE
    }

    private ItemStack professionButton(Villager.Profession profession){
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
                        .lore("&7If enabled, when a villager restocks ",
                                "&7at their work station, their trade ",
                                "&7selection resets.",
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
                .lore("&7Determines to what extent filters",
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
                        "&8&m                <>                ")
                .get();
        private static final ItemStack tradeModifierButton = new ItemBuilder(getButtonData("editor_trading_trade_modifiers", Material.WRITABLE_BOOK))
                .name("&dDynamic Item Modifiers")
                .stringTag(KEY_BUTTON, "tradeModifierButton")
                .lore("&7Modifiers are functions to edit",
                        "&7the output item based on player",
                        "&7stats.",
                        "&eClick to open the menu",
                        "&8&m                <>                ")
                .get();
        private static final ItemStack tradeWeightButton =
                new ItemBuilder(getButtonData("editor_trading_trade_weight", Material.IRON_BLOCK))
                        .name("&fWeight")
                        .lore("&7Determines how rare the trade is.",
                                "&7Higher weight means higher likelihood",
                                "&7of occurring")
                        .stringTag(KEY_BUTTON, "tradeWeightButton")
                        .get();
        private static final ItemStack tradeWeightQualityButton =
                new ItemBuilder(getButtonData("editor_trading_trade_weightqual", Material.IRON_INGOT))
                        .name("&fWeight Demand Modifier")
                        .lore("&7Modifies the weight of this trade",
                                "&7based on its demand")
                        .stringTag(KEY_BUTTON, "tradeWeightQualityButton")
                        .get();
    }
}
