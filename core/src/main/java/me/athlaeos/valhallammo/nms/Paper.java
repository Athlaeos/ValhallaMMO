package me.athlaeos.valhallammo.nms;

import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface Paper {
    void setConsumable(ItemBuilder builder, boolean edible, boolean canAlwaysEat, float eatTimeSeconds);

    void setTool(ItemBuilder builder, float miningSpeed, boolean canDestroyInCreative);

    default void resetAttackCooldown(Player p) {

    }
    default Sound blockSound(org.bukkit.block.Block b) {
        return null;
    }
    default void breakBlock(Player p, org.bukkit.block.Block b){

    }
    default float toolPower(org.bukkit.inventory.ItemStack tool, Material b){
        return 0F;
    }
    default float toolPower(org.bukkit.inventory.ItemStack tool, org.bukkit.block.Block b){
        return 0F;
    }
    default void blockBreakAnimation(Player p, org.bukkit.block.Block b, int id, int stage){

    }
    default void forceAttack(Player player, LivingEntity victim){

    }
}
