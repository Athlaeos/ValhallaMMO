package me.athlaeos.valhallammo.trading;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.dom.Weighted;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.persistence.GsonAdapter;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class CustomTradeRegistry {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LootPredicate.class, new GsonAdapter<LootPredicate>("PRED_TYPE"))
            .registerTypeAdapter(DynamicItemModifier.class, new GsonAdapter<DynamicItemModifier>("MOD_TYPE"))
            .registerTypeAdapter(Weighted.class, new GsonAdapter<Weighted>("WEIGHTED_IMPL"))
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ItemStackGSONAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();
    private static final NamespacedKey KEY_TRADE_ID = new NamespacedKey(ValhallaMMO.getInstance(), "trade_id");

    private int delayUntilWorking = 24000; // the time it takes for a villager who just got a profession to be able to work/be interacted with, in game time

    private static final Map<String, MerchantConfiguration> registeredMerchantConfigurations = new HashMap<>();
    private static final Map<Villager.Profession, MerchantConfiguration> merchantConfigurationByProfession = new HashMap<>();
    private static final Map<String, MerchantType> registeredMerchantTypes = new HashMap<>();
    private static final Map<String, MerchantTrade> registeredMerchantTrades = new HashMap<>();

    static {
        MerchantConfiguration configuration = new MerchantConfiguration("test", Villager.Profession.NONE);
        MerchantType type = new MerchantType("test");
        MerchantTrade t1 = new MerchantTrade("test1");
    }

    /**
     * Takes a villager and turns them into a custom trader. <br>
     * During this trade selection the individual trade's conditions will be ignored, and so the villager will have
     * every trade accessible to them *after* the weighted selection procedure is done. <br>
     * If `persist` is true, the villager will have {@link MerchantData} persisted to their PersistentDataContainer during this. <br>
     * If a villager already has custom trades, they will be overwritten.
     * @param villager The villager to be granted custom trades
     * @param persist If true, the returned MerchantData is also saved to the villager. If false, it is only returned
     * @return The new MerchantData representing all trades the villager has been granted, or null if no recipes were added
     */
    public static MerchantData createCustomTrader(Villager villager, boolean persist){
        MerchantConfiguration configuration = merchantConfigurationByProfession.get(villager.getProfession());
        if (configuration == null) return null;
        Collection<MerchantType> types = new HashSet<>();
        for (String type : new HashSet<>(configuration.getMerchantTypes())) {
            if (registeredMerchantTypes.containsKey(type)) types.add(registeredMerchantTypes.get(type));
            else {
                configuration.getMerchantTypes().remove(type);
                ValhallaMMO.logWarning("Merchant Configuration " + configuration.getId() + " had Merchant sub-type " + type + " added to it, but it doesn't exist! Removed.");
            }
        }
        MerchantType selectedType = Utils.weightedSelection(types, 1, 0, 0).stream().findFirst().orElse(null);
        if (selectedType == null) return null;
        List<MerchantData.TradeData> trades = new ArrayList<>();
        for (MerchantLevel level : selectedType.getTrades().keySet()){
            MerchantType.MerchantLevelTrades levelTrades = selectedType.getTrades(level);
            Collection<MerchantTrade> merchantTrades = new HashSet<>();
            for (String trade : new HashSet<>(levelTrades.getTrades())) {
                if (registeredMerchantTrades.containsKey(trade)) merchantTrades.add(registeredMerchantTrades.get(trade));
                else {
                    levelTrades.getTrades().remove(trade);
                    ValhallaMMO.logWarning("Merchant Type " + selectedType.getType() + " had trade " + trade + " added to it, but it doesn't exist! Removed.");
                }
            }
            Collection<MerchantTrade> selectedTrades = new HashSet<>(merchantTrades.stream().filter(t -> t.getWeight() == -1).toList());
            merchantTrades.removeIf(t -> t.getWeight() == -1);
            selectedTrades.addAll(Utils.weightedSelection(merchantTrades, Utils.randomAverage(selectedType.getRolls(level)), 0, 0));
            selectedTrades.forEach(t -> {
                ItemBuilder result = new ItemBuilder(t.getResult());
                DynamicItemModifier.modify(result, null, t.getModifiers(), false, true, true);
                setTradeKey(result.getMeta(), t);
                trades.add(new MerchantData.TradeData(t.getId(), level.getLevel(), result.get(), t.getMaxUses(), t.getMaxUses(), 0));
            });
        }
        MerchantData data = new MerchantData(selectedType, trades.toArray(new MerchantData.TradeData[0]));
        if (persist) data.serialize(villager);
        return data;
    }

    public static MerchantData getCustomTrader(Villager villager, boolean createIfAbsent){
        MerchantData data = MerchantData.deserialize(villager);
        if (createIfAbsent && data == null) data = createCustomTrader(villager, true);
        return data;
    }

    public static List<MerchantRecipe> recipesFromVillager(Villager villager, Player player){
        MerchantData data = MerchantData.deserialize(villager);
        if (data == null) return null;
        MerchantType type = registeredMerchantTypes.get(data.getType());
        if (type == null || data.getTypeVersion() < type.getVersion()) data = createCustomTrader(villager, true);
        if (data == null) return null;
        List<MerchantRecipe> recipes = new ArrayList<>();
        boolean modified = false;
        List<MerchantTrade> trades = new ArrayList<>();
        for (String tradeName : new HashSet<>(data.getTrades().keySet())){
            MerchantTrade trade = registeredMerchantTrades.get(tradeName);
            if (trade == null){
                data.getTrades().remove(tradeName);
                modified = true;
                String error = String.format("Villager at %d, %d, %d had a trade that no longer exists! Removed now", villager.getLocation().getBlockX(), villager.getLocation().getBlockY(), villager.getLocation().getBlockZ());
                ValhallaMMO.logWarning(error);
                player.sendMessage(Utils.chat("&c" + error + ", please notify admin"));
                continue;
            }
            trades.add(trade);
        }
        for (MerchantTrade trade : trades){
            MerchantData.TradeData tradeData = data.getTrades().get(trade.getId());
            if (tradeData.getLevel() > villager.getVillagerLevel()) continue;
            double maxUses = trade.getMaxUses();
            double remainingUses = tradeData.getSalesLeft();
            if (!trade.hasFixedUseCount()){
                double tradeCountMultiplier = 1; // TODO AccumulativeStatManager.getCachedStats("TRADE_USE_MULTIPLIER", player, 10000, true);
                maxUses *= tradeCountMultiplier;
                remainingUses *= tradeCountMultiplier;
            }
            if (remainingUses > maxUses) remainingUses = maxUses;
            int uses = (int) Math.floor(maxUses - remainingUses);
            MerchantRecipe recipe = new MerchantRecipe(tradeData.getItem(), uses, (int) Math.floor(maxUses), trade.rewardsExperience(), trade.getVillagerExperience(), trade.getDemandMultiplier(), tradeData.getDemand(), trade.getSpecialPrice());
            recipes.add(recipe);
        }
        if (modified) data.serialize(villager);
        return recipes;
    }

    public static void registerTrade(MerchantTrade trade){
        registeredMerchantTrades.put(trade.getId(), trade);
    }

    public static void registerMerchantType(MerchantType type){
        registeredMerchantTypes.put(type.getType(), type);
    }

    public static void registerConfiguration(MerchantConfiguration configuration){
        registeredMerchantConfigurations.put(configuration.getId(), configuration);
        merchantConfigurationByProfession.put(configuration.getType(), configuration);
    }

    public int getDelayUntilWorking() {
        return delayUntilWorking;
    }

    public static Map<Villager.Profession, MerchantConfiguration> getMerchantConfigurationByProfession() {
        return new HashMap<>(merchantConfigurationByProfession);
    }

    public static Map<String, MerchantConfiguration> getRegisteredMerchantConfigurations() {
        return new HashMap<>(registeredMerchantConfigurations);
    }

    public static Map<String, MerchantTrade> getRegisteredMerchantTrades() {
        return new HashMap<>(registeredMerchantTrades);
    }

    public static Map<String, MerchantType> getRegisteredMerchantTypes() {
        return new HashMap<>(registeredMerchantTypes);
    }

    public static void removeTradeKey(ItemMeta meta){
        meta.getPersistentDataContainer().remove(KEY_TRADE_ID);
    }

    public static void setTradeKey(ItemMeta meta, MerchantTrade trade){
        meta.getPersistentDataContainer().set(KEY_TRADE_ID, PersistentDataType.STRING, trade.getId());
    }

    public static MerchantTrade tradeFromKeyedMeta(ItemMeta meta){
        return registeredMerchantTrades.get(meta.getPersistentDataContainer().getOrDefault(KEY_TRADE_ID, PersistentDataType.STRING, ""));
    }
}
