package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.ValhallaMMO;
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
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class ServiceTrainingMenu extends Menu {
    private static final NamespacedKey KEY_METHOD = new NamespacedKey(ValhallaMMO.getInstance(), "training_method");

    private final List<TrainService> services;
    private final MerchantData data;
    private final float happiness;
    private final float reputation;
    private final float renown;
    private final int maxRowSizes;
    private final MerchantLevel level;

    public ServiceTrainingMenu(PlayerMenuUtility playerMenuUtility, List<TrainService> services, MerchantData data) {
        super(playerMenuUtility);
        this.data = data;
        this.services = services;
        this.happiness = data.getVillager() == null ? 0F : HappinessSourceRegistry.getHappiness(playerMenuUtility.getOwner(), data.getVillager());
        this.reputation = data.getPlayerMemory(playerMenuUtility.getOwner().getUniqueId()).getTradingReputation();
        this.renown = data.getPlayerMemory(playerMenuUtility.getOwner().getUniqueId()).getRenownReputation();
        this.level = CustomMerchantManager.getLevel(data);
        if (services.isEmpty()) maxRowSizes = 1;
        else {
            int biggest = -1;
            for (TrainService service : services) if (service.getRows() > biggest) biggest = service.getRows();
            maxRowSizes = biggest;
        }
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? switch (maxRowSizes) {
            case 1 -> "&f\uF808\uF31C";
            case 2 -> "&f\uF808\uF31D";
            case 3 -> "&f\uF808\uF31E";
            case 4 -> "&f\uF808\uF31F";
            case 5 -> "&f\uF808\uF320";
            default -> "&f\uF808\uF321";
        } : "&8Pick your training"); // TODO data driven and change menu texture
    }

    @Override
    public int getSlots() {
        return Math.max(1, maxRowSizes) * 9;
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
            if (!(service instanceof TrainService tc) || level == null || tc.getCost() == null || ItemUtils.isEmpty(tc.getCost().getItem())) return;
            Map<ItemStack, Integer> totalItems = getServiceCost(tc);
            if (totalItems == null) return;
            if (ItemUtils.timesContained(Arrays.asList(playerMenuUtility.getOwner().getInventory().getStorageContents()), totalItems, tc.getCost().getOption()) >= 1){
                ItemUtils.removeItems(playerMenuUtility.getOwner().getInventory(), totalItems, 1, tc.getCost().getOption());
                double expToPurchase = getExpToPurchase(tc);
                Skill skillToLevel = SkillRegistry.getSkill(tc.getSkillToLevel());
                skillToLevel.addEXP(playerMenuUtility.getOwner(), expToPurchase, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.MERCHANT_TRAINING);

                MerchantType type = CustomMerchantManager.getMerchantType(data.getType());
                if (type == null) return;
                int expToGrant = (int) ((1 + AccumulativeStatManager.getCachedStats("TRADING_MERCHANT_EXPERIENCE_MULTIPLIER", playerMenuUtility.getOwner(), 10000, true)));
                data.setExp(Math.min(type.getExpRequirement(MerchantLevel.MASTER), data.getExp() + expToGrant));
            } else Utils.sendMessage(e.getWhoClicked(), TranslationManager.getTranslation("service_training_cant_afford"));

            setMenuItems();
            return;
        }

        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    private double getExpToPurchase(TrainService tc){
        Skill skill = SkillRegistry.getSkill(tc.getSkillToLevel());
        if (skill == null) return -1;
        Profile profile = ProfileCache.getOrCache(playerMenuUtility.getOwner(), skill.getProfileType());
        if (profile == null) return -1;
        if (profile.getLevel() >= tc.getLimitPerLevel().getOrDefault(level, 0)) return -1;
        return Math.max(0, skill.expForLevel(profile.getLevel() + 1) - profile.getEXP());
    }

    private Map<ItemStack, Integer> getServiceCost(TrainService tc){
        Map<ItemStack, Integer> totalItems = new HashMap<>();
        double expToPurchase = getExpToPurchase(tc);
        if (expToPurchase <= 0) return null;
        int costCount = Math.max(1, (int) Math.round(expToPurchase / tc.getExpStep()));

        double relationshipCostMultiplier = Math.max(0, 1 - (CustomMerchantManager.getDiscountFormula() == null ? 0 : Utils.eval(CustomMerchantManager.getDiscountFormula()
                .replace("%happiness%", String.valueOf(happiness))
                .replace("%renown%", String.valueOf(renown))
                .replace("%reputation%", String.valueOf(reputation))
        )));
        int finalItemQuantityPrice = (int) Math.max(1, costCount * tc.getCost().getItem().getAmount() * relationshipCostMultiplier);

        ItemStack cost1 = tc.getCost().getItem().clone();
        cost1.setAmount(1);
        totalItems.put(cost1, finalItemQuantityPrice + totalItems.getOrDefault(cost1, 0));

        return totalItems;
    }

    @Override
    public void setMenuItems() {
        inventory.clear();
        MerchantLevel level = CustomMerchantManager.getLevel(data);
        if (level == null) return;
        for (TrainService service : services){
            if (service.getPrimaryButtonPosition() < 0 || service.getPrimaryButtonPosition() >= getSlots()) continue;
            Skill skill = SkillRegistry.getSkill(service.getSkillToLevel());
            if (skill == null) continue;
            Profile profile = ProfileCache.getOrCache(playerMenuUtility.getOwner(), skill.getProfileType());
            Map<ItemStack, Integer> cost = getServiceCost(service);
            if (cost == null || cost.isEmpty()) return;
            Optional<ItemStack> item = cost.keySet().stream().findAny();
            String costString = TranslationManager.translatePlaceholders(SlotEntry.toString(service.getCost()));
            int quantity = Math.max(0, cost.get(item.get()));

            ItemStack button = new ItemBuilder(service.getPrimaryButton().clone())
                    .placeholderLore("%skill%", skill.getDisplayName())
                    .placeholderLore("%maxlevel%", String.valueOf(service.getLimitPerLevel().getOrDefault(level, 0)))
                    .placeholderLore("%levelcurrent%", String.valueOf(profile.getLevel()))
                    .placeholderLore("%levelnext%", profile.getLevel() >= skill.getMaxLevel() ? TranslationManager.getTranslation("max_level") : String.valueOf(profile.getLevel() + 1))
                    .placeholderLore("%costamount%", String.valueOf(quantity))
                    .placeholderLore("%costdescription%", costString).translate().get();

            ItemStack blankServiceButton = new ItemBuilder(button).type(Material.LIME_DYE).data(9199200).stringTag(KEY_METHOD, service.getID()).get();
            for (int secondaryIndex : service.getSecondaryButtonPositions()) {
                if (secondaryIndex < 0 || secondaryIndex >= getSlots()) continue;
                inventory.setItem(secondaryIndex, blankServiceButton);
            }
            inventory.setItem(service.getPrimaryButtonPosition(), button);
        }
    }
}
