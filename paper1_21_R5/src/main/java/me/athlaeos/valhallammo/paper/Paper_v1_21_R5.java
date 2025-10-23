package me.athlaeos.valhallammo.paper;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.Tool;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.nms.Paper;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.version.ActivityMappings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.block.SoundType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public final class Paper_v1_21_R5 implements Paper {
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void setConsumable(ItemBuilder builder, boolean edible, boolean canAlwaysEat, float eatTimeSeconds) {
        ItemStack item = builder.get();
        if (edible){
            item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                    .consumeSeconds(eatTimeSeconds)
                    .build()
            );
            item.setData(DataComponentTypes.FOOD, FoodProperties.food()
                    .canAlwaysEat(canAlwaysEat)
                    .build()
            );
        } else item.unsetData(DataComponentTypes.CONSUMABLE);
        builder.setItem(item);
        builder.setMeta(ItemUtils.getItemMeta(item));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void setTool(ItemBuilder builder, float miningSpeed, boolean canDestroyInCreative) {
        ItemStack item = builder.get();
        item.setData(DataComponentTypes.TOOL, Tool.tool()
                        .defaultMiningSpeed(miningSpeed)
                        .canDestroyBlocksInCreative(canDestroyInCreative)
                .build()
        );
        builder.setItem(item);
        builder.setMeta(ItemUtils.getItemMeta(item));
    }

    @Override
    public ActivityMappings getActivity(LivingEntity entity) {
        Activity activity = ((CraftLivingEntity) entity).getHandle().getBrain().getActiveNonCoreActivity().orElse(null);
        if (activity != null) return ActivityMappings.fromName(activity.getName());
        return null;
    }

    @Override
    public void resetAttackCooldown(Player p) {
        ServerPlayer entityPlayer = ((CraftPlayer) p).getHandle();
        entityPlayer.resetAttackStrengthTicker();
    }

    @Override
    public Sound blockSound(Block b) {
        try {
            net.minecraft.world.level.block.Block nmsBlock = ((CraftWorld) b.getWorld()).getHandle().getBlockState(new BlockPos(b.getX(), b.getY(), b.getZ())).getBlock();
            SoundType soundEffectType = nmsBlock.defaultBlockState().getSoundType();

            Field soundEffectField = soundEffectType.getClass().getDeclaredField("fallSound");

            soundEffectField.setAccessible(true);

            SoundEvent soundEffect = (SoundEvent) soundEffectField.get(soundEffectType);
            Field keyField = SoundEvent.class.getDeclaredField("CODEC");

            keyField.setAccessible(true);

            ResourceLocation minecraftKey = (ResourceLocation) keyField.get(soundEffect);

            NamespacedKey key = NamespacedKey.fromString(String.format("%s:%s", minecraftKey.getNamespace(), minecraftKey.getPath()));
            return key == null ? Sound.BLOCK_STONE_HIT : Registry.SOUNDS.get(key);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }

        return Sound.BLOCK_STONE_HIT;
    }

    @Override
    public void breakBlock(Player p, Block b) {
        b.getWorld().spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 0.5, 0.5), 100, 0.1, 0.1, 0.1, 4, b.getBlockData());
        b.getWorld().playSound(b.getLocation(), b.getBlockData().getSoundGroup().getBreakSound(), 1.0f, 1.0f);
        ((CraftPlayer) p).getHandle().gameMode.destroyBlock(new BlockPos(b.getX(), b.getY(), b.getZ()));
    }

    @Override
    public float toolPower(ItemStack tool, Material b) {
        if (!ItemUtils.isEmpty(tool)) {
            net.minecraft.world.item.ItemStack craftItemStack = CraftItemStack.asNMSCopy(tool);
            CraftBlockData data = (CraftBlockData) b.createBlockData();
            return craftItemStack.getDestroySpeed(data.getState());
        }
        return 1;
    }

    @Override
    public float toolPower(ItemStack tool, Block b) {
        if (!ItemUtils.isEmpty(tool)) {
            net.minecraft.world.item.ItemStack craftItemStack = CraftItemStack.asNMSCopy(tool);
            net.minecraft.world.level.block.Block nmsBlock = ((CraftWorld) b.getWorld()).getHandle().getBlockState(new BlockPos(b.getX(), b.getY(), b.getZ())).getBlock();
            return craftItemStack.getDestroySpeed(nmsBlock.defaultBlockState());
        }

        return 1;
    }

    @Override
    public void blockBreakAnimation(Player p, Block b, int id, int stage) {
        ServerPlayer entityPlayer = ((CraftPlayer) p).getHandle();
        ServerGamePacketListenerImpl playerConnection = entityPlayer.connection;
        BlockPos blockPosition = new BlockPos(b.getX(), b.getY(), b.getZ());

        playerConnection.send(new ClientboundBlockDestructionPacket(id, blockPosition, stage));
    }

    @Override
    public void forceAttack(Player player, LivingEntity victim) {
        ((CraftPlayer) player).getHandle().attack(((CraftEntity) victim).getHandle());
    }
}
