package me.athlaeos.valhallammo.utility;

import com.jeff_media.customblockdata.CustomBlockData;
import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

public class BlockUtils {
    private static final NamespacedKey BLOCK_OWNER = new NamespacedKey(ValhallaMMO.getInstance(), "block_owner");

    public static void setOwner(Block b, UUID owner){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        customBlockData.set(BLOCK_OWNER, PersistentDataType.STRING, owner.toString());
    }

    public static boolean hasOwnership(Block b){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        return customBlockData.has(BLOCK_OWNER, PersistentDataType.STRING);
    }

    public static void removeOwnership(Block b){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        customBlockData.remove(BLOCK_OWNER);
    }

    public static Player getOwner(Block b){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        String value = customBlockData.get(BLOCK_OWNER, PersistentDataType.STRING);
        if (value != null) return ValhallaMMO.getInstance().getServer().getPlayer(UUID.fromString(value));
        return null;
    }
    private static final int[][] offsets = new int[][]{
            {1, 0, 0},
            {-1, 0, 0},
            {0, 1, 0},
            {0, -1, 0},
            {0, 0, 1},
            {0, 0, -1}
    };

    public static Collection<Block> getBlocksTouching(Block start, int radiusX, int radiusY, int radiusZ, Material... touching){
        Collection<Block> blocks = new HashSet<>();
        for(double x = start.getLocation().getX() - radiusX; x <= start.getLocation().getX() + radiusX; x++){
            for(double y = start.getLocation().getY() - radiusY; y <= start.getLocation().getY() + radiusY; y++){
                for(double z = start.getLocation().getZ() - radiusZ; z <= start.getLocation().getZ() + radiusZ; z++){
                    Location loc = new Location(start.getWorld(), x, y, z);
                    blocks.add(loc.getBlock());
                }
            }
        }
        if (touching.length == 0) return blocks;
        return blocks.stream().filter(block -> {
            for (int[] offset : offsets){
                Location l = block.getLocation().add(offset[0], offset[1], offset[2]);
                if (Arrays.asList(touching).contains(l.getBlock().getType())) return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    public static Collection<Block> getBlocksTouchingAnything(Block start, int radiusX, int radiusY, int radiusZ){
        return getBlocksTouching(start, radiusX, radiusY, radiusZ).stream().filter(block -> {
            for (int[] offset : offsets){
                Location l = block.getLocation().add(offset[0], offset[1], offset[2]);
                if (!l.getBlock().getType().isAir()) return true;
            }
            return false;
        }).collect(Collectors.toList());
    }
}
