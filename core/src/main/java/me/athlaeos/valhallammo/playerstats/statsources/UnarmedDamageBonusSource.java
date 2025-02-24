package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.MartialArtsProfile;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class UnarmedDamageBonusSource implements AccumulativeStatSource {
    @Override
    public double fetch(Entity p, boolean use) {
        if (p instanceof Player pl){
            MartialArtsProfile profile = ProfileCache.getOrCache(pl, MartialArtsProfile.class);
            if (!EntityUtils.isUnarmed(pl)) return 0;
            return profile.getDamageBonus();
        }
        return 0;
    }
}
