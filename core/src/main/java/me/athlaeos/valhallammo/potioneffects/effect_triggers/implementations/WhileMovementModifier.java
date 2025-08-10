package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class WhileMovementModifier implements EffectTrigger.ConstantTrigger{
    private final Boolean movementMode;
    public WhileMovementModifier(Boolean movementMode){
        this.movementMode = movementMode;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity) {
        if (!(entity instanceof Player p)) return false;
        return movementMode == null ? (!p.isSneaking() && !p.isSprinting()) : (movementMode ? p.isSprinting() : p.isSneaking());
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return "while_" + (movementMode == null ? "walking" : (movementMode ? "sprinting" : "sneaking"));
    }

    @Override
    public void onRegister() {
        // do nothing
    }
}
