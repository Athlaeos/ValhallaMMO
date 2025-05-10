package me.athlaeos.valhallammo.nms;

import io.netty.channel.Channel;
import me.athlaeos.valhallammo.block.DigPacketInfo;
import me.athlaeos.valhallammo.dom.EquippableWrapper;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.dom.Structures;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.version.ActivityMappings;
import me.athlaeos.valhallammo.version.AttributeMappings;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.PotionEffectMappings;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R2.generator.structure.CraftStructureType;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Field;
import java.util.*;

import static me.athlaeos.valhallammo.utility.ItemUtils.itemOrAir;

public final class NMS_v1_20_R2 implements NMS {
    @Override
    public void forceAttack(Player player, LivingEntity victim) {
        ((CraftPlayer) player).getHandle().attack(((CraftEntity) victim).getHandle());
    }

    @Override
    public Pair<Location, Structures> getNearestStructure(World world, Location location, Map<Structures, Integer> structuresToFind){
        Collection<Pair<Integer, Integer>> chunksToScan = new HashSet<>();
        int cX = location.getChunk().getX();
        int cZ = location.getChunk().getZ();
        int maxRadius = Collections.max(structuresToFind.values());
        for (int x = cX - maxRadius; x <= cX + maxRadius; x++){
            for (int z = cZ - maxRadius; z <= cZ + maxRadius; z++){
                chunksToScan.add(new Pair<>(x, z));
            }
        }
        Pair<Location, Structures> found = null;
        int closest = Integer.MAX_VALUE;
        for (Pair<Integer, Integer> chunk : chunksToScan){
            Map<Structure, StructureStart> structures = ((CraftWorld) world).getHandle()
                    .getChunk(chunk.getOne(), chunk.getTwo(), ChunkStatus.STRUCTURE_REFERENCES).getAllStarts();
            for (Structure s : structures.keySet()){
                Structures structure = Structures.fromStructure(CraftStructureType.minecraftToBukkit(s.type()));
                if (structure == null) continue;

                StructureStart start = structures.get(s);
                int distance = Utils.getManhattanDistance(cX, cZ, chunk.getOne(), chunk.getTwo());
                if (!structuresToFind.containsKey(structure)) continue;
                int maxDistance = structuresToFind.get(structure);
                if (distance > maxDistance || distance > closest) continue;

                closest = distance;

                BlockPos pos = start.getBoundingBox().getCenter();
                Location loc = new Location(location.getWorld(), pos.getX(), pos.getY(), pos.getZ());

                found = new Pair<>(loc, structure);
            }
        }
        return found;
    }

    @Override
    public void sendArmorChange(LivingEntity entity, org.bukkit.inventory.ItemStack helmet, org.bukkit.inventory.ItemStack chestplate, org.bukkit.inventory.ItemStack leggings, org.bukkit.inventory.ItemStack boots) {
        List<com.mojang.datafixers.util.Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> equipment = new ArrayList<>();
        if (entity.getEquipment() == null) return;
        equipment.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(itemOrAir(entity.getEquipment().getItemInMainHand()))));
        equipment.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(itemOrAir(entity.getEquipment().getItemInOffHand()))));
        equipment.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(itemOrAir(helmet))));
        equipment.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(itemOrAir(chestplate))));
        equipment.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(itemOrAir(leggings))));
        equipment.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.FEET, CraftItemStack.asNMSCopy(itemOrAir(boots))));
        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(entity.getEntityId(), equipment);
        PacketListener.broadcastPlayerPacket(entity, packet, true);
    }

    @Override
    public PotionType getPotionType(PotionMeta meta) {
        return meta.getBasePotionType();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isUpgraded(PotionMeta meta) {
        return meta.getBasePotionData().isUpgraded();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isExtended(PotionMeta meta) {
        return meta.getBasePotionData().isExtended();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setPotionType(PotionMeta meta, PotionType type) {
        meta.setBasePotionData(new PotionData(type, false, false));
    }

    @Override
    public Channel channel(Player p) {
        try {
            Field c = ServerGamePacketListenerImpl.class.getSuperclass().getDeclaredField("c");
            c.setAccessible(true);
            return ((Connection) c.get(((CraftPlayer) p).getHandle().connection)).channel;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DigPacketInfo readDiggingPacket(Player p, Object packet) {
        if (!(packet instanceof ServerboundPlayerActionPacket digPacket)) return null;
        BlockPos pos = digPacket.getPos();
        return new DigPacketInfo(p, pos.getX(), pos.getY(), pos.getZ(), DigPacketInfo.fromName(digPacket.getAction().name()));
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
        if (!ItemUtils.isEmpty(tool)) {
            ItemStack craftItemStack = CraftItemStack.asNMSCopy(tool);
            Level nmsWorld = ((CraftWorld) b.getWorld()).getHandle();
            Block nmsBlock = nmsWorld.getBlockState(new BlockPos(b.getX(), b.getY(), b.getZ())).getBlock();
            return craftItemStack.getDestroySpeed(nmsBlock.defaultBlockState());
        }

        return 1;
    }

    @Override
    public float toolPower(org.bukkit.inventory.ItemStack tool, Material b) {
        if (!ItemUtils.isEmpty(tool)) {
            ItemStack craftItemStack = CraftItemStack.asNMSCopy(tool);
            CraftBlockData data = (CraftBlockData) b.createBlockData();
            return craftItemStack.getDestroySpeed(data.getState());
        }

        return 1;
    }

    @Override
    public void breakBlock(Player p, org.bukkit.block.Block b) {
        b.getWorld().spawnParticle(Particle.BLOCK_CRACK, b.getLocation().add(0.5, 0.5, 0.5), 100, 0.1, 0.1, 0.1, 4, b.getBlockData());
        b.getWorld().playSound(b.getLocation(), b.getBlockData().getSoundGroup().getBreakSound(), 1.0f, 1.0f);
        ((CraftPlayer) p).getHandle().gameMode.destroyBlock(new BlockPos(b.getX(), b.getY(), b.getZ()));
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

            return Sound.valueOf(minecraftKey.getPath().toUpperCase(java.util.Locale.US)
                    .replace(".", "_")
                    .replace("_FALL", "_HIT"));
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }

        return Sound.BLOCK_STONE_HIT;
    }

    @Override
    public void resetAttackCooldown(Player p){
        ServerPlayer entityPlayer = ((CraftPlayer) p).getHandle();
        entityPlayer.resetAttackStrengthTicker();
    }

    @Override
    public void setEdible(ItemBuilder meta, boolean edible, boolean canAlwaysEat, float eatTimeSeconds) {
        // do nothing, incompatible
    }

    @Override
    public void setGlint(ItemMeta meta, boolean glint) {
        // do nothing, incompatible
    }

    @Override
    public void setMaxStackSize(ItemMeta meta, int stackSize) {
        // do nothing, incompatible
    }

    @Override
    public int getMaxStackSize(ItemMeta meta, Material baseMaterial) {
        return baseMaterial.getMaxStackSize();
    }

    @Override
    public void setFireResistant(ItemMeta meta, boolean fireResistant) {
        // do nothing, incompatible
    }

    @Override
    public void setHideTooltip(ItemMeta meta, boolean hideToolTip) {
        // do nothing, incompatible
    }

    @Override
    public void setBookContents(org.bukkit.inventory.ItemStack book, List<BaseComponent[]> pages) {

    }

    @Override
    public Enchantment getEnchantment(EnchantmentMappings mappedTo) {
        return NMS_v1_19_R1.oldMappings(mappedTo);
    }

    @Override
    public PotionEffectType getPotionEffectType(PotionEffectMappings mappedTo){
        return NMS_v1_19_R1.oldMappings(mappedTo);
    }

    @Override
    public Attribute getAttribute(AttributeMappings mappedTo) {
        return NMS_v1_19_R1.getMappedAttribute(mappedTo);
    }

    @Override
    public void addUniqueAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type, double amount, AttributeModifier.Operation operation) {
        NMS_v1_19_R1.addAttribute(e, uuid, identifier, type, amount, operation);
    }

    @Override
    public boolean hasUniqueAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type) {
        return NMS_v1_19_R1.hasAttribute(e, uuid, identifier, type);
    }

    @Override
    public double getUniqueAttributeValue(LivingEntity e, UUID uuid, String identifier, Attribute type) {
        return NMS_v1_19_R1.getAttributeValue(e, uuid, identifier, type);
    }

    @Override
    public void removeUniqueAttribute(LivingEntity e, String identifier, Attribute type) {
        NMS_v1_19_R1.removeAttribute(e, identifier, type);
    }

    @Override
    public void setItemModel(ItemMeta meta, String model){
        // not compatible
    }

    @Override
    public void setEquippable(ItemMeta meta, String modelKey, org.bukkit.inventory.EquipmentSlot slot, String cameraOverlayKey, Sound equipSound, List<EntityType> allowedTypes){
        // not compatible
    }

    @Override
    public void setToolTipStyle(ItemMeta meta, String namespacedKey){
        // not compatible
    }

    @Override
    public String getItemModel(ItemMeta meta) {
        // not compatible
        return null;
    }

    @Override
    public EquippableWrapper getEquippable(ItemMeta meta) {
        // not compatible
        return null;
    }

    @Override
    public String getToolTipStyle(ItemMeta meta) {
        // not compatible
        return null;
    }

    @Override
    public ActivityMappings getActivity(LivingEntity entity){
        Activity activity = ((CraftLivingEntity) entity).getHandle().getBrain().getActiveNonCoreActivity().orElse(null);
        if (activity != null) return ActivityMappings.fromName(activity.getName());
        return null;
    }
}
