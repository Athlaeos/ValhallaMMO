package me.athlaeos.valhallammo.trading.happiness.sources;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.happiness.HappinessSource;
import me.athlaeos.valhallammo.version.ActivityMappings;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;

public class Stress implements HappinessSource {
    private final float fearfulHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.fearful");
    private final float peacefulHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.peaceful");
    private final Collection<ActivityMappings> panicModes = Set.of(ActivityMappings.PANIC, ActivityMappings.RAID, ActivityMappings.PRE_RAID);

    @Override
    public String id() {
        return "STRESS";
    }

    @Override
    public float get(Player contextPlayer, Entity entity) {
        if (!(entity instanceof AbstractVillager v)) return 0;
        ActivityMappings activity = ValhallaMMO.getNms().getActivity(v);
        if (activity == null || !panicModes.contains(activity)) return peacefulHappiness;
        return fearfulHappiness;
    }

    @Override
    public boolean appliesTo(Entity entity) {
        return entity instanceof AbstractVillager;
    }
}
