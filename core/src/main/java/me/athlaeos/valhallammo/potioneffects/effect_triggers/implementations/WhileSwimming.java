package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.entity.LivingEntity;

public class WhileSwimming implements EffectTrigger.ConstantTrigger{
    private final boolean inverted;
    public WhileSwimming(boolean inverted){
        this.inverted = inverted;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity) {
        return inverted != entity.isSwimming();
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return "while" + (inverted ? "_not" : "") + "_swimming";
    }

    @Override
    public void onRegister() {
        // do nothing
    }
}
