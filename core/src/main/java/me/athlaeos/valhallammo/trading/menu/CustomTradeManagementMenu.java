package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantTrade;
import me.athlaeos.valhallammo.trading.dom.MerchantType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CustomTradeManagementMenu extends Menu {
    private static final NamespacedKey KEY_PROFESSION = new NamespacedKey(ValhallaMMO.getInstance(), "button_profession");
    private static final NamespacedKey KEY_BUTTON = new NamespacedKey(ValhallaMMO.getInstance(), "button_functionality");

    private View view = View.PROFESSIONS;
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
                List<MerchantType> types = new ArrayList<>(CustomMerchantManager.getMerchantConfiguration(currentProfession).getMerchantTypes().stream().map(CustomMerchantManager::getMerchantType).filter(Objects::nonNull).toList());
                types.sort(Comparator.comparing(MerchantType::getType));

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
            case NONE -> Buttons.professionNone.appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.NONE).getMerchantTypes().size())).get();
            case NITWIT -> Buttons.professionNitwit.appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.NITWIT).getMerchantTypes().size())).get();
            case ARMORER -> Buttons.professionArmorer.appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.ARMORER).getMerchantTypes().size())).get();
            case TOOLSMITH -> Buttons.professionToolsmith.appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.TOOLSMITH).getMerchantTypes().size())).get();
            case WEAPONSMITH -> Buttons.professionWeaponsmith.appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.WEAPONSMITH).getMerchantTypes().size())).get();
            case FLETCHER -> Buttons.professionFletcher.appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.FLETCHER).getMerchantTypes().size())).get();
            case CARTOGRAPHER -> Buttons.professionCartographer.appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.CARTOGRAPHER).getMerchantTypes().size())).get();
            case LIBRARIAN -> Buttons.professionLibrarian.appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.LIBRARIAN).getMerchantTypes().size())).get();
            case CLERIC -> Buttons.professionCleric.appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.CLERIC).getMerchantTypes().size())).get();
            case MASON -> Buttons.professionMason.appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.MASON).getMerchantTypes().size())).get();
            case LEATHERWORKER -> Buttons.professionLeatherworker.appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.LEATHERWORKER).getMerchantTypes().size())).get();
            case SHEPHERD -> Buttons.professionShepherd.appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.SHEPHERD).getMerchantTypes().size())).get();
            case FARMER -> Buttons.professionFarmer.appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.FARMER).getMerchantTypes().size())).get();
            case FISHERMAN -> Buttons.professionFisherman.appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.FISHERMAN).getMerchantTypes().size())).get();
            case BUTCHER -> Buttons.professionButcher.appendLore(String.format("&7(&e%d subtypes&7)", CustomMerchantManager.getMerchantConfiguration(Villager.Profession.BUTCHER).getMerchantTypes().size())).get();
        };
    }

    private static class Buttons{
        private static final ItemStack backToMenuButton =
                new ItemBuilder(Material.BOOK)
                        .name("&fBack")
                        .stringTag(KEY_BUTTON, "backToMenuButton")
                        .get();

        private static final ItemBuilder professionArmorer =
                new ItemBuilder(Material.BLAST_FURNACE)
                        .name("&fArmorer")
                        .lore("&7Subtypes assigned to the Armorer profession")
                        .stringTag(KEY_PROFESSION, "ARMORER");
        private static final ItemBuilder professionButcher =
                new ItemBuilder(Material.PORKCHOP)
                        .name("&fButcher")
                        .lore("&7Subtypes assigned to the Butcher profession")
                        .stringTag(KEY_PROFESSION, "BUTCHER");
        private static final ItemBuilder professionCartographer =
                new ItemBuilder(Material.MAP)
                        .name("&fCartographer")
                        .lore("&7Subtypes assigned to the Cartographer profession")
                        .stringTag(KEY_PROFESSION, "CARTOGRAPHER");
        private static final ItemBuilder professionCleric =
                new ItemBuilder(Material.BREWING_STAND)
                        .name("&fCleric")
                        .lore("&7Subtypes assigned to the Cleric profession")
                        .stringTag(KEY_PROFESSION, "CLERIC");
        private static final ItemBuilder professionFarmer =
                new ItemBuilder(Material.WHEAT)
                        .name("&fFarmer")
                        .lore("&7Subtypes assigned to the Farmer profession")
                        .stringTag(KEY_PROFESSION, "FARMER");
        private static final ItemBuilder professionFisherman =
                new ItemBuilder(Material.COD)
                        .name("&fFisherman")
                        .lore("&7Subtypes assigned to the Fisherman profession")
                        .stringTag(KEY_PROFESSION, "FISHERMAN");
        private static final ItemBuilder professionFletcher =
                new ItemBuilder(Material.BOW)
                        .name("&fFletcher")
                        .lore("&7Subtypes assigned to the Fletcher profession")
                        .stringTag(KEY_PROFESSION, "FLETCHER");
        private static final ItemBuilder professionLeatherworker =
                new ItemBuilder(Material.LEATHER)
                        .name("&fLeatherworker")
                        .lore("&7Subtypes assigned to the Leatherworker profession")
                        .stringTag(KEY_PROFESSION, "LEATHERWORKER");
        private static final ItemBuilder professionLibrarian =
                new ItemBuilder(Material.BOOKSHELF)
                        .name("&fLibrarian")
                        .lore("&7Subtypes assigned to the Librarian profession")
                        .stringTag(KEY_PROFESSION, "LIBRARIAN");
        private static final ItemBuilder professionMason =
                new ItemBuilder(Material.STONECUTTER)
                        .name("&fMason")
                        .lore("&7Subtypes assigned to the Mason profession")
                        .stringTag(KEY_PROFESSION, "MASON");
        private static final ItemBuilder professionNitwit =
                new ItemBuilder(Material.GREEN_WOOL)
                        .name("&fNitwit")
                        .lore("&7Subtypes assigned to the Nitwit profession")
                        .stringTag(KEY_PROFESSION, "NITWIT");
        private static final ItemBuilder professionShepherd =
                new ItemBuilder(Material.SHEARS)
                        .name("&fShepherd")
                        .lore("&7Subtypes assigned to the Shepherd profession")
                        .stringTag(KEY_PROFESSION, "SHEPHERD");
        private static final ItemBuilder professionToolsmith =
                new ItemBuilder(Material.SMITHING_TABLE)
                        .name("&fToolsmith")
                        .lore("&7Subtypes assigned to the Toolsmith profession")
                        .stringTag(KEY_PROFESSION, "TOOLSMITH");
        private static final ItemBuilder professionWeaponsmith =
                new ItemBuilder(Material.GRINDSTONE)
                        .name("&fWeaponsmith")
                        .lore("&7Subtypes assigned to the Weaponsmith profession")
                        .stringTag(KEY_PROFESSION, "WEAPONSMITH");
        private static final ItemBuilder professionNone =
                new ItemBuilder(Material.BROWN_WOOL)
                        .name("&fNone")
                        .lore("&7Subtypes assigned to villagers without profession")
                        .stringTag(KEY_PROFESSION, "NONE");

    }
}
