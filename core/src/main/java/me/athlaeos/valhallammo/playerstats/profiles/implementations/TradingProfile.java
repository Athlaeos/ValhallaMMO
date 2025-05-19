package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.TradingSkill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Collection;

@SuppressWarnings("unused")
public class TradingProfile extends Profile {
    {
        floatStat("tradingLuck", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());
        floatStat("tradingDiscount", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("tradingStockMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("tradingStockBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());

        floatStat("trainingDiscount", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("serviceDiscount", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("orderDeliverySpeedMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("positiveRenownMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("negativeRenownMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("positiveReputationMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("negativeReputationMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("merchantSkillMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("merchantExperienceMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("enchantingExperienceMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());

        floatStat("tradeGiftChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        floatStat("renownGiftChanceModifier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("reputationGiftChanceModifier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("tradeGiftCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        floatStat("orderMaxMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        // TODO implement all these mechanics

        stringSetStat("exclusiveTrades");
        stringSetStat("unlockedServices");
        doubleStat("tradingEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
    }

    public Collection<String> getExclusiveTrades(){ return getStringSet("exclusiveTrades");}
    public void setExclusiveTrades(Collection<String> value){ setStringSet("exclusiveTrades", value);}

    public Collection<String> getUnlockedServices(){ return getStringSet("unlockedServices");}
    public void setUnlockedServices(Collection<String> value){ setStringSet("unlockedServices", value);}

    public float getTradingLuck(){ return getFloat("tradingLuck");}
    public void setTradingLuck(float value){ setFloat("tradingLuck", value);}

    public float getTradingDiscount(){ return getFloat("tradingDiscount");}
    public void setTradingDiscount(float value){ setFloat("tradingDiscount", value);}

    public float getTradingStockMultiplier(){ return getFloat("tradingStockMultiplier");}
    public void setTradingStockMultiplier(float value){ setFloat("tradingStockMultiplier", value);}

    public float getTradingStockBonus(){ return getFloat("tradingStockBonus");}
    public void setTradingStockBonus(float value){ setFloat("tradingStockBonus", value);}

    public float getTrainingDiscount(){ return getFloat("trainingDiscount");}
    public void setTrainingDiscount(float value){ setFloat("trainingDiscount", value);}

    public float getServiceDiscount(){ return getFloat("serviceDiscount");}
    public void setServiceDiscount(float value){ setFloat("serviceDiscount", value);}

    public float getOrderDeliverySpeedMultiplier(){ return getFloat("orderDeliverySpeedMultiplier");}
    public void setOrderDeliverySpeedMultiplier(float value){ setFloat("orderDeliverySpeedMultiplier", value);}

    public float getPositiveRenownMultiplier(){ return getFloat("positiveRenownMultiplier");}
    public void setPositiveRenownMultiplier(float value){ setFloat("positiveRenownMultiplier", value);}

    public float getNegativeRenownMultiplier(){ return getFloat("negativeRenownMultiplier");}
    public void setNegativeRenownMultiplier(float value){ setFloat("negativeRenownMultiplier", value);}

    public float getPositiveReputationMultiplier(){ return getFloat("positiveReputationMultiplier");}
    public void setPositiveReputationMultiplier(float value){ setFloat("positiveReputationMultiplier", value);}

    public float getNegativeReputationMultiplier(){ return getFloat("negativeReputationMultiplier");}
    public void setNegativeReputationMultiplier(float value){ setFloat("negativeReputationMultiplier", value);}

    public float getMerchantSkillMultiplier(){ return getFloat("merchantSkillMultiplier");}
    public void setMerchantSkillMultiplier(float value){ setFloat("merchantSkillMultiplier", value);}

    public float getMerchantExperienceMultiplier(){ return getFloat("merchantExperienceMultiplier");}
    public void setMerchantExperienceMultiplier(float value){ setFloat("merchantExperienceMultiplier", value);}

    public float getEnchantingExperienceMultiplier(){ return getFloat("enchantingExperienceMultiplier");}
    public void setEnchantingExperienceMultiplier(float value){ setFloat("enchantingExperienceMultiplier", value);}

    public float getTradeGiftChance(){ return getFloat("tradeGiftChance");}
    public void setTradeGiftChance(float value){ setFloat("tradeGiftChance", value);}

    public float getRenownGiftChanceModifier(){ return getFloat("renownGiftChanceModifier");}
    public void setRenownGiftChanceModifier(float value){ setFloat("renownGiftChanceModifier", value);}

    public float getReputationGiftChanceModifier(){ return getFloat("reputationGiftChanceModifier");}
    public void setReputationGiftChanceModifier(float value){ setFloat("reputationGiftChanceModifier", value);}

    public float getTradeGiftCooldown(){ return getFloat("tradeGiftCooldown");}
    public void setTradeGiftCooldown(float value){ setFloat("tradeGiftCooldown", value);}

    public double getTradingEXPMultiplier(){ return getDouble("tradingEXPMultiplier");}
    public void setTradingEXPMultiplier(double value){ setDouble("tradingEXPMultiplier", value);}


    public TradingProfile(Player owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
        return "profiles_trading";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "profile_trading");

    @Override
    public TradingProfile getBlankProfile(Player owner) {
        return new TradingProfile(owner);
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return TradingSkill.class;
    }
}
