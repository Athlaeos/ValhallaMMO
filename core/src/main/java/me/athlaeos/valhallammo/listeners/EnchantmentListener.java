package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class EnchantmentListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPrepareEnchant(PrepareItemEnchantEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEnchanter().getWorld().getName())) return;
        ItemStack item = e.getItem();
        ItemMeta meta = ItemUtils.getItemMeta(item);
        if (CustomFlag.hasFlag(meta, CustomFlag.UNENCHANTABLE)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEnchanter().getWorld().getName())) return;
        ItemStack item = e.getItem();
        ItemMeta meta = ItemUtils.getItemMeta(item);
        if (CustomFlag.hasFlag(meta, CustomFlag.UNENCHANTABLE)) {
            e.setCancelled(true);
            return;
        }

        Player enchanter = e.getEnchanter();
        int lapisConsumed = e.whichButton() + 1;

        if (e.getEnchanter().getGameMode() != GameMode.CREATIVE){
            double saveChance = AccumulativeStatManager.getCachedStats("ENCHANTING_LAPIS_SAVE_CHANCE", enchanter, 10000, true);
            if (Utils.proc(enchanter, saveChance, false)){
                ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                    ItemStack lapisSlot = e.getInventory().getItem(1);
                    ItemStack newLapis = new ItemStack(Material.LAPIS_LAZULI, lapisConsumed);
                    if (!ItemUtils.isEmpty(lapisSlot)){
                        if (lapisSlot.isSimilar(newLapis)){
                            lapisSlot.setAmount(lapisSlot.getAmount() + lapisConsumed);
                            e.getInventory().setItem(1, lapisSlot);
                        } else {
                            Map<Integer, ItemStack> remainingItems = e.getEnchanter().getInventory().addItem(newLapis);
                            if (!remainingItems.isEmpty()){
                                for (ItemStack i : remainingItems.values()){
                                    Item drop = e.getEnchanter().getWorld().dropItemNaturally(e.getEnchanter().getEyeLocation(), i);
                                    drop.setOwner(e.getEnchanter().getUniqueId());
                                }
                            }
                        }
                    } else {
                        e.getInventory().setItem(1, newLapis);
                    }
                }, 1L);
            }
        }

        double refundChance = AccumulativeStatManager.getCachedStats("ENCHANTING_REFUND_CHANCE", e.getEnchanter(), 10000, true);
        if (Utils.proc(enchanter, refundChance, false)){
            double refundAmount = Math.max(0, Math.min(AccumulativeStatManager.getCachedStats("ENCHANTING_REFUND_AMOUNT", e.getEnchanter(), 10000, true), 1D));
            // refundAmount is now a value between 0 and 1
            int expSpent = EntityUtils.getTotalExperience(enchanter.getLevel()) - EntityUtils.getTotalExperience(enchanter.getLevel() - (e.whichButton() + 1));

            int refunded = Utils.randomAverage(expSpent * refundAmount);
            e.getEnchanter().giveExp(Math.max(0, refunded));
        }
    }
}
