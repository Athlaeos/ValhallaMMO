package me.athlaeos.valhallammo.nms;

import io.netty.channel.Channel;
import me.athlaeos.valhallammo.block.DigPacketInfo;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface NMS extends Listener {
    Channel channel(Player p);
    DigPacketInfo readDiggingPacket(Player p, Object packet);

    void blockBreakAnimation(Player p, Block b, int id, int stage);
    void blockParticleAnimation(Block b);
    float toolPower(ItemStack tool, Block b);
    float toolPower(ItemStack tool, Material b);
    void breakBlock(Player p, Block b);
    Sound blockSound(Block b);

    void setBookContents(ItemStack book, List<BaseComponent[]> pages);

}
