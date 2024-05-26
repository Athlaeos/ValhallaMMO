package me.athlaeos.valhallammo.nms;

import io.netty.channel.Channel;
import me.athlaeos.valhallammo.block.DigPacketInfo;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.dom.Structures;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import me.athlaeos.valhallammo.version.PotionEffectMappings;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;
import java.util.Map;

public interface NMS extends Listener {
    Channel channel(Player p);
    DigPacketInfo readDiggingPacket(Player p, Object packet);

    Pair<Location, Structures> getNearestStructure(World world, Location location, Map<Structures, Integer> structuresToFind);
    void blockBreakAnimation(Player p, Block b, int id, int stage);
    void blockParticleAnimation(Block b);
    float toolPower(ItemStack tool, Block b);
    float toolPower(ItemStack tool, Material b);
    void breakBlock(Player p, Block b);
    Sound blockSound(Block b);
    void resetAttackCooldown(Player p);

    void setBookContents(ItemStack book, List<BaseComponent[]> pages);

    Enchantment getEnchantment(EnchantmentMappings mappedTo);
    PotionType getPotionType(PotionMeta meta);
    PotionEffectType getPotionEffectType(PotionEffectMappings mappedTo);
    boolean isUpgraded(PotionMeta meta);
    boolean isExtended(PotionMeta meta);
    void setPotionType(PotionMeta meta, PotionType type);
}
