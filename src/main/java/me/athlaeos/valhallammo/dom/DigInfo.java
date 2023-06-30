package me.athlaeos.valhallammo.dom;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DigInfo {
    private final Player player;
    private final Block block;
    private final ItemStack minedWith;
    private final int animationId;
    private final float hardness;
    private int crackAmount;
    private long lastMineTime;

    public DigInfo(Player p, Block b, float hardness, ItemStack minedWith, int animationid){
        this.player = p;
        this.block = b;
        this.hardness = hardness;
        this.minedWith = minedWith;
        this.animationId = animationid;
    }

    public Player getPlayer() {
        return player;
    }

    public float getHardness() {
        return hardness;
    }

    public int getAnimationId() {
        return animationId;
    }

    public int getCrackAmount() {
        return crackAmount;
    }

    public ItemStack getMinedWith() {
        return minedWith;
    }

    public long getLastMineTime() {
        return lastMineTime;
    }

    public Block getBlock() {
        return block;
    }

    public void addCrack(){
        this.crackAmount++;
    }

    public void updateLastMineTime(){
        this.lastMineTime = System.currentTimeMillis();
    }
}
