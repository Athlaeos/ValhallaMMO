package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.item.CustomDurabilityManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class ItemDamageListener implements Listener {
    @EventHandler(priority= EventPriority.NORMAL)
    public void onDurabilityChange(PlayerItemDamageEvent e){
//        double durabilityMultiplier = 1 + AccumulativeStatManager.getStats("DURABILITY_MULTIPLIER_BONUS", e.getPlayer(), true);
//        double penalty = OverleveledEquipmentTool.getTool().getPenalty(e.getPlayer(), e.getItem(), "durability");
//        if (penalty > 0){
//            durabilityMultiplier = Math.max(0, durabilityMultiplier + penalty);
//            e.setDamage(Utils.randomAverage(e.getDamage() * durabilityMultiplier));
//        }
        ItemBuilder item = new ItemBuilder(e.getItem());
        if (CustomDurabilityManager.hasCustomDurability(item.getMeta())){
            CustomDurabilityManager.damage(item.getItem(), item.getMeta(), e.getDamage());
            e.setCancelled(CustomDurabilityManager.getDurability(item.getItem(), item.getMeta(), false) > 0);
        }
    }
}