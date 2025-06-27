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
    private static final List<Integer> indexesCost = List.of(12, 13, 14, 21, 22, 23, 30, 31, 32);
    private static final int indexInput = 19;
    private static final int indexOutput = 25;

    private final List<UpgradeService> services;
    private final MerchantData data;
    private final float happiness;
    private final float reputation;
    private final float renown;
    private final MerchantLevel level;
    private int page = 0;

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
            if (!(service instanceof TrainService tc) || level == null || tc.getCost() == null || ItemUtils.isEmpty(tc.getCost().getItem())) return;

            Skill skillToLevel = SkillRegistry.getSkill(tc.getSkillToLevel());
            Profile profile = ProfileCache.getOrCache(playerMenuUtility.getOwner(), skillToLevel.getProfileType());
            if (profile.getLevel() >= tc.getLimitPerLevel().getOrDefault(level, 0)) return;
            Map<ItemStack, Integer> totalItems = getServiceCost(tc);
            if (totalItems == null) return;
            if (ItemUtils.timesContained(Arrays.asList(playerMenuUtility.getOwner().getInventory().getStorageContents()), totalItems, tc.getCost().getOption()) >= 1){
                ItemUtils.removeItems(playerMenuUtility.getOwner().getInventory(), totalItems, 1, tc.getCost().getOption());
                double expToPurchase = getExpToPurchase(tc);
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

    @Override
    public void setMenuItems() {
        inventory.clear();
        MerchantLevel level = CustomMerchantManager.getLevel(data);
        if (level == null) return;
        inventory.setItem(45, previousPageButton);
        inventory.setItem(53, nextPageButton);
        for (UpgradeService service : services){
            if (service.getPrimaryButtonPosition() < 0 || service.getPrimaryButtonPosition() >= getSlots()) continue;
            Skill skill = SkillRegistry.getSkill(service.getSkillToLevel());
            if (skill == null) continue;
            Profile profile = ProfileCache.getOrCache(playerMenuUtility.getOwner(), skill.getProfileType());
            Map<ItemStack, Integer> cost = getServiceCost(service);
            if (cost == null || cost.isEmpty()) return;
            Optional<ItemStack> item = cost.keySet().stream().findAny();
            String costString = TranslationManager.translatePlaceholders(SlotEntry.toString(service.getCost()));
            int quantity = Math.max(0, cost.get(item.get()));

            ItemBuilder buttonBuilder = new ItemBuilder(service.getPrimaryButton().clone());
            if (profile.getLevel() >= service.getLimitPerLevel().getOrDefault(level, 0)) {
                buttonBuilder.lore(TranslationManager.translateListPlaceholders(CustomMerchantManager.getTradingConfig().getStringList("service_button_unavailable_training_description")));
            }
            buttonBuilder
                    .placeholderName("%skill%", skill.getDisplayName())
                    .placeholderName("%maxlevel%", String.valueOf(service.getLimitPerLevel().getOrDefault(level, 0)))
                    .placeholderName("%levelcurrent%", String.valueOf(profile.getLevel()))
                    .placeholderName("%levelnext%", profile.getLevel() >= skill.getMaxLevel() ? TranslationManager.getTranslation("max_level") : String.valueOf(profile.getLevel() + 1))
                    .placeholderName("%costamount%", String.valueOf(quantity))
                    .placeholderName("%costdescription%", costString)
                    .placeholderLore("%skill%", skill.getDisplayName())
                    .placeholderLore("%maxlevel%", String.valueOf(service.getLimitPerLevel().getOrDefault(level, 0)))
                    .placeholderLore("%levelcurrent%", String.valueOf(profile.getLevel()))
                    .placeholderLore("%levelnext%", profile.getLevel() >= skill.getMaxLevel() ? TranslationManager.getTranslation("max_level") : String.valueOf(profile.getLevel() + 1))
                    .placeholderLore("%costamount%", String.valueOf(quantity))
                    .placeholderLore("%costdescription%", costString)
                    .stringTag(KEY_METHOD, service.getID()).translate().get();

            ItemStack blankServiceButton = new ItemBuilder(buttonBuilder.get()).type(Material.LIME_DYE).data(9199200).get();
            for (int secondaryIndex : service.getSecondaryButtonPositions()) {
                if (secondaryIndex < 0 || secondaryIndex >= getSlots()) continue;
                inventory.setItem(secondaryIndex, blankServiceButton);
            }
            inventory.setItem(service.getPrimaryButtonPosition(), buttonBuilder.get());
        }
    }

    private static final ItemStack nextPageButton = new ItemBuilder(getButtonData("editor_nextpage", Material.ARROW))
            .name(TranslationManager.getTranslation("translation_next_page"))
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack previousPageButton = new ItemBuilder(getButtonData("editor_prevpage", Material.ARROW))
            .name(TranslationManager.getTranslation("translation_previous_page"))
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
}
