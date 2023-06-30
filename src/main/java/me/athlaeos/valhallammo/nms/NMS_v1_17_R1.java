package me.athlaeos.valhallammo.nms;

import io.netty.channel.Channel;
import me.athlaeos.valhallammo.dom.DigInfo;
import me.athlaeos.valhallammo.utility.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.lang.reflect.Field;
import java.util.List;

public class NMS_v1_17_R1 implements NMS{

    // block nms code was copied from https://gitlab.com/ranull/minecraft/dualwield/-/tree/master/

    @Override
    public Channel channel(Player p) {
        try {
            Field c = ServerGamePacketListenerImpl.class.getDeclaredField("connection");
            c.setAccessible(true);
            Connection connection = (Connection) c.get(((CraftPlayer) p).getHandle().connection);
            return connection.channel;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public DigInfo packetInfoAdapter(Player p, Object packet) {
        if (!(packet instanceof ServerboundPlayerActionPacket packetDig)) return null;
        BlockPos pos = packetDig.getPos();
        org.bukkit.block.Block b = p.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        return new DigInfo(p, b, blockHardness(b), p.getInventory().getItemInMainHand(), Utils.getRandom().nextInt(2000));
    }

    @Override
    public void swingAnimation(Player p, EquipmentSlot slot) {
        if (slot == EquipmentSlot.HAND) p.swingMainHand();
        else p.swingOffHand();
    }

    @Override
    public void blockBreakAnimation(Player p, org.bukkit.block.Block b, int id, int stage) {
        ServerPlayer entityPlayer = ((CraftPlayer) p).getHandle();
        ServerGamePacketListenerImpl playerConnection = entityPlayer.connection;
        BlockPos blockPosition = new BlockPos(b.getX(), b.getY(), b.getZ());

        playerConnection.send(new ClientboundBlockDestructionPacket(id, blockPosition, stage));
    }

    @Override
    public void blockParticleAnimation(org.bukkit.block.Block b) {
        b.getWorld().spawnParticle(org.bukkit.Particle.BLOCK_CRACK, b.getLocation().add(0.5, 0, 0.5),
                10, b.getBlockData());
    }

    @Override
    public float toolPower(org.bukkit.inventory.ItemStack tool, org.bukkit.block.Block b) {
        if (tool.getAmount() != 0) {
            ItemStack craftItemStack = CraftItemStack.asNMSCopy(tool);
            Level nmsWorld = ((CraftWorld) b.getWorld()).getHandle();
            Block nmsBlock = nmsWorld.getBlockState(new BlockPos(b.getX(), b.getY(), b.getZ())).getBlock();

            return craftItemStack.getDestroySpeed(nmsBlock.defaultBlockState());
        }

        return 1;
    }

    @Override
    public Sound blockSound(org.bukkit.block.Block b) {
        try {
            Level nmsWorld = ((CraftWorld) b.getWorld()).getHandle();
            Block nmsBlock = nmsWorld.getBlockState(new BlockPos(b.getX(), b.getY(), b.getZ())).getBlock();
            SoundType soundEffectType = nmsBlock.getSoundType(nmsBlock.defaultBlockState());

            Field soundEffectField = soundEffectType.getClass().getDeclaredField("fallSound");

            soundEffectField.setAccessible(true);

            SoundEvent soundEffect = (SoundEvent) soundEffectField.get(soundEffectType);
            Field keyField = SoundEvent.class.getDeclaredField("CODEC");

            keyField.setAccessible(true);

            ResourceLocation minecraftKey = (ResourceLocation) keyField.get(soundEffect);

            return Sound.valueOf(minecraftKey.getPath().toUpperCase()
                    .replace(".", "_")
                    .replace("_FALL", "_HIT"));
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }

        return Sound.BLOCK_STONE_HIT;
    }

    @Override
    public float blockHardness(org.bukkit.block.Block b) {
        Level nmsWorld = ((CraftWorld) b.getWorld()).getHandle();
        Block nmsBlock = nmsWorld.getBlockState(new BlockPos(b.getX(), b.getY(), b.getZ())).getBlock();

        return nmsBlock.defaultBlockState().getDestroySpeed(null, null);
    }

    @Override
    public boolean breakBlock(Player p, org.bukkit.block.Block b) {
        return p.breakBlock(b);
    }

    @Override
    public void setBookContents(org.bukkit.inventory.ItemStack book, List<BaseComponent[]> pages) {

    }
}
