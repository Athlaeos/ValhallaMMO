package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class ArmorPenetrationBreachEnchantmentSource implements AccumulativeStatSource {
    private final double penetrationPerLevel;

    public ArmorPenetrationBreachEnchantmentSource(boolean percentile){
        String value = ValhallaMMO.getPluginConfig().getString("breach_armor_penetration", "10%");
        boolean percentilePenetration = value.endsWith("%");
        if (percentile != percentilePenetration) penetrationPerLevel = 0;
        else penetrationPerLevel = Catch.catchOrElse(() -> Double.valueOf(value.replace("%", "")), 10D);
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (penetrationPerLevel == 0) return 0;
        if (statPossessor instanceof LivingEntity l){
            Enchantment breach = ValhallaMMO.getNms().getEnchantment(EnchantmentMappings.BREACH);
            if (breach == null) return 0;
            EntityProperties properties = EntityCache.getAndCacheProperties(l);
            ItemBuilder mainHand = properties.getMainHand();
            if (mainHand == null) return 0;
            int level = mainHand.getMeta().getEnchantLevel(breach);
            return penetrationPerLevel * level;
        }
        return 0;
    }
}
