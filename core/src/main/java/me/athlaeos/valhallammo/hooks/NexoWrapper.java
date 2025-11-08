package me.athlaeos.valhallammo.hooks;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.custom_block.NexoBlockPlaceEvent;
import com.nexomc.nexo.items.ItemBuilder;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.CustomPlacedBlock;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.stream.Collectors;

public class NexoWrapper implements Listener {
    public static String getNexoItemID(ItemStack item){
        return NexoItems.idFromItem(item);
    }

    public static ItemStack getNexoItem(String type){
        ItemBuilder item = NexoItems.itemFromId(type);
        return item == null ? null : item.getFinalItemStack();
    }

    public static String getNexoBlock(Block block){
        CustomBlockMechanic mechanic = NexoBlocks.customBlockMechanic(block);
        return mechanic == null ? null : mechanic.getItemID();
    }

    public static boolean setNexoBlock(Block block, String type){
//        if (!NexoBlocks.isCustomBlock(type)) return false;
        CustomBlockMechanic mechanic = NexoBlocks.customBlockMechanic(type);
        if (mechanic == null) {
            System.out.println("no mechanic found under name \n" + type);
            System.out.println("could be one of: " + String.join("\n", NexoBlocks.blockIDs()));
            return false;
        } else System.out.println("successfully found block under type " + type);
        NexoBlocks.place(type, block.getLocation());
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCustomBlockPlace(NexoBlockPlaceEvent e){
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(
                new me.athlaeos.valhallammo.event.CustomBlockPlaceEvent(e.getPlayer(), new CustomPlacedBlock(e.getMechanic().getItemID(), e.getBlock()))
        );
    }
}
