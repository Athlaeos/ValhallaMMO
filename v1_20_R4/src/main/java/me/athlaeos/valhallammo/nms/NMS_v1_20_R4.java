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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R4.generator.structure.CraftStructureType;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Field;
import java.util.*;

import static me.athlaeos.valhallammo.utility.ItemUtils.itemOrAir;

public final class NMS_v1_20_R4 implements NMS {
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
        return newMappings(mappedTo);
    }

    @Override
    public PotionEffectType getPotionEffectType(PotionEffectMappings mappedTo){
        return newMappings(mappedTo);
    }

    @Override
    public Attribute getAttribute(AttributeMappings mappedTo) {
        return NMS_v1_19_R1.getMappedAttribute(mappedTo);
    }

    public static Enchantment newMappings(EnchantmentMappings mapping){
        return switch (mapping){
            case FLAME -> Enchantment.FLAME;
            case POWER -> Enchantment.POWER;
            case INFINITY -> Enchantment.INFINITY;
            case PUNCH -> Enchantment.PUNCH;
            case CURSE_OF_BINDING -> Enchantment.BINDING_CURSE;
            case CHANNELING -> Enchantment.CHANNELING;
            case SHARPNESS -> Enchantment.SHARPNESS;
            case BANE_OF_ARTHROPODS -> Enchantment.BANE_OF_ARTHROPODS;
            case SMITE -> Enchantment.SMITE;
            case DEPTH_STRIDER -> Enchantment.DEPTH_STRIDER;
            case EFFICIENCY -> Enchantment.EFFICIENCY;
            case UNBREAKING -> Enchantment.UNBREAKING;
            case FIRE_ASPECT -> Enchantment.FIRE_ASPECT;
            case FROST_WALKER -> Enchantment.FROST_WALKER;
            case IMPALING -> Enchantment.IMPALING;
            case KNOCKBACK -> Enchantment.KNOCKBACK;
            case FORTUNE -> Enchantment.FORTUNE;
            case LOOTING -> Enchantment.LOOTING;
            case LOYALTY -> Enchantment.LOYALTY;
            case LUCK_OF_THE_SEA -> Enchantment.LUCK_OF_THE_SEA;
            case LURE -> Enchantment.LURE;
            case MENDING -> Enchantment.MENDING;
            case MULTISHOT -> Enchantment.MULTISHOT;
            case RESPIRATION -> Enchantment.RESPIRATION;
            case PIERCING -> Enchantment.PIERCING;
            case PROTECTION -> Enchantment.PROTECTION;
            case BLAST_PROTECTION -> Enchantment.BLAST_PROTECTION;
            case FEATHER_FALLING -> Enchantment.FEATHER_FALLING;
            case FIRE_PROTECTION -> Enchantment.FIRE_PROTECTION;
            case PROJECTILE_PROTECTION -> Enchantment.PROJECTILE_PROTECTION;
            case QUICK_CHARGE -> Enchantment.QUICK_CHARGE;
            case RIPTIDE -> Enchantment.RIPTIDE;
            case SILK_TOUCH -> Enchantment.SILK_TOUCH;
            case SOUL_SPEED -> Enchantment.SOUL_SPEED;
            case SWEEPING_EDGE -> Enchantment.SWEEPING_EDGE;
            case THORNS -> Enchantment.THORNS;
            case CURSE_OF_VANISHING -> Enchantment.VANISHING_CURSE;
            case AQUA_AFFINITY -> Enchantment.AQUA_AFFINITY;
            case BREACH -> Enchantment.BREACH;
            case DENSITY -> Enchantment.DENSITY;
            case WIND_BURST -> Enchantment.WIND_BURST;
            case SWIFT_SNEAK -> Enchantment.SWIFT_SNEAK;
        };
    }

    public static PotionEffectType newMappings(PotionEffectMappings mapping){
        return switch (mapping) {
            case LUCK -> PotionEffectType.LUCK;
            case HASTE -> PotionEffectType.HASTE;
            case SPEED -> PotionEffectType.SPEED;
            case HUNGER -> PotionEffectType.HUNGER;
            case NAUSEA -> PotionEffectType.NAUSEA;
            case POISON -> PotionEffectType.POISON;
            case WITHER -> PotionEffectType.WITHER;
            case GLOWING -> PotionEffectType.GLOWING;
            case BAD_LUCK -> PotionEffectType.UNLUCK;
            case DARKNESS -> PotionEffectType.DARKNESS;
            case BAD_OMEN -> PotionEffectType.BAD_OMEN;
            case SLOWNESS -> PotionEffectType.SLOWNESS;
            case STRENGTH -> PotionEffectType.STRENGTH;
            case WEAKNESS -> PotionEffectType.WEAKNESS;
            case BLINDNESS -> PotionEffectType.BLINDNESS;
            case ABSORPTION -> PotionEffectType.ABSORPTION;
            case LEVITATION -> PotionEffectType.LEVITATION;
            case JUMP_BOOST -> PotionEffectType.JUMP_BOOST;
            case RESISTANCE -> PotionEffectType.RESISTANCE;
            case SATURATION -> PotionEffectType.SATURATION;
            case HEALTH_BOOST -> PotionEffectType.HEALTH_BOOST;
            case INVISIBILITY -> PotionEffectType.INVISIBILITY;
            case REGENERATION -> PotionEffectType.REGENERATION;
            case NIGHT_VISION -> PotionEffectType.NIGHT_VISION;
            case SLOW_FALLING -> PotionEffectType.SLOW_FALLING;
            case CONDUIT_POWER -> PotionEffectType.CONDUIT_POWER;
            case DOLPHINS_GRACE -> PotionEffectType.DOLPHINS_GRACE;
            case INSTANT_DAMAGE -> PotionEffectType.INSTANT_DAMAGE;
            case INSTANT_HEALTH -> PotionEffectType.INSTANT_HEALTH;
            case MINING_FATIGUE -> PotionEffectType.MINING_FATIGUE;
            case FIRE_RESISTANCE -> PotionEffectType.FIRE_RESISTANCE;
            case WATER_BREATHING -> PotionEffectType.WATER_BREATHING;
            case HERO_OF_THE_VILLAGE -> PotionEffectType.HERO_OF_THE_VILLAGE;
            case OOZING -> PotionEffectType.OOZING;
            case WEAVING -> PotionEffectType.WEAVING;
            case INFESTED -> PotionEffectType.INFESTED;
            case WIND_CHARGED -> PotionEffectType.WIND_CHARGED;
            case RAID_OMEN -> PotionEffectType.RAID_OMEN;
            case TRIAL_OMEN -> PotionEffectType.TRIAL_OMEN;
        };
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
    @SuppressWarnings("UnstableApiUsage")
    public void addToolBlockRule(ItemMeta meta, Material blockType, float efficiency){
        ToolComponent tool = meta.getTool();
        tool.addRule(blockType, efficiency, true);
        meta.setTool(tool);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void setTool(ItemBuilder meta, float miningSpeed, boolean canDestroyInCreative){
        ToolComponent tool = meta.getMeta().getTool();
        tool.setDefaultMiningSpeed(miningSpeed);
        meta.getMeta().setTool(tool);
    }

    @Override
    public ActivityMappings getActivity(LivingEntity entity){
        Activity activity = ((CraftLivingEntity) entity).getHandle().getBrain().getActiveNonCoreActivity().orElse(null);
        if (activity != null) return ActivityMappings.fromName(activity.getName());
        return null;
    }
}
