package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArmorSwitchListener implements Listener {
    private static final Map<UUID, DelayedArmorUpdate> taskLimiters = new HashMap<>();

    private void updateArmor(Player who){
        DelayedArmorUpdate update = taskLimiters.get(who.getUniqueId());
        if (update != null) update.refresh();
        else {
            update = new DelayedArmorUpdate(who);
            update.runTaskTimer(ValhallaMMO.getInstance(), 1L, 1L);
        }
        taskLimiters.put(who.getUniqueId(), update);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryEquip(InventoryClickEvent e){
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() == InventoryType.PLAYER || e.getClickedInventory().getType() == InventoryType.CREATIVE){
            if (e.getSlotType() == InventoryType.SlotType.ARMOR){
                if (ItemUtils.isEmpty(e.getCurrentItem()) && ItemUtils.isEmpty(e.getCursor())) return; // both clicked slot and cursor are empty, no need to do anything
                // cursor or clicked item are empty, but not both. items are switched and therefore armor may be updated
                updateArmor((Player) e.getWhoClicked());
            } else if (e.getClick().isShiftClick() && !ItemUtils.isEmpty(e.getCurrentItem())){
                ItemBuilder clicked = new ItemBuilder(e.getCurrentItem());
                EquipmentClass type = EquipmentClass.getMatchingClass(clicked.getMeta());
                if (type == null || !type.isArmor()) return; // clicked item isn't armor, and so it can be assumed no equipment is equipped
                ItemStack armor = switch (type){
                    case HELMET -> e.getWhoClicked().getInventory().getItem(EquipmentSlot.HEAD);
                    case CHESTPLATE -> e.getWhoClicked().getInventory().getItem(EquipmentSlot.CHEST);
                    case LEGGINGS -> e.getWhoClicked().getInventory().getItem(EquipmentSlot.LEGS);
                    case BOOTS -> e.getWhoClicked().getInventory().getItem(EquipmentSlot.FEET);
                    default -> null;
                };
                if (ItemUtils.isEmpty(armor)){
                    // armor slot fitting for the clicked item is empty, update equipment
                    updateArmor((Player) e.getWhoClicked());
                } // armor slot is occupied, do not update
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDispenserEquip(BlockDispenseArmorEvent e){
        if (e.getTargetEntity() instanceof Player p){
            // armor equipped through dispenser, update equipment
            updateArmor(p);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHandEquip(PlayerInteractEvent e){
        if (e.useItemInHand() == Event.Result.DENY || ItemUtils.isEmpty(e.getItem()) || !e.getItem().getType().isItem()) return;
        ItemBuilder clicked = new ItemBuilder(e.getItem());
        EquipmentClass type = EquipmentClass.getMatchingClass(clicked.getMeta());
        if (!EquipmentClass.isArmor(type)) return; // clicked item isn't armor, and so it can be assumed no equipment is equipped
        // armor was clicked and might be swapped out, update equipment
        updateArmor(e.getPlayer());
    }

    private static class DelayedArmorUpdate extends BukkitRunnable{
        private static final int delay = 30; // after 1.5 seconds update equipment
        private int timer = delay;
        private final Player who;

        public DelayedArmorUpdate(Player who){
            this.who = who;
        }

        @Override
        public void run() {
            if (timer <= 0){
                EntityCache.resetEquipment(who);
                AccumulativeStatManager.updateStats(who);
                taskLimiters.remove(who.getUniqueId());
                cancel();
            } else {
                timer--;
            }
        }

        public void refresh(){
            timer = delay;
        }
    }
}
