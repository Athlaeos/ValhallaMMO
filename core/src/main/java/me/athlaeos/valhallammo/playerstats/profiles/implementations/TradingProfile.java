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
        floatStat("tradingStockMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("tradingStockBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());

        floatStat("trainingDiscount", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("serviceDiscount", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("orderDeliverySpeedMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("positiveRenownMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("negativeRenownMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("positiveReputationMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("negativeReputationMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("merchantSkillMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("merchantExperienceMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("enchantingExperienceMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());

        floatStat("tradeGiftChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        floatStat("renownGiftChanceModifier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        floatStat("reputationGiftChanceModifier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        floatStat("tradeGiftCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        // TODO implement all these mechanics

        stringSetStat("exclusiveTrades");
        stringSetStat("unlockedServices");
        doubleStat("tradingEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
    }

    public Collection<String> getExclusiveTrades(){ return getStringSet("exclusiveTrades");}
    public void setExclusiveTrades(Collection<String> value){ setStringSet("exclusiveTrades", value);}


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
