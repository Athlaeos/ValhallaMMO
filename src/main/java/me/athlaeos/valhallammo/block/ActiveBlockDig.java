package me.athlaeos.valhallammo.block;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.listeners.LootListener;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.MiningProfile;
import me.athlaeos.valhallammo.utility.BlockUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ActiveBlockDig {
    private final BukkitTask task;
    private final DigPacketInfo info;
    private final ItemBuilder minedWith;
    private final int breakTime;

    private int timesRan = 0;
    private int lastProgress = -1;

    public ActiveBlockDig(DigPacketInfo info, ItemBuilder minedWith, int breakTime){
        this.info = info;
        this.minedWith = minedWith;
        this.breakTime = breakTime;

        this.task = ValhallaMMO.getInstance().getServer().getScheduler().runTaskTimer(ValhallaMMO.getInstance(),
                this::run, 0L, 1L);
    }

    public void run(){
        int progress = getCracks();
        Block b = info.block();
        if (info.getDigger() == null || !info.getDigger().isOnline()) abort();
        else {
            if (progress != lastProgress){
                ValhallaMMO.getNms().blockBreakAnimation(info.getDigger(), b, info.id(), progress);
                lastProgress = progress;
            }
            if (timesRan == breakTime) {
                abort();
                ItemBuilder tool = null;
                if (ItemUtils.isEmpty(info.getDigger().getInventory().getItemInMainHand())) {
                    MiningProfile profile = ProfileCache.getOrCache(info.getDigger(), MiningProfile.class);
                    if (profile.getEmptyHandTool() != null) tool = profile.getEmptyHandTool();
                }
                // only prepare custom drops if tool is a valid item for the block
                if (tool != null && b.getDrops(new ItemStack(Material.STICK), info.getDigger()).isEmpty() && ValhallaMMO.getNms().toolPower(tool.getItem(), b) > 1) LootListener.prepareBlockDrops(b, new ArrayList<>(b.getDrops(tool.get())));
                ValhallaMMO.getNms().breakBlock(info.getDigger(), b);
                BlockUtils.removeCustomHardness(b);
            }
        }
        timesRan++;
    }

    private int getCracks(){
        return (int) Math.floor(((timesRan + 1D) / breakTime) * 10D);
    }

    public void abort(){
        task.cancel();
        ValhallaMMO.getNms().blockBreakAnimation(info.getDigger(), info.block(), info.id(), 10);
    }

    public ItemBuilder getMinedWith() {
        return minedWith;
    }
}
