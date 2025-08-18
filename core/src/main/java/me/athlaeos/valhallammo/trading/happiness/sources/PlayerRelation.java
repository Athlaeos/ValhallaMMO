package me.athlaeos.valhallammo.trading.happiness.sources;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.happiness.HappinessSource;
import me.athlaeos.valhallammo.trading.happiness.HappinessSourceRegistry;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PlayerRelation implements HappinessSource {
    private final float reputationHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.reputation_trading", 1);
    private final float renownHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.reputation_renown", -5);
    private final String message = TranslationManager.getTranslation("happiness_status_relationship");

    @Override
    public String id() {
        return "PLAYER_RELATION";
    }

    @Override
    public float get(Player contextPlayer, Entity entity) {
        Pair<Float, Float> values = getValues(contextPlayer, entity);
        return values.getOne() + values.getTwo();
    }

    private Pair<Float, Float> getValues(Player contextPlayer, Entity entity){
        MerchantData data = CustomMerchantManager.getMerchantDataPersistence().getAllData().get(entity.getUniqueId());
        if (data == null || contextPlayer == null) return new Pair<>(0F, 0F);
        MerchantData.MerchantPlayerMemory memory = data.getPlayerMemory(contextPlayer.getUniqueId());
        return new Pair<>(renownHappiness * memory.getRenownReputation(), reputationHappiness * memory.getTradingReputation());
    }

    @Override
    public String getHappinessStatus(float happiness, Player contextPlayer, Entity entity) {
        Pair<Float, Float> values = getValues(contextPlayer, entity);
        return message.replace("%renownprefix%", values.getOne() < 0.001 ? HappinessSourceRegistry.unhappyPrefix() : values.getOne() > 0.001 ? HappinessSourceRegistry.happyPrefix() : HappinessSourceRegistry.neutralPrefix())
                .replace("%reputationprefix%", values.getTwo() < 0.001 ? HappinessSourceRegistry.unhappyPrefix() : values.getTwo() > 0.001 ? HappinessSourceRegistry.happyPrefix() : HappinessSourceRegistry.neutralPrefix())
                .replace("%renown%", StringUtils.trimTrailingZeroes(String.format("%.1f", values.getOne())))
                .replace("%reputation%", StringUtils.trimTrailingZeroes(String.format("%.1f", values.getTwo())));
    }

    @Override
    public boolean appliesTo(Entity entity) {
        return entity instanceof AbstractVillager;
    }
}
