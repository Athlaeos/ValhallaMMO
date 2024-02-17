package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import org.bukkit.entity.Entity;

public class FishingLuckRainSource implements AccumulativeStatSource {
    private final double fishingLuckRain = ValhallaMMO.getPluginConfig().getDouble("fishing_luck_rain");

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        // entity is in rain and exposed to sky light
        if ((statPossessor.getWorld().hasStorm() || statPossessor.getWorld().isThundering())
        && statPossessor.getLocation().getBlock().getLightFromSky() > 14) return fishingLuckRain;
        return 0;
    }
}
