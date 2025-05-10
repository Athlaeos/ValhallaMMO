package me.athlaeos.valhallammo.trading.happiness.sources;

import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.happiness.HappinessSource;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PlayerRelation implements HappinessSource {
    private final float reputationHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.reputation_trading", 1);
    private final float renownHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.reputation_renown", -5);

    @Override
    public String id() {
        return "PLAYER_RELATION";
    }

    @Override
    public float get(Player contextPlayer, Entity entity) {
        MerchantData data = CustomMerchantManager.getMerchantDataPersistence().getAllData().get(entity.getUniqueId());
        if (data == null || contextPlayer == null) return 0;
        MerchantData.MerchantPlayerMemory memory = data.getPlayerMemory(contextPlayer.getUniqueId());
        return reputationHappiness * memory.getTradingReputation() + renownHappiness * memory.getRenownReputation();
    }

    @Override
    public boolean appliesTo(Entity entity) {
        return entity instanceof AbstractVillager;
    }
}
