package me.athlaeos.valhallammo.entities;

import me.athlaeos.valhallammo.listeners.CustomBreakSpeedListener;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.version.AttributeMappings;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityAttributeStats {
    private static final Map<String, AttributeDataHolder> attributesToUpdate = new HashMap<>();
    public static final UUID NEGATIVE_KNOCKBACK = UUID.fromString("7a0ea756-9315-45db-9ace-91b38562d567");
    public static final UUID ARMOR_NULLIFIER = UUID.fromString("e941f8f6-d10e-4b1c-9400-aaf5428f7f99");
    public static final UUID ARMOR_DISPLAY = UUID.fromString("25b57c1e-5d34-450b-ac9b-c88febc8f1c3");
    public static final UUID SNEAK_MOVEMENT = UUID.fromString("6597cee2-73a0-48e6-ae96-fb8ab4ed1440");
    public static final UUID SPRINT_MOVEMENT = UUID.fromString("41a06dac-306d-4093-a55d-2db7408e06d8");
    public static final UUID MOVEMENT_SPEED_BONUS = UUID.fromString("18053ff8-ca47-4212-8753-47a537aeb4a3");
    public static final UUID HEALTH_BONUS = UUID.fromString("7e5c7906-792f-40d8-b1a1-e5b7194b77d2");
    public static final UUID HEALTH_MULTIPLIER_BONUS = UUID.fromString("7bf71943-e92b-4ba6-b404-2d592e3a520d");
    public static final UUID TOUGHNESS_BONUS = UUID.fromString("048ace54-c23e-4072-9919-59c3ae075eee");
    public static final UUID LUCK_BONUS = UUID.fromString("005be4d1-105a-4471-9db4-f61412231238");
    public static final UUID ATTACK_DAMAGE_BONUS = UUID.fromString("c0af4848-6f51-46a7-a173-867d4da7e726");
    public static final UUID ATTACK_SPEED_BONUS = UUID.fromString("a7b83798-ca38-4d4a-b9f9-84350195ed20");
    public static final UUID BLOCK_REACH = UUID.fromString("29182c31-f403-421a-956b-5ec4a35a9c67");
    public static final UUID ATTACK_REACH_BONUS = UUID.fromString("80134680-c8a6-4640-ae1a-3a06fefc18ee");
    public static final UUID ATTACK_REACH_MULTIPLIER = UUID.fromString("ce9f747e-2ec1-441e-9965-f7acf876b6d5");
    public static final UUID STEP_HEIGHT = UUID.fromString("9cd03525-21c9-4d32-be70-0ec076cca3cb");
    public static final UUID SCALE = UUID.fromString("0099240a-a267-47ec-90bc-4c7b5210069d");
    public static final UUID GRAVITY = UUID.fromString("6ba2cb2b-55e8-4830-9487-85a5e40e2639");
    public static final UUID SAFE_FALLING_DISTANCE = UUID.fromString("2805f0d6-9830-4c3c-82c9-88cce6ae1919");
    public static final UUID FALL_DAMAGE_MULTIPLIER = UUID.fromString("b55d80e9-2762-4688-8ae3-d8c71467d670");
    public static final UUID JUMP_HEIGHT_MULTIPLIER = UUID.fromString("7a57e7db-5f30-4f57-a9fb-332ea5cc9226");

    static{
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_movement_modifier", MOVEMENT_SPEED_BONUS, "MOVEMENT_SPEED_BONUS", AttributeMappings.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_SCALAR));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_health_modifier", HEALTH_BONUS, "HEALTH_BONUS", AttributeMappings.MAX_HEALTH, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_health_multiplier_modifier", HEALTH_MULTIPLIER_BONUS, "HEALTH_MULTIPLIER_BONUS", AttributeMappings.MAX_HEALTH, AttributeModifier.Operation.ADD_SCALAR));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_toughness_modifier", TOUGHNESS_BONUS, "TOUGHNESS_BONUS", AttributeMappings.ARMOR_TOUGHNESS, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_luck_modifier", LUCK_BONUS, "LUCK_BONUS", AttributeMappings.LUCK, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_attack_damage_modifier", ATTACK_DAMAGE_BONUS, "ATTACK_DAMAGE_BONUS", AttributeMappings.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_attack_speed_modifier", ATTACK_SPEED_BONUS, "ATTACK_SPEED_BONUS", AttributeMappings.ATTACK_SPEED, AttributeModifier.Operation.ADD_SCALAR));

        registerAttributeToUpdate(new AttributeDataHolder("valhalla_block_reach_modifier", BLOCK_REACH, "BLOCK_REACH", AttributeMappings.BLOCK_INTERACTION_RANGE, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_entity_reach_modifier", ATTACK_REACH_BONUS, "ATTACK_REACH_BONUS", AttributeMappings.ENTITY_INTERACTION_RANGE, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_entity_reach_multiplier_modifier", ATTACK_REACH_MULTIPLIER, "ATTACK_REACH_MULTIPLIER", AttributeMappings.ENTITY_INTERACTION_RANGE, AttributeModifier.Operation.ADD_SCALAR));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_step_height_modifier", STEP_HEIGHT, "STEP_HEIGHT", AttributeMappings.STEP_HEIGHT, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_scale_modifier", SCALE, "SCALE", AttributeMappings.SCALE, AttributeModifier.Operation.ADD_SCALAR));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_gravity_modifier", GRAVITY, "GRAVITY", AttributeMappings.GRAVITY, AttributeModifier.Operation.ADD_SCALAR));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_safe_fall_distance_modifier", SAFE_FALLING_DISTANCE, "SAFE_FALLING_DISTANCE", AttributeMappings.SAFE_FALL_DISTANCE, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_fall_damage_multiplier_modifier", FALL_DAMAGE_MULTIPLIER, "FALL_DAMAGE_MULTIPLIER", AttributeMappings.FALL_DAMAGE_MULTIPLIER, AttributeModifier.Operation.ADD_SCALAR));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_jump_height_multiplier_modifier", JUMP_HEIGHT_MULTIPLIER, "JUMP_HEIGHT_MULTIPLIER", AttributeMappings.JUMP_STRENGTH, AttributeModifier.Operation.ADD_NUMBER));
    }

    public static void updateStats(LivingEntity e){
        for (AttributeDataHolder holder : attributesToUpdate.values()){
            updateSingleStat(e, holder.name);
        }
    }

    public static void updateSingleStat(LivingEntity e, String stat){
        AttributeDataHolder holder = attributesToUpdate.get(stat);
        if (holder == null) return;
        EntityUtils.removeUniqueAttribute(e, holder.name, holder.type.getAttribute());
        double value = AccumulativeStatManager.getCachedStats(holder.statSource(), e, 10000, true);
        if (holder.statSource.equals("JUMP_HEIGHT_MULTIPLIER")){
            EntityUtils.addUniqueAttribute(e, holder.uuid, holder.name(), holder.type().getAttribute(), 0.15 * value, holder.operation());
        } else EntityUtils.addUniqueAttribute(e, holder.uuid, holder.name(), holder.type().getAttribute(), value, holder.operation());
    }

    public static void removeStats(LivingEntity e){
        for (AttributeDataHolder holder : attributesToUpdate.values()){
            EntityUtils.removeUniqueAttribute(e, holder.name(), holder.type().getAttribute());
        }

        EntityUtils.removeUniqueAttribute(e, "armor_nullifier", Attribute.GENERIC_ARMOR);
        EntityUtils.removeUniqueAttribute(e, "armor_display", Attribute.GENERIC_ARMOR);
        EntityUtils.removeUniqueAttribute(e, "valhalla_negative_knockback_taken", Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (e instanceof Player p) CustomBreakSpeedListener.removeFatiguedPlayer(p);
    }

    private static void registerAttributeToUpdate(AttributeDataHolder holder){
        if (holder.type.getAttribute() == null) return;
        attributesToUpdate.put(holder.name, holder);
    }

    public static Map<String, AttributeDataHolder> getAttributesToUpdate() {
        return attributesToUpdate;
    }

    public record AttributeDataHolder(String name, UUID uuid, String statSource, AttributeMappings type, AttributeModifier.Operation operation) { }
}
