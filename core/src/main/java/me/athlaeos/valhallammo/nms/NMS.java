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
import me.athlaeos.valhallammo.version.PotionEffectMappings;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    void setEdible(ItemBuilder meta, boolean edible, boolean canAlwaysEat, float eatTimeSeconds);
    void setGlint(ItemMeta meta, boolean glint);
    void setMaxStackSize(ItemMeta meta, int stackSize);
    int getMaxStackSize(ItemMeta meta, Material baseMaterial);
    void setFireResistant(ItemMeta meta, boolean fireResistant);
    void setHideTooltip(ItemMeta meta, boolean hideToolTip);

    void setBookContents(ItemStack book, List<BaseComponent[]> pages);

    Enchantment getEnchantment(EnchantmentMappings mappedTo);
    PotionType getPotionType(PotionMeta meta);
    PotionEffectType getPotionEffectType(PotionEffectMappings mappedTo);
    Attribute getAttribute(AttributeMappings mappedTo);
    boolean isUpgraded(PotionMeta meta);
    boolean isExtended(PotionMeta meta);
    void setPotionType(PotionMeta meta, PotionType type);

    void addUniqueAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type, double amount, AttributeModifier.Operation operation);
    boolean hasUniqueAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type);
    double getUniqueAttributeValue(LivingEntity e, UUID uuid, String identifier, Attribute type);
    void removeUniqueAttribute(LivingEntity e, String identifier, Attribute type);

    void sendArmorChange(LivingEntity entity, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots);
    default void resetArmorChange(LivingEntity entity){
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return;
        sendArmorChange(entity, equipment.getHelmet(), equipment.getChestplate(), equipment.getLeggings(), equipment.getBoots());
    }
    default void onEnable(){
        // do nothing by default
    }
    void forceAttack(Player player, LivingEntity victim);

    void setEquippable(ItemMeta meta, String modelKey, EquipmentSlot slot, String cameraOverlayKey, Sound equipSound, List<EntityType> allowedTypes);
    void setItemModel(ItemMeta meta, String model);
    void setToolTipStyle(ItemMeta meta, String namespacedKey);
    String getItemModel(ItemMeta meta);
    EquippableWrapper getEquippable(ItemMeta meta);
    String getToolTipStyle(ItemMeta meta);
    default void addToolBlockRule(ItemMeta meta, Material blockType, float efficiency){
        // do nothing
    }
    default void setTool(ItemBuilder meta, float miningSpeed, boolean canDestroyInCreative){
        // do nothing
    }

    default void setCMDFloatList(ItemMeta meta, List<Float> floats){
        // do nothing
    }
    default void setCMDStringList(ItemMeta meta, List<String> strings){
        // do nothing
    }
    default void setCMDColorList(ItemMeta meta, List<Color> colors){
        // do nothing
    }
    default void setCMDBooleanList(ItemMeta meta, List<Boolean> booleans){
        // do nothing
    }
    default List<Float> getCMDFloatList(ItemMeta meta){
        return new ArrayList<>();
    }
    default List<String> getCMDStringList(ItemMeta meta){
        return new ArrayList<>();
    }
    default List<Color> getCMDColorList(ItemMeta meta){
        return new ArrayList<>();
    }
    default List<Boolean> getCMDBooleanList(ItemMeta meta){
        return new ArrayList<>();
    }
    default EntityExplodeEvent getExplosionEvent(Entity tnt, Location at, List<Block> blockList, float yield, int result){
        return new EntityExplodeEvent(tnt, at, blockList, yield);
    }
    ActivityMappings getActivity(LivingEntity entity);
}
