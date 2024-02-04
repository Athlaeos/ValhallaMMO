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

    private static final Map<UUID, Map<Integer, Integer>> chunkEXPEventMap = new HashMap<>();

    public static double getChunkEXPNerf(Chunk chunk, Player player){
        if (getCount(chunk, player) >= expEventNerfQuantity) return expEventNerfFactor;
        return 1;
    }
    public static double getChunkEXPOrbsNerf(Chunk chunk, Player player){
        if (getCount(chunk, player) >= expEventNerfQuantity) return expOrbsNerfFactor;
        return 1;
    }

    public static void increment(Chunk chunk, Player player){
        increment(chunk, player, 1);
    }

    public static void increment(Chunk chunk, Player player, int quantity){
        Map<Integer, Integer> countMap = chunkEXPEventMap.getOrDefault(player.getUniqueId(), new HashMap<>());
        int counter = countMap.getOrDefault(id(chunk), 0);
        counter += quantity;
        countMap.put(id(chunk), counter);
        chunkEXPEventMap.put(player.getUniqueId(), countMap);
    }

    public static int getCount(Chunk chunk, Player player){
        Map<Integer, Integer> countMap = chunkEXPEventMap.getOrDefault(player.getUniqueId(), new HashMap<>());
        return countMap.getOrDefault(id(chunk), 0);
    }

    private static int id(Chunk chunk){
        return ((chunk.getX() & 0xFFF) << 8) | (chunk.getZ() & 0xFF);
    }
}
