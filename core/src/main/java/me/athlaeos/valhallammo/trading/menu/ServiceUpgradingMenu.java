package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.dom.MerchantLevel;
import me.athlaeos.valhallammo.trading.happiness.HappinessSourceRegistry;
import me.athlaeos.valhallammo.trading.services.Service;
import me.athlaeos.valhallammo.trading.services.ServiceRegistry;
import me.athlaeos.valhallammo.trading.services.service_implementations.UpgradeService;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class ServiceUpgradingMenu extends Menu {
    private static final NamespacedKey KEY_METHOD = new NamespacedKey(ValhallaMMO.getInstance(), "training_method");
    private static final int indexPreviousPage = 45;
    private static final int indexNextPage = 53;
    private static final List<Integer> indexesUpgrades = List.of(46, 47, 48, 49, 50, 51, 52);
    private static final int indexCost = 22;
    private static final int indexInput = 19;
    private static final int indexOutput = 25;

    private final List<UpgradeService> services;
    private final MerchantData data;
    private final float happiness;
    private final float reputation;
    private final float renown;
    private final MerchantLevel level;
    private int page = 0;
    private UpgradeService selectedService = null;
    private ItemStack input = null;

    public ServiceUpgradingMenu(PlayerMenuUtility playerMenuUtility, List<UpgradeService> services, MerchantData data) {
        super(playerMenuUtility);
        this.data = data;
        this.services = services;
        this.happiness = data.getVillager() == null ? 0F : HappinessSourceRegistry.getHappiness(playerMenuUtility.getOwner(), data.getVillager());
        this.reputation = data.getPlayerMemory(playerMenuUtility.getOwner().getUniqueId()).getTradingReputation();
        this.renown = data.getPlayerMemory(playerMenuUtility.getOwner().getUniqueId()).getRenownReputation();
        this.level = CustomMerchantManager.getLevel(data);
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF324" : TranslationManager.getTranslation("service_menu_upgrading"));
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        if (e.getRawSlot() == indexPreviousPage) page--;
        else if (e.getRawSlot() == indexNextPage) page++;
        else if (e.getRawSlot() == indexInput) {
            // putting item in, or removing from input
            ItemUtils.calculateClickEvent(e, 1, indexInput);
            ItemStack input = inventory.getItem(indexInput);
            if (ItemUtils.isEmpty(input)) {
                this.input = null;
                inventory.setItem(indexOutput, null);
            } else this.input = input.clone();
        } else if (e.getRawSlot() == indexOutput) {
            if (selectedService == null || ItemUtils.isEmpty(input)) {
                setMenuItems();
                System.out.println("input null");
                return;
            }
            Map<ItemStack, Integer> cost = new HashMap<>(Map.of(selectedService.getCost().getItem(), getCostQuantity(selectedService)));
            if (ItemUtils.timesContained(Arrays.asList(playerMenuUtility.getOwner().getInventory().getStorageContents()), cost, selectedService.getCost().getOption()) <= 0) {
                // first check if player can afford it. cancel if not
                e.setCancelled(true);
                setMenuItems();
                System.out.println("cant afford");
                Utils.sendMessage(playerMenuUtility.getOwner(), TranslationManager.getTranslation("service_upgrading_cant_afford"));
                return;
            }
            ItemBuilder clicked = ItemUtils.isEmpty(e.getCurrentItem()) ? null : new ItemBuilder(e.getCurrentItem());

            if (clicked == null || CustomFlag.hasFlag(clicked.getMeta(), CustomFlag.UNCRAFTABLE)) {
                e.setCancelled(true);
                System.out.println("invalid modifiers 1");
            } else {
                ItemBuilder testOutput = new ItemBuilder(input);
                DynamicItemModifier.modify(ModifierContext.builder(testOutput)
                        .crafter(playerMenuUtility.getOwner())
                        .setOtherType(data)
                        .entity(data.getVillager())
                        .validate()
                        .get(), selectedService.getModifiers());
                if (CustomFlag.hasFlag(testOutput.getMeta(), CustomFlag.UNCRAFTABLE)) {
                    System.out.println("invalid modifiers 2");
                    e.setCancelled(true);
                } else {
                    ItemBuilder finalOutput = new ItemBuilder(input);
                    DynamicItemModifier.modify(ModifierContext.builder(finalOutput)
                            .crafter(playerMenuUtility.getOwner())
                            .setOtherType(data)
                            .entity(data.getVillager())
                            .validate()
                            .executeUsageMechanics()
                            .get(), selectedService.getModifiers());
                    e.setCurrentItem(finalOutput.get());
                    ItemUtils.calculateClickEvent(e, 1);
                    // check if output is empty after clicking, which would determine if the player could take the item out properly
                    if (ItemUtils.isEmpty(inventory.getItem(indexOutput))) {
                        // item successfully removed, pay up!
                        ItemUtils.removeItems(playerMenuUtility.getOwner().getInventory(), cost, 1, selectedService.getCost().getOption());
                        input = null;
                        inventory.setItem(indexInput, null);
                    } else {
                        input = null;
                    }
                }
            }
        } else if (indexesUpgrades.contains(e.getRawSlot())) {
            ItemBuilder clicked = ItemUtils.isEmpty(e.getCurrentItem()) ? null : new ItemBuilder(e.getCurrentItem());
            if (clicked == null) {
                setMenuItems();
                return;
            }
            String m = ItemUtils.getPDCString(KEY_METHOD, clicked.getMeta(), null);
            if (m != null) {
                e.setCancelled(true);
                Service service = ServiceRegistry.getService(m);
                if (!(service instanceof UpgradeService us) || level == null || us.getCost() == null || ItemUtils.isEmpty(us.getCost().getItem())) {
                    setMenuItems();
                    return;
                }
                selectedService = us;
                setMenuItems();
                return;
            }
        } else {
            System.out.println("something else");
            ItemUtils.calculateClickEvent(e, 1, indexInput);
            ItemStack input = inventory.getItem(indexInput);
            if (ItemUtils.isEmpty(input)) {
                this.input = null;
                inventory.setItem(indexOutput, null);
            } else this.input = input.clone();
        }

        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    private int getCostQuantity(UpgradeService service) {
        double relationshipCostMultiplier = Math.max(0, 1 - (CustomMerchantManager.getDiscountFormula() == null ? 0 : Utils.eval(CustomMerchantManager.getDiscountFormula()
                .replace("%happiness%", String.valueOf(happiness))
                .replace("%renown%", String.valueOf(renown))
                .replace("%reputation%", String.valueOf(reputation))
        )));
        return (int) Math.max(1, service.getCost().getItem().getAmount() * relationshipCostMultiplier);
    }

    private static final ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").get();

    @Override
    public void setMenuItems() {
        inventory.clear();
        if (!ValhallaMMO.getPluginConfig().getBoolean("admin_gui_filler_removal"))
            for (int i = 0; i < 54; i++) inventory.setItem(i, filler);
        MerchantLevel level = CustomMerchantManager.getLevel(data);
        if (level == null) return;
        inventory.setItem(45, previousPageButton);
        inventory.setItem(53, nextPageButton);
        List<ItemStack> buttons = new ArrayList<>();
        for (UpgradeService service : services) {
            String costString = TranslationManager.translatePlaceholders(SlotEntry.toString(service.getCost()));
            String targetString = TranslationManager.translatePlaceholders(SlotEntry.toString(service.getInput()));

            ItemStack button = new ItemBuilder(service.getUpgradeIcon())
                    .translate()
                    .placeholderLore("%target%", targetString)
                    .placeholderLore("%item%", costString)
                    .placeholderLore("%quantity%", String.valueOf(getCostQuantity(service)))
                    .stringTag(KEY_METHOD, service.getID()).get();

            buttons.add(button);
        }

        inventory.setItem(indexInput, input);
        if (selectedService != null) {
            if (!ItemUtils.isEmpty(input) && selectedService.getInput().getOption().matches(selectedService.getInput().getItem(), input)) {
                ItemBuilder output = new ItemBuilder(input);
                DynamicItemModifier.modify(ModifierContext.builder(output)
                        .crafter(playerMenuUtility.getOwner())
                        .setOtherType(data)
                        .entity(data.getVillager())
                        .validate()
                        .get(), selectedService.getModifiers());
                inventory.setItem(indexOutput, output.get());
            }

            ItemStack cost = selectedService.getCost().getItem().clone();
            cost.setAmount(getCostQuantity(selectedService));
            inventory.setItem(indexCost, cost);
        }

        Map<Integer, List<ItemStack>> pages = Utils.paginate(indexesUpgrades.size(), buttons);
        if (page >= pages.size()) page = pages.size() - 1;
        else if (page < 0) page = 0;
        List<ItemStack> page = pages.get(this.page);
        for (int i = 0; i < page.size(); i++) {
            ItemStack item = page.get(i);
            inventory.setItem(indexesUpgrades.get(i), item);
        }
        inventory.setItem(indexPreviousPage, previousPageButton);
        inventory.setItem(indexNextPage, nextPageButton);
    }

    @Override
    public void onClose() {
        if (!ItemUtils.isEmpty(input)) {
            ItemUtils.addItem(playerMenuUtility.getOwner(), input.clone(), false);
            inventory.setItem(indexInput, null);
        }
    }

    private static final ItemStack nextPageButton = new ItemBuilder(getButtonData("editor_nextpage", Material.ARROW))
            .name(TranslationManager.getTranslation("translation_next_page"))
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack previousPageButton = new ItemBuilder(getButtonData("editor_prevpage", Material.ARROW))
            .name(TranslationManager.getTranslation("translation_previous_page"))
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
}
