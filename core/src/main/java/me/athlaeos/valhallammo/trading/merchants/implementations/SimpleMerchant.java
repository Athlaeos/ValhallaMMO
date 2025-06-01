package me.athlaeos.valhallammo.trading.merchants.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.TradingProfile;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.dom.MerchantTrade;
import me.athlaeos.valhallammo.trading.dom.MerchantType;
import me.athlaeos.valhallammo.trading.merchants.VirtualMerchant;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class SimpleMerchant extends VirtualMerchant {
    public SimpleMerchant(PlayerMenuUtility utility, UUID merchantID, MerchantData data, List<MerchantRecipe> recipes) {
        super(utility, merchantID, data, recipes);

        TradingProfile profile = ProfileCache.getOrCache(utility.getOwner(), TradingProfile.class);
        Collection<MerchantRecipe> toRemove = new HashSet<>();
        AttributeInstance luckAttribute = utility.getOwner().getAttribute(Attribute.GENERIC_LUCK);
        double luck = AccumulativeStatManager.getCachedStats("TRADING_LUCK", utility.getOwner(), 10000, true);
        if (luckAttribute != null) luck += luckAttribute.getValue();
        LootContext context = new LootContext.Builder(utility.getOwner().getLocation()).killer(utility.getOwner()).lootedEntity(ValhallaMMO.getInstance().getServer().getEntity(merchantID) instanceof Villager v ? v : null).lootingModifier(0).luck((float) luck).build();
        for (MerchantRecipe recipe : recipes){
            ItemMeta meta = recipe.getResult().getItemMeta();
            if (meta == null) {
                toRemove.add(recipe);
                continue;
            }
            MerchantTrade trade = CustomMerchantManager.tradeFromKeyedMeta(meta);
            if (trade == null){
                toRemove.add(recipe);
                continue;
            }
            if (trade.failsPredicates(trade.getPredicateSelection(), context) || (trade.isExclusive() && !profile.getExclusiveTrades().contains(trade.getID()))) toRemove.add(recipe);
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
        MerchantType type = CustomMerchantManager.getMerchantType(getData().getType());
        return Utils.chat(villager == null || villager.getCustomName() == null ?
                type == null ? "" : type.getName() == null ? type.getType() : type.getName() :
                villager.getCustomName());
    }
}
