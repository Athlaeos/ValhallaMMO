package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.entity.LivingEntity;

public class WhileMounted implements EffectTrigger.ConstantTrigger{
    private final boolean inverted;
    public WhileMounted(boolean inverted){
        this.inverted = inverted;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity) {
        return inverted == (entity.getVehicle() == null);
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return "while_" + (inverted ? "unmounted" : "mounted");
    }

    @Override
    public void onRegister() {
        // do nothing
    }
}