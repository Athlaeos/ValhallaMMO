package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.TradingProfile;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import org.bukkit.entity.*;

public class TradingReputationGiftChanceSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    @Override
    public double fetch(Entity p, boolean use) {
        return 0;
    }

    @Override
    public double fetch(Entity victim, Entity attackedBy, boolean use) {
        if (!(victim instanceof AbstractVillager v) || !(attackedBy instanceof Player p)) return 0;

        MerchantData data = CustomMerchantManager.getMerchantDataPersistence().getAllData().get(v.getUniqueId());
        if (data == null) return 0;
        TradingProfile profile = ProfileCache.getOrCache(p, TradingProfile.class);
        MerchantData.MerchantPlayerMemory memory = data.getPlayerMemory(p.getUniqueId());
        return memory.getTradingReputation() * profile.getReputationGiftChanceModifier();
    }
}
