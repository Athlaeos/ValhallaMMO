package me.athlaeos.valhallammo.hooks;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.CustomPlacedBlock;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class IAWrapper implements Listener {
    public static String getItemsAdderItemID(ItemStack item){
        CustomStack customStack = CustomStack.byItemStack(item);
        return customStack == null ? null : customStack.getId();
    }

    public static ItemStack getItemsAdderItem(String type){
        CustomStack item = CustomStack.getInstance(type);
        return item == null ? null : item.getItemStack();
    }

    public static String getItemsAdderBlock(Block block){
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        return customBlock == null ? null : customBlock.getId();
    }

    public static boolean setItemsAdderBlock(Block block, String type){
        CustomBlock customBlock = CustomBlock.getInstance(type);
        if (customBlock == null) return false;
        return customBlock.place(block.getLocation()) != null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCustomBlockPlace(CustomBlockPlaceEvent e){
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(
                new me.athlaeos.valhallammo.event.CustomBlockPlaceEvent(e.getPlayer(), new CustomPlacedBlock(e.getNamespacedID(), e.getBlock(), e.getReplacedBlockState()))
        );
    }
}
