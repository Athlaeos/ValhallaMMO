package me.athlaeos.valhallammo.block;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.MiningSpeed;
import me.athlaeos.valhallammo.listeners.CustomBreakSpeedListener;
import me.athlaeos.valhallammo.listeners.LootListener;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.MiningProfile;
import me.athlaeos.valhallammo.utility.BlockUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class BlockDigProcess {
    private final Block block;
    private float health = 1F;
    private int ticksSinceUpdate = 0;

    private int lastStage = 0;

    public void damage(Player by, float damage, boolean force){
        // if block is forcefully damaged, the breaking cooldown is ignored
        if (!force && !Timer.isCooldownPassed(by.getUniqueId(), "delay_block_breaking_allowed")) return;
        health -= damage;
        ticksSinceUpdate = 0;
        if (health <= 0F) {
            // if block is not forcefully damaged and the damage was not enough to instantly break the block,
            // a cooldown is applied in which the player can not break the next block
            if (!force && damage < 1F && CustomBreakSpeedListener.isVanillaBlockBreakDelay()) Timer.setCooldown(by.getUniqueId(), 300, "delay_block_breaking_allowed");
            breakBlockInstantly(by, block);
        } else {
            lastStage = getCracks();
            sendCracks(block, lastStage);
        }
    }

    public void damage(Player by, float damage){
        damage(by, damage, false);
    }

    public static void breakBlockInstantly(Player by, Block block){
        if (!blocksToBreakInstantly.isEmpty()) blocksToBreakInstantly.put(by.getUniqueId(), block.getLocation());
        else blocksToBreakInstantly2.put(by.getUniqueId(), block.getLocation());

        if (instantBlockBreakerTask != null && !done) return;
        done = false;
        instantBlockBreakerTask = ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
            try {
                new HashSet<>((blocksToBreakInstantly.isEmpty() ? blocksToBreakInstantly2 : blocksToBreakInstantly).entrySet()).forEach(e -> {
                    Player p = ValhallaMMO.getInstance().getServer().getPlayer(e.getKey());
                    if (p == null || !p.isOnline()){
                        (blocksToBreakInstantly.isEmpty() ? blocksToBreakInstantly2 : blocksToBreakInstantly).remove(e.getKey());
                        return;
                    }
                    Block b = e.getValue().getBlock();
                    ItemBuilder tool = null;
                    ItemBuilder originalTool = null;
                    if (ItemUtils.isEmpty(p.getInventory().getItemInMainHand())) {
                        MiningProfile profile = ProfileCache.getOrCache(p, MiningProfile.class);
                        if (profile.getEmptyHandTool() != null) tool = profile.getEmptyHandTool();
                    } else {
                        tool = EntityCache.getAndCacheProperties(p).getMainHand();
                        originalTool = tool;
                    }
                    if (tool != null && !tool.getEmbeddedTools().isEmpty()) {
                        ItemBuilder optimalTool = MiningSpeed.getOptimalEmbeddedTool(tool.getEmbeddedTools(), tool.getMeta(), b);
                        if (optimalTool != null) tool = optimalTool;
                    }
                    if (tool != null && !BlockUtils.hasDrops(b, p, originalTool == null ? null : originalTool.getItem()) && ValhallaMMO.getNms().toolPower(tool.getItem(), b) > 1)
                        LootListener.prepareBlockDrops(b, new ArrayList<>(b.getDrops(tool.get())));
                    ValhallaMMO.getNms().breakBlock(p, b);
                    CustomBreakSpeedListener.getBlockDigProcesses().remove(b.getLocation());
                    for (UUID uuid : CustomBreakSpeedListener.getTotalMiningBlocks().getOrDefault(b.getLocation(), new HashSet<>())) CustomBreakSpeedListener.getMiningPlayers().remove(uuid);
                    CustomBreakSpeedListener.getTotalMiningBlocks().remove(b.getLocation());
                    DigPacketInfo.resetBlockSpecificCache(p.getUniqueId());
                    BlockUtils.removeCustomHardness(b);
                    sendCracks(b, -1);
                });
            } catch (ConcurrentModificationException ignored){
                ValhallaMMO.logWarning("Caught CME on block breakage, not good but better than nothing");
            }
            (blocksToBreakInstantly.isEmpty() ? blocksToBreakInstantly2 : blocksToBreakInstantly).clear();
            done = true;
        });
    }

    private static boolean done = false;
    private static BukkitTask instantBlockBreakerTask = null;
    private static final Map<UUID, Location> blocksToBreakInstantly = new HashMap<>();
    private static final Map<UUID, Location> blocksToBreakInstantly2 = new HashMap<>();

    public int getTicksSinceUpdate() {
        return ticksSinceUpdate;
    }

    public static void sendCracks(Block block, int cracks){
        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
            for (Entity p : block.getWorld().getNearbyEntities(block.getLocation(), 20, 20, 20, (e) -> e instanceof Player))
                ValhallaMMO.getNms().blockBreakAnimation((Player) p, block, getID(block), cracks);
        });
    }

    private static int getID(Block block){
        return ((block.getX() & 0xFFF) << 20) | ((block.getZ() & 0xFFF) << 8) | (block.getY() & 0xFF);
    }

    public BlockDigProcess(Block b){
        this.block = b;
    }

    public int getCracks(){
        if (health <= 0 || health >= 1F) return -1;
        return (int) Math.floor((1F - health) * 10F);
    }
    public void incrementTicksSinceUpdate(){
        ticksSinceUpdate++;
    }
    public void heal(float health){
        this.health += health;
        int cracks = getCracks();
        if (lastStage == cracks) return;
        lastStage = cracks;
        sendCracks(block, cracks);
    }

    public float getHealth() {
        return health;
    }
}
