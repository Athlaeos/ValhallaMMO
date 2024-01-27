package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.block.BlockDigProcess;
import me.athlaeos.valhallammo.block.DigPacketInfo;
import me.athlaeos.valhallammo.event.PrepareBlockBreakEvent;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.MathUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomBreakSpeedListener implements Listener {
    private static final boolean BLOCK_RECOVERY = ValhallaMMO.getPluginConfig().getBoolean("block_recovery", true);
    private static final int BLOCK_RECOVERY_DELAY = ValhallaMMO.getPluginConfig().getInt("block_recovery_delay", 60);
    private static final float BLOCK_RECOVERY_SPEED = (float) ValhallaMMO.getPluginConfig().getDouble("block_recovery_speed", 0.02F);

    private static boolean disabled = true;
    private static final Map<UUID, PotionEffect> previousFatigueEffects = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> previousFatigueEffectRemoved = new ConcurrentHashMap<>();
    private static final PotionEffect fatigueEffect = new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, -1, true, false, false);

    private static final Map<Block, BlockDigProcess> blockDigProcesses = new ConcurrentHashMap<>();
    private static final Map<Block, Collection<UUID>> totalMiningBlocks = new ConcurrentHashMap<>(); // total blocks currently being mined, contents will match all values of diggingPlayers combined
    private static final Map<UUID, Collection<Block>> miningPlayers = new ConcurrentHashMap<>();
    private static final Collection<Location> instantBlockBreaks = ConcurrentHashMap.newKeySet();

    @EventHandler
    public void onPrepareMining(PrepareBlockBreakEvent e){
        int[][] offsets = MathUtils.getOffsetsBetweenPoints(new int[]{-1, 0, -1}, new int[]{1, 0, 1});
        for (int[] offset : offsets){
            Block b = e.getBlock().getLocation().add(offset[0], offset[1], offset[2]).getBlock();
            if (e.getBlock().equals(b)) continue;
            e.getAdditionalBlocks().add(b);
        }
    }

    public CustomBreakSpeedListener(){
        disabled = false;

        new BukkitRunnable(){
            @Override
            public void run() {
                for (Block b : new HashSet<>(blockDigProcesses.keySet())){
                    BlockDigProcess process = blockDigProcesses.get(b);
                    if (totalMiningBlocks.containsKey(b)){
                        // block is in the process of being mined
                        for (UUID uuid : totalMiningBlocks.get(b)){
                            Player player = ValhallaMMO.getInstance().getServer().getPlayer(uuid);
                            if (player == null) continue;
                            process.damage(player, DigPacketInfo.damage(player, b));
                        }
                    } else if (BLOCK_RECOVERY) {
                        if (process.getTicksSinceUpdate() >= BLOCK_RECOVERY_DELAY){
                            process.heal(BLOCK_RECOVERY_SPEED);
                            if (process.getHealth() >= 1) blockDigProcesses.remove(b);
                        } else process.incrementTicksSinceUpdate();
                    } else {
                        totalMiningBlocks.remove(b);
                        blockDigProcesses.remove(b);
                    }
                }
            }
        }.runTaskTimer(ValhallaMMO.getInstance(), 1L, 1L);
    }

    public static void onStart(DigPacketInfo info){
        if (info == null || disabled || ItemUtils.breaksInstantly(info.getBlock().getType()) || info.getType() != DigPacketInfo.Type.START) return;
        Block b = info.getBlock();

        float initialDamage = instantBlockBreaks.contains(b.getLocation()) ? 999 : DigPacketInfo.damage(info.getDigger(), b);
        PrepareBlockBreakEvent event = new PrepareBlockBreakEvent(b, info.getDigger());
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) initialDamage = 0;
        if (initialDamage > 1){
            event.getAdditionalBlocks().add(b);
            event.getAdditionalBlocks().forEach(bl -> BlockDigProcess.breakBlockInstantly(info.getDigger(), bl));
            return;
        }

        fatiguePlayer(info.getDigger());

        BlockDigProcess process = blockDigProcesses.get(b);
        if (process == null) {
            process = new BlockDigProcess(b);
            blockDigProcesses.put(b, process);
        }
        Collection<UUID> playersMining = totalMiningBlocks.getOrDefault(b, new HashSet<>());
        playersMining.add(info.getDigger().getUniqueId());
        totalMiningBlocks.put(b, playersMining);
        process.damage(info.getDigger(), initialDamage + 0.01F);

        for (Block block : event.getAdditionalBlocks()){
            float damage = instantBlockBreaks.contains(b.getLocation()) ? 999 : DigPacketInfo.damage(info.getDigger(), b);
            BlockDigProcess p = blockDigProcesses.get(block);
            if (p == null) {
                p = new BlockDigProcess(block);
                blockDigProcesses.put(block, p);
            }
            Collection<UUID> mP = totalMiningBlocks.getOrDefault(block, new HashSet<>());
            mP.add(info.getDigger().getUniqueId());
            totalMiningBlocks.put(block, mP);
            p.damage(info.getDigger(), damage * 1.01F);
        }

        event.getAdditionalBlocks().add(b);
        miningPlayers.put(info.getDigger().getUniqueId(), event.getAdditionalBlocks());
    }

    public static void onStop(DigPacketInfo info){
        Player p = info.getDigger();
        if (!info.finished() || p == null || disabled || info.getType() == DigPacketInfo.Type.START) return;
        Collection<UUID> playersMining = totalMiningBlocks.getOrDefault(info.getBlock(), new HashSet<>());
        playersMining.remove(p.getUniqueId());
        if (playersMining.isEmpty()) totalMiningBlocks.remove(info.getBlock());
        else totalMiningBlocks.put(info.getBlock(), playersMining);
        miningPlayers.remove(p.getUniqueId());
        removeFatiguedPlayer(p);

        if (!BLOCK_RECOVERY){
            totalMiningBlocks.remove(info.getBlock());
            blockDigProcesses.remove(info.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent e){
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE || ItemUtils.breaksInstantly(e.getBlock().getType())) return;
        BlockDigProcess process = blockDigProcesses.get(e.getBlock());
        if (process != null){
            totalMiningBlocks.remove(e.getBlock());
            blockDigProcesses.remove(e.getBlock());
            return;
        }
        if (instantBlockBreaks.remove(e.getBlock().getLocation())) return;
        e.setCancelled(true);
    }

    public static void markInstantBreak(Block b){
        if (disabled) return;
        instantBlockBreaks.add(b.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent e){
        removeFatiguedPlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e){
        removeFatiguedPlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent e){
        removeFatiguedPlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent e){
        removeFatiguedPlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onResurrect(EntityResurrectEvent e){
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof Player p && miningPlayers.containsKey(p.getUniqueId())){
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> fatiguePlayer(p), 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMilk(PlayerItemConsumeEvent e){
        if (e.isCancelled()) return;
        if (miningPlayers.containsKey(e.getPlayer().getUniqueId())){
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> fatiguePlayer(e.getPlayer()), 1L);
        }
    }

    private static void fatiguePlayer(Player p){
        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
            PotionEffect previousEffect = p.getPotionEffect(PotionEffectType.SLOW_DIGGING);
            if (previousEffect != null && previousEffect.getAmplifier() < 5) {
                previousFatigueEffects.put(p.getUniqueId(), previousEffect);
                previousFatigueEffectRemoved.put(p.getUniqueId(), System.currentTimeMillis());
            }
            p.addPotionEffect(fatigueEffect);
        });
    }

    private static void removeFatiguedPlayer(Player p){
        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
            p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
            PotionEffect previousEffect = previousFatigueEffects.get(p.getUniqueId());
            if (previousEffect != null){
                int newDuration = previousEffect.getDuration() - (int) ((System.currentTimeMillis() - previousFatigueEffectRemoved.get(p.getUniqueId())) / 50D);
                if (newDuration > 0) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, newDuration, previousEffect.getAmplifier()));
                }
                previousFatigueEffectRemoved.remove(p.getUniqueId());
                previousFatigueEffects.remove(p.getUniqueId());
            }
        });
    }

    public static Map<Block, BlockDigProcess> getBlockDigProcesses() {
        return blockDigProcesses;
    }

    public static Map<Block, Collection<UUID>> getTotalMiningBlocks() {
        return totalMiningBlocks;
    }

    public static Map<UUID, Collection<Block>> getMiningPlayers() {
        return miningPlayers;
    }
}
