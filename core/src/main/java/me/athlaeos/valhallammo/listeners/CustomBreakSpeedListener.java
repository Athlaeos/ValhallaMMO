package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.block.BlockDigProcess;
import me.athlaeos.valhallammo.block.DigPacketInfo;
import me.athlaeos.valhallammo.event.PrepareBlockBreakEvent;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CustomBreakSpeedListener implements Listener {
    private static final boolean BLOCK_RECOVERY = ValhallaMMO.getPluginConfig().getBoolean("block_recovery", true);
    private static final int BLOCK_RECOVERY_DELAY = ValhallaMMO.getPluginConfig().getInt("block_recovery_delay", 60);
    private static final float BLOCK_RECOVERY_SPEED = (float) ValhallaMMO.getPluginConfig().getDouble("block_recovery_speed", 0.02F);
    private static final boolean VANILLA_BLOCK_BREAK_DELAY = ValhallaMMO.getPluginConfig().getBoolean("block_break_delay", true);
    public static boolean isBlockRecovery() { return BLOCK_RECOVERY; }
    public static int getBlockRecoveryDelay() { return BLOCK_RECOVERY_DELAY; }
    public static float getBlockRecoverySpeed() { return BLOCK_RECOVERY_SPEED; }
    public static boolean isVanillaBlockBreakDelay() { return VANILLA_BLOCK_BREAK_DELAY; }

    private static boolean disabled = true;
    private static final PotionEffect fatigueEffect = new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, -1, false, false, false);

    private static final Map<Location, BlockDigProcess> blockDigProcesses = new ConcurrentHashMap<>();
    private static final Map<Location, Collection<UUID>> totalMiningBlocks = new ConcurrentHashMap<>(); // total blocks currently being mined, contents will match all values of diggingPlayers combined
    private static final Map<UUID, Collection<Location>> miningPlayers = new ConcurrentHashMap<>();
    private static final Collection<Location> instantBlockBreaks = ConcurrentHashMap.newKeySet();

    public CustomBreakSpeedListener(){
        disabled = false;

        new BukkitRunnable(){
            @Override
            public void run() {
                for (Location l : new HashSet<>(blockDigProcesses.keySet())){
                    Block b = l.getBlock();
                    BlockDigProcess process = blockDigProcesses.get(l);
                    if (totalMiningBlocks.containsKey(l)){
                        // block is in the process of being mined
                        for (UUID uuid : totalMiningBlocks.get(l)){
                            Player player = ValhallaMMO.getInstance().getServer().getPlayer(uuid);
                            if (player == null) continue;
                            float dmg = DigPacketInfo.damage(player, b);
                            process.damage(player, dmg);
                        }
                    } else if (BLOCK_RECOVERY) {
                        if (process.getTicksSinceUpdate() >= BLOCK_RECOVERY_DELAY){
                            process.heal(BLOCK_RECOVERY_SPEED);
                            if (process.getHealth() >= 1) blockDigProcesses.remove(l);
                        } else process.incrementTicksSinceUpdate();
                    } else {
                        totalMiningBlocks.remove(l);
                        blockDigProcesses.remove(l);
                    }
                }
            }
        }.runTaskTimer(ValhallaMMO.getInstance(), 1L, 1L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onStart(BlockDamageEvent e){
        if (ItemUtils.breaksInstantly(e.getBlock().getType()) ||
                ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName())) return;
        e.setCancelled(true);
    }

    public static void onStart(DigPacketInfo info){
        if (info == null || disabled || ItemUtils.breaksInstantly(info.getBlock().getType()) || info.getType() != DigPacketInfo.Type.START ||
                ValhallaMMO.isWorldBlacklisted(info.getBlock().getWorld().getName())) return;
        Block b = info.getBlock();
        DigPacketInfo.resetBlockSpecificCache(info.getDigger().getUniqueId());

        float initialDamage = instantBlockBreaks.contains(b.getLocation()) ? 999 : DigPacketInfo.damage(info.getDigger(), b);
        PrepareBlockBreakEvent event = new PrepareBlockBreakEvent(b, info.getDigger());
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) initialDamage = 0;
        if (initialDamage >= 1) {
            BlockDigProcess.breakBlockInstantly(info.getDigger(), b);
            if (event.getAdditionalBlocks().isEmpty()) return;
        } else {
            BlockDigProcess process = blockDigProcesses.get(b.getLocation());
            if (process == null) {
                process = new BlockDigProcess(b);
                blockDigProcesses.put(b.getLocation(), process);
            }
            Collection<UUID> playersMining = totalMiningBlocks.getOrDefault(b.getLocation(), new HashSet<>());
            playersMining.add(info.getDigger().getUniqueId());
            totalMiningBlocks.put(b.getLocation(), playersMining);
            process.damage(info.getDigger(), initialDamage);
        }

        for (Block block : event.getAdditionalBlocks()){
            float damage = instantBlockBreaks.contains(b.getLocation()) ? 999 : DigPacketInfo.damage(info.getDigger(), b);
            if (damage >= 1) BlockDigProcess.breakBlockInstantly(info.getDigger(), block);
            else {
                BlockDigProcess p = blockDigProcesses.get(block.getLocation());
                if (p == null) {
                    p = new BlockDigProcess(block);
                    blockDigProcesses.put(block.getLocation(), p);
                }
                Collection<UUID> mP = totalMiningBlocks.getOrDefault(block.getLocation(), new HashSet<>());
                mP.add(info.getDigger().getUniqueId());
                totalMiningBlocks.put(block.getLocation(), mP);
                p.damage(info.getDigger(), damage * 1.01F);
            }
        }

        event.getAdditionalBlocks().add(b);
        miningPlayers.put(info.getDigger().getUniqueId(), event.getAdditionalBlocks().stream().map(Block::getLocation).collect(Collectors.toSet()));
    }

    public static void onStop(DigPacketInfo info){
        Player p = info.getDigger();
        if (!info.finished() || p == null || disabled || info.getType() == DigPacketInfo.Type.START ||
                ValhallaMMO.isWorldBlacklisted(info.getBlock().getWorld().getName())) return;
        Collection<UUID> playersMining = totalMiningBlocks.getOrDefault(info.getBlock().getLocation(), new HashSet<>());
        playersMining.remove(p.getUniqueId());
        if (playersMining.isEmpty()) totalMiningBlocks.remove(info.getBlock().getLocation());
        else totalMiningBlocks.put(info.getBlock().getLocation(), playersMining);
        for (Location b : miningPlayers.getOrDefault(p.getUniqueId(), new HashSet<>())) {
            Collection<UUID> miningPlayers = totalMiningBlocks.getOrDefault(b, new HashSet<>());
            miningPlayers.remove(p.getUniqueId());
            if (miningPlayers.isEmpty()) totalMiningBlocks.remove(b);
            else totalMiningBlocks.put(b, miningPlayers);
        }
        miningPlayers.remove(p.getUniqueId());

        if (!BLOCK_RECOVERY){
            totalMiningBlocks.remove(info.getBlock().getLocation());
            blockDigProcesses.remove(info.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent e){
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE || ItemUtils.breaksInstantly(e.getBlock().getType()) ||
                ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName())) return;
        if (!e.getPlayer().hasPotionEffect(PotionEffectType.SLOW_DIGGING)){
            e.setCancelled(true);
            return;
        }
        BlockDigProcess process = blockDigProcesses.get(e.getBlock().getLocation());
        if (process != null){
            totalMiningBlocks.remove(e.getBlock().getLocation());
            blockDigProcesses.remove(e.getBlock().getLocation());
            return;
        }
        if (instantBlockBreaks.remove(e.getBlock().getLocation())) return;
        if (!totalMiningBlocks.containsKey(e.getBlock().getLocation())) return;
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
        fatiguePlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent e){
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> fatiguePlayer(e.getPlayer()), 2L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPotionEffect(EntityPotionEffectEvent e){
        if (e.isCancelled() || e.getCause() == EntityPotionEffectEvent.Cause.PLUGIN || !(e.getEntity() instanceof Player p)) return;
        if (e.getOldEffect() != null && (e.getOldEffect().getType() == PotionEffectType.FAST_DIGGING || e.getOldEffect().getType() == PotionEffectType.SLOW_DIGGING))
            DigPacketInfo.resetMinerCache(e.getEntity().getUniqueId());
        if (e.getNewEffect() != null && (e.getNewEffect().getType() == PotionEffectType.FAST_DIGGING || e.getNewEffect().getType() == PotionEffectType.SLOW_DIGGING))
            DigPacketInfo.resetMinerCache(e.getEntity().getUniqueId());
        if ((e.getAction() == EntityPotionEffectEvent.Action.ADDED || e.getAction() == EntityPotionEffectEvent.Action.CHANGED) &&
                e.getNewEffect() != null && e.getNewEffect().getType() == PotionEffectType.SLOW_DIGGING && e.getNewEffect().getAmplifier() >= 0) {
            // change in mining fatigue effect
            e.setOverride(true);
        } else if ((e.getAction() == EntityPotionEffectEvent.Action.REMOVED || e.getAction() == EntityPotionEffectEvent.Action.CLEARED) &&
                e.getOldEffect() != null && e.getOldEffect().getType() == PotionEffectType.SLOW_DIGGING && (e.getOldEffect().getAmplifier() < 0 || e.getOldEffect().getAmplifier() > 4)) {
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> fatiguePlayer(p), 2L);
        }
    }

    private static void fatiguePlayer(Player p){
        p.addPotionEffect(fatigueEffect);
    }

    public static void removeFatiguedPlayer(Player p){
        PotionEffect effect = p.getPotionEffect(PotionEffectType.SLOW_DIGGING);
        if (effect != null && effect.getAmplifier() >= 0 && effect.getAmplifier() < 5) return;
        p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
    }

    public static Map<Location, BlockDigProcess> getBlockDigProcesses() {
        return blockDigProcesses;
    }

    public static Map<Location, Collection<UUID>> getTotalMiningBlocks() {
        return totalMiningBlocks;
    }

    public static Map<UUID, Collection<Location>> getMiningPlayers() {
        return miningPlayers;
    }
}
