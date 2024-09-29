package me.athlaeos.valhallammo.trading;

import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.listeners.LootListener;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.TradingProfile;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class SimpleMerchant extends VirtualMerchant {
    public SimpleMerchant(PlayerMenuUtility utility, Villager villager, List<MerchantRecipe> recipes) {
        super(utility, villager, recipes);

        TradingProfile profile = ProfileCache.getOrCache(utility.getOwner(), TradingProfile.class);
        Collection<MerchantRecipe> toRemove = new HashSet<>();
        for (MerchantRecipe recipe : recipes){
            ItemMeta meta = recipe.getResult().getItemMeta();
            if (meta == null) {
                toRemove.add(recipe);
                continue;
            }
            MerchantTrade trade = CustomTradeRegistry.tradeFromKeyedMeta(meta);
            if (trade == null){
                toRemove.add(recipe);
                continue;
            }
            AttributeInstance luckAttribute = utility.getOwner().getAttribute(Attribute.GENERIC_LUCK);
            double luck = 0; // TODO AccumulativeStatManager.getCachedStats("TRADING_LUCK", utility.getOwner(), 10000, true);
            if (luckAttribute != null) luck += luckAttribute.getValue();
            LootContext context = new LootContext.Builder(villager.getLocation()).killer(utility.getOwner()).lootedEntity(null).lootingModifier(0).luck((float) luck).build();
            if (trade.failsPredicates(trade.getPredicateSelection(), context) || (trade.isExclusive() && !profile.getExclusiveTrades().contains(trade.getId()))) toRemove.add(recipe);
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
        return "test";
    }
}
