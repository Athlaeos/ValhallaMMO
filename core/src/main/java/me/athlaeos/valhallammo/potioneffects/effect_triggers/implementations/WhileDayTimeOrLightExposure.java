package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.dom.DayTime;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

public class WhileDayTimeOrLightExposure implements EffectTrigger.ConstantTrigger{
    private final Boolean day; // true = day, false = night, null = don't check daytime
    private final Boolean light; // true = light, false = darkness, null = don't check light level
    private final Boolean outside; // true = outside, false = inside, null = don't check
    public WhileDayTimeOrLightExposure(Boolean day, Boolean light, Boolean outside){
        this.day = day;
        this.light = light;
        this.outside = outside;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity) {
        DayTime time = DayTime.getTime(entity.getWorld());
        Block b = entity.getEyeLocation().getBlock();
        byte lightLevel = b.getLightLevel();
        byte skyExposure = b.getLightFromSky();
        if (time.isDay()) lightLevel = (byte) Math.max(lightLevel, skyExposure); // if its day, light level should be equal to sky exposure even if no actual light is present

        // this is to check if there aren't any leaves or glass in the way that would prevent
        if (lightLevel == (byte) 15 && b.getWorld().getHighestBlockYAt(b.getLocation()) > b.getY()) skyExposure--;
        boolean outside = skyExposure > (byte) 14;
        if (day != null) {
            if (light != null) {
                // if day and light are checked, one of the two must pass
                if (light && lightLevel > (byte) 0) return this.outside == null || this.outside == outside;
                else if (!light && lightLevel <= (byte) 0) return this.outside == null || this.outside == outside;
                else if (time == DayTime.TIMELESS) return false;
                else return day == time.isDay() && (this.outside == null || this.outside == outside);
            } else {
                if (time == DayTime.TIMELESS) return false;
                else return (this.outside == null || this.outside == outside) && day == time.isDay();
            }
        } else {
            return (light && lightLevel > (byte) 0) || (!light && lightLevel <= (byte) 0) && (this.outside == null || this.outside == outside);
        }
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        if (day != null) {
            if (light != null) return ("while_" + (day ? "day" : "night") + "_or_" + (light ? "light" : "dark")) + (outside == null ? "" : outside ? "_and_outside" : "_and_sheltered"); // require daytime or nighttime, or some light or total darkness
            else return ("while_" + (day ? "day" : "night")) + (outside == null ? "" : outside ? "_and_outside" : "_and_sheltered"); // only require daytime or nighttime, and skylight access
        } else {
            if (light != null) return ("while_" + (light ? "light" : "dark")) + (outside == null ? "" : outside ? "_and_outside" : "_and_sheltered"); // only require some light or total darkness
            else {
                if (outside != null) return "while_" + (outside ? "outside" : "sheltered");
                else return null; // invalid
            }
        }
    }

    @Override
    public void onRegister() {
        // do nothing
    }
}