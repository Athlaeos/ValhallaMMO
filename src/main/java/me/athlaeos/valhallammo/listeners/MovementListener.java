package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.entities.EntityAttributeStats;
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
    private static final Map<UUID, Vector> lastMovementVectors = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.isCancelled()) return;
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
            EntityAttributeStats.updateStats(e.getPlayer());

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
        EntityAttributeStats.updateStats(p);
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.isCancelled()) return;
        if (e.isSneaking()){
            double sneakSpeedBonus = AccumulativeStatManager.getCachedStats("SNEAK_MOVEMENT_SPEED_BONUS", e.getPlayer(), 10000, true);
            EntityUtils.addUniqueAttribute(e.getPlayer(), "valhalla_sneak_movement_modifier", Attribute.GENERIC_MOVEMENT_SPEED, sneakSpeedBonus, AttributeModifier.Operation.ADD_SCALAR);
        } else {
            EntityUtils.removeUniqueAttribute(e.getPlayer(), "valhalla_sneak_movement_modifier", Attribute.GENERIC_MOVEMENT_SPEED);
        }
    }

    @EventHandler
    public void onPlayerToggleSprint(PlayerToggleSprintEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.isCancelled()) return;
        if (e.isSprinting()){
            double sneakSpeedBonus = AccumulativeStatManager.getCachedStats("SPRINT_MOVEMENT_SPEED_BONUS", e.getPlayer(), 10000, true);

            EntityUtils.addUniqueAttribute(e.getPlayer(), "valhalla_sprint_movement_modifier", Attribute.GENERIC_MOVEMENT_SPEED, sneakSpeedBonus, AttributeModifier.Operation.ADD_SCALAR);
        } else {
            EntityUtils.removeUniqueAttribute(e.getPlayer(), "valhalla_sprint_movement_modifier", Attribute.GENERIC_MOVEMENT_SPEED);
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
}
