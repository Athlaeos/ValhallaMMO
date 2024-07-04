package me.athlaeos.valhallammo.entities.damageindicators.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.CustomDamageType;
import me.athlaeos.valhallammo.entities.Dummy;
import me.athlaeos.valhallammo.entities.damageindicators.DamageIndicatorStrategy;
import me.athlaeos.valhallammo.hooks.DecentHologramsHook;
import org.bukkit.EntityEffect;
import org.bukkit.entity.LivingEntity;

public class DecentHologramsDPSIndicator implements DamageIndicatorStrategy {
    private static final boolean dummiesOnly = ValhallaMMO.getPluginConfig().getBoolean("dummies_only");

    @Override
    public boolean sendDamage(LivingEntity l, CustomDamageType damageType, double damage, double mitigated) {
        if (damage < 0.05 && mitigated < 0.05) return false;
        return DecentHologramsHook.update(l, damageType, damage, mitigated, false);
    }

    @Override
    public boolean sendCriticalDamage(LivingEntity l, CustomDamageType damageType, double damage, double mitigated) {
        if (damage < 0.05 && mitigated < 0.05) return false;
        if (Dummy.isDummy(l)) l.playEffect(EntityEffect.ARMOR_STAND_HIT);
        return DecentHologramsHook.update(l, damageType, damage, mitigated, true);
    }

    @Override
    public boolean use(LivingEntity l) {
        boolean dummy = Dummy.isDummy(l);
        if (!ValhallaMMO.isHookFunctional(DecentHologramsHook.class) || (dummiesOnly && !dummy)) return false;
        if (dummy) l.playEffect(EntityEffect.ARMOR_STAND_HIT);
        return true;
    }
}
