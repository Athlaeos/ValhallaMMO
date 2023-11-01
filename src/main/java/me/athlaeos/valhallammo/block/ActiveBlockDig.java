package me.athlaeos.valhallammo.block;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.BlockUtils;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

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
