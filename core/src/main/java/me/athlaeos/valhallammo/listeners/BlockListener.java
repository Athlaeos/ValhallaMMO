package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.BlockStore;
import me.athlaeos.valhallammo.utility.BlockUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class BlockListener implements Listener {

    public BlockListener(){
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskTimer(ValhallaMMO.getInstance(), () -> {
            for (Location b : blocksToConsiderBroken) {
                BlockStore.setPlaced(b.getBlock(), false);
                BlockStore.setBreakReason(b.getBlock(), BlockStore.BreakReason.MINED);
            }
            blocksToConsiderBroken.clear();
        }, 0L, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFallBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock fallingBlock) {
            Location loc = event.getEntity().getLocation();
            BlockStore.setPlaced(loc.getBlock(), event.getTo() == fallingBlock.getBlockData().getMaterial());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName())) return;
        BlockStore.setPlaced(e.getBlock(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onStructureForm(StructureGrowEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getWorld().getName())) return;
        for (BlockState b : e.getBlocks()){
            BlockStore.setPlaced(b.getBlock(), false);
        }
    }

    @EventHandler(priority =EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName())) return;
        if (e.getBlock().getBlockData() instanceof Directional d){
            for (Block b : e.getBlocks().stream().map(b -> b.getRelative(d.getFacing())).collect(Collectors.toSet())){
                BlockStore.setPlaced(b, true);
                blocksToConsiderBroken.remove(b.getLocation());
            }
        }
    }

    @EventHandler(priority =EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName())) return;
        if (e.getBlock().getBlockData() instanceof Directional d){
            for (Block b : e.getBlocks().stream().map(b -> b.getRelative(d.getFacing().getOppositeFace())).collect(Collectors.toSet())){
                BlockStore.setPlaced(b, true);
                blocksToConsiderBroken.remove(b.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (e instanceof BlockMultiPlaceEvent me){
            me.getReplacedBlockStates().forEach(b -> BlockStore.setPlaced(b.getBlock(), true));
        } else {
            if (e.getBlock().getBlockData() instanceof Bisected bisected) {
                if (bisected.getHalf() == Bisected.Half.TOP) {
                    BlockStore.setPlaced(e.getBlock().getRelative(BlockFace.DOWN), true);
                    blocksToConsiderBroken.remove(e.getBlock().getRelative(BlockFace.DOWN).getLocation());
                } else {
                    BlockStore.setPlaced(e.getBlock().getRelative(BlockFace.UP), true);
                    blocksToConsiderBroken.remove(e.getBlock().getRelative(BlockFace.UP).getLocation());
                }
            } else {
                BlockStore.setPlaced(e.getBlock(), true);
                blocksToConsiderBroken.remove(e.getBlock().getLocation());
            }
        }
    }

    private static final Collection<Location> blocksToConsiderBroken = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName())) return;
        if (e.getBlock().getBlockData() instanceof Bisected bisected) {
            if (bisected.getHalf() == Bisected.Half.TOP) {
                Block down = e.getBlock().getRelative(BlockFace.DOWN);
                blocksToConsiderBroken.add(down.getLocation());
            } else {
                Block up = e.getBlock().getRelative(BlockFace.UP);
                blocksToConsiderBroken.add(up.getLocation());
            }
        } else {
            blocksToConsiderBroken.add(e.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getEntity() instanceof TNTPrimed tnt) || tnt.getSource() == null) return;
        Player responsible = null;
        if (tnt.getSource() instanceof Player p) responsible = p;
        else if (tnt.getSource() instanceof AbstractArrow a && a.getShooter() instanceof Player p) responsible = p;
        if (responsible == null) return;
        double multiplier = AccumulativeStatManager.getCachedStats("EXPLOSION_RADIUS_MULTIPLIER", responsible, 10000, true);
        e.setRadius((float) (e.getRadius() * (1 + multiplier)));
    }

    private static final float EXPLOSION_IMMUNE_HARDNESS = 10;
    private static final float EXPLOSION_IMMUNE_BLAST_RESISTANCE = 30;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExplosion(BlockExplodeEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName())) return;
        e.blockList().removeIf(b -> {
            float hardness = BlockUtils.getHardness(b);
            return hardness < 0 || hardness >= EXPLOSION_IMMUNE_HARDNESS || b.getType().getBlastResistance() >= EXPLOSION_IMMUNE_BLAST_RESISTANCE;
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName())) return;
        e.blockList().removeIf(b -> {
            float hardness = BlockUtils.getHardness(b);
            return hardness < 0 || hardness >= EXPLOSION_IMMUNE_HARDNESS || b.getType().getBlastResistance() >= EXPLOSION_IMMUNE_BLAST_RESISTANCE;
        });
    }
}
