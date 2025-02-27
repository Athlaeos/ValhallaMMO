package me.athlaeos.valhallammo.nms;

import io.netty.channel.Channel;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.block.DigPacketInfo;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.EquippableWrapper;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.dom.Structures;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.trading.GossipTypeWrapper;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
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
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_21_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_21_R2.generator.structure.CraftStructureType;
import org.bukkit.craftbukkit.v1_21_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.tag.DamageTypeTags;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static me.athlaeos.valhallammo.utility.ItemUtils.itemOrAir;

public final class NMS_v1_21_R2 implements NMS {
    Class<GossipContainer> gossipContainerClass = null;
    Class<?> gossipEntryClass = null;
    Field gossipContainerGossips = null;
    Field entityGossipsEntries = null;
    Constructor<?>[] entityGossipConstructors = null;

    public NMS_v1_21_R2(){
        try {
            gossipContainerClass = GossipContainer.class;
            gossipContainerGossips = gossipContainerClass.getDeclaredField("c");
            gossipContainerGossips.setAccessible(true);
        } catch (NoSuchFieldException ignored){
            ValhallaMMO.logSevere("Could not find field name 'gossips' in GossipContainer, some villager reputation-related functionality may not work");
        }
        try {
            gossipEntryClass = Class.forName("net.minecraft.world.entity.ai.gossip.GossipContainer$EntityGossips");
            entityGossipConstructors = gossipEntryClass.getConstructors();
            entityGossipsEntries = gossipEntryClass.getDeclaredField("a");
            entityGossipsEntries.setAccessible(true);
        } catch (ClassNotFoundException ignored){
            ValhallaMMO.logSevere("Could not find class for GossipContainer$GossipEntry, some villager reputation-related functionality may not work");
        } catch (NoSuchFieldException ignored){
            ValhallaMMO.logSevere("Could not find field name 'entries' in GossipContainer$GossipEntry, some villager reputation-related functionality may not work");
        }
    }

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
    public void setEdible(ItemMeta meta, boolean edible, boolean canAlwaysEat, float eatTimeSeconds) {
        if (edible){
            FoodComponent food = meta.getFood();
            food.setCanAlwaysEat(canAlwaysEat);
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
        return NMS_v1_21_R1.getMappedAttribute(mappedTo);
    }

    @Override
    public void setItemModel(ItemMeta meta, String model){
        meta.setItemModel(StringUtils.isEmpty(model) ? null : NamespacedKey.fromString(model));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void setEquippable(ItemMeta meta, String modelKey, org.bukkit.inventory.EquipmentSlot slot, String cameraOverlayKey, Sound equipSound, List<EntityType> allowedTypes){
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
    public int getReputation(Player player, Villager villager) {
        return ((CraftVillager) villager).getHandle().getPlayerReputation(((CraftPlayer) player).getHandle());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void modifyReputation(Player player, Villager villager, GossipTypeWrapper reason) {
        if (entityGossipsEntries == null || gossipContainerGossips == null) return;
        GossipType type = Catch.catchOrElse(() -> GossipType.valueOf(reason.toString()), null);
        if (type == null) return;
        try {
            int oldrep = getReputation(player, villager);
            Map<UUID, Object> gossipsMap = (Map<UUID, Object>) gossipContainerGossips.get(((CraftVillager) villager).getHandle().getGossips());
            if (gossipsMap == null) gossipsMap = new HashMap<>();
            if (!gossipsMap.containsKey(player.getUniqueId())) {
                Object o = entityGossipConstructors[0].newInstance();
                gossipsMap.put(player.getUniqueId(), o);
            }
            Object2IntMap<GossipType> gossipMap = (Object2IntMap<GossipType>) entityGossipsEntries.get(gossipsMap.get(player.getUniqueId()));
            int current = gossipMap.getOrDefault(type, 0) + 1;
            gossipMap.put(type, current);
            System.out.println("changed reputation from " + oldrep + " to " + getReputation(player, villager));
        } catch (IllegalAccessException ignored){
            ValhallaMMO.logSevere("Could not access gossip map for GossipContainer, disabling reputation features for villagers");
            entityGossipsEntries = null;
        } catch (InvocationTargetException | InstantiationException e) {
            ValhallaMMO.logSevere("Could not construct EntityGossip, disabling reputation features for villagers");
            entityGossipsEntries = null;
        }
    }
}
