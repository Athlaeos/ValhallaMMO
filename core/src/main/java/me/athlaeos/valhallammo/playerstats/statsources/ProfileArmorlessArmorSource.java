package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ProfileArmorlessArmorSource implements AccumulativeStatSource, EvEAccumulativeStatSource {

    @Override
    public double fetch(Entity p, boolean use) {
        return fetch(p, null, use);
    }

    @Override
    public double fetch(Entity victim, Entity attackedBy, boolean use) {
        if (victim instanceof Player pl){
            EntityProperties properties = EntityCache.getAndCacheProperties(pl);
            int quantity = properties.getLightArmorCount() + properties.getHeavyArmorCount();
            if (quantity > 0) return 0;
            return ProfileCache.getOrCache(pl, PowerProfile.class).getArmorlessArmor();
        }
        return 0;
    }
}
