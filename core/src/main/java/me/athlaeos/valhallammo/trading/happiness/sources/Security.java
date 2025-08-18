package me.athlaeos.valhallammo.trading.happiness.sources;

import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.happiness.HappinessSource;
import me.athlaeos.valhallammo.trading.happiness.HappinessSourceRegistry;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Security implements HappinessSource {
    private final int insecurityRequirement = CustomMerchantManager.getTradingConfig().getInt("insecurity_max");
    private final int securityMax = CustomMerchantManager.getTradingConfig().getInt("security_requirement");
    private final float insecurityHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.insecure");
    private final float securityHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.secure");
    private final String happy = TranslationManager.getTranslation("happiness_status_confidence_happy");
    private final String neutral = TranslationManager.getTranslation("happiness_status_confidence_neutral");
    private final String unhappy = TranslationManager.getTranslation("happiness_status_confidence_unhappy");

    @Override
    public String id() {
        return "SECURITY";
    }

    @Override
    public float get(Player contextPlayer, Entity entity) {
        MerchantData data = CustomMerchantManager.getMerchantDataPersistence().getAllData().get(entity.getUniqueId());
        if (data == null || contextPlayer == null) return 0;
        long timeLastTradedWith = -1;
        for (MerchantData.TradeData d : data.getTrades().values()){
            if (d.getLastTraded() > timeLastTradedWith) timeLastTradedWith = d.getLastTraded();
        }
        if (timeLastTradedWith < 0) return 0;
        long difference = CustomMerchantManager.time() - timeLastTradedWith;
        if (difference <= securityMax) return securityHappiness;
        else if (difference >= insecurityRequirement) return insecurityHappiness;
        else return 0;
    }

    @Override
    public String getHappinessStatus(float happiness, Player contextPlayer, Entity entity) {
        return (happiness > 0.001 ? happy : happiness < -0.001 ? unhappy : neutral)
                .replace("%prefix%", happiness > 0.001 ? HappinessSourceRegistry.happyPrefix() : happiness < -0.001 ? HappinessSourceRegistry.unhappyPrefix() : HappinessSourceRegistry.neutralPrefix())
                .replace("%happiness%", StringUtils.trimTrailingZeroes(String.format("%.1f", happiness)));
    }

    @Override
    public boolean appliesTo(Entity entity) {
        return entity instanceof AbstractVillager;
    }
}
