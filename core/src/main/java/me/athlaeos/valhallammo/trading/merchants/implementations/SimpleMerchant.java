package me.athlaeos.valhallammo.trading.merchants.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.TradingProfile;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.dom.MerchantTrade;
import me.athlaeos.valhallammo.trading.dom.MerchantType;
import me.athlaeos.valhallammo.trading.dom.ProfessionWrapper;
import me.athlaeos.valhallammo.trading.merchants.VirtualMerchant;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;

import java.util.*;

public class SimpleMerchant extends VirtualMerchant {
    public SimpleMerchant(PlayerMenuUtility utility, UUID merchantID, MerchantData data, List<Pair<MerchantTrade, MerchantRecipe>> recipes) {
        super(utility, merchantID, data, recipes);

        TradingProfile profile = ProfileCache.getOrCache(utility.getOwner(), TradingProfile.class);
        Collection<Pair<MerchantTrade, MerchantRecipe>> toRemove = new HashSet<>();
        AttributeInstance luckAttribute = utility.getOwner().getAttribute(Attribute.GENERIC_LUCK);
        double luck = AccumulativeStatManager.getCachedStats("TRADING_LUCK", utility.getOwner(), 10000, true);
        if (luckAttribute != null) luck += luckAttribute.getValue();
        LootContext context = new LootContext.Builder(utility.getOwner().getLocation()).killer(utility.getOwner()).lootedEntity(ValhallaMMO.getInstance().getServer().getEntity(merchantID) instanceof Villager v ? v : null).lootingModifier(0).luck((float) luck).build();
        for (Pair<MerchantTrade, MerchantRecipe> pair : recipes){
            MerchantRecipe recipe = pair.getTwo();
            ItemMeta meta = recipe.getResult().getItemMeta();
            if (meta == null) {
                toRemove.add(pair);
                continue;
            }
            recipe.getResult().setItemMeta(meta);
            MerchantTrade trade = pair.getOne();
            if (trade == null){
                toRemove.add(pair);
                continue;
            }
            if (trade.failsPredicates(trade.getPredicateSelection(), context) || (trade.isExclusive() && !profile.getExclusiveTrades().contains(trade.getID()))) {
                toRemove.add(pair);
                continue;
            }
            MerchantData.TradeData tradeData = data.getTrades().get(trade.getID());
            if (tradeData == null) continue;
            double perTradeWeight = trade.getPerTradeWeight(utility.getOwner(), tradeData);
            if (perTradeWeight <= 0) continue;
            setMaxTimesTradeable(trade.getID(), (int) (tradeData.getRemainingUses() / perTradeWeight));
        }
        getRecipes().removeIf(toRemove::contains);
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onOpen() {

    }

    @Override
    public String getMenuName() {
        AbstractVillager villager = getData().getVillager();
        if (villager != null && villager.getCustomName() != null) return villager.getCustomName();
        MerchantType type = CustomMerchantManager.getMerchantType(getData().getType());
        if (type == null) return "";
        if (type.getName() != null) return type.getName();
        for (ProfessionWrapper profession : CustomMerchantManager.getMerchantConfigurations().keySet()){
            if (CustomMerchantManager.getMerchantConfiguration(profession).getMerchantTypes().contains(getData().getType())){
                return TranslationManager.getTranslation("profession_" + profession.toString().toLowerCase(Locale.US));
            }
        }
        return type.getType();
    }
}
