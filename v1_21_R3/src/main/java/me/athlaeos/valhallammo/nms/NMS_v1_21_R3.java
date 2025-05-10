package me.athlaeos.valhallammo.nms;

import io.netty.channel.Channel;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.block.DigPacketInfo;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.EquippableWrapper;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.dom.Structures;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
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
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R3.generator.structure.CraftStructureType;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.inventory.meta.components.consumable.ConsumableComponent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.tag.DamageTypeTags;

import java.lang.reflect.Field;
import java.util.*;

import static me.athlaeos.valhallammo.utility.ItemUtils.itemOrAir;

public final class NMS_v1_21_R3 implements NMS {
    @Override
    public void onEnable() {
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new CrafterCraftListener(), ValhallaMMO.getInstance());
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new VaultLootListener(), ValhallaMMO.getInstance());
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new JumpInputListener(), ValhallaMMO.getInstance());
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
     * Since 1.20.5 PotionTypes are added for extended and amplified potions as well, which are accounted for in {@link PotionEffectRegistry}.
     * Because of this, these methods can always return false because the difference is no longer needed
     */
    @Override
    public boolean isUpgraded(PotionMeta meta) {
        return false;
    }

    /**
     * Since 1.20.5 PotionTypes are added for extended and amplified potions as well, which are accounted for in {@link PotionEffectRegistry}.
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
        NMS_v1_21_R1.addAttribute(e, identifier, type, amount, operation);
    }

    @Override
    public boolean hasUniqueAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type) {
        return NMS_v1_21_R1.hasAttribute(e, identifier, type);
    }

    @Override
    public double getUniqueAttributeValue(LivingEntity e, UUID uuid, String identifier, Attribute type) {
        return NMS_v1_21_R1.getAttributeValue(e, identifier, type);
    }

    @Override
    public void removeUniqueAttribute(LivingEntity e, String identifier, Attribute type) {
        NMS_v1_21_R1.removeAttribute(e, identifier, type);
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

    @SuppressWarnings("deprecation")
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
            meta.getMeta().setFood(food);
            ConsumableComponent consumable = meta.getMeta().getConsumable();
            consumable.setConsumeSeconds(eatTimeSeconds);
            consumable.setAnimation(ConsumableComponent.Animation.EAT);
            consumable.setSound(Sound.ENTITY_GENERIC_EAT);
            meta.getMeta().setConsumable(consumable);
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
    public int getMaxStackSize(ItemMeta meta, Material defaultType) {
        return meta.hasMaxStackSize() ? meta.getMaxStackSize() : defaultType.getMaxStackSize();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void setFireResistant(ItemMeta meta, boolean fireResistant) {
        meta.setDamageResistant(DamageTypeTags.IS_FIRE);
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
        return switch (mappedTo){
            case LUCK -> Attribute.LUCK;
            case ARMOR -> Attribute.ARMOR;
            case SCALE -> Attribute.SCALE;
            case GRAVITY -> Attribute.GRAVITY;
            case MAX_HEALTH -> Attribute.MAX_HEALTH;
            case STEP_HEIGHT -> Attribute.STEP_HEIGHT;
            case TEMPT_RANGE -> Attribute.TEMPT_RANGE;
            case ATTACK_SPEED -> Attribute.ATTACK_SPEED;
            case BURNING_TIME -> Attribute.BURNING_TIME;
            case FLYING_SPEED -> Attribute.FLYING_SPEED;
            case FOLLOW_RANGE -> Attribute.FOLLOW_RANGE;
            case OXYGEN_BONUS -> Attribute.OXYGEN_BONUS;
            case ATTACK_DAMAGE -> Attribute.ATTACK_DAMAGE;
            case JUMP_STRENGTH, HORSE_JUMP_STRENGTH -> Attribute.JUMP_STRENGTH;
            case MAX_ABSORPTION -> Attribute.MAX_ABSORPTION;
            case MOVEMENT_SPEED -> Attribute.MOVEMENT_SPEED;
            case SNEAKING_SPEED -> Attribute.SNEAKING_SPEED;
            case ARMOR_TOUGHNESS -> Attribute.ARMOR_TOUGHNESS;
            case ATTACK_KNOCKBACK -> Attribute.ATTACK_KNOCKBACK;
            case BLOCK_BREAK_SPEED -> Attribute.BLOCK_BREAK_SPEED;
            case MINING_EFFICIENCY -> Attribute.MINING_EFFICIENCY;
            case SAFE_FALL_DISTANCE -> Attribute.SAFE_FALL_DISTANCE;
            case MOVEMENT_EFFICIENCY -> Attribute.MOVEMENT_EFFICIENCY;
            case KNOCKBACK_RESISTANCE -> Attribute.KNOCKBACK_RESISTANCE;
            case SPAWN_REINFORCEMENTS -> Attribute.SPAWN_REINFORCEMENTS;
            case SWEEPING_DAMAGE_RATIO -> Attribute.SWEEPING_DAMAGE_RATIO;
            case FALL_DAMAGE_MULTIPLIER -> Attribute.FALL_DAMAGE_MULTIPLIER;
            case SUBMERGED_MINING_SPEED -> Attribute.SUBMERGED_MINING_SPEED;
            case BLOCK_INTERACTION_RANGE -> Attribute.BLOCK_INTERACTION_RANGE;
            case ENTITY_INTERACTION_RANGE -> Attribute.ENTITY_INTERACTION_RANGE;
            case WATER_MOVEMENT_EFFICIENCY -> Attribute.WATER_MOVEMENT_EFFICIENCY;
            case EXPLOSION_KNOCKBACK_RESISTANCE -> Attribute.EXPLOSION_KNOCKBACK_RESISTANCE;
        };
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void setItemModel(ItemMeta meta, String model){
        Number asNumber = Catch.catchOrElse(() -> model.contains(".") || model.contains(",") ? Float.parseFloat(model) : Integer.parseInt(model), -1);
        if (asNumber.floatValue() < 0 || asNumber.intValue() < 0) {
            meta.setItemModel(StringUtils.isEmpty(model) ? null : NamespacedKey.fromString(model));
        } else {
            CustomModelDataComponent component = meta.getCustomModelDataComponent();
            if (asNumber instanceof Float f) {
                List<Float> floats = new ArrayList<>(List.of(f));
                component.setFloats(floats);
                meta.setCustomModelDataComponent(component);
            } else if (asNumber instanceof Integer i) meta.setCustomModelData(i);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void setCMDBooleanList(ItemMeta meta, List<Boolean> booleans) {
        CustomModelDataComponent component = meta.getCustomModelDataComponent();
        component.setFlags(booleans);
        meta.setCustomModelDataComponent(component);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void setCMDColorList(ItemMeta meta, List<Color> colors) {
        CustomModelDataComponent component = meta.getCustomModelDataComponent();
        component.setColors(colors);
        meta.setCustomModelDataComponent(component);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void setCMDFloatList(ItemMeta meta, List<Float> floats) {
        CustomModelDataComponent component = meta.getCustomModelDataComponent();
        component.setFloats(floats);
        meta.setCustomModelDataComponent(component);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void setCMDStringList(ItemMeta meta, List<String> strings) {
        CustomModelDataComponent component = meta.getCustomModelDataComponent();
        component.setStrings(strings);
        meta.setCustomModelDataComponent(component);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public List<Boolean> getCMDBooleanList(ItemMeta meta) {
        return meta.getCustomModelDataComponent().getFlags();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public List<Color> getCMDColorList(ItemMeta meta) {
        return meta.getCustomModelDataComponent().getColors();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public List<Float> getCMDFloatList(ItemMeta meta) {
        return meta.getCustomModelDataComponent().getFloats();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public List<String> getCMDStringList(ItemMeta meta) {
        return meta.getCustomModelDataComponent().getStrings();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void setEquippable(ItemMeta meta, String modelKey, EquipmentSlot slot, String cameraOverlayKey, Sound equipSound, List<EntityType> allowedTypes){
        if (slot == null || StringUtils.isEmpty(modelKey)) meta.setEquippable(null);
        else {
            EquippableComponent equippableComponent = meta.getEquippable();
            equippableComponent.setModel(NamespacedKey.fromString(modelKey));
            equippableComponent.setSlot(slot);
            equippableComponent.setAllowedEntities(allowedTypes == null || allowedTypes.isEmpty() ? null : allowedTypes);
            equippableComponent.setCameraOverlay(StringUtils.isEmpty(cameraOverlayKey) ? null : NamespacedKey.fromString(cameraOverlayKey));
            equippableComponent.setSwappable(true);
            equippableComponent.setEquipSound(equipSound == null ? Sound.ITEM_ARMOR_EQUIP_GENERIC : equipSound);
            equippableComponent.setDamageOnHurt(true);
            meta.setEquippable(equippableComponent);
        }
    }

    @Override
    public void setToolTipStyle(ItemMeta meta, String namespacedKey){
        meta.setTooltipStyle(StringUtils.isEmpty(namespacedKey) ? null : NamespacedKey.fromString(namespacedKey));
    }

    @Override
    public String getItemModel(ItemMeta meta) {
        return !meta.hasItemModel() || meta.getItemModel() == null ? null : meta.getItemModel().toString();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public EquippableWrapper getEquippable(ItemMeta meta) {
        if (!meta.hasEquippable() || meta.getEquippable().getModel() == null) return null;
        EquippableComponent e = meta.getEquippable();
        return new EquippableWrapper(e.getModel().toString(), e.getSlot(), e.getCameraOverlay() == null ? null : e.getCameraOverlay().toString(), e.getEquipSound(), e.getAllowedEntities());
    }

    @Override
    public String getToolTipStyle(ItemMeta meta) {
        return !meta.hasTooltipStyle() || meta.getTooltipStyle() == null ? null : meta.getTooltipStyle().toString();
    }

    @Override
    public void sendArmorChange(LivingEntity entity, org.bukkit.inventory.ItemStack helmet, org.bukkit.inventory.ItemStack chestplate, org.bukkit.inventory.ItemStack leggings, org.bukkit.inventory.ItemStack boots) {
        List<com.mojang.datafixers.util.Pair<net.minecraft.world.entity.EquipmentSlot, ItemStack>> equipment = new ArrayList<>();
        if (entity.getEquipment() == null) return;
        equipment.add(new com.mojang.datafixers.util.Pair<>(net.minecraft.world.entity.EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(itemOrAir(entity.getEquipment().getItemInMainHand()))));
        equipment.add(new com.mojang.datafixers.util.Pair<>(net.minecraft.world.entity.EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(itemOrAir(entity.getEquipment().getItemInOffHand()))));
        equipment.add(new com.mojang.datafixers.util.Pair<>(net.minecraft.world.entity.EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(itemOrAir(helmet))));
        equipment.add(new com.mojang.datafixers.util.Pair<>(net.minecraft.world.entity.EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(itemOrAir(chestplate))));
        equipment.add(new com.mojang.datafixers.util.Pair<>(net.minecraft.world.entity.EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(itemOrAir(leggings))));
        equipment.add(new com.mojang.datafixers.util.Pair<>(net.minecraft.world.entity.EquipmentSlot.FEET, CraftItemStack.asNMSCopy(itemOrAir(boots))));
        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(entity.getEntityId(), equipment);
        PacketListener.broadcastPlayerPacket(entity, packet, true);
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
