package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.DayTime;
import me.athlaeos.valhallammo.dom.MoonPhase;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import org.bukkit.entity.Entity;

public class FishingLuckFullMoonSource implements AccumulativeStatSource {
    private final double fishingLuckFullMoon = ValhallaMMO.getPluginConfig().getDouble("fishing_luck_fullmoon");

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        // entity is in rain and exposed to sky light
        MoonPhase phase = MoonPhase.getPhase(statPossessor.getWorld());
        DayTime time = DayTime.getTime(statPossessor.getWorld());
        if (phase == MoonPhase.FULL && !time.isDay()) return fishingLuckFullMoon;
        return 0;
    }
}
