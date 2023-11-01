package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class FishingLuckLuckSource implements AccumulativeStatSource {
    private final double fishingLuckLuck = ValhallaMMO.getPluginConfig().getDouble("fishing_luck_luck");

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof LivingEntity l){
            AttributeInstance luckInstance = l.getAttribute(Attribute.GENERIC_LUCK);
            if (luckInstance != null) return luckInstance.getValue() * fishingLuckLuck;
        }
        return 0;
    }
}
