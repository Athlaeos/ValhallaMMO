package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.CustomDurabilityManager;
import me.athlaeos.valhallammo.item.CustomID;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class AnvilListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvilRepair(PrepareAnvilEvent e){
        if (e.getInventory().getLocation() == null || e.getInventory().getLocation().getWorld() == null ||
                ValhallaMMO.isWorldBlacklisted(e.getInventory().getLocation().getWorld().getName())) return;

        ItemStack item1 = e.getInventory().getItem(0);
        ItemStack item2 = e.getInventory().getItem(1);
        ItemStack result = e.getResult();
        if (ItemUtils.isEmpty(item1) || ItemUtils.isEmpty(item2) || ItemUtils.isEmpty(result)) return;
        ItemMeta item1Meta = ItemUtils.getItemMeta(item1);
        ItemMeta item2Meta = ItemUtils.getItemMeta(item2);
        ItemMeta resultMeta = ItemUtils.getItemMeta(result);
        if (!CustomDurabilityManager.hasCustomDurability(item1Meta) || !CustomDurabilityManager.hasCustomDurability(resultMeta)) return;
        if (item1.getType() == item2.getType()){
            Integer i1id = CustomID.getID(item1Meta);
            Integer i2id = CustomID.getID(item2Meta);
            if ((i1id == null) != (i2id == null) || (i1id != null && !i1id.equals(i2id))) {
                e.setResult(null); // items are the same type, but have different weapon ids, and are therefore considered different
                return;
            }
        }
        if (!(resultMeta instanceof Damageable d) || result.getType().getMaxDurability() <= 0) return;
        double fraction = (((int) result.getType().getMaxDurability() - d.getDamage()) / (double) result.getType().getMaxDurability());

        int maxDurability = CustomDurabilityManager.getDurability(resultMeta, true);
        if (fraction >= 0.97) CustomDurabilityManager.setDurability(resultMeta, maxDurability, maxDurability);
        else CustomDurabilityManager.setDurability(resultMeta, fraction);
        ItemUtils.setMetaNoClone(result, resultMeta);

        e.setResult(result);
    }
}
