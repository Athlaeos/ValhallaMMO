package me.athlaeos.valhallammo.hooks;

import me.athlaeos.valhallammo.dom.CustomPlacedBlock;
import me.athlaeos.valhallammo.event.CustomBlockPlaceEvent;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class CEWrapper implements Listener {

    public static String getCraftEngineItemID(ItemStack item) {
        Key id = CraftEngineItems.getCustomItemId(item);
        return id == null ? null : id.asString();
    }

    public static ItemStack getCraftEngineItem(String id) {
        BukkitItemDefinition definition = CraftEngineItems.byId(id);
        if (definition == null) return null;
        return definition.buildBukkitItem();
    }

    public static String getCraftEngineBlock(Block block) {
        ImmutableBlockState state = CraftEngineBlocks.getCustomBlockState(block);
        return state == null ? null : state.owner().value().id().asString();
    }

    public static boolean setCraftEngineBlock(Block block, String id) {
        return CraftEngineBlocks.place(block.getLocation(), Key.of(id), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCustomBlockPlace(net.momirealms.craftengine.bukkit.api.event.CustomBlockPlaceEvent event) {
        Block block = event.bukkitBlock();
        String id = event.customBlock().id().asString();
        CustomPlacedBlock placedBlock = new CustomPlacedBlock(id, block);
        Bukkit.getPluginManager().callEvent(new CustomBlockPlaceEvent(event.getPlayer(), placedBlock));
    }
}
