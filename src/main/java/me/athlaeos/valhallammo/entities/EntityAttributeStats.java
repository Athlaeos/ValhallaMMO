package me.athlaeos.valhallammo.entities;

import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

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

    static{
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_movement_modifier", UUID.fromString("18053ff8-ca47-4212-8753-47a537aeb4a3"), "MOVEMENT_SPEED_BONUS", Attribute.GENERIC_MOVEMENT_SPEED, AttributeModifier.Operation.ADD_SCALAR));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_health_modifier", UUID.fromString("7e5c7906-792f-40d8-b1a1-e5b7194b77d2"), "HEALTH_BONUS", Attribute.GENERIC_MAX_HEALTH, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_health_multiplier_modifier", UUID.fromString("7bf71943-e92b-4ba6-b404-2d592e3a520d"), "HEALTH_MULTIPLIER_BONUS", Attribute.GENERIC_MAX_HEALTH, AttributeModifier.Operation.ADD_SCALAR));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_toughness_modifier", UUID.fromString("048ace54-c23e-4072-9919-59c3ae075eee"), "TOUGHNESS_BONUS", Attribute.GENERIC_ARMOR_TOUGHNESS, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_luck_modifier", UUID.fromString("005be4d1-105a-4471-9db4-f61412231238"), "LUCK_BONUS", Attribute.GENERIC_LUCK, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_attack_damage_modifier", UUID.fromString("c0af4848-6f51-46a7-a173-867d4da7e726"), "ATTACK_DAMAGE_BONUS", Attribute.GENERIC_ATTACK_DAMAGE, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_attack_speed_modifier", UUID.fromString("a7b83798-ca38-4d4a-b9f9-84350195ed20"), "ATTACK_SPEED_BONUS", Attribute.GENERIC_ATTACK_SPEED, AttributeModifier.Operation.ADD_SCALAR));

        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5)){
            registerAttributeToUpdate(new AttributeDataHolder("valhalla_block_reach_modifier", UUID.fromString("29182c31-f403-421a-956b-5ec4a35a9c67"), "BLOCK_REACH", Attribute.valueOf("GENERIC_BLOCK_INTERACTION_RANGE"), AttributeModifier.Operation.ADD_NUMBER));
            registerAttributeToUpdate(new AttributeDataHolder("valhalla_block_reach_modifier", UUID.fromString("80134680-c8a6-4640-ae1a-3a06fefc18ee"), "ATTACK_REACH_BONUS", Attribute.valueOf("GENERIC_ENTITY_INTERACTION_RANGE"), AttributeModifier.Operation.ADD_NUMBER));
            registerAttributeToUpdate(new AttributeDataHolder("valhalla_block_reach_modifier", UUID.fromString("ce9f747e-2ec1-441e-9965-f7acf876b6d5"), "ATTACK_REACH_MULTIPLIER", Attribute.valueOf("GENERIC_ENTITY_INTERACTION_RANGE"), AttributeModifier.Operation.ADD_SCALAR));
            registerAttributeToUpdate(new AttributeDataHolder("valhalla_step_height_modifier", UUID.fromString("9cd03525-21c9-4d32-be70-0ec076cca3cb"), "STEP_HEIGHT", Attribute.valueOf("GENERIC_STEP_HEIGHT"), AttributeModifier.Operation.ADD_NUMBER));
            registerAttributeToUpdate(new AttributeDataHolder("valhalla_scale_modifier", UUID.fromString("0099240a-a267-47ec-90bc-4c7b5210069d"), "SCALE", Attribute.valueOf("GENERIC_SCALE"), AttributeModifier.Operation.ADD_SCALAR));
        }
    }

    public static void updateStats(LivingEntity e){
        for (AttributeDataHolder holder : attributesToUpdate.values()){
            double value = AccumulativeStatManager.getCachedStats(holder.statSource(), e, 10000, true);
            EntityUtils.addUniqueAttribute(e, holder.uuid, holder.name(), holder.type(), value, holder.operation());
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

    public record AttributeDataHolder(String name, UUID uuid, String statSource, Attribute type, AttributeModifier.Operation operation) { }
}
