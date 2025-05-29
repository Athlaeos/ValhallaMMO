package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ResultChangingModifier;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.ExactChoice;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.TradingProfile;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.dom.MerchantLevel;
import me.athlaeos.valhallammo.trading.dom.MerchantTrade;
import me.athlaeos.valhallammo.trading.dom.MerchantType;
import me.athlaeos.valhallammo.trading.happiness.HappinessSourceRegistry;
import me.athlaeos.valhallammo.trading.services.service_implementations.OrderService;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class ServiceOrderingMenu extends Menu {
    private static final NamespacedKey KEY_TRADE = new NamespacedKey(ValhallaMMO.getInstance(), "trade");

    private static final int[] indexesCostSection = new int[]{
            0, 1, 2,
            9, 10, 11,
            18, 19, 20,
            27, 28, 29,
            36, 37, 38
    };
    private static final int[] indexesResultsSection = new int[]{
            6, 7, 8,
            15, 16, 17,
            24, 25, 26,
            33, 34, 35,
            42, 43, 44
    };
    private static final int indexTimeButton = 13;
    private static final List<Integer> indexesConfirmButton = List.of(
            30, 31, 32,
            39, 40, 41
    );
    private static final int indexPreviousPage = 45;
    private static final int[] indexesTrades = new int[]{46, 47, 48, 49, 50, 51, 52};
    private static final int indexNextPage = 53;

    private final OrderService service;
    private final Map<String, Integer> orders = new HashMap<>();
    private final MerchantData data;
    private final float happiness;
    private final float renown;
    private int page = 0;
    private final List<MerchantTrade> orderableTrades = new ArrayList<>();

    public ServiceOrderingMenu(PlayerMenuUtility playerMenuUtility, OrderService service, MerchantData data) {
        super(playerMenuUtility);
        this.data = data;
        this.service = service;
        this.happiness = data.getVillager() == null ? 0F : HappinessSourceRegistry.getHappiness(playerMenuUtility.getOwner(), data.getVillager());
        this.renown = data.getPlayerMemory(playerMenuUtility.getOwner().getUniqueId()).getRenownReputation();

        MerchantType type = CustomMerchantManager.getMerchantType(data.getType());
        for (MerchantLevel level : MerchantLevel.values()){
            for (String t : type.getTrades().get(level).getTrades()){
                MerchantTrade trade = CustomMerchantManager.getTrade(t);
                if (trade == null || trade.getMaxOrderCount() <= 0) continue;
                orderableTrades.add(trade);
            }
        }
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF322" : "&8Make your order"); // TODO data driven
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
        String t = ItemUtils.getPDCString(KEY_TRADE, clicked.getMeta(), null);
        if (t != null){
            MerchantTrade trade = CustomMerchantManager.getTrade(t);
            if (trade == null) return;
            int count = orders.getOrDefault(t, 0);
            double maxOrderCountMultiplier = 1 + AccumulativeStatManager.getCachedStats("TRADING_ORDER_MAX_COUNT_MULTIPLIER", playerMenuUtility.getOwner(), 10000, true);
            count = Math.min((int) (maxOrderCountMultiplier * trade.getMaxOrderCount()), Math.max(0, count + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 8 : 1))));
            if (count <= 0) orders.remove(t);
            else orders.put(t, count);
            setMenuItems();
            return;
        }

        switch (e.getRawSlot()){
            case indexNextPage -> page++;
            case indexPreviousPage -> page--;
        }
        if (indexesConfirmButton.contains(e.getRawSlot())) {
            if (orders.isEmpty()) {
                e.getWhoClicked().closeInventory();
                return;
            }
            Map<ItemStack, Integer> totalItems = getOrderCost();
            if (ItemUtils.timesContained(Arrays.asList(playerMenuUtility.getOwner().getInventory().getStorageContents()), totalItems, new ExactChoice()) >= 1){
                ItemUtils.removeItems(playerMenuUtility.getOwner().getInventory(), totalItems, 1, new ExactChoice());
                e.getWhoClicked().closeInventory();
                TradingProfile profile = ProfileCache.getOrCache(playerMenuUtility.getOwner(), TradingProfile.class);
                long orderTime = service.getBaseOrderTime() + (service.getOrderTimeBonusPerTrade() * orders.size());

                MerchantData.OrderData existingOrder = data.getPendingOrders().get(playerMenuUtility.getOwner().getUniqueId());
                if (existingOrder != null) {
                    for (String order : existingOrder.getOrder().keySet()){
                        int orderCount = orders.getOrDefault(order, 0) + existingOrder.getOrder().get(order);
                        if (orderCount > 1) orderTime += ((orderCount - 1) * service.getOrderTimeBonusPerItem());
                        orders.put(order, orderCount);
                    }
                }
                orderTime = (long) (orderTime * (1 + profile.getOrderDeliverySpeedMultiplier()));
                data.getPendingOrders().put(playerMenuUtility.getOwner().getUniqueId(), new MerchantData.OrderData(orderTime, orders));
                if (data.getVillager() != null) data.getVillager().getWorld().playSound(data.getVillager().getLocation(), Sound.ENTITY_VILLAGER_YES, 1F, 1F);
                return;
            } else Utils.sendMessage(e.getWhoClicked(), TranslationManager.getTranslation("service_order_cant_afford"));
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

        inventory.setItem(indexPreviousPage, previousPageButton);
        inventory.setItem(indexNextPage, nextPageButton);
        ItemStack blankServiceButton = new ItemBuilder(confirmButton).type(Material.LIME_DYE).data(9199200).get();
        for (int i = 0; i < indexesConfirmButton.size(); i++){
            if (i == 0) inventory.setItem(indexesConfirmButton.get(i), confirmButton);
            else inventory.setItem(indexesConfirmButton.get(i), blankServiceButton);
        }

        TradingProfile profile = ProfileCache.getOrCache(playerMenuUtility.getOwner(), TradingProfile.class);
        long orderTime = CustomMerchantManager.getTradingConfig().getLong("delivery_time");
        orderTime = (long) (orderTime * (1 + profile.getOrderDeliverySpeedMultiplier()));
        inventory.setItem(indexTimeButton, new ItemBuilder(deliveryTimeLabel)
                .placeholderLore("%time%", timeFormat(orderTime)).get()
        );

        Map<ItemStack, Integer> totalItems = getOrderCost();
        Map<ItemStack, Integer> totalResults = new HashMap<>();
        for (String t : orders.keySet()){
            int quantity = orders.get(t);
            MerchantTrade trade = CustomMerchantManager.getTrade(t);
            if (quantity <= 0 || trade == null) continue;

            ItemStack result = trade.getResult().clone();
            result.setAmount(1);
            for (int i = 0; i < quantity; i++)
                totalResults.put(result, trade.getResult().getAmount() + totalResults.getOrDefault(result, 0));
        }
        List<ItemStack> tradeButtons = new ArrayList<>();
        List<String> costFormat = TranslationManager.getListTranslation("service_order_trade_prefix");
        double maxOrderCountMultiplier = 1 + AccumulativeStatManager.getCachedStats("TRADING_ORDER_MAX_COUNT_MULTIPLIER", playerMenuUtility.getOwner(), 10000, true);

        for (MerchantTrade trade : orderableTrades){
            ItemBuilder item1 = new ItemBuilder(trade.getScalingCostItem());
            ItemBuilder item2 = ItemUtils.isEmpty(trade.getOptionalCostItem()) ? null : new ItemBuilder(trade.getOptionalCostItem());
            int quantity = orders.getOrDefault(trade.getID(), 0);

            float bulkCostMultiplier = Math.max(0, 1 - (quantity <= service.getBulkMinimumOrdersForDiscount() ? 0 : Math.min(service.getBulkMaxDiscount(), (quantity - service.getBulkMinimumOrdersForDiscount()) * service.getBulkDiscountPerItem())));
            float reputation = data.getPlayerMemory(playerMenuUtility.getOwner().getUniqueId()).getTradingReputation();
            if (reputation < 0) reputation *= trade.getNegativeReputationMultiplier();
            else if (reputation > 0) reputation *= trade.getPositiveReputationMultiplier();

            double relationshipCostMultiplier = Math.max(0, 1 - (CustomMerchantManager.getDiscountFormula() == null ? 0 : Utils.eval(CustomMerchantManager.getDiscountFormula()
                    .replace("%happiness%", String.valueOf(happiness))
                    .replace("%renown%", String.valueOf(renown))
                    .replace("%reputation%", String.valueOf(reputation))
            )));
            float price = (float) (trade.getScalingCostItem().getAmount() * relationshipCostMultiplier * bulkCostMultiplier);

            List<String> prefix = new ArrayList<>();
            for (String line : costFormat){
                if (item2 == null && (line.contains("%item2%") || line.contains("%amount2%"))) continue;
                prefix.add(line
                        .replace("%max_orders%", String.valueOf((int) (maxOrderCountMultiplier * trade.getMaxOrderCount())))
                        .replace("%times_ordered%", String.valueOf(orders.getOrDefault(trade.getID(), 0)))
                        .replace("%item%", ItemUtils.getItemName(item1.getMeta()))
                        .replace("%amount%", String.format("%.2f", price))
                        .replace("%item2%", item2 == null ? "": ItemUtils.getItemName(item2.getMeta()))
                        .replace("%amount2%", item2 == null ? "": String.valueOf(item2.getItem().getAmount()))
                );
            }
            ItemBuilder result = new ItemBuilder(trade.getResult());

            ResultChangingModifier changingModifier = (ResultChangingModifier) trade.getModifiers().stream().filter(m -> m instanceof ResultChangingModifier).findFirst().orElse(null);
            if (changingModifier != null) {
                ItemStack resultItem = changingModifier.getNewResult(ModifierContext.builder(result).crafter(playerMenuUtility.getOwner()).validate().get());
                if (!ItemUtils.isEmpty(resultItem)) result = new ItemBuilder(resultItem);
            }
            if (ItemUtils.isEmpty(result.getItem()) || CustomFlag.hasFlag(result.getMeta(), CustomFlag.UNCRAFTABLE)) continue;

            result.stringTag(KEY_TRADE, trade.getID());
            result.prependLore(prefix);

            tradeButtons.add(result.get());
        }
        Map<Integer, List<ItemStack>> pages = Utils.paginate(7, tradeButtons);
        if (page < 0) page = 0;
        else if (page >= pages.size()) page = pages.size() - 1;

        for (int i = 0; i < pages.getOrDefault(page, new ArrayList<>()).size(); i++){
            inventory.setItem(indexesTrades[i], pages.get(page).get(i));
        }

        List<ItemStack> decompressedCosts = ItemUtils.decompressStacks(totalItems);
        for (int i = 0; i < decompressedCosts.size(); i++){
            if (i >= indexesCostSection.length) break;
            inventory.setItem(indexesCostSection[i], decompressedCosts.get(i));
        }
        List<ItemStack> decompressedResults = ItemUtils.decompressStacks(totalResults);
        for (int i = 0; i < decompressedResults.size(); i++){
            if (i >= indexesResultsSection.length) break;
            inventory.setItem(indexesResultsSection[i], decompressedResults.get(i));
        }
    }

    private Map<ItemStack, Integer> getOrderCost(){
        Map<ItemStack, Integer> totalItems = new HashMap<>();
        for (String t : orders.keySet()){
            int quantity = orders.get(t);
            MerchantTrade trade = CustomMerchantManager.getTrade(t);
            if (quantity <= 0 || trade == null) continue;
            float bulkCostMultiplier = Math.max(0, 1 - (quantity <= service.getBulkMinimumOrdersForDiscount() ? 0 : Math.min(service.getBulkMaxDiscount(), (quantity - service.getBulkMinimumOrdersForDiscount()) * service.getBulkDiscountPerItem())));
            float reputation = data.getPlayerMemory(playerMenuUtility.getOwner().getUniqueId()).getTradingReputation();
            if (reputation < 0) reputation *= trade.getNegativeReputationMultiplier();
            else if (reputation > 0) reputation *= trade.getPositiveReputationMultiplier();

            double relationshipCostMultiplier = Math.max(0, 1 - (CustomMerchantManager.getDiscountFormula() == null ? 0 : Utils.eval(CustomMerchantManager.getDiscountFormula()
                    .replace("%happiness%", String.valueOf(happiness))
                    .replace("%renown%", String.valueOf(renown))
                    .replace("%reputation%", String.valueOf(reputation))
            )));
            int price = (int) Math.max(1, quantity * trade.getScalingCostItem().getAmount() * relationshipCostMultiplier * bulkCostMultiplier);

            ItemStack cost1 = trade.getScalingCostItem().clone();
            cost1.setAmount(1);
            totalItems.put(cost1, price + totalItems.getOrDefault(cost1, 0));

            if (!ItemUtils.isEmpty(trade.getOptionalCostItem())) {
                ItemStack cost2 = trade.getOptionalCostItem().clone();
                cost2.setAmount(1);
                int quantityRequired = trade.getOptionalCostItem().getAmount();
                for (int i = 0; i < quantity; i++)
                    totalItems.put(cost2, quantityRequired + totalItems.getOrDefault(cost2, 0));
            }
        }
        return totalItems;
    }

    public List<MerchantTrade> getOrderableTrades() {
        return orderableTrades;
    }

    public static String timeFormat(long ticks){
        if (ticks < 24000) return TranslationManager.getTranslation("service_order_time_format_less_than_1_day")
                .replace("%days%", String.valueOf((int) ticks / 24000))
                .replace("%hours%", String.valueOf((int) (ticks % 24000) / 1000));
        else return TranslationManager.getTranslation("service_order_time_format_more_than_1_day")
                .replace("%days%", String.valueOf((int) ticks / 24000))
                .replace("%hours%", String.valueOf((int) (ticks % 24000) / 1000));
    }

    private static final ItemStack nextPageButton = new ItemBuilder(getButtonData("editor_nextpage", Material.ARROW))
            .name(TranslationManager.getTranslation("translation_next_page"))
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack previousPageButton = new ItemBuilder(getButtonData("editor_prevpage", Material.ARROW))
            .name(TranslationManager.getTranslation("translation_previous_page"))
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack confirmButton = new ItemBuilder(getButtonData("order_confirm", Material.LIME_DYE))
            .name(TranslationManager.getTranslation("service_confirm_order"))
            .lore(TranslationManager.getListTranslation("service_confirm_order_lore")).get();
    private static final ItemStack deliveryTimeLabel = new ItemBuilder(getButtonData("editor_recipe_cooking_cooktime", Material.CLOCK))
            .name(TranslationManager.getTranslation("service_order_time"))
            .lore(TranslationManager.getListTranslation("service_order_time_lore"))
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
}
