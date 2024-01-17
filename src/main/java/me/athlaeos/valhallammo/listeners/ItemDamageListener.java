package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.item.CustomDurabilityManager;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemSkillRequirements;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemDamageListener implements Listener {
    @EventHandler(priority= EventPriority.HIGHEST)
    public void onDurabilityChange(PlayerItemDamageEvent e){
        if (e.isCancelled()) return;
        double durabilityBonus = AccumulativeStatManager.getCachedStats("DURABILITY_BONUS", e.getPlayer(), 10000, true);
        // the durabilityBonus represents a % chance to not spend durability, effectively raising it. If this amount is negative though it's treated as a damage multiplier

        ItemMeta meta = ItemUtils.getItemMeta(e.getItem());
        if (meta == null) return;
        durabilityBonus += ItemSkillRequirements.getPenalty(e.getPlayer(), meta, "durability");

        // examples:
        // +0.2 leads to 1/1.2 = ~0.83 and so damage is multiplied by that to produce roughly +20% durability
        // -11 leads to 11 - 1) = 10, 10x durability taken
        double multiplier = durabilityBonus < 0 ? -durabilityBonus - 1 : 1 / (durabilityBonus + 1);
        e.setDamage(Utils.randomAverage(e.getDamage() * Math.max(0, multiplier)));

        if (CustomDurabilityManager.hasCustomDurability(meta)){
            CustomDurabilityManager.damage(meta, e.getDamage());
            if (CustomDurabilityManager.getDurability(meta, false) > 0) {
                e.setCancelled(true);
                ItemUtils.setItemMeta(e.getItem(), meta);
            } else if (meta instanceof Damageable d && e.getItem().getType().getMaxDurability() > 0) {
                d.setDamage(e.getItem().getType().getMaxDurability());
                e.getItem().setItemMeta(d);
                // e.getItem().setType(Material.AIR);
                // e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
            }
        }
    }

    @EventHandler(priority=EventPriority.NORMAL)
    public void onItemMend(PlayerItemMendEvent e){
        if (e.isCancelled() || ItemUtils.isEmpty(e.getItem())) return;
        ItemMeta meta = ItemUtils.getItemMeta(e.getItem());
        if (CustomFlag.hasFlag(meta, CustomFlag.UNMENDABLE)){
            e.setCancelled(true);
            return;
        }
        if (CustomDurabilityManager.hasCustomDurability(meta)){
            CustomDurabilityManager.damage(meta, -e.getRepairAmount());
            e.setCancelled(true);
            ItemUtils.setItemMeta(e.getItem(), meta);
        }
    }
}