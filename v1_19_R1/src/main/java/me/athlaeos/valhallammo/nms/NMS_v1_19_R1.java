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
import net.minecraft.core.RegistryAccess;
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
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.generator.strucutre.CraftStructure;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
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

public final class NMS_v1_19_R1 implements NMS {
    @Override
    public void forceAttack(Player player, LivingEntity victim) {
        ((CraftPlayer) player).getHandle().attack(((CraftEntity) victim).getHandle());
    }

    @Override
    public Channel channel(Player p) {
        return ((CraftPlayer) p).getHandle().connection.connection.channel;
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
        CraftServer server = (CraftServer) Bukkit.getServer();
        RegistryAccess.Frozen access = server.getServer().registryAccess();
        for (Pair<Integer, Integer> chunk : chunksToScan){
            Map<Structure, StructureStart> structures = ((CraftWorld) world).getHandle()
                    .getChunk(chunk.getOne(), chunk.getTwo(), ChunkStatus.STRUCTURE_REFERENCES).getAllStarts();
            for (Structure s : structures.keySet()){
                Structures structure = Structures.fromStructure(CraftStructure.minecraftToBukkit(s, access));
                if (structure == null) continue;

                StructureStart start = structures.get(s);
                int distance = Utils.getManhattanDistance(location.getChunk().getX(), location.getChunk().getZ(), chunk.getOne(), chunk.getTwo());
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
        return oldMappings(mappedTo);
    }

    @Override
    public PotionType getPotionType(PotionMeta meta) {
        return meta.getBasePotionData().getType();
    }

    @Override
    public PotionEffectType getPotionEffectType(PotionEffectMappings mappedTo) {
        return oldMappings(mappedTo);
    }

    @Override
    public Attribute getAttribute(AttributeMappings mappedTo) {
        return getMappedAttribute(mappedTo);
    }

    public static Attribute getMappedAttribute(AttributeMappings mappedTo){
        return switch (mappedTo){
            case LUCK -> Attribute.GENERIC_LUCK;
            case ARMOR -> Attribute.GENERIC_ARMOR;
            case MAX_HEALTH -> Attribute.GENERIC_MAX_HEALTH;
            case ATTACK_SPEED -> Attribute.GENERIC_ATTACK_SPEED;
            case FLYING_SPEED -> Attribute.GENERIC_FLYING_SPEED;
            case ATTACK_DAMAGE -> Attribute.GENERIC_ATTACK_DAMAGE;
            case MOVEMENT_SPEED -> Attribute.GENERIC_MOVEMENT_SPEED;
            case ARMOR_TOUGHNESS -> Attribute.GENERIC_ARMOR_TOUGHNESS;
            case ATTACK_KNOCKBACK -> Attribute.GENERIC_ATTACK_KNOCKBACK;
            case HORSE_JUMP_STRENGTH -> Attribute.HORSE_JUMP_STRENGTH;
            case KNOCKBACK_RESISTANCE -> Attribute.GENERIC_KNOCKBACK_RESISTANCE;
            case SPAWN_REINFORCEMENTS -> Attribute.ZOMBIE_SPAWN_REINFORCEMENTS;
            default -> null;
        };
    }

    @Override
    public boolean isUpgraded(PotionMeta meta) {
        return meta.getBasePotionData().isUpgraded();
    }

    @Override
    public boolean isExtended(PotionMeta meta) {
        return meta.getBasePotionData().isExtended();
    }

    @Override
    public void setPotionType(PotionMeta meta, PotionType type) {
        meta.setBasePotionData(new PotionData(type, false, false));
    }

    @Override
    public void addUniqueAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type, double amount, AttributeModifier.Operation operation) {
        addAttribute(e, uuid, identifier, type, amount, operation);
    }

    @Override
    public boolean hasUniqueAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type) {
        return hasAttribute(e, uuid, identifier, type);
    }

    @Override
    public double getUniqueAttributeValue(LivingEntity e, UUID uuid, String identifier, Attribute type) {
        return getAttributeValue(e, uuid, identifier, type);
    }

    @Override
    public void removeUniqueAttribute(LivingEntity e, String identifier, Attribute type) {
        removeAttribute(e, identifier, type);
    }

    public static Enchantment oldMappings(EnchantmentMappings mapping){
        return switch (mapping){
            case FLAME -> Enchantment.ARROW_FIRE;
            case POWER -> Enchantment.ARROW_DAMAGE;
            case INFINITY -> Enchantment.ARROW_INFINITE;
            case PUNCH -> Enchantment.ARROW_KNOCKBACK;
            case CURSE_OF_BINDING -> Enchantment.BINDING_CURSE;
            case CHANNELING -> Enchantment.CHANNELING;
            case SHARPNESS -> Enchantment.DAMAGE_ALL;
            case BANE_OF_ARTHROPODS -> Enchantment.DAMAGE_ARTHROPODS;
            case SMITE -> Enchantment.DAMAGE_UNDEAD;
            case DEPTH_STRIDER -> Enchantment.DEPTH_STRIDER;
            case EFFICIENCY -> Enchantment.DIG_SPEED;
            case UNBREAKING -> Enchantment.DURABILITY;
            case FIRE_ASPECT -> Enchantment.FIRE_ASPECT;
            case FROST_WALKER -> Enchantment.FROST_WALKER;
            case IMPALING -> Enchantment.IMPALING;
            case KNOCKBACK -> Enchantment.KNOCKBACK;
            case FORTUNE -> Enchantment.LOOT_BONUS_BLOCKS;
            case LOOTING -> Enchantment.LOOT_BONUS_MOBS;
            case LOYALTY -> Enchantment.LOYALTY;
            case LUCK_OF_THE_SEA -> Enchantment.LUCK;
            case LURE -> Enchantment.LURE;
            case MENDING -> Enchantment.MENDING;
            case MULTISHOT -> Enchantment.MULTISHOT;
            case RESPIRATION -> Enchantment.OXYGEN;
            case PIERCING -> Enchantment.PIERCING;
            case PROTECTION -> Enchantment.PROTECTION_ENVIRONMENTAL;
            case BLAST_PROTECTION -> Enchantment.PROTECTION_EXPLOSIONS;
            case FEATHER_FALLING -> Enchantment.PROTECTION_FALL;
            case FIRE_PROTECTION -> Enchantment.PROTECTION_FIRE;
            case PROJECTILE_PROTECTION -> Enchantment.PROTECTION_PROJECTILE;
            case QUICK_CHARGE -> Enchantment.QUICK_CHARGE;
            case RIPTIDE -> Enchantment.RIPTIDE;
            case SILK_TOUCH -> Enchantment.SILK_TOUCH;
            case SOUL_SPEED -> Enchantment.SOUL_SPEED;
            case SWEEPING_EDGE -> Enchantment.SWEEPING_EDGE;
            case THORNS -> Enchantment.THORNS;
            case CURSE_OF_VANISHING -> Enchantment.VANISHING_CURSE;
            case AQUA_AFFINITY -> Enchantment.WATER_WORKER;
            default -> null;
        };
    }

    public static PotionEffectType oldMappings(PotionEffectMappings mapping){
        return switch (mapping){
            case LUCK -> PotionEffectType.LUCK;
            case HASTE -> PotionEffectType.FAST_DIGGING;
            case SPEED -> PotionEffectType.SPEED;
            case HUNGER -> PotionEffectType.HUNGER;
            case NAUSEA -> PotionEffectType.CONFUSION;
            case POISON -> PotionEffectType.POISON;
            case WITHER -> PotionEffectType.WITHER;
            case GLOWING -> PotionEffectType.GLOWING;
            case BAD_LUCK -> PotionEffectType.UNLUCK;
            case DARKNESS -> PotionEffectType.DARKNESS;
            case BAD_OMEN -> PotionEffectType.BAD_OMEN;
            case SLOWNESS -> PotionEffectType.SLOW;
            case STRENGTH -> PotionEffectType.INCREASE_DAMAGE;
            case WEAKNESS -> PotionEffectType.WEAKNESS;
            case BLINDNESS -> PotionEffectType.BLINDNESS;
            case ABSORPTION -> PotionEffectType.ABSORPTION;
            case LEVITATION -> PotionEffectType.LEVITATION;
            case JUMP_BOOST -> PotionEffectType.JUMP;
            case RESISTANCE -> PotionEffectType.DAMAGE_RESISTANCE;
            case SATURATION -> PotionEffectType.SATURATION;
            case HEALTH_BOOST -> PotionEffectType.HEALTH_BOOST;
            case INVISIBILITY -> PotionEffectType.INVISIBILITY;
            case REGENERATION -> PotionEffectType.REGENERATION;
            case NIGHT_VISION -> PotionEffectType.NIGHT_VISION;
            case SLOW_FALLING -> PotionEffectType.SLOW_FALLING;
            case CONDUIT_POWER -> PotionEffectType.CONDUIT_POWER;
            case DOLPHINS_GRACE -> PotionEffectType.DOLPHINS_GRACE;
            case INSTANT_DAMAGE -> PotionEffectType.HARM;
            case INSTANT_HEALTH -> PotionEffectType.HEAL;
            case MINING_FATIGUE -> PotionEffectType.SLOW_DIGGING;
            case FIRE_RESISTANCE -> PotionEffectType.FIRE_RESISTANCE;
            case WATER_BREATHING -> PotionEffectType.WATER_BREATHING;
            case HERO_OF_THE_VILLAGE -> PotionEffectType.HERO_OF_THE_VILLAGE;
            default -> null;
        };
    }

    public static void addAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type, double amount, AttributeModifier.Operation operation){
        AttributeInstance instance = e.getAttribute(type);
        if (instance != null){
            instance.getModifiers().stream().filter(m -> m != null && m.getName().equals(identifier)).forEach(instance::removeModifier);
            if (amount != 0) instance.addModifier(new AttributeModifier(uuid, identifier, amount, operation));
        }
    }

    public static boolean hasAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type){
        AttributeInstance instance = e.getAttribute(type);
        return instance != null && instance.getModifiers().stream().anyMatch(m -> m != null && m.getName().equals(identifier));
    }

    public static double getAttributeValue(LivingEntity e, UUID uuid, String identifier, Attribute type){
        AttributeInstance instance = e.getAttribute(type);
        if (instance != null) return instance.getModifiers().stream().filter(m -> m != null && m.getName().equals(identifier) && m.getUniqueId().equals(uuid)).map(AttributeModifier::getAmount).findFirst().orElse(0D);
        return 0;
    }

    public static void removeAttribute(LivingEntity e, String identifier, Attribute type){
        AttributeInstance instance = e.getAttribute(type);
        if (instance != null) instance.getModifiers().stream().filter(m -> m != null && m.getName().equals(identifier)).forEach(instance::removeModifier);
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
