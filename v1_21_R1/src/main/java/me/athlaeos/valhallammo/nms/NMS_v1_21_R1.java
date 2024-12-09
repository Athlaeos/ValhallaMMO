package me.athlaeos.valhallammo.nms;

import io.netty.channel.Channel;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.block.DigPacketInfo;
import me.athlaeos.valhallammo.dom.EquippableWrapper;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.dom.Structures;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import me.athlaeos.valhallammo.version.PotionEffectMappings;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.generator.structure.CraftStructureType;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Field;
import java.util.*;

public final class NMS_v1_21_R1 implements NMS {

    @Override
    public void onEnable() {
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new CrafterCraftListener(), ValhallaMMO.getInstance());
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new VaultLootListener(), ValhallaMMO.getInstance());
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
    public PotionType getPotionType(PotionMeta meta) {
        return meta.getBasePotionType();
    }

    /**
     * Since 1.20.5 PotionTypes are added for extended and amplified potions as well, which are accounted for in {@link me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry}.
     * Because of this, these methods can always return false because the difference is no longer needed
     */
    @Override
    public boolean isUpgraded(PotionMeta meta) {
        return false;
    }

    /**
     * Since 1.20.5 PotionTypes are added for extended and amplified potions as well, which are accounted for in {@link me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry}.
     * Because of this, these methods can always return false because the difference is no longer needed
     */
    @Override
    public boolean isExtended(PotionMeta meta) {
        return false;
    }

    @Override
    public void setPotionType(PotionMeta meta, PotionType type) {
        meta.setBasePotionType(type);
    }

    @Override
    public void addUniqueAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type, double amount, AttributeModifier.Operation operation) {
        addAttribute(e, identifier, type, amount, operation);
    }

    @Override
    public boolean hasUniqueAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type) {
        return hasAttribute(e, identifier, type);
    }

    @Override
    public double getUniqueAttributeValue(LivingEntity e, UUID uuid, String identifier, Attribute type) {
        return getAttributeValue(e, identifier, type);
    }

    @Override
    public void removeUniqueAttribute(LivingEntity e, String identifier, Attribute type) {
        removeAttribute(e, identifier, type);
    }

    @Override
    public Channel channel(Player p) {
        try {
            Field c = ServerGamePacketListenerImpl.class.getSuperclass().getDeclaredField("e");
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
        b.getWorld().spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 0, 0.5),
                10, b.getBlockData());
    }

    @Override
    public float toolPower(org.bukkit.inventory.ItemStack tool, org.bukkit.block.Block b) {
        if (!ItemUtils.isEmpty(tool)) {
            ItemStack craftItemStack = CraftItemStack.asNMSCopy(tool);
            Block nmsBlock = ((CraftWorld) b.getWorld()).getHandle().getBlockState(new BlockPos(b.getX(), b.getY(), b.getZ())).getBlock();
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
        b.getWorld().spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 0.5, 0.5), 100, 0.1, 0.1, 0.1, 4, b.getBlockData());
        b.getWorld().playSound(b.getLocation(), b.getBlockData().getSoundGroup().getBreakSound(), 1.0f, 1.0f);
        ((CraftPlayer) p).getHandle().gameMode.destroyBlock(new BlockPos(b.getX(), b.getY(), b.getZ()));
    }

    @Override
    public Sound blockSound(org.bukkit.block.Block b) {
        try {
            Block nmsBlock = ((CraftWorld) b.getWorld()).getHandle().getBlockState(new BlockPos(b.getX(), b.getY(), b.getZ())).getBlock();
            SoundType soundEffectType = nmsBlock.defaultBlockState().getSoundType();

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

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void setEdible(ItemMeta meta, boolean edible, boolean canAlwaysEat, float eatTimeSeconds) {
        if (edible){
            FoodComponent food = meta.getFood();
            food.setCanAlwaysEat(canAlwaysEat);
            food.setEatSeconds(eatTimeSeconds);
            meta.setFood(food);
        } else meta.setFood(null);
    }

    @Override
    public void setGlint(ItemMeta meta, boolean glint) {
        meta.setEnchantmentGlintOverride(glint);
    }

    @Override
    public void setMaxStackSize(ItemMeta meta, int stackSize) {
        meta.setMaxStackSize(stackSize);
    }

    @Override
    public int getMaxStackSize(ItemMeta meta, Material baseMaterial) {
        return meta.hasMaxStackSize() ? meta.getMaxStackSize() : baseMaterial.getMaxStackSize();
    }

    @Override
    public void setFireResistant(ItemMeta meta, boolean fireResistant) {
        meta.setFireResistant(fireResistant);
    }

    @Override
    public void setHideTooltip(ItemMeta meta, boolean hideToolTip) {
        meta.setHideTooltip(hideToolTip);
    }

    @Override
    public void setBookContents(org.bukkit.inventory.ItemStack book, List<BaseComponent[]> pages) {

    }

    @Override
    public Enchantment getEnchantment(EnchantmentMappings mappedTo) {
        return NMS_v1_20_R4.newMappings(mappedTo);
    }

    @Override
    public PotionEffectType getPotionEffectType(PotionEffectMappings mappedTo){
        return NMS_v1_20_R4.newMappings(mappedTo);
    }

    public static void addAttribute(LivingEntity e, String identifier, Attribute type, double amount, AttributeModifier.Operation operation){
        AttributeInstance instance = e.getAttribute(type);
        if (instance != null) {
            NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), identifier);
            instance.getModifiers().stream().filter(m -> m != null && (m.getKey().equals(key) || m.getName().equals(identifier))).forEach(instance::removeModifier);
            if (amount != 0) instance.addModifier(new AttributeModifier(key, amount, operation, EquipmentSlotGroup.ANY));
        }
    }

    public static boolean hasAttribute(LivingEntity e, String identifier, Attribute type){
        AttributeInstance instance = e.getAttribute(type);
        NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), identifier);
        return instance != null && instance.getModifiers().stream().anyMatch(m -> m != null && (m.getKey().equals(key) || m.getName().equals(identifier)));
    }

    public static double getAttributeValue(LivingEntity e, String identifier, Attribute type){
        AttributeInstance instance = e.getAttribute(type);
        NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), identifier);
        if (instance != null) return instance.getModifiers().stream().filter(m -> m != null  && (m.getKey().equals(key) || m.getName().equals(identifier))).map(AttributeModifier::getAmount).findFirst().orElse(0D);
        return 0;
    }

    public static void removeAttribute(LivingEntity e, String identifier, Attribute type){
        AttributeInstance instance = e.getAttribute(type);
        NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), identifier);
        if (instance != null) instance.getModifiers().stream().filter(m -> m != null && (m.getKey().equals(key) || m.getName().equals(identifier))).forEach(instance::removeModifier);
    }

    @Override
    public void setItemModel(ItemMeta meta, String model){
        // not compatible
    }

    @Override
    public void setEquippable(ItemMeta meta, String modelKey, EquipmentSlot slot, String cameraOverlayKey, Sound equipSound, List<EntityType> allowedTypes){
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
}
