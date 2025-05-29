package me.athlaeos.valhallammo.trading.dom;

import me.athlaeos.valhallammo.dom.Weighted;

import java.util.*;

public class MerchantType implements Weighted {
    private final String type;
    private int version = 0; // if the version saved on the villager is lower than that of their type, their trades are reset
    private String name = null;
    private boolean resetTradesDaily = false; // if realistic, villagers reset their trades when they restock
    private boolean canLoseProfession = true; // if true, villagers with this merchant type may lose their profession if they have no exp or levels
    private boolean perPlayerStock = false; // if perPlayerStock, each player can trade up to the trade's limit times
    private double weight = 10; // the weight determines how likely this merchant type is to be picked as a new villager's type
    private final Map<MerchantLevel, MerchantLevelTrades> trades = new HashMap<>(Map.of(
            MerchantLevel.NOVICE, new MerchantLevelTrades(0, 2, 0, new HashSet<>()),
            MerchantLevel.APPRENTICE, new MerchantLevelTrades(10, 2, 0, new HashSet<>()),
            MerchantLevel.JOURNEYMAN, new MerchantLevelTrades(40, 2, 0, new HashSet<>()),
            MerchantLevel.EXPERT, new MerchantLevelTrades(160, 2, 0, new HashSet<>()),
            MerchantLevel.MASTER, new MerchantLevelTrades(640, 2, 0, new HashSet<>())
    ));
    private final Collection<String> services = new HashSet<>(Set.of("trading"));

    public MerchantType(String type){
        this.type = type;
    }

    public String getType() { return type; }
    public void setName(String name) { this.name = name; }
    public void setResetTradesDaily(boolean resetTradesDaily) { this.resetTradesDaily = resetTradesDaily; }
    public void setPerPlayerStock(boolean perPlayerStock) { this.perPlayerStock = perPlayerStock; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setVersion(int version) { this.version = version; }
    public void setCanLoseProfession(boolean canLoseProfession) { this.canLoseProfession = canLoseProfession; }
    public String getName() { return name; }
    public boolean resetsTradesDaily() { return resetTradesDaily; }
    public boolean isPerPlayerStock() { return perPlayerStock; }
    public double getWeight() { return weight; }
    public Collection<String> getServices() { return services; }

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
        trades.get(level).getTrades().add(trade.getID());
    }

    public double getRolls(MerchantLevel level){
        return Math.max(0, trades.get(level).getRolls());
    }
    public double getRollQuality(MerchantLevel level){
        return Math.max(0, trades.get(level).getRollQuality());
    }
    public int getExpRequirement(MerchantLevel level){
        if (level == MerchantLevel.NOVICE) return 0;
        int accumulated = 0;
        for (MerchantLevel l : MerchantLevel.values()) {
            if (l.getLevel() > level.getLevel()) break;
            accumulated += Math.max(0, trades.get(l).getExpRequirement());
        }
        return accumulated;
    }
    public int getRawExpRequirement(MerchantLevel level){
        if (level == MerchantLevel.NOVICE) return 0;
        return trades.get(level).getExpRequirement();
    }
    public void setRolls(MerchantLevel level, double rolls) {
        trades.get(level).setRolls(rolls);
    }
    public void setRollQuality(MerchantLevel level, double rollQuality) {
        trades.get(level).setRollQuality(rollQuality);
    }
    public void setExpRequirement(MerchantLevel level, int expRequirement) {
        trades.get(level).setExpRequirement(expRequirement);
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
        public void setExpRequirement(int expRequirement) { this.expRequirement = expRequirement; }
        public void setRollQuality(double rollQuality) { this.rollQuality = rollQuality; }
        public Collection<String> getTrades() { return trades; }
        public double getRollQuality() { return rollQuality; }
        public int getExpRequirement() { return expRequirement; }
    }
}
