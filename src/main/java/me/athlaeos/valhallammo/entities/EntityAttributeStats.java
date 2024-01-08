package me.athlaeos.valhallammo.entities;

import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public class EntityAttributeStats {
    private static final Map<String, AttributeDataHolder> attributesToUpdate = new HashMap<>();

    static{
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_movement_modifier", "MOVEMENT_SPEED_BONUS", Attribute.GENERIC_MOVEMENT_SPEED, AttributeModifier.Operation.ADD_SCALAR));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_health_modifier", "HEALTH_BONUS", Attribute.GENERIC_MAX_HEALTH, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_health_multiplier_modifier", "HEALTH_MULTIPLIER_BONUS", Attribute.GENERIC_MAX_HEALTH, AttributeModifier.Operation.ADD_SCALAR));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_toughness_modifier", "TOUGHNESS_BONUS", Attribute.GENERIC_ARMOR_TOUGHNESS, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_luck_modifier", "LUCK_BONUS", Attribute.GENERIC_LUCK, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_attack_damage_modifier", "ATTACK_DAMAGE_BONUS", Attribute.GENERIC_ATTACK_DAMAGE, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_attack_speed_modifier", "ATTACK_SPEED_BONUS", Attribute.GENERIC_ATTACK_SPEED, AttributeModifier.Operation.ADD_SCALAR));

        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5)){
            registerAttributeToUpdate(new AttributeDataHolder("valhalla_block_reach_modifier", "BLOCK_REACH", Attribute.valueOf("GENERIC_BLOCK_INTERACTION_RANGE"), AttributeModifier.Operation.ADD_NUMBER));
            registerAttributeToUpdate(new AttributeDataHolder("valhalla_block_reach_modifier", "ATTACK_REACH_BONUS", Attribute.valueOf("GENERIC_ENTITY_INTERACTION_RANGE"), AttributeModifier.Operation.ADD_NUMBER));
            registerAttributeToUpdate(new AttributeDataHolder("valhalla_block_reach_modifier", "ATTACK_REACH_MULTIPLIER", Attribute.valueOf("GENERIC_ENTITY_INTERACTION_RANGE"), AttributeModifier.Operation.ADD_SCALAR));
            registerAttributeToUpdate(new AttributeDataHolder("valhalla_step_height_modifier", "STEP_HEIGHT", Attribute.valueOf("GENERIC_STEP_HEIGHT"), AttributeModifier.Operation.ADD_NUMBER));
            registerAttributeToUpdate(new AttributeDataHolder("valhalla_scale_modifier", "SCALE", Attribute.valueOf("GENERIC_SCALE"), AttributeModifier.Operation.ADD_SCALAR));
        }
    }

    public static void updateStats(LivingEntity e){
        for (AttributeDataHolder holder : attributesToUpdate.values()){
            double value = AccumulativeStatManager.getCachedStats(holder.statSource(), e, 10000, true);
            EntityUtils.addUniqueAttribute(e, holder.name(), holder.type(), value, holder.operation());
        }
    }

    public static void removeStats(LivingEntity e){
        for (AttributeDataHolder holder : attributesToUpdate.values()){
            EntityUtils.removeUniqueAttribute(e, holder.name(), holder.type());
        }
    }

    private static void registerAttributeToUpdate(AttributeDataHolder holder){
        attributesToUpdate.put(holder.name, holder);
    }

    public static Map<String, AttributeDataHolder> getAttributesToUpdate() {
        return attributesToUpdate;
    }

    public record AttributeDataHolder(String name, String statSource, Attribute type, AttributeModifier.Operation operation) { }
}
