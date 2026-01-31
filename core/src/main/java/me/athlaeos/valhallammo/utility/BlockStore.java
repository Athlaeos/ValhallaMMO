package me.athlaeos.valhallammo.utility;

import com.jeff_media.customblockdata.CustomBlockData;
import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class BlockStore {
    private static final NamespacedKey NEW_PLACED_FORMAT = new NamespacedKey(ValhallaMMO.getInstance(), "new_placed_format");
    private static final NamespacedKey LEGACY_BLOCK_PLACED_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "block_placement_status");

    private static final Collection<Location> PLACED_BLOCK_CACHE = new HashSet<>();

    /**
     * Returns true if the block has been placed, or false if it hasn't
     * @param block the block
     * @return true if placed, false if not
     */
    public static boolean isPlaced(Block block) {
        if (PLACED_BLOCK_CACHE.contains(block.getLocation())) {
            return true;
        }

        Chunk chunk = block.getChunk();
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        if (!pdc.has(NEW_PLACED_FORMAT, PersistentDataType.BOOLEAN)) {
            migrateBlockData(chunk, pdc);
        }
        return pdc.has(blockPlacedKey(block), PersistentDataType.BOOLEAN);
    }

    /**
     * Sets the placement status of the block.
     * Skills involving the breaking of blocks should not reward the player if the block was placed
     * @param block the block to change its status of
     * @param placed the placement status
     */
    public static void setPlaced(Block block, boolean placed) {
        Location location = block.getLocation();
        if (placed && PLACED_BLOCK_CACHE.contains(location)) {
            return;
        }

        Chunk chunk = block.getChunk();
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        if (!pdc.has(NEW_PLACED_FORMAT, PersistentDataType.BOOLEAN)) {
            migrateBlockData(chunk, pdc);
        }

        NamespacedKey blockKey = blockPlacedKey(block);
        if (placed) {
            pdc.set(blockKey, PersistentDataType.BOOLEAN, true);
            PLACED_BLOCK_CACHE.add(block.getLocation());
        } else {
            pdc.remove(blockKey);
            PLACED_BLOCK_CACHE.remove(block.getLocation());
        }
    }

    public static NamespacedKey blockPlacedKey(Block block) {
        return new NamespacedKey(ValhallaMMO.getInstance(), "placed_" + (block.getX() & 0x000F) + "_" + block.getY() + "_" + (block.getZ() & 0x000F));
    }

    private static void migrateBlockData(Chunk chunk, PersistentDataContainer pdc) {
        pdc.set(NEW_PLACED_FORMAT, PersistentDataType.BOOLEAN, true);
        for (Block block : CustomBlockData.getBlocksWithCustomData(ValhallaMMO.getInstance(), chunk)) {
            PersistentDataContainer blockPdc = new CustomBlockData(block, ValhallaMMO.getInstance());
            if (blockPdc.has(LEGACY_BLOCK_PLACED_KEY, PersistentDataType.INTEGER)) {
                pdc.set(blockPlacedKey(block), PersistentDataType.INTEGER, 1);
            }
        }
    }

    public enum BreakReason {
        EXPLOSION,
        MINED,
        NOT_BROKEN
    }
}
