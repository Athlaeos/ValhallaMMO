package me.athlaeos.valhallammo.trading.happiness.sources;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.happiness.HappinessSource;
import me.athlaeos.valhallammo.utility.BlockUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;

import java.util.*;

public class Space implements HappinessSource, Listener {
    private final int freeSpace = CustomMerchantManager.getTradingConfig().getInt("free_space_requirement", 64);
    private final int imprisonedSpace = CustomMerchantManager.getTradingConfig().getInt("imprisonment_space_max", 5);
    private final float freeHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.free", 1);
    private final float imprisonedHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.imprisoned", -5);

    private final Map<UUID, Float> happinessCache = new HashMap<>();

    public Space(){
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    @Override
    public String id() {
        return "FREEDOM";
    }

    @EventHandler
    public void onRestock(VillagerReplenishTradeEvent e){
        happinessCache.remove(e.getEntity().getUniqueId());
    }

    private final int[][] freedomScanArea = new int[][]{
            {-1, -1, 0}, {-1, 0, 0}, {-1, 1, 0},
            {1, -1, 0}, {1, 0, 0}, {1, 1, 0},
            {0, -1, -1}, {0, 0, -1}, {0, 1, -1},
            {0, -1, 1}, {0, 0, 1}, {0, 1, 1}
    };

    private boolean isFreeBlock(Block b){
        if (!b.getType().isSolid() && !b.getType().isOccluding()) return false; // a non-solid non-occluding block is not considered free space
        Block oneAbove = b.getLocation().add(0, 1, 0).getBlock();
        Block twoAbove = b.getLocation().add(0, 2, 0).getBlock();
        Block threeAbove = b.getLocation().add(0, 3, 0).getBlock();
        if ((oneAbove.getType().isAir() || !oneAbove.getType().isOccluding()) && (twoAbove.getType().isAir() || !twoAbove.getType().isOccluding()) &&
                (threeAbove.getType().isAir() || !threeAbove.getType().isOccluding())) return true;
        if (oneAbove.getType().toString().endsWith("_DOOR") && oneAbove.getType() != Material.IRON_DOOR) {
            // for regular doors, a passage needs to be free through it
            Block north1 = oneAbove.getRelative(BlockFace.NORTH);
            Block south1 = oneAbove.getRelative(BlockFace.SOUTH);
            if ((north1.getType().isAir() || !north1.getType().isOccluding()) && (south1.getType().isAir() || !south1.getType().isOccluding())) {
                Block north2 = twoAbove.getRelative(BlockFace.NORTH);
                Block south2 = twoAbove.getRelative(BlockFace.SOUTH);
                return (north2.getType().isAir() || !north2.getType().isOccluding()) && (south2.getType().isAir() || !south2.getType().isOccluding());
            }
            Block east1 = oneAbove.getRelative(BlockFace.EAST);
            Block west1 = oneAbove.getRelative(BlockFace.WEST);
            if ((east1.getType().isAir() || !east1.getType().isOccluding()) && (west1.getType().isAir() || !west1.getType().isOccluding())) {
                Block east2 = twoAbove.getRelative(BlockFace.EAST);
                Block west2 = twoAbove.getRelative(BlockFace.WEST);
                return (east2.getType().isAir() || !east2.getType().isOccluding()) && (west2.getType().isAir() || !west2.getType().isOccluding());
            }
        }
        return false;
    }

    @Override
    public float get(Player contextPlayer, Entity entity) {
        if (happinessCache.containsKey(entity.getUniqueId())) return happinessCache.get(entity.getUniqueId());
        Collection<Block> vein = BlockUtils.getBlockVein(entity.getLocation().getBlock().getRelative(BlockFace.DOWN), freeSpace + 1, this::isFreeBlock, freedomScanArea);
        float happiness = 0;

        if (vein.size() >= freeSpace) happiness = freeHappiness;
        else if (vein.size() <= imprisonedSpace) happiness = imprisonedHappiness;
        happinessCache.put(entity.getUniqueId(), happiness);
        return happiness;
    }

    @Override
    public boolean appliesTo(Entity entity) {
        return entity instanceof LivingEntity;
    }
}
