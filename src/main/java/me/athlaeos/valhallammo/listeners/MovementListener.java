package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MovementListener implements Listener {
    private static final Map<String, AttributeDataHolder> attributesToUpdate = new HashMap<>();
    private static final Map<UUID, Vector> lastMovementVectors = new HashMap<>();

    static{
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_movement_modifier", "MOVEMENT_SPEED_BONUS", Attribute.GENERIC_MOVEMENT_SPEED, AttributeModifier.Operation.ADD_SCALAR));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_health_modifier", "HEALTH_BONUS", Attribute.GENERIC_MAX_HEALTH, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_health_multiplier_modifier", "HEALTH_MULTIPLIER_BONUS", Attribute.GENERIC_MAX_HEALTH, AttributeModifier.Operation.ADD_SCALAR));
        // TODO armor display registerAttributeToUpdate(new AttributeDataHolder("valhalla_armor_modifier", "ARMOR_BONUS", Attribute.GENERIC_ARMOR, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_toughness_modifier", "TOUGHNESS_BONUS", Attribute.GENERIC_ARMOR_TOUGHNESS, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_luck_modifier", "LUCK_BONUS", Attribute.GENERIC_LUCK, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_attack_damage_modifier", "ATTACK_DAMAGE_BONUS", Attribute.GENERIC_ATTACK_DAMAGE, AttributeModifier.Operation.ADD_NUMBER));
        registerAttributeToUpdate(new AttributeDataHolder("valhalla_attack_speed_modifier", "ATTACK_SPEED_BONUS", Attribute.GENERIC_ATTACK_SPEED, AttributeModifier.Operation.ADD_SCALAR));
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName())) return;
        if (e.isCancelled()) return;
        if (e.getTo() == null) {
            lastMovementVectors.remove(e.getPlayer().getUniqueId());
            return;
        }
        lastMovementVectors.put(e.getPlayer().getUniqueId(), e.getTo().toVector().subtract(e.getFrom().toVector()));
        if (Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "delay_combat_update")) { // combat status is checked every half second
            EntityAttackListener.updateCombatStatus(e.getPlayer());
            Timer.setCooldown(e.getPlayer().getUniqueId(), 500, "delay_combat_update");
        }
        if (Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "delay_movement_update")){
            for (AttributeDataHolder holder : attributesToUpdate.values()){
                double value = AccumulativeStatManager.getCachedStats(holder.statSource, e.getPlayer(), 10000, true);

                EntityUtils.addUniqueAttribute(e.getPlayer(), holder.name, holder.type, value, holder.operation);
            }

            AttributeInstance armorInstance = e.getPlayer().getAttribute(Attribute.GENERIC_ARMOR);
            EntityUtils.removeUniqueAttribute(e.getPlayer(), "armor_nullifier", Attribute.GENERIC_ARMOR);
            EntityUtils.removeUniqueAttribute(e.getPlayer(), "armor_display", Attribute.GENERIC_ARMOR);
            if (armorInstance != null){
                double totalArmor = AccumulativeStatManager.getCachedStats("ARMOR_TOTAL", e.getPlayer(), 10000, true);
                int scale = ValhallaMMO.getPluginConfig().getInt("armor_scale", 50);
                double newArmor = Math.max(0, Math.min(20, (totalArmor / scale) * 20));
                EntityUtils.addUniqueAttribute(e.getPlayer(), "armor_nullifier", Attribute.GENERIC_ARMOR, -armorInstance.getValue(), AttributeModifier.Operation.ADD_NUMBER); // sets armor bar to 0
                EntityUtils.addUniqueAttribute(e.getPlayer(), "armor_display", Attribute.GENERIC_ARMOR, newArmor, AttributeModifier.Operation.ADD_NUMBER); // then increases armor to match a custom scale
            }

            Timer.setCooldown(e.getPlayer().getUniqueId(), 10000, "delay_movement_update");
        }
    }

    public static void resetAttributeStats(Player p){
        Timer.setCooldown(p.getUniqueId(), 0, "delay_movement_update");
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName())) return;
        if (!e.isCancelled()){
            if (e.isSneaking()){
                double sneakSpeedBonus = AccumulativeStatManager.getCachedStats("SNEAK_MOVEMENT_SPEED_BONUS", e.getPlayer(), 10000, true);
                EntityUtils.addUniqueAttribute(e.getPlayer(), "valhalla_sneak_movement_modifier", Attribute.GENERIC_MOVEMENT_SPEED, sneakSpeedBonus, AttributeModifier.Operation.ADD_SCALAR);
            } else {
                EntityUtils.removeUniqueAttribute(e.getPlayer(), "valhalla_sneak_movement_modifier", Attribute.GENERIC_MOVEMENT_SPEED);
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSprint(PlayerToggleSprintEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName())) return;
        if (!e.isCancelled()){
            if (e.isSprinting()){
                double sneakSpeedBonus = AccumulativeStatManager.getCachedStats("SPRINT_MOVEMENT_SPEED_BONUS", e.getPlayer(), 10000, true);

                EntityUtils.addUniqueAttribute(e.getPlayer(), "valhalla_sprint_movement_modifier", Attribute.GENERIC_MOVEMENT_SPEED, sneakSpeedBonus, AttributeModifier.Operation.ADD_SCALAR);
            } else {
                EntityUtils.removeUniqueAttribute(e.getPlayer(), "valhalla_sprint_movement_modifier", Attribute.GENERIC_MOVEMENT_SPEED);
            }
        }
    }

    /**
     * When a player moves, the distance (squared) is recorded in this map. This can then be used elsewhere to figure out
     * exactly how fast a player is moving at a given time, since Player#getVelocity() doens't seem to work properly
     * when a player is just walking (standing still returns the same velocity as walking/running)
     * @return the last recorded distance of a player moving, squared
     */
    public static Map<UUID, Vector> getLastMovementVectors() {
        return lastMovementVectors;
    }

    private static void registerAttributeToUpdate(AttributeDataHolder holder){
        attributesToUpdate.put(holder.name, holder);
    }

    public static void registerAttributeToUpdate(String name, String statSource, Attribute type, AttributeModifier.Operation operation){
        registerAttributeToUpdate(new AttributeDataHolder(name, statSource, type, operation));
    }

    public static Map<String, AttributeDataHolder> getAttributesToUpdate() {
        return attributesToUpdate;
    }

    public record AttributeDataHolder(String name, String statSource, Attribute type, AttributeModifier.Operation operation) { }
}
