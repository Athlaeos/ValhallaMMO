package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class WhileFlying implements EffectTrigger.ConstantTrigger{
    private final boolean inverted;
    public WhileFlying(boolean inverted){
        this.inverted = inverted;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity) {
        return entity instanceof Player pl && inverted != (pl.isFlying() || pl.isGliding());
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return "while_" + (inverted ? "not_flying" : "flying");
    }

    @Override
    public void onRegister() {
        // do nothing
    }
}