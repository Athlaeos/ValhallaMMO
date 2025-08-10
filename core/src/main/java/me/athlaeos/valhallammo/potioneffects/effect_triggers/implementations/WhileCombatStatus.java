package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.listeners.EntityAttackListener;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class WhileCombatStatus implements EffectTrigger.ConstantTrigger{
    private final boolean shouldBeInCombat;
    public WhileCombatStatus(boolean shouldBeInCombat){
        this.shouldBeInCombat = shouldBeInCombat;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity) {
        if (!(entity instanceof Player p)) return false;
        return shouldBeInCombat == EntityAttackListener.isInCombat(p);
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return "while_" + (shouldBeInCombat ? "in_combat" : "out_of_combat");
    }

    @Override
    public void onRegister() {
        // do nothing
    }
}
