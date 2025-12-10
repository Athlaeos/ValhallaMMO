package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.entity.LivingEntity;

public class WhileOnFire implements EffectTrigger.ConstantTrigger{
    private final boolean inverted;
    public WhileOnFire(boolean inverted){
        this.inverted = inverted;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity) {
        return inverted != entity.getFireTicks() > 0;
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return "while_" + (inverted ? "not_" : "") + "on_fire";
    }

    @Override
    public void onRegister() {
        // do nothing
    }
}