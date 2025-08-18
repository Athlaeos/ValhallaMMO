package me.athlaeos.valhallammo.trading.happiness.sources;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.happiness.HappinessSource;
import me.athlaeos.valhallammo.trading.happiness.HappinessSourceRegistry;
import me.athlaeos.valhallammo.utility.StringUtils;
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
    private final String happy = TranslationManager.getTranslation("happiness_status_company_happy");
    private final String neutral = TranslationManager.getTranslation("happiness_status_company_neutral");
    private final String unhappy = TranslationManager.getTranslation("happiness_status_company_unhappy");
    private final String overcrowded = TranslationManager.getTranslation("happiness_status_company_overcrowded");

    @Override
    public String id() {
        return "POPULATION_DENSITY";
    }

    @Override
    public float get(Player contextPlayer, Entity entity) {
        return getStatus(contextPlayer, entity).getOne();
    }

    private Pair<Float, PopulationStatus> getStatus(Player contextPLayer, Entity entity){
        int inOvercrowdedRadius = 0;
        for (Entity e : entity.getNearbyEntities(overcrowdedRadius, overcrowdedRadius, overcrowdedRadius)){
            if (!(e instanceof AbstractVillager) || e.equals(entity)) continue;
            inOvercrowdedRadius++;
        }
        if (inOvercrowdedRadius >= overcrowdedRequirement) return new Pair<>(overcrowdedHappiness, PopulationStatus.OVERPOPULATED);
        int inAccompaniedRadius = 0;
        for (Entity e : entity.getNearbyEntities(accompaniedRadius, accompaniedRadius, accompaniedRadius)){
            if (!(e instanceof AbstractVillager) || e.equals(entity)) continue;
            inAccompaniedRadius++;
        }
        if (inAccompaniedRadius <= lonelyRequirement) return new Pair<>(lonelyHappiness, PopulationStatus.LONELY);
        if (inAccompaniedRadius >= accompaniedRequirement) return new Pair<>(accompaniedHappiness, PopulationStatus.ACCOMPANIED);
        return new Pair<>(0F, PopulationStatus.NEUTRAL);
    }

    @Override
    public String getHappinessStatus(float happiness, Player contextPlayer, Entity entity) {
        Pair<Float, PopulationStatus> status = getStatus(contextPlayer, entity);
        return (switch(status.getTwo()){
            case LONELY -> unhappy;
            case ACCOMPANIED -> happy;
            case OVERPOPULATED -> overcrowded;
            case NEUTRAL -> neutral;
        }).replace("%prefix%", switch(status.getTwo()){
            case LONELY, OVERPOPULATED -> HappinessSourceRegistry.unhappyPrefix();
            case ACCOMPANIED -> HappinessSourceRegistry.happyPrefix();
            case NEUTRAL -> HappinessSourceRegistry.neutralPrefix();
        }).replace("%happiness%", StringUtils.trimTrailingZeroes(String.format("%.1f", happiness)));
    }

    @Override
    public boolean appliesTo(Entity entity) {
        return entity instanceof AbstractVillager;
    }

    private enum PopulationStatus{
        LONELY,
        ACCOMPANIED,
        OVERPOPULATED,
        NEUTRAL
    }
}
