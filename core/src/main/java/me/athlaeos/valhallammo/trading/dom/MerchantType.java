package me.athlaeos.valhallammo.trading.dom;

import me.athlaeos.valhallammo.dom.Weighted;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MerchantType implements Weighted {
    private final String type;
    private int version = 0; // if the version saved on the villager is lower than that of their type, their trades are reset
    private String name = null;
    private boolean resetTradesOnRestock = false; // if realistic, villagers reset their trades when they restock
    private boolean canLoseProfession = true; // if true, villagers with this merchant type may lose their profession if they have no exp or levels
    private boolean perPlayerStock = false; // if perPlayerStock, each player can trade up to the trade's limit times
    private double weight = 10; // the weight determines how likely this merchant type is to be picked as a new villager's type
    private final Map<MerchantLevel, MerchantLevelTrades> trades = new HashMap<>(Map.of(
            MerchantLevel.NOVICE, new MerchantLevelTrades(0, 2, 0, new HashSet<>()),
            MerchantLevel.APPRENTICE, new MerchantLevelTrades(10, 2, 0, new HashSet<>()),
            MerchantLevel.JOURNEYMAN, new MerchantLevelTrades(50, 2, 0, new HashSet<>()),
            MerchantLevel.EXPERT, new MerchantLevelTrades(210, 2, 0, new HashSet<>()),
            MerchantLevel.MASTER, new MerchantLevelTrades(850, 2, 0, new HashSet<>())
    ));

    public MerchantType(String type){
        this.type = type;
    }

    public String getType() { return type; }
    public void setName(String name) { this.name = name; }
    public void setResetTradesOnRestock(boolean resetTradesOnRestock) { this.resetTradesOnRestock = resetTradesOnRestock; }
    public void setPerPlayerStock(boolean perPlayerStock) { this.perPlayerStock = perPlayerStock; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setVersion(int version) { this.version = version; }
    public void setCanLoseProfession(boolean canLoseProfession) { this.canLoseProfession = canLoseProfession; }
    public String getName() { return name; }
    public boolean isResetTradesOnRestock() { return resetTradesOnRestock; }
    public boolean isPerPlayerStock() { return perPlayerStock; }
    public double getWeight() { return weight; }

    @Override
    public double getWeight(double luck, double fortune) {
        return weight;
    }

    public boolean canLoseProfession() { return canLoseProfession; }
    public int getVersion() { return version; }

    public MerchantLevelTrades getTrades(MerchantLevel level){
        return trades.get(level);
    }

    public Map<MerchantLevel, MerchantLevelTrades> getTrades() {
        return trades;
    }

    public void addTrade(MerchantLevel level, MerchantTrade trade){
        trades.get(level).getTrades().add(trade.getId());
    }

    public double getRolls(MerchantLevel level){
        return Math.max(0, trades.get(level).getRolls());
    }

    public static class MerchantLevelTrades{
        private double rolls;
        private double rollQuality;
        private int expRequirement;
        private final Collection<String> trades;

        private MerchantLevelTrades(int expRequirement, double rolls, double rollQuality, Collection<String> trades){
            this.expRequirement = expRequirement;
            this.rolls = rolls;
            this.rollQuality = rollQuality;
            this.trades = trades;
        }

        public double getRolls() { return rolls; }
        public void setRolls(double rolls) { this.rolls = rolls; }
        public Collection<String> getTrades() { return trades; }
        public double getRollQuality() { return rollQuality; }
        public int getExpRequirement() { return expRequirement; }
    }
}
