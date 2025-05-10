package me.athlaeos.valhallammo.trading.happiness.sources;

import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.happiness.HappinessSource;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Security implements HappinessSource {
    private final int insecurityRequirement = CustomMerchantManager.getTradingConfig().getInt("insecurity_max");
    private final int securityMax = CustomMerchantManager.getTradingConfig().getInt("security_requirement");
    private final float insecurityHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.insecure");
    private final float securityHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.secure");

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
    public boolean appliesTo(Entity entity) {
        return entity instanceof AbstractVillager;
    }
}
