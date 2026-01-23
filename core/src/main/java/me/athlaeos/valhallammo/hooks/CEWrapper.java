package me.athlaeos.valhallammo.hooks;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.CustomPlacedBlock;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockPlaceEvent;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.world.BukkitExistingBlock;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.item.BuildableItem;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class CEWrapper implements Listener {
    public static String getCraftEngineItemID(ItemStack item){
        CustomItem<?> customItem = CraftEngineItems.byItemStack(item);
        System.out.println("item detected " + (customItem == null ? null : customItem.id().asString()));
        return customItem == null ? null : customItem.id().asString();
    }

    public static ItemStack getCraftEngineItem(String type){
        Optional<BuildableItem<ItemStack>> item = BukkitItemManager.instance().getVanillaItem(Key.from(type));
        return item.map(BuildableItem::buildItemStack).orElse(null);
    }

    public static String getCraftEngineBlock(Block block){
        BukkitExistingBlock existingBlock = new BukkitExistingBlock(block);
        CustomBlock customBlock = existingBlock.customBlock();
        System.out.println("block detected: " + (customBlock == null ? null : customBlock.id().asString()));
        return customBlock == null ? null : customBlock.id().asString();
    }

    public static boolean setCraftEngineBlock(Block block, String type){
        CustomBlock customBlock = CraftEngineBlocks.byId(Key.from(type));
        if (customBlock == null) return false;
        return CraftEngineBlocks.place(block.getLocation(), Key.of(type), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCustomBlockPlace(CustomBlockPlaceEvent e){
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(
                new me.athlaeos.valhallammo.event.CustomBlockPlaceEvent(e.getPlayer(), new CustomPlacedBlock(e.customBlock().id().asString(), e.bukkitBlock()))
        );
    }
}
