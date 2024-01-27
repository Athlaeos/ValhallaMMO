package me.athlaeos.valhallammo.block;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.listeners.CustomBreakSpeedListener;
import me.athlaeos.valhallammo.listeners.LootListener;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.MiningProfile;
import me.athlaeos.valhallammo.utility.BlockUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class BlockDigProcess {
    private final Block block;
    private float health = 1F;
    private int ticksSinceUpdate = 0;

    private int lastStage = 0;

    public void damage(Player by, float damage){
        health -= damage;
        ticksSinceUpdate = 0;
        if (health <= 0F) {
            breakBlockInstantly(by, block);
        } else {
            int cracks = getCracks();
            if (lastStage == cracks) return;
            lastStage = cracks;
            ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
                for (Entity p : block.getWorld().getNearbyEntities(block.getLocation(), 20, 20, 20, (e) -> e instanceof Player))
                    ValhallaMMO.getNms().blockBreakAnimation((Player) p, block, getID(), cracks);
            });
        }
    }

    public static void breakBlockInstantly(Player by, Block block){
        ItemBuilder tool = null;
        if (ItemUtils.isEmpty(by.getInventory().getItemInMainHand())) {
            MiningProfile profile = ProfileCache.getOrCache(by, MiningProfile.class);
            if (profile.getEmptyHandTool() != null) tool = profile.getEmptyHandTool();
        }
        if (tool != null && block.getDrops(new ItemStack(Material.STICK), by).isEmpty() && ValhallaMMO.getNms().toolPower(tool.getItem(), block) > 1)
            LootListener.prepareBlockDrops(block, new ArrayList<>(block.getDrops(tool.get())));
        ValhallaMMO.getNms().breakBlock(by, block);
        CustomBreakSpeedListener.getBlockDigProcesses().remove(block);
        for (UUID uuid : CustomBreakSpeedListener.getTotalMiningBlocks().getOrDefault(block, new HashSet<>())) CustomBreakSpeedListener.getMiningPlayers().remove(uuid);
        CustomBreakSpeedListener.getTotalMiningBlocks().remove(block);
        BlockUtils.removeCustomHardness(block);
    }

    public int getTicksSinceUpdate() {
        return ticksSinceUpdate;
    }

    private int getID(){
        return ((block.getX() & 0xFFF) << 20) | ((block.getZ() & 0xFFF) << 8) | (block.getY() & 0xFF);
    }

    public BlockDigProcess(Block b){
        this.block = b;
    }

    private int getCracks(){
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
        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
            for (Entity p : block.getWorld().getNearbyEntities(block.getLocation(), 20, 20, 20, (e) -> e instanceof Player))
                ValhallaMMO.getNms().blockBreakAnimation((Player) p, block, getID(), cracks);
        });
    }

    public float getHealth() {
        return health;
    }
}
