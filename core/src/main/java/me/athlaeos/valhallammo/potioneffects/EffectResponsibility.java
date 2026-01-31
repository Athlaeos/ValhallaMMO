package me.athlaeos.valhallammo.potioneffects;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.CustomDamageType;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EffectResponsibility {
    private static final Map<UUID, Map<String, Responsibility>> responsibleUntil = new HashMap<>();

    public static void clearResponsibility(UUID victim){
        responsibleUntil.remove(victim);
    }

    /**
     * Marks the offender responsible for inflicting the given damageType effect for the given amount of time. Unless: <br>
     * The responsibility period is negative, in which case nothing happens<br>
     * The responsibility period is 0, in which case the responsibility is removed<br>
     * @param victim the entity getting the inflicted damage
     * @param offender the entity responsible for inflicting the effect
     * @param damageType the damage type inflicted on the victim
     * @param responsibleForTicks how long the offender will be responsible for inflicting the given damage type, in game ticks
     */
    public static void markResponsible(UUID victim, UUID offender, CustomDamageType damageType, int responsibleForTicks){
        if (responsibleForTicks < 0) return;
        Map<String, Responsibility> map = responsibleUntil.getOrDefault(victim, new HashMap<>());
        if (responsibleForTicks == 0) {
            map.remove(damageType.getType());
            if (map.isEmpty()) responsibleUntil.remove(victim);
            else responsibleUntil.put(victim, map);
        } else {
            Responsibility responsibility = new Responsibility(offender, victim, System.currentTimeMillis() + (responsibleForTicks * 50L));
            map.put(damageType.getType(), responsibility);
            responsibleUntil.put(victim, map);
        }
    }

    /**
     * Returns the entity responsible for inflicting the given damage type on the victim
     * @param victim the entity affected by anything responsible's inflicted effects
     * @param damageType the damage type to check its responsibility status from
     * @return the entity responsible for inflicting the effect, if any
     */
    public static Entity getResponsible(UUID victim, CustomDamageType damageType){
        Responsibility responsibility = responsibleUntil.getOrDefault(victim, new HashMap<>()).get(damageType.getType());
        if (responsibility == null) return null; // nobody responsible for this damage taken
        if (responsibility.responsibleUntil < System.currentTimeMillis()) {
            Map<String, Responsibility> map = responsibleUntil.getOrDefault(victim, new HashMap<>());
            map.remove(damageType.getType());
            if (map.isEmpty()) responsibleUntil.remove(victim);
            else responsibleUntil.put(victim, map);
            return null; // responsibility expired
        }
        Entity responsible = ValhallaMMO.getInstance().getServer().getEntity(responsibility.responsible);
        if (responsible == null || !responsible.isValid() || responsible.isDead()) return null; // responsible entity is dead or removed
        return responsible; // returns responsible entity
    }

    /**
     * Gathers how much extra damage the victim entity should be taking when receiving damage of the given type, or 0 if no extra damage should be taken.
     * Can be negative
     * @param victim the entity damaged by the given damage type
     * @param damageType the damage type the entity took
     * @return the fraction of extra damage the victim should take more of (or less, if negative) if the effect is inflicted by an entity with damage-boosting stats
     */
    public static double getResponsibleDamageBuff(Entity victim, CustomDamageType damageType){
        Entity responsible = getResponsible(victim.getUniqueId(), damageType);
        if (responsible == null || damageType.damageMultiplier() == null) return 0D;
        else return AccumulativeStatManager.getCachedRelationalStats(damageType.damageMultiplier(), victim, responsible, 10000, true);
    }

    public static double getResponsibleDamageBuff(Entity victim, Entity damagerDefault, CustomDamageType damageType){
        Entity responsible = Utils.thisorDefault(getResponsible(victim.getUniqueId(), damageType), damagerDefault);
        if (responsible == null || damageType.damageMultiplier() == null) return 0D;
        else return AccumulativeStatManager.getCachedRelationalStats(damageType.damageMultiplier(), victim, responsible, 10000, true);
    }

    private record Responsibility(UUID responsible, UUID victim, long responsibleUntil){}
}
