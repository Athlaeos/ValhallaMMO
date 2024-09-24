package me.athlaeos.valhallammo.trading;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MerchantType {
    private final String type;
    private String name = null;
    private boolean realistic = false; // if realistic, villagers reset their trades when they restock
    private boolean canLoseProfession = true; // if true, villagers with this merchant type may lose their profession if they have no exp or levels
    private boolean perPlayerStock = false; // if perPlayerStock, each player can trade up to the trade's limit times
    private double weight = 10; // the weight determines how likely this merchant type is to be picked as a new villager's type
    private final Map<MerchantLevel, MerchantLevelTrades> trades = new HashMap<>(Map.of(
            MerchantLevel.NOVICE, new MerchantLevelTrades(2, 0, new HashSet<>()),
            MerchantLevel.APPRENTICE, new MerchantLevelTrades(2, 0, new HashSet<>()),
            MerchantLevel.JOURNEYMAN, new MerchantLevelTrades(2, 0, new HashSet<>()),
            MerchantLevel.EXPERT, new MerchantLevelTrades(2, 0, new HashSet<>()),
            MerchantLevel.MASTER, new MerchantLevelTrades(2, 0, new HashSet<>())
    ));
    private final Map<MerchantLevel, Integer> levelDetails = new HashMap<>(Map.of(
            MerchantLevel.NOVICE, 0,
            MerchantLevel.APPRENTICE, 0,
            MerchantLevel.JOURNEYMAN, 0,
            MerchantLevel.EXPERT, 0,
            MerchantLevel.MASTER, 0
    ));

    public MerchantType(String type){
        this.type = type;
    }

    public String getType() { return type; }
    public void setName(String name) { this.name = name; }
    public void setRealistic(boolean realistic) { this.realistic = realistic; }
    public void setPerPlayerStock(boolean perPlayerStock) { this.perPlayerStock = perPlayerStock; }
    public void setWeight(double weight) { this.weight = weight; }
    public String getName() { return name; }
    public boolean isRealistic() { return realistic; }
    public boolean isPerPlayerStock() { return perPlayerStock; }
    public double getWeight() { return weight; }

    public Collection<String> getTrades(MerchantLevel level){
        return trades.get(level).getTrades();
    }

    public double getRolls(MerchantLevel level, double luck){
        return Math.max(0, trades.get(level).getRolls() + (luck * trades.get(level).getRollQuality()));
    }

    private static class MerchantLevelDetails{
        private int requiredEXP;
    }

    private static class MerchantLevelTrades{
        private double rolls;
        private double rollQuality;
        private final Collection<String> trades;

        private MerchantLevelTrades(double rolls, double rollQuality, Collection<String> trades){
            this.rolls = rolls;
            this.rollQuality = rollQuality;
            this.trades = trades;
        }

        public double getRollQuality() { return rollQuality; }
        public double getRolls() { return rolls; }
        public void setRollQuality(double rollQuality) { this.rollQuality = rollQuality; }
        public void setRolls(double rolls) { this.rolls = rolls; }
        public Collection<String> getTrades() { return trades; }
    }
}
