package me.athlaeos.valhallammo.trading.dom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MerchantData {
    private static final Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ItemStackGSONAdapter()).create();
    private static final int reputationMax = CustomMerchantManager.getTradingConfig().getInt("reputation_upper_limit", 100);
    private static final int reputationMin = CustomMerchantManager.getTradingConfig().getInt("reputation_lower_limit", -100);
    private static final int renownMax = CustomMerchantManager.getTradingConfig().getInt("renown_upper_limit", 100);
    private static final int renownMin = CustomMerchantManager.getTradingConfig().getInt("renown_lower_limit", -100);
    private static final int demandMax = CustomMerchantManager.getTradingConfig().getInt("demand_max", 24);

    private final UUID villagerUUID;
    private final String type;
    private final int typeVersion;
    private int day; // keeps track of which day the merchant had last reset their demand tracker
    private final Map<String, TradeData> trades = new HashMap<>();
    private final Map<UUID, MerchantPlayerMemory> playerMemory = new HashMap<>(); // Tracks the amount of times a player traded with a villager. Partially used to determine if a player is a frequent customer or not.
    private int exp = 0;

    public MerchantData(AbstractVillager villager, MerchantType type, TradeData... data){
        this.villagerUUID = villager == null ? null : villager.getUniqueId();
        this.type = type.getType();
        this.typeVersion = type.getVersion();
        for (TradeData datum : data) trades.put(datum.getTrade(), datum);
        this.day = CustomMerchantManager.today();
    }

    public MerchantData(AbstractVillager villager, MerchantType type, Collection<TradeData> data){
        this.villagerUUID = villager == null ? null : villager.getUniqueId();
        this.type = type.getType();
        this.typeVersion = type.getVersion();
        for (TradeData datum : data) trades.put(datum.getTrade(), datum);
        this.day = CustomMerchantManager.today();
    }

    @NotNull
    public Map<String, TradeData> getTrades() {
        return trades;
    }

    public String getType() { return type; }
    public int getTypeVersion() { return typeVersion; }
    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }
    public MerchantPlayerMemory getPlayerMemory(UUID player) {
        if (!playerMemory.containsKey(player)) playerMemory.put(player, new MerchantPlayerMemory());
        return playerMemory.get(player);
    }
    public Map<UUID, MerchantPlayerMemory> getPlayerMemory() {
        return playerMemory;
    }
    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }
    public UUID getVillagerUUID() { return villagerUUID; }
    public AbstractVillager getVillager(){
        if (villagerUUID == null) return null;
        return ValhallaMMO.getInstance().getServer().getEntity(villagerUUID) instanceof AbstractVillager a ? a : null;
    }

    public static MerchantData deserialize(String data){
        return gson.fromJson(data, MerchantData.class);
    }

    public String serialize(){
        return gson.toJson(this);
    }

    public static class TradeData{
        private final String trade;
        private final int level;
        private final ItemStack item;
        private final int maxUses;
        private double remainingUses;
        private int demand = 0;
        private Map<UUID, Double> perPlayerRemainingUses = new HashMap<>();
        private final int basePrice;
        private long lastRestocked = -1;
        private long lastTraded = -1;
        public TradeData(String trade, int basePrice, int level, ItemStack item, int maxSales){
            this.trade = trade;
            this.basePrice = basePrice;
            this.level = level;
            this.item = item;
            this.maxUses = maxSales;
            this.remainingUses = maxSales;
        }

        public double getRemainingUses() { return remainingUses; }
        public int getDemand() { return demand; }
        public int getLevel() { return level; }
        public int getMaxUses() { return maxUses; }
        public ItemStack getItem() { return item; }
        public String getTrade() { return trade; }
        public long getLastRestocked() { return lastRestocked; }
        public long getLastTraded() { return lastTraded; }
        public int getBasePrice() { return basePrice; }
        public double getRemainingUses(Player player, boolean perPlayerStock){
            if (perPlayerStock) return perPlayerRemainingUses.getOrDefault(player.getUniqueId(), (double) maxUses);
            else return remainingUses;
        }

        public void setDemand(int demand) { this.demand = Math.min(demandMax, demand); }
        public void setRemainingUses(double remainingUses) { this.remainingUses = remainingUses; }
        public void setRemainingUses(Player player, double remainingUses, boolean perPlayerStock){
            if (perPlayerStock) perPlayerRemainingUses.put(player.getUniqueId(), remainingUses);
            else this.remainingUses = remainingUses;
        }
        public void resetRemainingUses(boolean perPlayerStock){
            if (perPlayerStock) perPlayerRemainingUses.clear();
            else this.remainingUses = maxUses;
        }
        public void setLastRestocked(long lastRestocked) { this.lastRestocked = lastRestocked; }
        public void setLastTraded(long lastTraded) { this.lastTraded = lastTraded; }
    }

    public static class MerchantPlayerMemory{
        private int timesTraded = 0;
        private long lastTimeTraded = 0;
        private float tradingReputation;
        private float renownReputation;
        private final Map<String, Double> perPlayerTradesLeft = new HashMap<>();

        public int getTimesTraded() { return timesTraded; }
        public float getRenownReputation() { return renownReputation; }
        public float getTradingReputation() { return tradingReputation; }
        public long getLastTimeTraded() { return lastTimeTraded; }
        public Map<String, Double> getPerPlayerTradesLeft() { return perPlayerTradesLeft; }
        public void setLastTimeTraded(long lastTimeTraded) { this.lastTimeTraded = lastTimeTraded; }
        public void setTimesTraded(int timesTraded) { this.timesTraded = timesTraded; }
        public void setRenownReputation(float renownReputation) { this.renownReputation = Math.max(renownMin, Math.min(renownMax, renownReputation)); }
        public void setTradingReputation(float tradingReputation) { this.tradingReputation = Math.max(reputationMin, Math.min(reputationMax, tradingReputation)); }
    }
}
