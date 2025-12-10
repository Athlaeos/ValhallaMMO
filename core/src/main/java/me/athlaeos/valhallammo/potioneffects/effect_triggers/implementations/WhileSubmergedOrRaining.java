package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

public class WhileSubmergedOrRaining implements EffectTrigger.ConstantTrigger{
    private final boolean requireSubmerged; // true = need to be in water, false = need to be on land
    public WhileSubmergedOrRaining(boolean requireSubmerged){
        this.requireSubmerged = requireSubmerged;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity) {
        byte skyExposure = entity.getLocation().getBlock().getLightFromSky();
        if (entity.getWorld().getHighestBlockYAt(entity.getLocation()) > entity.getLocation().getBlockY()) skyExposure--;
        String biome = entity.getLocation().getBlock().getBiome().getKey().getKey();
        boolean isWarm = biome.contains("savanna") || biome.contains("desert") || biome.contains("badlands");
        boolean isTouchingRain = skyExposure == (byte) 15 && entity.getWorld().hasStorm() && (!isWarm && entity.getLocation().getBlock().getTemperature() > 0.15);
        boolean isInWater = isTouchingRain || entity.isSwimming() || entity.getLocation().getBlock().getType() == Material.WATER || entity.getEyeLocation().getBlock().getType() == Material.WATER;
        return requireSubmerged == isInWater;
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return requireSubmerged ? "while_touching_water" : "while_not_touching_water";
    }

    @Override
    public void onRegister() {
        // do nothing
    }
}
