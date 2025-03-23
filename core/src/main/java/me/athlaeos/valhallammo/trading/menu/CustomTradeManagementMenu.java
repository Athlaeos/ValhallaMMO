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

    private View view = View.PROFESSIONS;
    private int page = 0;
    private Villager.Profession currentProfession = null;
    private MerchantType currentSubType = null;
    private MerchantTrade currentTrade = null;

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
                                        "    Master:     &d" + type.getTrades().get(MerchantLevel.MASTER).getTrades().size())
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
                    page = Math.max(0, Math.min(pages.size() - 1, page));
                    for (ItemStack i : pages.get(page)){
                        inventory.addItem(i);
                    }

                    if (page < pages.size() - 1) inventory.setItem(53, Buttons.pageForwardButton);
                    if (page > 0) inventory.setItem(45, Buttons.pageBackButton);
                }
                inventory.setItem(49, Buttons.backToMenuButton);
            }
            case SUBTYPE -> {
                if (currentSubType != null){
                    inventory.setItem(0, new ItemBuilder(Buttons.subtypeNameButton).name(currentSubType.getName() == null ? "&7No name" : currentSubType.getName()).get());
                    inventory.setItem(8, new ItemBuilder(Buttons.subtypeVersionButton).name("&fVersion: " + currentSubType.getVersion()).get());
                }
                inventory.setItem(49, Buttons.backToMenuButton);
            }
        }
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
                new ItemBuilder(Material.BOOK)
                        .name("&fBack")
                        .stringTag(KEY_BUTTON, "backToMenuButton")
                        .get();
        private static final ItemStack pageBackButton =
                new ItemBuilder(Material.ARROW)
                        .name("&fPrevious Page")
                        .stringTag(KEY_BUTTON, "pageBackButton")
                        .get();
        private static final ItemStack pageForwardButton =
                new ItemBuilder(Material.ARROW)
                        .name("&fNext Page")
                        .stringTag(KEY_BUTTON, "pageForwardButton")
                        .get();
        private static final ItemStack createNewButton = new ItemBuilder(getButtonData("editor_trademanager_add", Material.LIME_DYE))
                .name("&b&lNew Entry")
                .stringTag(KEY_BUTTON, "createNewButton")
                .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();

        private static final ItemStack professionArmorer =
                new ItemBuilder(Material.BLAST_FURNACE)
                        .name("&fArmorer")
                        .lore("&7Subtypes assigned to the Armorer profession")
                        .stringTag(KEY_PROFESSION, "ARMORER")
                        .get();
        private static final ItemStack professionButcher =
                new ItemBuilder(Material.PORKCHOP)
                        .name("&fButcher")
                        .lore("&7Subtypes assigned to the Butcher profession")
                        .stringTag(KEY_PROFESSION, "BUTCHER")
                        .get();
        private static final ItemStack professionCartographer =
                new ItemBuilder(Material.MAP)
                        .name("&fCartographer")
                        .lore("&7Subtypes assigned to the Cartographer profession")
                        .stringTag(KEY_PROFESSION, "CARTOGRAPHER")
                        .get();
        private static final ItemStack professionCleric =
                new ItemBuilder(Material.BREWING_STAND)
                        .name("&fCleric")
                        .lore("&7Subtypes assigned to the Cleric profession")
                        .stringTag(KEY_PROFESSION, "CLERIC")
                        .get();
        private static final ItemStack professionFarmer =
                new ItemBuilder(Material.WHEAT)
                        .name("&fFarmer")
                        .lore("&7Subtypes assigned to the Farmer profession")
                        .stringTag(KEY_PROFESSION, "FARMER")
                        .get();
        private static final ItemStack professionFisherman =
                new ItemBuilder(Material.COD)
                        .name("&fFisherman")
                        .lore("&7Subtypes assigned to the Fisherman profession")
                        .stringTag(KEY_PROFESSION, "FISHERMAN")
                        .get();
        private static final ItemStack professionFletcher =
                new ItemBuilder(Material.BOW)
                        .name("&fFletcher")
                        .lore("&7Subtypes assigned to the Fletcher profession")
                        .stringTag(KEY_PROFESSION, "FLETCHER")
                        .get();
        private static final ItemStack professionLeatherworker =
                new ItemBuilder(Material.LEATHER)
                        .name("&fLeatherworker")
                        .lore("&7Subtypes assigned to the Leatherworker profession")
                        .stringTag(KEY_PROFESSION, "LEATHERWORKER")
                        .get();
        private static final ItemStack professionLibrarian =
                new ItemBuilder(Material.BOOKSHELF)
                        .name("&fLibrarian")
                        .lore("&7Subtypes assigned to the Librarian profession")
                        .stringTag(KEY_PROFESSION, "LIBRARIAN")
                        .get();
        private static final ItemStack professionMason =
                new ItemBuilder(Material.STONECUTTER)
                        .name("&fMason")
                        .lore("&7Subtypes assigned to the Mason profession")
                        .stringTag(KEY_PROFESSION, "MASON")
                        .get();
        private static final ItemStack professionNitwit =
                new ItemBuilder(Material.GREEN_WOOL)
                        .name("&fNitwit")
                        .lore("&7Subtypes assigned to the Nitwit profession")
                        .stringTag(KEY_PROFESSION, "NITWIT")
                        .get();
        private static final ItemStack professionShepherd =
                new ItemBuilder(Material.SHEARS)
                        .name("&fShepherd")
                        .lore("&7Subtypes assigned to the Shepherd profession")
                        .stringTag(KEY_PROFESSION, "SHEPHERD")
                        .get();
        private static final ItemStack professionToolsmith =
                new ItemBuilder(Material.SMITHING_TABLE)
                        .name("&fToolsmith")
                        .lore("&7Subtypes assigned to the Toolsmith profession")
                        .stringTag(KEY_PROFESSION, "TOOLSMITH")
                        .get();
        private static final ItemStack professionWeaponsmith =
                new ItemBuilder(Material.GRINDSTONE)
                        .name("&fWeaponsmith")
                        .lore("&7Subtypes assigned to the Weaponsmith profession")
                        .stringTag(KEY_PROFESSION, "WEAPONSMITH")
                        .get();
        private static final ItemStack professionNone =
                new ItemBuilder(Material.BROWN_WOOL)
                        .name("&fNone")
                        .lore("&7Subtypes assigned to villagers without profession")
                        .stringTag(KEY_PROFESSION, "NONE")
                        .get();

        private static final ItemStack subtypeNameButton =
                new ItemBuilder(Material.WRITABLE_BOOK)
                        .lore("&7This is what the trading",
                                "&7inventory title will display",
                                "&7during trading, if the villager",
                                "&7has no display name of their own",
                                "",
                                "&6Click to edit")
                        .stringTag(KEY_BUTTON, "subtypeNameButton")
                        .get();
        private static final ItemStack subtypeVersionButton =
                new ItemBuilder(Material.REDSTONE_TORCH)
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
                new ItemBuilder(Material.BARRIER)
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
                new ItemBuilder(Material.EMERALD)
                        .name("&fTrades")
                        .lore("",
                                "&6Click to edit")
                        .stringTag(KEY_BUTTON, "subtypeTradesButton")
                        .get();
        private static final ItemStack subtypeWeightButton =
                new ItemBuilder(Material.EMERALD)
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
                new ItemBuilder(Material.ENDER_CHEST)
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
                new ItemBuilder(Material.EMERALD)
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
    }
}
