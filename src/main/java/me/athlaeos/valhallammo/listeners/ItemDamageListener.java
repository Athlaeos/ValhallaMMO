package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.item.CustomDurabilityManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.ItemSkillRequirements;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class ItemDamageListener implements Listener {
    @EventHandler(priority= EventPriority.HIGHEST)
    public void onDurabilityChange(PlayerItemDamageEvent e){
        double durabilityBonus = AccumulativeStatManager.getCachedStats("DURABILITY_BONUS", e.getPlayer(), 10000, true);
        // the durabilityBonus represents a % chance to not spend durability, effectively raising it. If this amount is negative though it's treated as a damage multiplier

        ItemBuilder item = new ItemBuilder(e.getItem());
        durabilityBonus += ItemSkillRequirements.getPenalty(e.getPlayer(), item.getMeta(), "durability");

        // examples:
        // +0.2 leads to 1/1.2 = ~0.83 and so damage is multiplied by that to produce roughly +20% durability
        // -11 leads to 11 - 1) = 10, 10x durability taken
        double multiplier = durabilityBonus < 0 ? -durabilityBonus - 1 : 1 / (durabilityBonus + 1);

        e.setDamage(Utils.randomAverage(e.getDamage() * Math.max(0, multiplier)));

        if (CustomDurabilityManager.hasCustomDurability(item.getMeta())){
            CustomDurabilityManager.damage(item.getItem(), item.getMeta(), e.getDamage());
            e.setCancelled(CustomDurabilityManager.getDurability(item.getItem(), item.getMeta(), false) > 0);
        }
    }
}