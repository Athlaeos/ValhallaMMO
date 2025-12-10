package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.entity.LivingEntity;

public class WhileRaining implements EffectTrigger.ConstantTrigger{
    private final boolean requireSubmerged; // true = need to be in water, false = need to be on land
    public WhileRaining(boolean requireSubmerged){
        this.requireSubmerged = requireSubmerged;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity) {
        byte skyExposure = entity.getLocation().getBlock().getLightFromSky();
        if (entity.getWorld().getHighestBlockYAt(entity.getLocation()) > entity.getLocation().getBlockY()) skyExposure--;
        String biome = entity.getLocation().getBlock().getBiome().getKey().getKey();
        boolean isWarm = biome.contains("savanna") || biome.contains("desert") || biome.contains("badlands");
        boolean isTouchingRain = skyExposure == (byte) 15 && entity.getWorld().hasStorm() && (!isWarm && entity.getLocation().getBlock().getTemperature() > 0.15);
        return requireSubmerged == isTouchingRain;
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return requireSubmerged ? "while_raining" : "while_not_raining";
    }

    @Override
    public void onRegister() {
        // do nothing
    }
}
