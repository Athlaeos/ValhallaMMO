package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.dom.MerchantLevel;
import me.athlaeos.valhallammo.trading.dom.MerchantType;
import me.athlaeos.valhallammo.trading.happiness.HappinessSourceRegistry;
import me.athlaeos.valhallammo.trading.services.Service;
import me.athlaeos.valhallammo.trading.services.ServiceRegistry;
import me.athlaeos.valhallammo.trading.services.service_implementations.TrainService;
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
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF321" : "&8Pick your training"); // TODO data driven and change menu texture
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        if (e.getClickedInventory() instanceof PlayerInventory) return;

        ItemBuilder clicked = ItemUtils.isEmpty(e.getCurrentItem()) ? null : new ItemBuilder(e.getCurrentItem());
        if (clicked == null) return;
        String m = ItemUtils.getPDCString(KEY_METHOD, clicked.getMeta(), null);
        if (m != null){
            Service service = ServiceRegistry.getService(m);
            if (!(service instanceof UpgradeService us) || level == null || us.getCost() == null || ItemUtils.isEmpty(us.getCost().getItem())) return;


            setMenuItems();
            return;
        }

        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    private int getCostQuantity(UpgradeService service){
        double relationshipCostMultiplier = Math.max(0, 1 - (CustomMerchantManager.getDiscountFormula() == null ? 0 : Utils.eval(CustomMerchantManager.getDiscountFormula()
                .replace("%happiness%", String.valueOf(happiness))
                .replace("%renown%", String.valueOf(renown))
                .replace("%reputation%", String.valueOf(reputation))
        )));
        return (int) Math.max(1, service.getCost().getItem().getAmount() * relationshipCostMultiplier);
    }

    @Override
    public void setMenuItems() {
        inventory.clear();
        MerchantLevel level = CustomMerchantManager.getLevel(data);
        if (level == null) return;
        inventory.setItem(45, previousPageButton);
        inventory.setItem(53, nextPageButton);
        List<ItemStack> buttons = new ArrayList<>();
        for (UpgradeService service : services){
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

        if (!ItemUtils.isEmpty(input)) {
            inventory.setItem(indexInput, input);
        }
        if (selectedService != null){
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
