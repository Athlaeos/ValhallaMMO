package me.athlaeos.valhallammo.trading.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.trading.*;
import me.athlaeos.valhallammo.trading.dom.*;
import me.athlaeos.valhallammo.trading.merchants.VirtualMerchant;
import me.athlaeos.valhallammo.trading.merchants.implementations.SimpleMerchant;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MerchantListener implements Listener {
    private static final Map<UUID, VirtualMerchant> activeTradingMenus = new HashMap<>();
    private final boolean convertAllVillagers = ValhallaMMO.getPluginConfig().getBoolean("customize_all_villagers");

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent e){
        if (e.getInventory().getType() != InventoryType.MERCHANT) return;
        virtualMerchantClose((Player) e.getPlayer(), getCurrentActiveVirtualMerchant((Player) e.getPlayer()));
    }

    public static void virtualMerchantClose(Player p, VirtualMerchant virtualMerchant){
        if (virtualMerchant == null) return;
        setActiveTradingMenu(p, null);
        virtualMerchant.onClose();
        UUID villager = virtualMerchant.getMerchantID();
        if (villager == null || !(ValhallaMMO.getInstance().getServer().getEntity(villager) instanceof Villager v)) return;
        v.setVillagerExperience(v.getVillagerExperience() + virtualMerchant.getExpToGrant());
    }

//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onPrepareTrade(TradeSelectEvent e){
//        if (e.getMerchant().getTrader() == null || e.isCancelled()) return;
//        VirtualMerchant merchantInterface = activeTradingMenus.get(e.getMerchant().getTrader().getUniqueId());
//        if (merchantInterface == null) return;
//        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
//            ItemStack result = e.getInventory().getItem(2);
//            if (ItemUtils.isEmpty(result)) return;
//            ItemMeta meta = result.getItemMeta();
//            if (meta == null) return;
//            CustomMerchantManager.removeTradeKey(meta);
//            result.setItemMeta(meta);
//        }, 1L); // TODO don't forget that this key must still be added to trades
//    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTrade(InventoryClickEvent e){
        if (!(e.getClickedInventory() instanceof MerchantInventory m) || e.isCancelled() || m.getSelectedRecipe() == null || e.getRawSlot() != 2 || ItemUtils.isEmpty(m.getItem(2))) return;
        VirtualMerchant merchantInterface = activeTradingMenus.get(e.getWhoClicked().getUniqueId());
        if (merchantInterface == null || merchantInterface.getMerchantID() == null) return;
        UUID merchantID = merchantInterface.getMerchantID();
        MerchantRecipe recipe = m.getSelectedRecipe();
        ItemStack result = recipe.getResult();
        ItemMeta meta = ItemUtils.isEmpty(result) ? null : result.getItemMeta();
        if (meta == null) return;
        MerchantTrade trade = CustomMerchantManager.tradeFromKeyedMeta(meta);
        if (trade == null) return;
        CustomMerchantManager.removeTradeKey(meta);
        result.setItemMeta(meta);

        ItemStack item1 = m.getItem(0);
        ItemStack item2 = m.getItem(1);
        ItemStack cost1 = recipe.getAdjustedIngredient1();
        ItemStack cost2 = recipe.getIngredients().size() > 1 ? recipe.getIngredients().get(1) : null;

        ClickType clickType = e.getClick();
        int timesTraded = 1;
        switch (clickType){
            case DROP, CONTROL_DROP -> {
                if (!ItemUtils.isEmpty(e.getCursor())){
                    e.setCancelled(true);
                    return;
                }
            } // do nothing special because these actions do not require empty inventory space
            case LEFT -> {
                if (!ItemUtils.isEmpty(e.getCursor()) && !ItemUtils.isEmpty(result) &&
                        (!e.getCursor().isSimilar(result) ||
                                (e.getCursor().getAmount() + result.getAmount() > ValhallaMMO.getNms().getMaxStackSize(e.getCursor().getItemMeta(), e.getCursor().getType()))) ||
                        recipe.getMaxUses() - recipe.getUses() <= 0){
                    // cursor cannot stack with result item, or the merchant recipe has been exhausted. do not proceed
                    e.setCancelled(true);
                    return;
                }
            }
            case SHIFT_LEFT, SHIFT_RIGHT -> {
                // calculate how many items can be traded
                // the max amount of items the player could trade if they have enough inventory space,
                int maxTradeable = 99;

                int available = ItemUtils.maxInventoryFit((Player) e.getWhoClicked(), result); // max items available to fit in inventory
                timesTraded = Math.min(available, maxTradeable);

                if (!ItemUtils.isEmpty(cost1)){
                    // if the recipe has a primary cost, here we calculate how often the player can make this trade based on the item in slot 0 and the cost of the recipe
                    if (!ItemUtils.isEmpty(item1)) timesTraded = Math.min(timesTraded, (int) Math.floor(item1.getAmount() / (double) cost1.getAmount()));
                    else timesTraded = 0; // the ingredient has a cost, but no items are present in slot 1. No trade mad
                } // if not, assuming the recipe has no secondary cost that isn't met, the trade can be made indefinitely
                if (!ItemUtils.isEmpty(cost2)) {
                    // we do the same with the secondary cost
                    if (!ItemUtils.isEmpty(item2)) timesTraded = Math.min(timesTraded, (int) Math.floor(item2.getAmount() / (double) cost2.getAmount()));
                    else timesTraded = 0;
                }
                timesTraded = Math.min(timesTraded, recipe.getMaxUses() - recipe.getUses());
                if (timesTraded <= 0) {
                    e.setCancelled(true);
                    return;
                }
            }
            default -> {
                e.setCancelled(true);
                return;
            }
        }

        int finalTimesTraded = timesTraded;
        CustomMerchantManager.getMerchantData(merchantID, data -> {
            PlayerTradeItemEvent event = new PlayerTradeItemEvent((Player) e.getWhoClicked(), merchantID, data, m.getMerchant(), recipe, trade, result, finalTimesTraded);
            ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
            if (event.isCancelled() || event.getTimesTraded() <= 0){
                e.setCancelled(true);
                return;
            }

            m.setItem(2, event.getResult());
            if (merchantID != null){
                int reputationQuantity = event.getTimesTraded();
                MerchantData.MerchantPlayerMemory memory = event.getMerchantData().getPlayerMemory(e.getWhoClicked().getUniqueId());
                // TODO calculate trading reputation based on current happiness
                memory.setTradingReputation(memory.getTradingReputation() + reputationQuantity);
                int exp = Utils.randomAverage(event.getTimesTraded() * event.getCustomTrade().getVillagerExperience());
                merchantInterface.setExpToGrant(merchantInterface.getExpToGrant() + exp);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVillagerInteract(PlayerInteractAtEntityEvent e){
        if (!(e.getRightClicked() instanceof Villager v) || !convertAllVillagers && !CustomMerchantManager.isCustomMerchant(v)) return;
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
            CustomMerchantManager.getMerchantData(v, data -> {
                if (data == null && convertAllVillagers) data = CustomMerchantManager.convertToRandomMerchant(v);
                if (data == null) return;
                MerchantData.MerchantPlayerMemory reputation = data.getPlayerMemory(e.getPlayer().getUniqueId());
                // TODO actually do something with reputation data
                MerchantConfiguration configuration = CustomMerchantManager.getMerchantConfigurationByProfession().get(v.getProfession());
                if (configuration == null) return;
                List<MerchantRecipe> recipes = CustomMerchantManager.recipesFromData(data, e.getPlayer());
                if (recipes != null) {
                    // TODO data driven custom merchants instead of hardcoded simple ones
                    VirtualMerchant merchant = new SimpleMerchant(PlayerMenuUtilManager.getPlayerMenuUtility(e.getPlayer()), v.getUniqueId(), data, recipes);
                    if (merchant.getRecipes().isEmpty()) v.shakeHead();
                    else merchant.open();
                } else v.shakeHead();
            });
        });
        e.setCancelled(true);
    }

    public static VirtualMerchant getCurrentActiveVirtualMerchant(Player player){
        return activeTradingMenus.get(player.getUniqueId());
    }

    public static void setActiveTradingMenu(Player player, VirtualMerchant inventory){
        activeTradingMenus.put(player.getUniqueId(), inventory);
    }
}
