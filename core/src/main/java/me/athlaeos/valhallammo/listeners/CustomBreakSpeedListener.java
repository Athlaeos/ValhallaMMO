package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.block.BlockDigProcess;
import me.athlaeos.valhallammo.block.DigPacketInfo;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.event.PrepareBlockBreakEvent;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.version.AttributeMappings;
import me.athlaeos.valhallammo.version.PotionEffectMappings;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
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
    private static PotionEffect fatigueEffect;
    public static final UUID FATIGUE_MODIFIER_UUID = UUID.fromString("40e606a7-8401-4f13-a539-7dfcd0c3c8a2");

    private static final Map<Location, BlockDigProcess> blockDigProcesses = new ConcurrentHashMap<>();
    private static final Map<Location, Collection<UUID>> totalMiningBlocks = new ConcurrentHashMap<>(); // total blocks currently being mined, contents will match all values of diggingPlayers combined
    private static final Map<UUID, Collection<Location>> miningPlayers = new ConcurrentHashMap<>();
    private static final Collection<Location> instantBlockBreaks = ConcurrentHashMap.newKeySet();

    public CustomBreakSpeedListener(){
        disabled = false;
        fatigueEffect = new PotionEffect(PotionEffectMappings.MINING_FATIGUE.getPotionEffectType(), Integer.MAX_VALUE, -1, false, false, false);

        new BukkitRunnable(){
            @Override
            public void run() {
                for (Location l : new HashSet<>(blockDigProcesses.keySet())){
                    Block b = l.getBlock();
                    BlockDigProcess process = blockDigProcesses.getOrDefault(l, new BlockDigProcess(b));
                    if (totalMiningBlocks.containsKey(l)){
                        // block is in the process of being mined
                        for (UUID uuid : new HashMap<>(totalMiningBlocks).getOrDefault(l, new HashSet<>())){
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
        if ((!MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) && ItemUtils.breaksInstantly(e.getBlock().getType())) ||
                ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName())) return;
        e.setCancelled(true);
    }

    public static void onStart(DigPacketInfo info){
        if (info == null || disabled ||
                (!MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) && ItemUtils.breaksInstantly(info.getBlock().getType())) ||
                info.getType() != DigPacketInfo.Type.START || ValhallaMMO.isWorldBlacklisted(info.getBlock().getWorld().getName())) return;

        Block b = info.getBlock();
        DigPacketInfo.resetBlockSpecificCache(info.getDigger().getUniqueId());

        float initialDamage = instantBlockBreaks.contains(b.getLocation()) ? 999 : DigPacketInfo.damage(info.getDigger(), b);
        PrepareBlockBreakEvent event = new PrepareBlockBreakEvent(b, info.getDigger());
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) initialDamage = 0;
        if (initialDamage >= 1 || ItemUtils.breaksInstantly(b.getType())) {
            BlockDigProcess.breakBlockInstantly(info.getDigger(), b);
            if (event.getAdditionalBlocks().isEmpty()) return;
        } else {
            BlockDigProcess process = blockDigProcesses.computeIfAbsent(b.getLocation(), k -> new BlockDigProcess(b));
            Collection<UUID> playersMining = totalMiningBlocks.getOrDefault(b.getLocation(), new HashSet<>());
            playersMining.add(info.getDigger().getUniqueId());
            totalMiningBlocks.put(b.getLocation(), playersMining);
            process.damage(info.getDigger(), initialDamage);
        }

        for (Block block : event.getAdditionalBlocks()){
            float damage = instantBlockBreaks.contains(b.getLocation()) ? 999 : DigPacketInfo.damage(info.getDigger(), b);
            if (damage >= 1 || ItemUtils.breaksInstantly(b.getType())) BlockDigProcess.breakBlockInstantly(info.getDigger(), block);
            else {
                BlockDigProcess p = blockDigProcesses.computeIfAbsent(block.getLocation(), k -> new BlockDigProcess(block));
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
            BlockDigProcess.sendCracks(info.getBlock(), -1);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent e){
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE || ItemUtils.breaksInstantly(e.getBlock().getType()) ||
                ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName())) return;
        if (!isFatigued(e.getPlayer())){
            e.setCancelled(true);
            fatiguePlayer(e.getPlayer(), true);
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
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName())) return;
        fatiguePlayer(e.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName())) removeFatiguedPlayer(e.getPlayer());
        else fatiguePlayer(e.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent e){
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            fatiguePlayer(e.getPlayer(), true);
            fatiguePlayer(e.getPlayer(), true);
        }, 10L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent e){
        if (e.getCause() == EntityPotionEffectEvent.Cause.PLUGIN || !(e.getEntity() instanceof Player p) || MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5)) return;
        if (e.getOldEffect() != null && (e.getOldEffect().getType() == PotionEffectMappings.HASTE.getPotionEffectType() || e.getOldEffect().getType() == PotionEffectMappings.MINING_FATIGUE.getPotionEffectType()))
            DigPacketInfo.resetMinerCache(e.getEntity().getUniqueId());
        if (e.getNewEffect() != null && (e.getNewEffect().getType() == PotionEffectMappings.HASTE.getPotionEffectType() || e.getNewEffect().getType() == PotionEffectMappings.MINING_FATIGUE.getPotionEffectType()))
            DigPacketInfo.resetMinerCache(e.getEntity().getUniqueId());
        if ((e.getAction() == EntityPotionEffectEvent.Action.ADDED || e.getAction() == EntityPotionEffectEvent.Action.CHANGED) &&
                e.getNewEffect() != null && e.getNewEffect().getType() == PotionEffectMappings.MINING_FATIGUE.getPotionEffectType() && e.getNewEffect().getAmplifier() >= 0) {
            // change in mining fatigue effect
            e.setOverride(true);
        } else if ((e.getAction() == EntityPotionEffectEvent.Action.REMOVED || e.getAction() == EntityPotionEffectEvent.Action.CLEARED) &&
                e.getOldEffect() != null && e.getOldEffect().getType() == PotionEffectMappings.MINING_FATIGUE.getPotionEffectType() && (e.getOldEffect().getAmplifier() < 0 || e.getOldEffect().getAmplifier() > 4)) {
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> fatiguePlayer(p, true), 2L);
        }
    }

    private static final Attribute BREAK_SPEED = AttributeMappings.BLOCK_BREAK_SPEED.getAttribute();

    public static void fatiguePlayer(Player p, boolean force){
        if (isFatigued(p) && !force) return;
        if (BREAK_SPEED != null) {
            // for some reason once doesn't work and right now i really don't feel like figuring out why
            double miningSpeed = EntityUtils.getPlayerMiningSpeed(p);
            EntityUtils.addUniqueAttribute(p, FATIGUE_MODIFIER_UUID, "valhalla_mining_speed_nullifier", BREAK_SPEED, -miningSpeed, AttributeModifier.Operation.ADD_NUMBER);
            EntityUtils.addUniqueAttribute(p, FATIGUE_MODIFIER_UUID, "valhalla_mining_speed_nullifier", BREAK_SPEED, -miningSpeed, AttributeModifier.Operation.ADD_NUMBER);
        } else p.addPotionEffect(fatigueEffect);
    }

    public static void removeFatiguedPlayer(Player p){
        if (BREAK_SPEED != null) {
            EntityUtils.removeUniqueAttribute(p, "valhalla_mining_speed_nullifier", BREAK_SPEED);
        } else {
            PotionEffect effect = p.getPotionEffect(PotionEffectMappings.MINING_FATIGUE.getPotionEffectType());
            if (effect != null && effect.getAmplifier() >= 0 && effect.getAmplifier() < 5) return;
            p.removePotionEffect(PotionEffectMappings.MINING_FATIGUE.getPotionEffectType());
        }
    }

    public static boolean isFatigued(Player p){
        if (BREAK_SPEED != null) {
            return EntityUtils.hasUniqueAttribute(p, FATIGUE_MODIFIER_UUID, "valhalla_mining_speed_nullifier", BREAK_SPEED);
        } else return p.hasPotionEffect(PotionEffectMappings.MINING_FATIGUE.getPotionEffectType());
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
