package me.athlaeos.valhallammo.utility;

import com.jeff_media.customblockdata.CustomBlockData;
import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class BlockStore {
    private static final NamespacedKey blockPlacedKey = new NamespacedKey(ValhallaMMO.getInstance(), "block_placement_status");
    private static final Collection<Location> placedBlockCache = new HashSet<>();

    private static final Map<Location, BreakReason> breakReasonCache = new HashMap<>();

    /**
     * Returns true if the block has been placed, or false if it hasn't
     * @param b the block
     * @return true if placed, false if not
     */
    public static boolean isPlaced(Block b){
        if (placedBlockCache.contains(b.getLocation())) return true;
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        return customBlockData.has(blockPlacedKey, PersistentDataType.INTEGER);
    }

    /**
     * Sets the placement status of the block.
     * Skills involving the breaking of blocks should not reward the player if the block was placed
     * @param b the block to change its status of
     * @param placed the placement status
     */
    public static void setPlaced(Block b, boolean placed){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
//        if (placed){
//            customBlockData.set(blockPlacedKey, PersistentDataType.INTEGER, 1);
//            placedBlockCache.add(b.getLocation());
//        } else {
//            customBlockData.remove(blockPlacedKey);
//            placedBlockCache.remove(b.getLocation());
//        } todo persistent data containers not supported
    }

    /**
     * Returns the break reason of a block, if there is one.
     * @param b the block to check its break reason
     * @return returns NOT_BROKEN if the block is not broken, or if the reason was not specified.
     * returns EXPLOSION if the block was registered to be broken by an explosion
     * returns MINED if the block was registered to be mined by a player
     */
    public static BreakReason getBreakReason(Block b){
        return breakReasonCache.getOrDefault(b.getLocation(), BreakReason.NOT_BROKEN);
    }

    /**
     * Sets the break reason of the block. If null or {@link BreakReason#NOT_BROKEN} is used, it is removed instead.
     * @param b the block to set the break reason
     * @param reason the break reason
     */
    public static void setBreakReason(Block b, BreakReason reason){
        if (reason == null || reason == BreakReason.NOT_BROKEN){
            breakReasonCache.remove(b.getLocation());
        } else {
            breakReasonCache.put(b.getLocation(), reason);
        }
    }

    public enum BreakReason{
        EXPLOSION,
        MINED,
        NOT_BROKEN
    }
}
