package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

public class WhileSubmerged implements EffectTrigger.ConstantTrigger{
    private final boolean requireSubmerged; // true = need to be in water, false = need to be on land
    public WhileSubmerged(boolean requireSubmerged){
        this.requireSubmerged = requireSubmerged;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity) {
        boolean isInWater = entity.isSwimming() || entity.getLocation().getBlock().getType() == Material.WATER || entity.getEyeLocation().getBlock().getType() == Material.WATER;
        return requireSubmerged == isInWater;
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return requireSubmerged ? "while_in_water" : "while_not_in_water";
    }

    @Override
    public void onRegister() {
        // do nothing
    }
}
