package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.MiningProfile;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class DrillingMiningSpeedSource implements AccumulativeStatSource {

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof Player p && !Timer.isCooldownPassed(p.getUniqueId(), "mining_drilling_duration")){
            MiningProfile profile = ProfileCache.getOrCache(p, MiningProfile.class);
            return profile.getDrillingSpeedBonus();
        }
        return 0;
    }
}
