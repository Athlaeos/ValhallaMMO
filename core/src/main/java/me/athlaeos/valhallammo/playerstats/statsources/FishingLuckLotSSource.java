package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class FishingLuckLotSSource implements AccumulativeStatSource {
    private final double fishingLuckLotS = ValhallaMMO.getPluginConfig().getDouble("fishing_luck_lots");

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof LivingEntity l){
            EntityProperties properties = EntityCache.getAndCacheProperties(l);
            if (properties.getMainHand() != null) return properties.getMainHand().getItem().getEnchantmentLevel(Enchantment.LUCK) * fishingLuckLotS;
        }
        return 0;
    }
}
