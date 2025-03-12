package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.PotionBelt;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PotionBeltListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConsumeItem(PlayerItemConsumeEvent e){
        if (e.isCancelled() || e.getItem().getType() != Material.POTION) return;
        ItemStack consumed = e.getItem();
        if (ItemUtils.isEmpty(consumed)) return;
        ItemBuilder consumableBuilder = new ItemBuilder(consumed);
        if (!PotionBelt.isPotionBelt(consumableBuilder.getMeta())) return;

        ItemStack replacement = PotionBelt.deleteSelectedPotion(consumableBuilder);
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            if (e.getHand() == EquipmentSlot.HAND) e.getPlayer().getInventory().setItemInMainHand(replacement);
            else e.getPlayer().getInventory().setItemInOffHand(replacement);
            ItemUtils.addItem(e.getPlayer(), new ItemStack(Material.GLASS_BOTTLE), false);
            e.getPlayer().playSound(e.getPlayer(), Sound.ITEM_BOTTLE_FILL, 1F, 1F);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onScroll(PlayerItemHeldEvent e){
        if (e.isCancelled() || !e.getPlayer().isSneaking()) return;
        ItemStack previousItem = e.getPlayer().getInventory().getItem(e.getPreviousSlot());
        if (ItemUtils.isEmpty(previousItem)) return;
        ItemBuilder hand = new ItemBuilder(previousItem);
        if (!PotionBelt.isPotionBelt(hand.getMeta())) return;
        int offset = getOffset(e.getPreviousSlot(), e.getNewSlot());
        e.setCancelled(true);
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            ItemStack belt = PotionBelt.swapSelectedPotion(hand, offset);
            if (ItemUtils.isEmpty(belt)) return;
            e.getPlayer().getInventory().setItemInMainHand(belt);
            e.getPlayer().playSound(e.getPlayer(), Sound.ITEM_BOTTLE_EMPTY, 1F, 1F);
        }, 1L);
    }

    private int getOffset(int from, int to){
        int diff = to - from;
        if (diff > 4) return -9 + diff;
        else if (diff < -4) return 9 + diff;
        else return diff;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onThrow(ProjectileLaunchEvent e){
        if (!(e.getEntity() instanceof ThrownPotion t) || !(e.getEntity().getShooter() instanceof Player p)) return;
        EquipmentSlot hand;
        ItemStack thrown;
        if (p.getInventory().getItemInMainHand().getType() == Material.SPLASH_POTION || p.getInventory().getItemInMainHand().getType() == Material.LINGERING_POTION) {
            hand = EquipmentSlot.HAND;
            thrown = p.getInventory().getItemInMainHand();
        } else if (p.getInventory().getItemInOffHand().getType() == Material.SPLASH_POTION || p.getInventory().getItemInOffHand().getType() == Material.LINGERING_POTION) {
            hand = EquipmentSlot.OFF_HAND;
            thrown = p.getInventory().getItemInOffHand();
        } else return;
        ItemBuilder item = new ItemBuilder(thrown);
        if (!PotionBelt.isPotionBelt(item.getMeta())) return;
        ItemStack after = PotionBelt.deleteSelectedPotion(item);
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            if (hand == EquipmentSlot.HAND) p.getInventory().setItemInMainHand(after);
            else p.getInventory().setItemInOffHand(after);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent e){
        if (e.isCancelled() || (!e.getItemDrop().getItemStack().equals(e.getPlayer().getInventory().getItemInMainHand()))) return;
        ItemStack dropped = e.getItemDrop().getItemStack();
        if (ItemUtils.isEmpty(dropped)) return;
        ItemBuilder item = new ItemBuilder(dropped);
        if (!PotionBelt.isPotionBelt(item.getMeta())) return;
        PotionBelt.PotionExtractionDetails consumptionDetails = PotionBelt.removeSelectedPotion(item);
        if (consumptionDetails == null) return;
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            ItemStack newDrop = consumptionDetails.removed();
            if (ItemUtils.isEmpty(newDrop)) {
                e.getItemDrop().setItemStack(consumptionDetails.newBelt());
            } else {
                e.getItemDrop().setItemStack(consumptionDetails.removed());
                e.getPlayer().getInventory().setItemInMainHand(consumptionDetails.newBelt());
            }
            e.getPlayer().playSound(e.getPlayer(), Sound.ITEM_BOTTLE_FILL, 1F, 1F);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent e){
        if (e.isCancelled() || !e.isRightClick() || e.isShiftClick()) return;
        ItemStack clicked = e.getCurrentItem();
        ItemStack cursor = e.getCursor();
        if (ItemUtils.isEmpty(clicked)) return;
        ItemBuilder item = new ItemBuilder(clicked);
        if (!PotionBelt.isPotionBelt(item.getMeta())) return;
        if (ItemUtils.isEmpty(cursor)){
            PotionBelt.PotionExtractionDetails details = PotionBelt.removeSelectedPotion(item);
            if (details == null || ItemUtils.isEmpty(details.newBelt()) || ItemUtils.isEmpty(details.removed())) return;
            e.getWhoClicked().setItemOnCursor(details.removed());
            e.setCurrentItem(details.newBelt());
            ((Player) e.getWhoClicked()).playSound(e.getWhoClicked(), Sound.ITEM_BOTTLE_FILL, 1F, 1F);
            e.setCancelled(true);
        } else {
            if (cursor.getType() != Material.POTION && cursor.getType() != Material.SPLASH_POTION && cursor.getType() != Material.LINGERING_POTION) return;
            ItemStack newBelt = PotionBelt.addPotion(item, cursor);
            if (!ItemUtils.isEmpty(newBelt)){
                e.setCancelled(true);
                e.setCurrentItem(newBelt);
                e.getWhoClicked().setItemOnCursor(null);
                ((Player) e.getWhoClicked()).playSound(e.getWhoClicked(), Sound.ITEM_BOTTLE_FILL, 1F, 1F);
            }
        }
    }
}
