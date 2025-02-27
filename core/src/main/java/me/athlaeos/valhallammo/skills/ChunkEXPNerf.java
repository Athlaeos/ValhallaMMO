package me.athlaeos.valhallammo.skills;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChunkEXPNerf {
    private static final int expEventNerfQuantity = ValhallaMMO.getPluginConfig().getInt("chunk_exp_nerf_quantity", 30);
    private static final double expEventNerfFactor = ValhallaMMO.getPluginConfig().getDouble("chunk_exp_nerf_factor", 0.1);
    private static final double expOrbsNerfFactor = ValhallaMMO.getPluginConfig().getDouble("chunk_exp_orbs_nerf_factor", 0.3);

    private static final Map<UUID, Map<String, Map<Integer, Integer>>> chunkEXPEventMap = new HashMap<>();
    private static final Map<String, Map<Integer, Integer>> genericChunkEventMap = new HashMap<>();

    public static boolean doesChunkEXPNerfApply(Chunk chunk, Player player, String key, int customLimit){
        return getCount(chunk, player, key) >= customLimit;
    }
    public static boolean doesChunkEXPNerfApply(Chunk chunk, Player player, String key){
        return doesChunkEXPNerfApply(chunk, player, key, expEventNerfQuantity);
    }
    public static double getChunkEXPNerf(Chunk chunk, Player player, String key){
        return getChunkEXPNerf(chunk, player, key, expEventNerfFactor);
    }
    public static double getChunkEXPNerf(Chunk chunk, Player player, String key, double customFactor){
        if (doesChunkEXPNerfApply(chunk, player, key)) return customFactor;
        return 1;
    }
    public static double getChunkEXPOrbsNerf(Chunk chunk, Player player, String key){
        return getChunkEXPOrbsNerf(chunk, player, key, expOrbsNerfFactor);
    }
    public static double getChunkEXPOrbsNerf(Chunk chunk, Player player, String key, double customFactor){
        if (doesChunkEXPNerfApply(chunk, player, key)) return customFactor;
        return 1;
    }

    public static void increment(Chunk chunk, Player player, String key){
        increment(chunk, player, key, 1);
    }

    public static void increment(Chunk chunk, Player player, String key, int quantity){
        Map<String, Map<Integer, Integer>> byKey = player == null ? genericChunkEventMap : chunkEXPEventMap.getOrDefault(player.getUniqueId(), new HashMap<>());
        Map<Integer, Integer> countMap = byKey.getOrDefault(key, new HashMap<>());
        int counter = countMap.getOrDefault(id(chunk), 0);
        counter += quantity;
        countMap.put(id(chunk), counter);
        byKey.put(key, countMap);
        if (player != null) chunkEXPEventMap.put(player.getUniqueId(), byKey);
    }

    public static int getCount(Chunk chunk, Player player, String key){
        Map<Integer, Integer> countMap = player == null ? genericChunkEventMap.getOrDefault(key, new HashMap<>()) : chunkEXPEventMap.getOrDefault(player.getUniqueId(), new HashMap<>()).getOrDefault(key, new HashMap<>());
        return countMap.getOrDefault(id(chunk), 0);
    }

    private static int id(Chunk chunk){
        return ((chunk.getX() & 0xFFF) << 8) | (chunk.getZ() & 0xFF);
    }
}
