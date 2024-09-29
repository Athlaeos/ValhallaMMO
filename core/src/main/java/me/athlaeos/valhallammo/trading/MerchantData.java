package me.athlaeos.valhallammo.trading;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.crafting.recipetypes.ValhallaRecipe;
import me.athlaeos.valhallammo.persistence.GsonAdapter;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MerchantData {
    private static final Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ItemStackGSONAdapter()).create();
    private static final NamespacedKey VILLAGER_DATA = new NamespacedKey(ValhallaMMO.getInstance(), "villager_data");

    private final String type;
    private final int typeVersion;
    private Map<String, TradeData> trades = new HashMap<>();

    public MerchantData(MerchantType type, TradeData... data){
        this.type = type.getType();
        this.typeVersion = type.getVersion();
        for (TradeData datum : data) trades.put(datum.id, datum);
    }

    @NotNull
    public Map<String, TradeData> getTrades() {
        if (trades == null) trades = new HashMap<>();
        return trades;
    }

    public String getType() { return type; }
    public int getTypeVersion() { return typeVersion; }

    public static MerchantData deserialize(Villager villager){
        if (!villager.getPersistentDataContainer().has(VILLAGER_DATA, PersistentDataType.STRING)) return null;
        return deserialize(villager.getPersistentDataContainer().get(VILLAGER_DATA, PersistentDataType.STRING));
    }

    public static MerchantData deserialize(String data){
        return gson.fromJson(data, MerchantData.class);
    }

    public String serialize(){
        return gson.toJson(this);
    }

    public void serialize(Villager villager){
        villager.getPersistentDataContainer().set(VILLAGER_DATA, PersistentDataType.STRING, serialize());
    }

    public static class TradeData{
        private final String id;
        private final int level;
        private final ItemStack item;
        private final int maxSales;
        private final double salesLeft;
        private final int demand;

        public TradeData(String id, int level, ItemStack item, int maxSales, double salesLeft, int demand){
            this.id = id;
            this.level = level;
            this.item = item;
            this.maxSales = maxSales;
            this.salesLeft = salesLeft;
            this.demand = demand;
        }

        public String getId() { return id; }
        public ItemStack getItem() { return item; }
        public int getMaxSales() { return maxSales; }
        public double getSalesLeft() { return salesLeft; }
        public int getLevel() { return level; }
        public int getDemand() { return demand; }
    }
}
