package me.athlaeos.valhallammo.version;

import com.jeff_media.customblockdata.CustomBlockData;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.event.ValhallaLootPopulateEvent;
import me.athlaeos.valhallammo.listeners.LootListener;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrushableBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ArchaeologyListener implements Listener {
    private static final NamespacedKey PREVENT_CUSTOM_GENERATION = new NamespacedKey(ValhallaMMO.getInstance(), "generated_custom_archaeology");
    private static final Collection<String> brushable = Set.of("SUSPICIOUS_GRAVEL", "SUSPICIOUS_SAND");
    private static final Map<Block, org.bukkit.loot.LootTable> suspiciousLootTables = new HashMap<>();

    public static boolean isBrushable(Material material){
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20)) return brushable.contains(material.toString());
        return false;
    }
    public static boolean isBrushable(BlockState state){
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20)) return state instanceof BrushableBlock;
        return false;
    }

    public static Map<Block, org.bukkit.loot.LootTable> getSuspiciousLootTables() {
        return suspiciousLootTables;
    }

    public static void setPreventRepeatGeneration(Block b, boolean prevent){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        if (prevent) customBlockData.set(PREVENT_CUSTOM_GENERATION, PersistentDataType.BYTE, (byte) 0);
        else customBlockData.remove(PREVENT_CUSTOM_GENERATION);
    }

    public static boolean preventRepeatGeneration(Block b){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        return customBlockData.has(PREVENT_CUSTOM_GENERATION, PersistentDataType.STRING);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBrush(PlayerInteractEvent e){
        Block b = e.getClickedBlock();
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.useInteractedBlock() == Event.Result.DENY ||
                e.useItemInHand() == Event.Result.DENY || e.getAction() != Action.RIGHT_CLICK_BLOCK ||
                e.getHand() != EquipmentSlot.HAND || b == null || ItemUtils.isEmpty(e.getPlayer().getInventory().getItemInMainHand()) ||
                e.getPlayer().getInventory().getItemInMainHand().getType() != Material.BRUSH || !LootListener.getPreparedExtraDrops(b).isEmpty() ||
                !(b.getState() instanceof BrushableBlock brushable)) return;

        if (setCustomArchaeologyDrops(b, e.getPlayer(), brushable)) e.setCancelled(true);
    }

    public static boolean setCustomArchaeologyDrops(Block b, Player p, BrushableBlock brushable){
        if (brushable.getLootTable() != null) suspiciousLootTables.put(b.getLocation().getBlock(), brushable.getLootTable());

        LootTable table = LootTableRegistry.getLootTable(b, b.getType());
        if (table == null && brushable.getLootTable() != null) table = LootTableRegistry.getLootTable(brushable.getLootTable().getKey());
        if (table == null || preventRepeatGeneration(b)) return false;

        LootTableRegistry.setLootTable(b, null);
        setPreventRepeatGeneration(b, true);
        AttributeInstance luckInstance = p.getAttribute(Attribute.GENERIC_LUCK);
        double luck = luckInstance == null ? 0 : luckInstance.getValue();
        luck += AccumulativeStatManager.getCachedStats("DIGGING_ARCHAEOLOGY_LUCK", p, 10000, true);
        LootContext context = new LootContext.Builder(b.getLocation()).killer(null).lootedEntity(p).lootingModifier(0).luck((float) luck).build();

        if (prepareArchaeologyBrushing(b, table, context)) {
            brushable.setItem(null);
            List<ItemStack> extraDrops = LootListener.getPreparedExtraDrops(b);
            if (!extraDrops.isEmpty()){
                brushable.setItem(extraDrops.get(0));
                extraDrops.remove(0);
                brushable.update(true, false);
            }
        } else return false;

        if (brushable.getLootTable() != null){
            // setting the item doesn't work if the block is already being brushed, so we cancel the first interaction instance
            // so that the item can be set and we remove the loot table afterwards. that way the item is set and it can be brushed out
            brushable.setLootTable(null);
            brushable.update();
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBrush(BlockDropItemEvent e){
        if (e.getBlockState() instanceof BrushableBlock) {
            BlockFace face = EntityUtils.getSelectedBlockFace(e.getPlayer());
            List<ItemStack> extraDrops = LootListener.getPreparedExtraDrops(e.getBlock());
            for (ItemStack i : extraDrops)
                e.getBlock().getWorld().dropItem(face == null ? e.getBlock().getLocation() : e.getBlock().getRelative(face).getLocation().add(0.5, 0.5, 0.5), i);
            LootListener.clear(e.getBlock());
        }
    }

    private static boolean prepareArchaeologyBrushing(Block b, LootTable table, LootContext context){
        LootTableRegistry.setLootTable(b, null);
        List<ItemStack> generatedLoot = LootTableRegistry.getLoot(table, context, LootTable.LootType.ARCHAEOLOGY);
        ValhallaLootPopulateEvent loottableEvent = new ValhallaLootPopulateEvent(table, context, generatedLoot);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(loottableEvent);
        if (!loottableEvent.isCancelled()){
            LootListener.prepareBlockDrops(b, loottableEvent.getDrops());
            return switch (loottableEvent.getPreservationType()){
                case CLEAR -> true;
                case KEEP -> false;
                case CLEAR_UNLESS_EMPTY -> !loottableEvent.getDrops().isEmpty();
            };
        }
        return false;
    }
}
