package me.athlaeos.valhallammo.utility;

import com.jeff_media.customblockdata.CustomBlockData;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.item.MaterialClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BlockUtils {
    private static final NamespacedKey BLOCK_OWNER = new NamespacedKey(ValhallaMMO.getInstance(), "block_owner");
    private static final NamespacedKey CUSTOM_HARDNESS = new NamespacedKey(ValhallaMMO.getInstance(), "custom_hardness");

    public static void setCustomHardness(Block b, float hardness){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        customBlockData.set(CUSTOM_HARDNESS, PersistentDataType.FLOAT, hardness);
    }

    public static float getHardness(Block b){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        return customBlockData.getOrDefault(CUSTOM_HARDNESS, PersistentDataType.FLOAT, b.getType().getHardness());
    }

    public static void removeCustomHardness(Block b){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        customBlockData.remove(CUSTOM_HARDNESS);
    }

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

    public static List<Block> getBlockVein(Block origin, int limit, Predicate<Block> filter, int[]... offsets){
        if (offsets.length == 0 || limit <= 0) return new ArrayList<>();
        Collection<Block> vein = new HashSet<>();
        Collection<Block> scanBlocks = new HashSet<>(Set.of(origin));
        getSurroundingBlocks(scanBlocks, vein, limit, filter, offsets);

        List<Block> ordered = new ArrayList<>(vein);
        ordered.sort(Comparator.comparingInt(b -> Utils.getManhattanDistance(b.getLocation(), origin.getLocation())));
        return ordered;
    }

    private static void getSurroundingBlocks(Collection<Block> scanBlocks, Collection<Block> currentVein, int limit, Predicate<Block> filter, int[]... offsets){
        Collection<Block> newBlocksToScan = new HashSet<>();
        if (currentVein.size() >= limit) return;

        for (Block b : scanBlocks){
            for (int[] offset : offsets){
                Block block = b.getLocation().add(offset[0], offset[1], offset[2]).getBlock();
                if (!filter.test(block) || currentVein.contains(block)) continue;
                currentVein.add(block);
                if (currentVein.size() >= limit) return;
                newBlocksToScan.add(block);
            }
        }
        if (newBlocksToScan.isEmpty()) return;
        getSurroundingBlocks(newBlocksToScan, currentVein, limit, filter, offsets);
    }

    private static final Map<String, Collection<UUID>> blockAlteringPlayers = new HashMap<>();

    public static Map<String, Collection<UUID>> getBlockAlteringPlayers() {
        return blockAlteringPlayers;
    }

    public static void processBlocks(Player responsible, List<Block> blocks, Predicate<Player> validation, Action<Block> process, Action<Block> onFinish){
        if (process == null || blocks.isEmpty()) return;
        for (Block b : blocks){
            if (validation != null && !validation.test(responsible)) continue;
            process.act(b);
        }
        if (onFinish != null) onFinish.act(blocks.get(blocks.size() - 1));
    }

    public static void processBlocksPulse(Player responsible, Block origin, List<Block> blocks, Predicate<Player> validation, Action<Block> process, Action<Block> onFinish){
        Map<Integer, List<Block>> sortedByDistance = new HashMap<>();
        for (Block b : blocks) {
            int distance = Utils.getManhattanDistance(origin.getLocation(), b.getLocation());
            List<Block> existingBlocks = sortedByDistance.getOrDefault(distance, new ArrayList<>());
            existingBlocks.add(b);
            sortedByDistance.put(distance, existingBlocks);
        }
        for (Integer distance : sortedByDistance.keySet()){
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> processBlocks(responsible, blocks, validation, process, null), (long) distance);
        }
        if (onFinish != null) onFinish.act(blocks.get(blocks.size() - 1));
    }
}
