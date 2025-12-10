package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class WhileBlocking implements EffectTrigger.ConstantTrigger{
    private final boolean inverted;
    public WhileBlocking(boolean inverted){
        this.inverted = inverted;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity) {
        return entity instanceof Player pl && inverted != pl.isBlocking();
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return "while_" + (inverted ? "not_blocking" : "blocking");
    }

    @Override
    public void onRegister() {
        // do nothing
    }
}