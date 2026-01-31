package me.athlaeos.valhallammo.version;

import com.destroystokyo.paper.loottable.LootableBlockInventory;
import com.destroystokyo.paper.loottable.LootableEntityInventory;
import com.destroystokyo.paper.loottable.LootableInventory;
import com.destroystokyo.paper.loottable.LootableInventoryReplenishEvent;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.event.ValhallaLootPopulateEvent;
import me.athlaeos.valhallammo.event.ValhallaLootReplacementEvent;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.loot.ReplacementTable;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaperLootRefillHandler implements Listener {
    public static boolean isRefillPending(BlockState i){
        if (ValhallaMMO.isUsingPaperMC()) return false;
        if (i instanceof LootableInventory l) return l.hasPendingRefill();
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReplenish(LootableInventoryReplenishEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName())) return;

        Inventory inventory = e.getInventory() instanceof LootableBlockInventory lb && lb.getBlock().getState() instanceof Container c ? c.getInventory() :
                e.getInventory() instanceof LootableEntityInventory le && le instanceof InventoryHolder ih ? ih.getInventory() : null;
        if (inventory == null || inventory.getLocation() == null) return;
        int originalSize = inventory.getContents().length;
        org.bukkit.loot.LootTable lootTable = e.getInventory().getLootTable();
        if (lootTable == null) return;
        AttributeInstance luckInstance = e.getPlayer().getAttribute(Attribute.GENERIC_LUCK);
        double luck = luckInstance == null ? 0 : luckInstance.getValue();
        LootContext context = new LootContext.Builder(inventory.getLocation()).killer(e.getPlayer()).lootedEntity(e.getPlayer()).lootingModifier(0).luck((float) luck).build();
        LootTable table = LootTableRegistry.getLootTable(inventory.getLocation().getBlock(), inventory.getLocation().getBlock().getType());
        if (table == null) table = LootTableRegistry.getLootTable(lootTable.getKey());
        if (table != null) {
            // LootTableRegistry.setLootTable(b, null);
            List<ItemStack> loot = LootTableRegistry.getLoot(table, context, LootTable.LootType.CONTAINER);
            ValhallaLootPopulateEvent loottableEvent = new ValhallaLootPopulateEvent(table, context, loot);
            ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(loottableEvent);
            if (!loottableEvent.isCancelled()){
                boolean skip = false;
                switch (loottableEvent.getPreservationType()){
                    case CLEAR -> {
                        for (int i = 0; i < originalSize; i++) inventory.setItem(i, null);
                    }
                    case CLEAR_UNLESS_EMPTY -> {
                        if (!loottableEvent.getDrops().isEmpty()) {
                            for (int i = 0; i < originalSize; i++) inventory.setItem(i, null);
                        }
                    }
                    case KEEP -> {
                        if (loottableEvent.getDrops().isEmpty()) skip = true;
                    }
                }
                if (!skip){
                    List<ItemStack> drops = new ArrayList<>(loottableEvent.getDrops());
                    for (int i = 0; i < inventory.getSize() - drops.size(); i++) drops.add(null);
                    Collections.shuffle(drops);
                    for (int i = 0; i < drops.size(); i++) {
                        ItemStack drop = drops.get(i);
                        if (ItemUtils.isEmpty(drop)) continue;
                        if (i > inventory.getSize() - 1) break;
                        inventory.setItem(i, drop);
                    }
                }
            }
        }

        ReplacementTable replacementTable = LootTableRegistry.getReplacementTable(e.getInventory().getLootTable().getKey());
        ReplacementTable globalTable = LootTableRegistry.getGlobalReplacementTable();
        ValhallaLootReplacementEvent event = new ValhallaLootReplacementEvent(replacementTable, context);
        if (replacementTable != null) ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
        if (replacementTable == null || !event.isCancelled()){
            for (int i = 0; i < inventory.getSize(); i++){
                ItemStack item = inventory.getItem(i);
                if (ItemUtils.isEmpty(item)) continue;
                ItemStack replacement = LootTableRegistry.getReplacement(replacementTable, context, LootTable.LootType.CONTAINER, item);
                if (!ItemUtils.isEmpty(replacement)) item = replacement;
                ItemStack globalReplacement = LootTableRegistry.getReplacement(globalTable, context, LootTable.LootType.CONTAINER, item);
                if (!ItemUtils.isEmpty(globalReplacement)) item = globalReplacement;
                if (!ItemUtils.isEmpty(item)) inventory.setItem(i, item);
            }
        }
    }
}
