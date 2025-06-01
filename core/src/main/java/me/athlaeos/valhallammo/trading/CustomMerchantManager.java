package me.athlaeos.valhallammo.trading;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Weighted;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.persistence.Database;
import me.athlaeos.valhallammo.persistence.GsonAdapter;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.trading.data.MerchantDataPersistence;
import me.athlaeos.valhallammo.trading.data.implementations.SQLite;
import me.athlaeos.valhallammo.trading.dom.*;
import me.athlaeos.valhallammo.trading.happiness.HappinessSourceRegistry;
import me.athlaeos.valhallammo.utility.Callback;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.AttributeMappings;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.AbstractVillager;
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

    private static final int delayUntilWorking = getTradingConfig().getInt("delay_until_working"); // the time it takes for a villager who just got a profession to be able to work/be interacted with, in game time
    private static final String discountFormula = getTradingConfig().getString("discount", "(0.0025 * %happiness%) + (0.002 * %reputation%) + (0.0015 * %renown%)");
    private static final double renownUnforgivable = getTradingConfig().getDouble("renown_unforgivable", -90);
    private static final double reputationUnforgivable = getTradingConfig().getDouble("reputation_unforgivable", -90);

    private static MerchantDataPersistence merchantDataPersistence;

    private static final Map<Villager.Profession, MerchantConfiguration> merchantConfigurations = new HashMap<>();
    private static final MerchantConfiguration travelingMerchantConfiguration = new MerchantConfiguration(null);
    private static final Map<String, MerchantType> registeredMerchantTypes = new HashMap<>();
    private static final Map<String, MerchantTrade> registeredMerchantTrades = new HashMap<>();

    static {
        for (ProfessionWrapper profession : ProfessionWrapper.values()){
            merchantConfigurations.put(profession.getProfession(), new MerchantConfiguration(profession.getProfession()));
        }
    }

    /**
     * Takes a villager and turns them into a custom trader, of a type fitting their profession. <br>
     * During this trade selection the individual trade's conditions will be ignored, and so the villager will have
     * every trade accessible to them *after* the weighted selection procedure is done. <br>
     * If a villager already has custom trades, they will be overwritten.
     * @param villager The villager to be granted custom trades
     * @return The new MerchantData representing all trades the villager has been granted, or null if no recipes were added
     */
    public static MerchantData convertToRandomMerchant(AbstractVillager villager, Player interactingPlayer){
        villager.getPersistentDataContainer().set(KEY_CUSTOM_VILLAGER, PersistentDataType.BYTE, (byte) 0);
        MerchantConfiguration configuration = villager instanceof Villager v ? merchantConfigurations.get(v.getProfession()) : travelingMerchantConfiguration;
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

        return createMerchant(villager.getUniqueId(), selectedType, interactingPlayer);
    }

    public static MerchantData createMerchant(UUID id, MerchantType type, Player interactingPlayer){
        AbstractVillager villager = ValhallaMMO.getInstance().getServer().getEntity(id) instanceof AbstractVillager a ? a : null;
        MerchantData data = new MerchantData(villager, type, generateRandomTrades(villager, type, interactingPlayer));
        merchantDataPersistence.setData(id, data);
        return data;
    }

    public static List<MerchantData.TradeData> generateRandomTrades(AbstractVillager villager, MerchantType type, Player player){
        float luck = getTradingLuck(player);
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
            Collection<MerchantTrade> selectedTrades = new HashSet<>(merchantTrades.stream().filter(t -> t.getWeight() == -1 && t.isTradeable()).toList());
            merchantTrades.removeIf(t -> t.getWeight() == -1 || !t.isTradeable());
            selectedTrades.addAll(Utils.weightedSelection(merchantTrades, Utils.randomAverage(type.getRolls(level)), luck, 0));
            selectedTrades.forEach(t -> {
                ItemBuilder result = prepareTradeResult(villager, t, player);
                if (result == null) return;

                ItemBuilder cost = new ItemBuilder(t.getScalingCostItem());
                int boundMax = t.getPriceRandomPositiveOffset() - t.getPriceRandomNegativeOffset();
                int randomOffset = boundMax <= 0 ? 0 : (Utils.getRandom().nextInt(boundMax) + t.getPriceRandomNegativeOffset());
                int price = Math.max(1, Math.min(ValhallaMMO.getNms().getMaxStackSize(cost.getMeta(), cost.getItem().getType()), cost.getItem().getAmount() + randomOffset));
                trades.add(new MerchantData.TradeData(t.getID(), price, level.getLevel(), result.get(), t.getMaxUses()));
            });
        }
        return trades;
    }

    public static ItemBuilder prepareTradeResult(AbstractVillager villager, MerchantTrade t, Player player){
        ItemBuilder result = new ItemBuilder(t.getResult());
        DynamicItemModifier.modify(ModifierContext.builder(result).crafter(player).validate().entity(villager).executeUsageMechanics().get(), t.getModifiers());
        if (CustomFlag.hasFlag(result.getMeta(), CustomFlag.UNCRAFTABLE)) return null;
        setTradeKey(result.getMeta(), t);
        return result;
    }

    public static void getMerchantData(AbstractVillager villager, Callback<MerchantData> whenReady){
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
        AbstractVillager villager = data.getVillager();
        float happiness = villager == null ? 0F : HappinessSourceRegistry.getHappiness(player, villager);
        float renown = data.getPlayerMemory(player.getUniqueId()).getRenownReputation();
        double expVanillaToCustomModifier = (double) level.getDefaultExpRequirement() / type.getExpRequirement(level);
        for (MerchantTrade trade : trades){
            float reputation = data.getPlayerMemory(player.getUniqueId()).getTradingReputation();
            if (reputation < 0) reputation *= trade.getNegativeReputationMultiplier();
            else if (reputation > 0) reputation *= trade.getPositiveReputationMultiplier();
            MerchantData.TradeData tradeData = data.getTrades().get(trade.getID());
            if (tradeData.getLevel() > level.getLevel()) continue;
            double perTradeWeight = trade.getPerTradeWeight(player, tradeData);
            int finalMaxUses = (int) Math.floor(trade.getMaxUses() / perTradeWeight);
            int finalRemainingUses = (int) Math.floor(tradeData.getRemainingUses(player, type.isPerPlayerStock()) / perTradeWeight);
            if (finalRemainingUses > finalMaxUses) finalRemainingUses = finalMaxUses;
            int uses = (finalMaxUses - finalRemainingUses);
            double price = tradeData.getBasePrice();

            price = Math.min(price + trade.getDemandPriceMax(), price * (1 + (tradeData.getDemand() * trade.getDemandPriceMultiplier())));
            double discount = discountFormula == null ? 0 : Utils.eval(discountFormula
                    .replace("%happiness%", String.valueOf(happiness))
                    .replace("%renown%", String.valueOf(renown))
                    .replace("%reputation%", String.valueOf(reputation))
            );
            price = Math.round(price * (1 - discount) * (1 + AccumulativeStatManager.getCachedStats("TRADING_DISCOUNT", player, 10000, true)));

            int specialPrice = (int) Math.round(price) - tradeData.getBasePrice();// Math.round(trade.getScalingCostItem().getAmount() * (1 + (tradeData.getDemand() * trade.getDemandPriceMultiplier())));
            // specialprice is simply a PRICE OFFSET, so with a price of 8 and a specialprice of 2 the final price is 10
            int villagerExperience = (int) Math.round(trade.getVillagerExperience() * (1) * expVanillaToCustomModifier); // TODO player villager experience multiplier

            ItemStack cost = trade.getScalingCostItem().clone();
            cost.setAmount(tradeData.getBasePrice());

            ItemBuilder result = new ItemBuilder(tradeData.getItem());
            setTradeKey(result.getMeta(), trade);
            MerchantRecipe recipe = new MerchantRecipe(result.get(), uses, finalMaxUses, false, villagerExperience, 1, 0, specialPrice);
            List<ItemStack> ingredients = new ArrayList<>();
            ingredients.add(cost);
            if (!ItemUtils.isEmpty(trade.getOptionalCostItem())) ingredients.add(trade.getOptionalCostItem());
            recipe.setIngredients(ingredients);
            recipes.add(recipe);
        }
        return recipes;
    }

    public static void registerTrade(MerchantTrade trade){
        registeredMerchantTrades.put(trade.getID(), trade);
    }

    public static void registerMerchantType(MerchantType type){
        registeredMerchantTypes.put(type.getType(), type);
    }

    public static void registerConfiguration(MerchantConfiguration configuration){
        merchantConfigurations.put(configuration.getType(), configuration);
    }

    public static int getDelayUntilWorking() {
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
        meta.getPersistentDataContainer().set(KEY_TRADE_ID, PersistentDataType.STRING, trade.getID());
    }

    public static MerchantTrade tradeFromKeyedMeta(ItemMeta meta){
        return registeredMerchantTrades.get(meta.getPersistentDataContainer().getOrDefault(KEY_TRADE_ID, PersistentDataType.STRING, ""));
    }

    @SuppressWarnings("all")
    public static void loadAll(){
        File configurations = new File(ValhallaMMO.getInstance().getDataFolder(), "/trading/configurations.json");
        File types = new File(ValhallaMMO.getInstance().getDataFolder(), "/trading/types.json");
        File trades = new File(ValhallaMMO.getInstance().getDataFolder(), "/trading/trades.json");
        loadConfigurationsFromFile(configurations);

        ensureCreation(configurations);
        ensureCreation(types);
        ensureCreation(trades);

        loadConfigurationsFromFile(configurations);
        loadMerchantTypesFromFile(types);
        loadTradesFromFile(trades);
    }

    @SuppressWarnings("all")
    public static void loadConfigurationsFromFile(File f){
        ensureCreation(f);
        try (BufferedReader setsReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))){
            MerchantConfiguration[] data = gson.fromJson(setsReader, MerchantConfiguration[].class);
            if (data == null) return;
            for (MerchantConfiguration merchant : data) registerConfiguration(merchant);
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not load merchant configurations from " + f.getName() + ", " + exception.getMessage());
        } catch (NoClassDefFoundError ignored){}
    }

    @SuppressWarnings("all")
    public static void loadMerchantTypesFromFile(File f){
        ensureCreation(f);
        try (BufferedReader setsReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))){
            MerchantType[] data = gson.fromJson(setsReader, MerchantType[].class);
            if (data == null) return;
            for (MerchantType type : data) registerMerchantType(type);
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not load merchant types from " + f.getName() + ", " + exception.getMessage());
        } catch (NoClassDefFoundError ignored){}
    }

    @SuppressWarnings("all")
    private static void ensureCreation(File f){
        try {
            if (!f.exists()) f.createNewFile();
        } catch (Exception ignored){}
    }

    public static void loadTradesFromFile(File f){
        ensureCreation(f);
        try (BufferedReader setsReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))){
            MerchantTrade[] data = gson.fromJson(setsReader, MerchantTrade[].class);
            if (data == null) return;
            for (MerchantTrade trade : data) registerTrade(trade);
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not load merchant trades from " + f.getName() + ", " + exception.getMessage());
        } catch (NoClassDefFoundError ignored){}
    }

    @SuppressWarnings("all")
    public static void saveAll(){
        File configurations = new File(ValhallaMMO.getInstance().getDataFolder(), "/trading/configurations.json");
        File types = new File(ValhallaMMO.getInstance().getDataFolder(), "/trading/types.json");
        File trades = new File(ValhallaMMO.getInstance().getDataFolder(), "/trading/trades.json");

        ensureCreation(configurations);
        ensureCreation(types);
        ensureCreation(trades);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configurations, StandardCharsets.UTF_8))){
            JsonElement element = gson.toJsonTree(new ArrayList<>(merchantConfigurations.values()), new TypeToken<ArrayList<MerchantConfiguration>>(){}.getType());
            gson.toJson(element, writer);
            writer.flush();
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not save items to trading/configurations.json, " + exception.getMessage());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(types, StandardCharsets.UTF_8))){
            JsonElement element = gson.toJsonTree(new ArrayList<>(registeredMerchantTypes.values()), new TypeToken<ArrayList<MerchantType>>(){}.getType());
            gson.toJson(element, writer);
            writer.flush();
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not save items to trading/types.json, " + exception.getMessage());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(trades, StandardCharsets.UTF_8))){
            JsonElement element = gson.toJsonTree(new ArrayList<>(registeredMerchantTrades.values()), new TypeToken<ArrayList<MerchantTrade>>(){}.getType());
            gson.toJson(element, writer);
            writer.flush();
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not save items to trading/trades.json, " + exception.getMessage());
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

    public static boolean isCustomMerchant(AbstractVillager villager){
        return villager.getPersistentDataContainer().has(KEY_CUSTOM_VILLAGER, PersistentDataType.BYTE);
    }

    public static MerchantConfiguration getMerchantConfiguration(Villager.Profession ofProfession){
        if (merchantConfigurations.get(ofProfession) == null) merchantConfigurations.put(ofProfession, new MerchantConfiguration(ofProfession));
        return merchantConfigurations.get(ofProfession);
    }

    public static MerchantConfiguration getTravelingMerchantConfiguration(){
        return travelingMerchantConfiguration;
    }

    public static void removeTrade(MerchantTrade trade){
        registeredMerchantTrades.remove(trade.getID());
        for (MerchantType type : registeredMerchantTypes.values()){
            for (MerchantLevel level : MerchantLevel.values()) {
                MerchantType.MerchantLevelTrades trades = type.getTrades(level);
                if (trades != null) trades.getTrades().remove(trade.getID());
            }
        }
    }

    public static float getTradingLuck(Player p){
        AttributeInstance luckInstance = p.getAttribute(AttributeMappings.LUCK.getAttribute());
        float luck = luckInstance == null ? 0 : (float) luckInstance.getValue();
        luck += (float) AccumulativeStatManager.getCachedStats("TRADING_LUCK", p, 10000, true);
        return luck;
    }

    public static void modifyTradingReputation(MerchantData data, Player toPlayer, float reputation){
        if (reputation == 0) return;
        MerchantData.MerchantPlayerMemory memory = data.getPlayerMemory(toPlayer.getUniqueId());
        if (memory.getTradingReputation() < reputationUnforgivable && reputation > 0) return;
        if (reputation > 0) reputation *= (1 + (float) AccumulativeStatManager.getCachedStats("TRADING_POS_REPUTATION_MULTIPLIER", toPlayer, 10000, true));
        else reputation *= (1 + (float) AccumulativeStatManager.getCachedStats("TRADING_NEG_REPUTATION_MULTIPLIER", toPlayer, 10000, true));
        memory.setRenownReputation(memory.getRenownReputation() + reputation);

        AbstractVillager villager = data.getVillager();
        if (villager == null) return;
        int particleCount = Math.min(1, Math.round(reputation / 10F));
        if (reputation < 0) toPlayer.spawnParticle(Particle.VILLAGER_ANGRY, villager.getEyeLocation(), particleCount, 0.5, 0.5, 0.5);
        else toPlayer.spawnParticle(Particle.VILLAGER_HAPPY, villager.getEyeLocation(), particleCount, 0.5, 0.5, 0.5);
    }

    public static void modifyRenownReputation(MerchantData data, Player toPlayer, float reputation){
        if (reputation == 0) return;
        MerchantData.MerchantPlayerMemory memory = data.getPlayerMemory(toPlayer.getUniqueId());
        if (memory.getRenownReputation() < renownUnforgivable && reputation > 0) return;
        if (reputation > 0) reputation *= (1 + (float) AccumulativeStatManager.getCachedStats("TRADING_POS_RENOWN_MULTIPLIER", toPlayer, 10000, true));
        else reputation *= (1 + (float) AccumulativeStatManager.getCachedStats("TRADING_NEG_RENOWN_MULTIPLIER", toPlayer, 10000, true));
        memory.setRenownReputation(memory.getRenownReputation() + reputation);

        AbstractVillager villager = data.getVillager();
        if (villager == null) return;
        int particleCount = Math.min(1, Math.round(reputation / 10F));
        if (reputation < 0) toPlayer.spawnParticle(Particle.VILLAGER_ANGRY, villager.getEyeLocation(), particleCount, 0.5, 0.5, 0.5);
        else toPlayer.spawnParticle(Particle.VILLAGER_HAPPY, villager.getEyeLocation(), particleCount, 0.5, 0.5, 0.5);
    }

    public static void removeType(MerchantType type){
        registeredMerchantTypes.remove(type.getType());
    }

    public static MerchantType getMerchantType(String id){
        return registeredMerchantTypes.get(id);
    }

    public static MerchantTrade getTrade(String id){
        return registeredMerchantTrades.get(id);
    }

    public static int today(){
        return (int) Math.floor(time() / 24000D);
    }

    public static long time(){
        return ValhallaMMO.getInstance().getServer().getWorlds().getFirst().getFullTime();
    }

    public static YamlConfiguration getTradingConfig(){
        return ConfigManager.getConfig("trading/trading.yml").get();
    }

    public static String getDiscountFormula() {
        return discountFormula;
    }
}
