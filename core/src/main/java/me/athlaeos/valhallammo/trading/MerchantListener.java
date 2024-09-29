package me.athlaeos.valhallammo.trading;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent e){
        if (e.getInventory().getType() != InventoryType.MERCHANT) return;
        virtualMerchantClose((Player) e.getPlayer(), getCurrentActiveVirtualMerchant((Player) e.getPlayer()));
    }

    public static void virtualMerchantClose(Player p, VirtualMerchant virtualMerchant){
        if (virtualMerchant == null) return;
        setActiveTradingMenu(p, null);
        virtualMerchant.onClose();
        Villager villager = virtualMerchant.getVillager();
        if (villager == null) return;
        villager.setVillagerExperience(villager.getVillagerExperience() + virtualMerchant.getExpToGrant());
        System.out.println("granted " + virtualMerchant.getExpToGrant() + " experience");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPrepareTrade(TradeSelectEvent e){
        if (e.getMerchant().getTrader() == null || e.isCancelled()) return;
        VirtualMerchant merchantInterface = activeTradingMenus.get(e.getMerchant().getTrader().getUniqueId());
        if (merchantInterface == null) return;
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            ItemStack result = e.getInventory().getItem(2);
            if (ItemUtils.isEmpty(result)) return;
            ItemMeta meta = result.getItemMeta();
            if (meta == null) return;
            CustomTradeRegistry.removeTradeKey(meta);
            result.setItemMeta(meta);
        }, 1L); // don't forget that this key must still be added to trades
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTrade(InventoryClickEvent e){
        if (!(e.getClickedInventory() instanceof MerchantInventory m) || e.isCancelled() || m.getSelectedRecipe() == null || e.getRawSlot() != 2 || ItemUtils.isEmpty(m.getItem(2))) return;
        VirtualMerchant merchantInterface = activeTradingMenus.get(e.getWhoClicked().getUniqueId());
        if (merchantInterface == null) {
            e.setCancelled(true);
            ValhallaMMO.logWarning("Something went wrong accessing a merchant menu, please notify the plugin developer as this should never occur");
            return;
        }
        Villager villager = merchantInterface.getVillager();

        MerchantRecipe recipe = m.getSelectedRecipe();
        ItemStack result = recipe.getResult();
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

        PlayerTradeItemEvent event = new PlayerTradeItemEvent((Player) e.getWhoClicked(), villager, m.getMerchant(), recipe, result, timesTraded, GossipTypeWrapper.TRADING);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled() || event.getTimesTraded() <= 0){
            e.setCancelled(true);
            System.out.println("trade was cancelled");
            return;
        }
        System.out.println("traded item " + event.getTimesTraded() + " times");
        m.setItem(2, event.getResult());
        if (villager != null){
            for (int i = 0; i < event.getTimesTraded(); i++){
                ValhallaMMO.getNms().modifyReputation((Player) e.getWhoClicked(), villager, event.getReputationInfluence());
                System.out.println("granted " + event.getReputationInfluence() + " reputation");
            }
            merchantInterface.setExpToGrant(merchantInterface.getExpToGrant() + (event.getTimesTraded() * event.getRecipeTraded().getVillagerExperience()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVillagerInteract(PlayerInteractAtEntityEvent e){
        if (!(e.getRightClicked() instanceof Villager v)) return;
        int reputation = ValhallaMMO.getNms().getReputation(e.getPlayer(), v);
        System.out.println("reputation: " + reputation);

        MerchantConfiguration configuration = CustomTradeRegistry.getMerchantConfigurationByProfession().get(v.getProfession());
        if (configuration == null) return;
        System.out.println("opening custom merchant");
        new SimpleMerchant(PlayerMenuUtilManager.getPlayerMenuUtility(e.getPlayer()), v, CustomTradeRegistry.recipesFromVillager(v, e.getPlayer())).open();
        e.setCancelled(true);
    }

    public static VirtualMerchant getCurrentActiveVirtualMerchant(Player player){
        return activeTradingMenus.get(player.getUniqueId());
    }

    public static void setActiveTradingMenu(Player player, VirtualMerchant inventory){
        activeTradingMenus.put(player.getUniqueId(), inventory);
    }
}
