package me.athlaeos.valhallammo.entities.damageindicators;

import me.athlaeos.valhallammo.dom.CustomDamageType;
import org.bukkit.entity.LivingEntity;

public interface DamageIndicatorStrategy {
    /**
     * Triggers a regular damage instance for the given damage indicator
     * @param l the entity to damage
     * @param damageType the damage type to send (determines the symbol and color displayed)
     * @param damage the damage to send
     * @return true if damage should be nullified (like when the entity is a dummy) or false otherwise
     */
    boolean sendDamage(LivingEntity l, CustomDamageType damageType, double damage, double mitigated);

    /**
     * Triggers a critical damage instance for the given damage indicator
     * @param l the entity to damage
     * @param damageType the damage type to send (determines the symbol and color displayed)
     * @param damage the damage to send
     * @return true if damage should be nullified (like when the entity is a dummy) or false otherwise
     */
    boolean sendCriticalDamage(LivingEntity l, CustomDamageType damageType, double damage, double mitigated);
    boolean use(LivingEntity l);
}
