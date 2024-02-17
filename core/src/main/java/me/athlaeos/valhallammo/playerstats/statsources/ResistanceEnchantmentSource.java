package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class ResistanceEnchantmentSource implements AccumulativeStatSource {
    private final String key;
    private final Enchantment enchantment;

    public ResistanceEnchantmentSource(Enchantment enchantment, String key){
        this.enchantment = enchantment;
        this.key = key;
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof LivingEntity l){
            double resistancePerPiece = ValhallaMMO.getPluginConfig().getDouble(key + "_effectiveness");
            double resistanceCap = ValhallaMMO.getPluginConfig().getDouble(key + "_cap");
            int totalLevelCount = 0;
            for (ItemBuilder i : EntityCache.getAndCacheProperties(l).getIterable(false, false)){
                totalLevelCount += i.getItem().getEnchantmentLevel(enchantment);
            }
            return Math.min(resistanceCap, totalLevelCount * resistancePerPiece);
        }
        return 0;
    }
}
