package me.athlaeos.valhallammo.trading.happiness.sources;

import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.happiness.HappinessSource;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Company implements HappinessSource {
    private final int accompaniedRadius = CustomMerchantManager.getTradingConfig().getInt("accompanied_radius", 64);
    private final int accompaniedRequirement = CustomMerchantManager.getTradingConfig().getInt("accompanied_requirement", 5);
    private final float accompaniedHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.accompanied", 1);
    private final int lonelyRequirement = CustomMerchantManager.getTradingConfig().getInt("loneliness_max", 5);
    private final float lonelyHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.lonely", 1);
    private final int overcrowdedRadius = CustomMerchantManager.getTradingConfig().getInt("overcrowded_radius", 8);
    private final int overcrowdedRequirement = CustomMerchantManager.getTradingConfig().getInt("overcrowded_requirement", 5);
    private final float overcrowdedHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.overcrowded", -5);

    @Override
    public String id() {
        return "POPULATION_DENSITY";
    }

    @Override
    public float get(Player contextPlayer, Entity entity) {
        int inOvercrowdedRadius = 0;
        for (Entity e : entity.getNearbyEntities(overcrowdedRadius, overcrowdedRadius, overcrowdedRadius)){
            if (!(e instanceof AbstractVillager) || e.equals(entity)) continue;
            inOvercrowdedRadius++;
        }
        if (inOvercrowdedRadius >= overcrowdedRequirement) return overcrowdedHappiness;
        int inAccompaniedRadius = 0;
        for (Entity e : entity.getNearbyEntities(accompaniedRadius, accompaniedRadius, accompaniedRadius)){
            if (!(e instanceof AbstractVillager) || e.equals(entity)) continue;
            inAccompaniedRadius++;
        }
        if (inAccompaniedRadius <= lonelyRequirement) return lonelyHappiness;
        if (inAccompaniedRadius >= accompaniedRequirement) return accompaniedHappiness;
        return 0;
    }

    @Override
    public boolean appliesTo(Entity entity) {
        return entity instanceof AbstractVillager;
    }
}
