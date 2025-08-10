package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.entity.LivingEntity;

public class Constant implements EffectTrigger.ConstantTrigger{
    @Override
    public boolean shouldTrigger(LivingEntity entity) {
        return true;
    }

    @Override
    public int tickDelay() {
        return 20;
    }

    @Override
    public String id() {
        return "constant";
    }

    @Override
    public void onRegister() {
        // do nothing
    }
}
