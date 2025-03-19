package me.athlaeos.valhallammo.trading;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.dom.Weighted;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.persistence.Database;
import me.athlaeos.valhallammo.persistence.GsonAdapter;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import me.athlaeos.valhallammo.trading.data.MerchantConfigurationData;
import me.athlaeos.valhallammo.trading.data.MerchantDataPersistence;
import me.athlaeos.valhallammo.trading.data.implementations.SQLite;
import me.athlaeos.valhallammo.trading.dom.*;
import me.athlaeos.valhallammo.utility.Callback;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CustomMerchantManager {
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
    private static final NamespacedKey KEY_CUSTOM_VILLAGER = new NamespacedKey(ValhallaMMO.getInstance(), "is_custom_villager");

    private final int delayUntilWorking = ValhallaMMO.getPluginConfig().getInt("delay_until_working"); // the time it takes for a villager who just got a profession to be able to work/be interacted with, in game time
    private static MerchantDataPersistence merchantDataPersistence;

    private static final Map<Villager.Profession, MerchantConfiguration> merchantConfigurations = new HashMap<>();
    private static final Map<String, MerchantType> registeredMerchantTypes = new HashMap<>();
    private static final Map<String, MerchantTrade> registeredMerchantTrades = new HashMap<>();

    /**
     * Takes a villager and turns them into a custom trader, of a type fitting their profession. <br>
     * During this trade selection the individual trade's conditions will be ignored, and so the villager will have
     * every trade accessible to them *after* the weighted selection procedure is done. <br>
     * If a villager already has custom trades, they will be overwritten.
     * @param villager The villager to be granted custom trades
     * @return The new MerchantData representing all trades the villager has been granted, or null if no recipes were added
     */
    public static MerchantData convertToRandomMerchant(Villager villager){
        villager.getPersistentDataContainer().set(KEY_CUSTOM_VILLAGER, PersistentDataType.BYTE, (byte) 0);
        MerchantConfiguration configuration = merchantConfigurations.get(villager.getProfession());
        if (configuration == null || configuration.getMerchantTypes().isEmpty()) return null; // No configuration available, do not do anything
        Collection<MerchantType> types = new HashSet<>();
        for (String type : new HashSet<>(configuration.getMerchantTypes())) {
            if (registeredMerchantTypes.containsKey(type)) types.add(registeredMerchantTypes.get(type));
            else {
                configuration.getMerchantTypes().remove(type);
                ValhallaMMO.logWarning("Merchant Configuration for " + configuration.getType() + " had Merchant sub-type " + type + " added to it, but it doesn't exist! Removed.");
            }
        }
        MerchantType selectedType = Utils.weightedSelection(types, 1, 0, 0).stream().findFirst().orElse(null);
        if (selectedType == null) return null; // No merchant type selected

        return createMerchant(villager.getUniqueId(), selectedType);
    }

    public static MerchantData createMerchant(UUID id, MerchantType type){
        List<MerchantData.TradeData> trades = new ArrayList<>();
        for (MerchantLevel level : type.getTrades().keySet()){
            MerchantType.MerchantLevelTrades levelTrades = type.getTrades(level);
            Collection<MerchantTrade> merchantTrades = new HashSet<>();
            for (String trade : new HashSet<>(levelTrades.getTrades())) {
                if (registeredMerchantTrades.containsKey(trade)) merchantTrades.add(registeredMerchantTrades.get(trade));
                else {
                    levelTrades.getTrades().remove(trade);
                    ValhallaMMO.logWarning("Merchant Type " + type.getType() + " had trade " + trade + " added to it, but it doesn't exist! Removed.");
                }
            }
            Collection<MerchantTrade> selectedTrades = new HashSet<>(merchantTrades.stream().filter(t -> t.getWeight() == -1).toList());
            merchantTrades.removeIf(t -> t.getWeight() == -1);
            selectedTrades.addAll(Utils.weightedSelection(merchantTrades, Utils.randomAverage(type.getRolls(level)), 0, 0));
            selectedTrades.forEach(t -> {
                ItemBuilder result = new ItemBuilder(t.getResult());
                DynamicItemModifier.modify(result, null, t.getModifiers(), false, true, true);
                setTradeKey(result.getMeta(), t);
                trades.add(new MerchantData.TradeData(t.getId(), level.getLevel(), result.get(), t.getMaxUses(), t.getMaxUses(), 0));
            });
        }
        MerchantData data = new MerchantData(type, trades.toArray(new MerchantData.TradeData[0]));
        merchantDataPersistence.setData(id, data);
        return data;
    }

    public static void getMerchantData(Villager villager, Callback<MerchantData> whenReady){
        merchantDataPersistence.getData(villager.getUniqueId(), whenReady);
    }

    public static void getMerchantData(UUID id, Callback<MerchantData> whenReady){
        merchantDataPersistence.getData(id, whenReady);
    }

    public static MerchantLevel getLevel(MerchantData data){
        MerchantType merchantType = registeredMerchantTypes.get(data.getType());
        if (merchantType == null) return null;
        MerchantLevel currentLevel = null;
        for (MerchantLevel level : MerchantLevel.values()){
            MerchantType.MerchantLevelTrades trades = merchantType.getTrades(level);
            if (trades == null) continue;
            if (data.getExp() >= trades.getExpRequirement() || currentLevel == null) currentLevel = level;
        }
        return currentLevel;
    }

    public static List<MerchantRecipe> recipesFromData(MerchantData data, Player player){
        MerchantType type = registeredMerchantTypes.get(data.getType());
        MerchantLevel level = getLevel(data);
        if (type == null || level == null) return null;
        List<MerchantRecipe> recipes = new ArrayList<>();
        List<MerchantTrade> trades = new ArrayList<>();
        for (String tradeName : new HashSet<>(data.getTrades().keySet())){
            MerchantTrade trade = registeredMerchantTrades.get(tradeName);
            if (trade == null){
                data.getTrades().remove(tradeName);
                String error = "Villager data had trade " + tradeName + " that no longer exists! Removed now";
                ValhallaMMO.logWarning(error);
                player.sendMessage(Utils.chat("&c" + error + ", please notify admin"));
                continue;
            }
            trades.add(trade);
        }
        for (MerchantTrade trade : trades){
            MerchantData.TradeData tradeData = data.getTrades().get(trade.getId());
            if (tradeData.level() > level.getLevel()) continue;
            double maxUses = trade.getMaxUses();
            double remainingUses = tradeData.salesLeft();
            if (!trade.hasFixedUseCount()){
                double tradeCountMultiplier = 1; // TODO AccumulativeStatManager.getCachedStats("TRADE_USE_MULTIPLIER", player, 10000, true);
                maxUses *= tradeCountMultiplier;
                remainingUses *= tradeCountMultiplier;
            }
            if (remainingUses > maxUses) remainingUses = maxUses;
            int uses = (int) Math.floor(maxUses - remainingUses);
            MerchantRecipe recipe = new MerchantRecipe(tradeData.item(), uses, (int) Math.floor(maxUses), trade.rewardsExperience(), Math.round(trade.getVillagerExperience()), trade.getDemandMultiplier(), tradeData.demand(), trade.getSpecialPrice());
            List<ItemStack> ingredients = new ArrayList<>();
            ingredients.add(trade.getScalingCostItem());
            if (!ItemUtils.isEmpty(trade.getOptionalCostItem())) ingredients.add(trade.getOptionalCostItem());
            recipe.setIngredients(ingredients);
            recipes.add(recipe);
        }
        return recipes;
    }

    public static void registerTrade(MerchantTrade trade){
        registeredMerchantTrades.put(trade.getId(), trade);
    }

    public static void registerMerchantType(MerchantType type){
        registeredMerchantTypes.put(type.getType(), type);
    }

    public static void registerConfiguration(MerchantConfiguration configuration){
        merchantConfigurations.put(configuration.getType(), configuration);
    }

    public int getDelayUntilWorking() {
        return delayUntilWorking;
    }

    public static Map<Villager.Profession, MerchantConfiguration> getMerchantConfigurations() {
        return new HashMap<>(merchantConfigurations);
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

    @SuppressWarnings("all")
    public static void loadTradesFromFile(File f){
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (BufferedReader setsReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))){
            MerchantConfigurationData data = gson.fromJson(setsReader, MerchantConfigurationData.class);
            if (data == null) return;
            for (MerchantConfiguration merchant : data.getMerchantConfigurations()) registerConfiguration(merchant);
            for (MerchantTrade merchant : data.getRegisteredMerchantTrades()) registerTrade(merchant);
            for (MerchantType merchant : data.getRegisteredMerchantTypes()) registerMerchantType(merchant);
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not load merchant configurations from merchant_configurations.json, " + exception.getMessage());
        } catch (NoClassDefFoundError ignored){}
    }

    @SuppressWarnings("all")
    public static void saveMerchantConfigurations(){
        File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/merchant_configurations.json");
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8))){
            JsonElement element = gson.toJsonTree(new MerchantConfigurationData(registeredMerchantTrades.values(), registeredMerchantTypes.values(), merchantConfigurations.values()), MerchantConfigurationData.class);
            gson.toJson(element, writer);
            writer.flush();
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not save items to merchant_configurations.json, " + exception.getMessage());
        }
    }

    public static void setupDatabase(){
        SQLite sqlite = new SQLite();
        if (sqlite.getConnection() != null) merchantDataPersistence = sqlite;

        if (merchantDataPersistence instanceof Database db) db.createTable(null);
    }

    public static void setMerchantDataPersistence(MerchantDataPersistence merchantDataPersistence) {
        CustomMerchantManager.merchantDataPersistence = merchantDataPersistence;

        if (merchantDataPersistence instanceof Database db) db.createTable(null);
    }

    public static MerchantDataPersistence getMerchantDataPersistence() {
        return merchantDataPersistence;
    }

    public static boolean isCustomMerchant(Villager villager){
        return villager.getPersistentDataContainer().has(KEY_CUSTOM_VILLAGER, PersistentDataType.BYTE);
    }

    public static MerchantConfiguration getMerchantConfiguration(Villager.Profession ofProfession){
        if (merchantConfigurations.get(ofProfession) == null) merchantConfigurations.put(ofProfession, new MerchantConfiguration(ofProfession));
        return merchantConfigurations.get(ofProfession);
    }

    public static MerchantType getMerchantType(String id){
        return registeredMerchantTypes.get(id);
    }
}
