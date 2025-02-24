package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.MartialArtsProfile;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class UnarmedDodgeChanceSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    @Override
    public double fetch(Entity p, boolean use) {
        return fetch(p, null, use);
    }

    @Override
    public double fetch(Entity victim, Entity attackedBy, boolean use) {
        if (victim instanceof Player pl){
            MartialArtsProfile profile = ProfileCache.getOrCache(pl, MartialArtsProfile.class);

            EntityProperties properties = EntityCache.getAndCacheProperties(pl);
            if (!EntityUtils.isUnarmed(pl) || Timer.getTimerResult(pl.getUniqueId(), "time_since_punch") < (profile.getAttackDodgeChanceCooldown() * 50L)) return 0;
            int quantity = properties.getLightArmorCount() + properties.getHeavyArmorCount();
            return profile.getBaseDodgeChance() * (quantity == 0 ? profile.getDefenselessDodgeChanceMultiplier() : 1);
        }
        return 0;
    }
}
