package me.athlaeos.valhallammo.trading.dom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MerchantData {
    private static final Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ItemStackGSONAdapter()).create();

    private final String type;
    private final int typeVersion;
    private final Map<String, TradeData> trades = new HashMap<>();
    private final Map<UUID, MerchantPlayerMemory> playerMemory = new HashMap<>(); // Tracks the amount of times a player traded with a villager. Partially used to determine if a player is a frequent customer or not.
    private float happiness = 0; // Happiness is determined by living conditions. The lower it is, the lower quality the trades. If low enough, trading is prevented entirely
    private int exp = 0;

    public MerchantData(MerchantType type, TradeData... data){
        this.type = type.getType();
        this.typeVersion = type.getVersion();
        for (TradeData datum : data) trades.put(datum.getTrade(), datum);
    }

    public MerchantData(MerchantType type, Collection<TradeData> data){
        this.type = type.getType();
        this.typeVersion = type.getVersion();
        for (TradeData datum : data) trades.put(datum.getTrade(), datum);
    }

    @NotNull
    public Map<String, TradeData> getTrades() {
        return trades;
    }

    public String getType() { return type; }
    public int getTypeVersion() { return typeVersion; }
    public float getHappiness() { return happiness; }
    public void setHappiness(float happiness) { this.happiness = happiness; }
    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }
    public MerchantPlayerMemory getPlayerMemory(UUID player) {
        if (!playerMemory.containsKey(player)) playerMemory.put(player, new MerchantPlayerMemory());
        return playerMemory.get(player);
    }

    public static MerchantData deserialize(String data){
        return gson.fromJson(data, MerchantData.class);
    }

    public String serialize(){
        return gson.toJson(this);
    }

    // TODO reset demand of all trades on restock and have it affect
    public static class TradeData{
        private final String trade;
        private final int level;
        private final ItemStack item;
        private final int maxSales;
        private double salesLeft;
        private int demand = 0; // TODO on restock, set demand to tradesUntilRestock and reset tradesUntilRestock
        private int tradesUntilRestock = 0;
        public TradeData(String trade, int level, ItemStack item, int maxSales){
            this.trade = trade;
            this.level = level;
            this.item = item;
            this.maxSales = maxSales;
            this.salesLeft = maxSales;
        }

        public int getTradesUntilRestock() { return tradesUntilRestock; }
        public double getSalesLeft() { return salesLeft; }
        public int getDemand() { return demand; }
        public int getLevel() { return level; }
        public int getMaxSales() { return maxSales; }
        public ItemStack getItem() { return item; }
        public String getTrade() { return trade; }

        public void setDemand(int demand) { this.demand = demand; }
        public void setTradesUntilRestock(int tradesUntilRestock) { this.tradesUntilRestock = tradesUntilRestock; }
        public void setSalesLeft(double salesLeft) { this.salesLeft = salesLeft; }

        public void restock(){
            this.demand = tradesUntilRestock;
            tradesUntilRestock = 0;
            salesLeft = maxSales;
        }
    }

    public static class MerchantPlayerMemory{
        private int timesTraded = 0;
        private long lastTimeTraded = 0;
        private float tradingReputation;
        private float renownReputation;

        public int getTimesTraded() { return timesTraded; }
        public float getRenownReputation() { return renownReputation; }
        public float getTradingReputation() { return tradingReputation; }
        public long getLastTimeTraded() { return lastTimeTraded; }
        public void setLastTimeTraded(long lastTimeTraded) { this.lastTimeTraded = lastTimeTraded; }
        public void setTimesTraded(int timesTraded) { this.timesTraded = timesTraded; }
        public void setRenownReputation(float renownReputation) { this.renownReputation = renownReputation; }
        public void setTradingReputation(float tradingReputation) { this.tradingReputation = tradingReputation; }
    }
}
