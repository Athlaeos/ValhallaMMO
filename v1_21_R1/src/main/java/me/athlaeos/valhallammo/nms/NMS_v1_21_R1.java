package me.athlaeos.valhallammo.nms;

import io.netty.channel.Channel;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.block.DigPacketInfo;
import me.athlaeos.valhallammo.dom.EquippableWrapper;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.dom.Structures;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.ActivityMappings;
import me.athlaeos.valhallammo.version.AttributeMappings;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
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
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.generator.structure.CraftStructureType;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Field;
import java.util.*;

import static me.athlaeos.valhallammo.utility.ItemUtils.itemOrAir;

public final class NMS_v1_21_R1 implements NMS {

    @Override
    public void onEnable() {
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new CrafterCraftListener(), ValhallaMMO.getInstance());
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new VaultLootListener(), ValhallaMMO.getInstance());
    }

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
    public void sendArmorChange(LivingEntity entity, org.bukkit.inventory.ItemStack helmet, org.bukkit.inventory.ItemStack chestplate, org.bukkit.inventory.ItemStack leggings, org.bukkit.inventory.ItemStack boots) {
        List<com.mojang.datafixers.util.Pair<EquipmentSlot, ItemStack>> equipment = new ArrayList<>();
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

            return Sound.valueOf(minecraftKey.getPath().toUpperCase(Locale.US)
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
    public void setEdible(ItemBuilder meta, boolean edible, boolean canAlwaysEat, float eatTimeSeconds) {
        if (ValhallaMMO.getPaper() != null){
            ValhallaMMO.getPaper().setConsumable(meta, edible, canAlwaysEat, eatTimeSeconds);
            return;
        }
        if (edible){
            FoodComponent food = meta.getMeta().getFood();
            food.setCanAlwaysEat(canAlwaysEat);
            food.setEatSeconds(eatTimeSeconds);
            meta.getMeta().setFood(food);
        } else meta.getMeta().setFood(null);
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

    @Override
    public Attribute getAttribute(AttributeMappings mappedTo) {
        return getMappedAttribute(mappedTo);
    }

    public static Attribute getMappedAttribute(AttributeMappings mappedTo){
        return switch (mappedTo){
            case LUCK -> Attribute.GENERIC_LUCK;
            case ARMOR -> Attribute.GENERIC_ARMOR;
            case SCALE -> Attribute.GENERIC_SCALE;
            case GRAVITY -> Attribute.GENERIC_GRAVITY;
            case MAX_HEALTH -> Attribute.GENERIC_MAX_HEALTH;
            case STEP_HEIGHT -> Attribute.GENERIC_STEP_HEIGHT;
            case ATTACK_SPEED -> Attribute.GENERIC_ATTACK_SPEED;
            case BURNING_TIME -> Attribute.GENERIC_BURNING_TIME;
            case FLYING_SPEED -> Attribute.GENERIC_FLYING_SPEED;
            case FOLLOW_RANGE -> Attribute.GENERIC_FOLLOW_RANGE;
            case OXYGEN_BONUS -> Attribute.GENERIC_OXYGEN_BONUS;
            case ATTACK_DAMAGE -> Attribute.GENERIC_ATTACK_DAMAGE;
            case JUMP_STRENGTH, HORSE_JUMP_STRENGTH -> Attribute.GENERIC_JUMP_STRENGTH;
            case MAX_ABSORPTION -> Attribute.GENERIC_MAX_ABSORPTION;
            case MOVEMENT_SPEED -> Attribute.GENERIC_MOVEMENT_SPEED;
            case SNEAKING_SPEED -> Attribute.PLAYER_SNEAKING_SPEED;
            case ARMOR_TOUGHNESS -> Attribute.GENERIC_ARMOR_TOUGHNESS;
            case ATTACK_KNOCKBACK -> Attribute.GENERIC_ATTACK_KNOCKBACK;
            case BLOCK_BREAK_SPEED -> Attribute.PLAYER_BLOCK_BREAK_SPEED;
            case MINING_EFFICIENCY -> Attribute.PLAYER_MINING_EFFICIENCY;
            case SAFE_FALL_DISTANCE -> Attribute.GENERIC_SAFE_FALL_DISTANCE;
            case MOVEMENT_EFFICIENCY -> Attribute.GENERIC_MOVEMENT_EFFICIENCY;
            case KNOCKBACK_RESISTANCE -> Attribute.GENERIC_KNOCKBACK_RESISTANCE;
            case SPAWN_REINFORCEMENTS -> Attribute.ZOMBIE_SPAWN_REINFORCEMENTS;
            case SWEEPING_DAMAGE_RATIO -> Attribute.PLAYER_SWEEPING_DAMAGE_RATIO;
            case FALL_DAMAGE_MULTIPLIER -> Attribute.GENERIC_FALL_DAMAGE_MULTIPLIER;
            case SUBMERGED_MINING_SPEED -> Attribute.PLAYER_SUBMERGED_MINING_SPEED;
            case BLOCK_INTERACTION_RANGE -> Attribute.PLAYER_BLOCK_INTERACTION_RANGE;
            case ENTITY_INTERACTION_RANGE -> Attribute.PLAYER_ENTITY_INTERACTION_RANGE;
            case WATER_MOVEMENT_EFFICIENCY -> Attribute.GENERIC_WATER_MOVEMENT_EFFICIENCY;
            case EXPLOSION_KNOCKBACK_RESISTANCE -> Attribute.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE;
            default -> null;
        };
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
    public void setEquippable(ItemMeta meta, String modelKey, org.bukkit.inventory.EquipmentSlot slot, String cameraOverlayKey, Sound equipSound, List<EntityType> allowedTypes) {

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
        if (ValhallaMMO.getPaper() != null){
            ValhallaMMO.getPaper().setTool(meta, miningSpeed, canDestroyInCreative);
            return;
        }
        ToolComponent tool = meta.getMeta().getTool();
        tool.setDefaultMiningSpeed(miningSpeed);
        meta.getMeta().setTool(tool);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public EntityExplodeEvent getExplosionEvent(Entity tnt, Location at, List<org.bukkit.block.Block> blockList, float yield, int result) {
        return new EntityExplodeEvent(tnt, at, blockList, yield, ExplosionResult.values()[result]);
    }

    @Override
    public ActivityMappings getActivity(LivingEntity entity){
        Activity activity = ((CraftLivingEntity) entity).getHandle().getBrain().getActiveNonCoreActivity().orElse(null);
        if (activity != null) return ActivityMappings.fromName(activity.getName());
        return null;
    }
}
