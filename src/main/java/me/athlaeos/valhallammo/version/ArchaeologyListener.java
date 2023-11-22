package me.athlaeos.valhallammo.version;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.event.ValhallaLootPopulateEvent;
import me.athlaeos.valhallammo.listeners.LootListener;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrushableBlock;
import org.bukkit.block.Container;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.List;

public class ArchaeologyListener implements Listener {
    public static boolean isBrushable(BlockState state){
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20)) return state instanceof BrushableBlock;
        return false;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBrush(PlayerInteractEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.useInteractedBlock() == Event.Result.DENY ||
                e.useItemInHand() == Event.Result.DENY || e.getAction() != Action.RIGHT_CLICK_BLOCK ||
                e.getHand() != EquipmentSlot.HAND || e.getClickedBlock() == null || ItemUtils.isEmpty(e.getPlayer().getInventory().getItemInMainHand()) ||
                e.getPlayer().getInventory().getItemInMainHand().getType() != Material.BRUSH || !LootListener.getPreparedExtraDrops(e.getClickedBlock()).isEmpty() ||
                !(e.getClickedBlock().getState() instanceof BrushableBlock brushable)) return;

        Block b = e.getClickedBlock();
        LootTable table = LootTableRegistry.getLootTable(b, b.getType());
        if (table == null && brushable.getLootTable() != null) table = LootTableRegistry.getLootTable(brushable.getLootTable().getKey());
        if (table == null) return;
        LootTableRegistry.setLootTable(b, null);
        AttributeInstance luckInstance = e.getPlayer().getAttribute(Attribute.GENERIC_LUCK);
        double luck = luckInstance == null ? 0 : luckInstance.getValue();
        LootContext context = new LootContext.Builder(b.getLocation()).killer(null).lootedEntity(e.getPlayer()).lootingModifier(0).luck((float) luck).build();
        List<ItemStack> loot = LootTableRegistry.getLoot(table, context, LootTable.LootType.ARCHAEOLOGY);
        ValhallaLootPopulateEvent loottableEvent = new ValhallaLootPopulateEvent(table, context, loot);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(loottableEvent);
        if (!loottableEvent.isCancelled()){
            switch (loottableEvent.getPreservationType()){
                case CLEAR -> brushable.setItem(null);
                case CLEAR_UNLESS_EMPTY -> {
                    if (!loottableEvent.getDrops().isEmpty()) brushable.setItem(null);
                }
                case KEEP -> {
                    if (loottableEvent.getDrops().isEmpty()) return;
                }
            }
            if (loottableEvent.getDrops().isEmpty()) return;
            brushable.setItem(loottableEvent.getDrops().get(0));
            loottableEvent.getDrops().remove(0);
            LootListener.prepareBlockDrops(b, loottableEvent.getDrops());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent e){
        if (e.getBlockReplacedState() instanceof BrushableBlock) System.out.println("replaced brushable block with " + e.getBlock().getType());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlace(BlockFromToEvent e){
        if (e.getBlock().getState() instanceof BrushableBlock) System.out.println("fromto brushable block with " + e.getBlock().getType());
    }
}
