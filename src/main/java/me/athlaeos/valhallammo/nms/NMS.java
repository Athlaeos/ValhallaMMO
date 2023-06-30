package me.athlaeos.valhallammo.nms;

import io.netty.channel.Channel;
import me.athlaeos.valhallammo.dom.DigInfo;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface NMS extends Listener {
    Channel channel(Player p);
    DigInfo packetInfoAdapter(Player p, Object packet);

    void swingAnimation(Player p, EquipmentSlot slot);
    void blockBreakAnimation(Player p, Block b, int id, int stage);
    void blockParticleAnimation(Block b);
    float toolPower(ItemStack tool, Block b);
    Sound blockSound(Block b);
    float blockHardness(Block b);
    boolean breakBlock(Player p, Block b);

    void setBookContents(ItemStack book, List<BaseComponent[]> pages);
}
