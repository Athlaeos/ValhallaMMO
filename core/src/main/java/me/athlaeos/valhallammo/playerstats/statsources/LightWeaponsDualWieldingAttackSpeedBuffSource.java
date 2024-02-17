package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class LightWeaponsDualWieldingAttackSpeedBuffSource implements AccumulativeStatSource {
    private final boolean enabled;
    private final double attackSpeedBuff;

    public LightWeaponsDualWieldingAttackSpeedBuffSource(){
        YamlConfiguration config = ConfigManager.getConfig("skills/light_weapons.yml").reload().get();
        enabled = config.getBoolean("dual_wielding_bonus", true);
        attackSpeedBuff = config.getDouble("dual_wielding_attack_speed", 0.2D);
    }

    @Override
    public double fetch(Entity p, boolean use) {
        if (p instanceof Player pl && enabled){
            EntityProperties properties = EntityCache.getAndCacheProperties(pl);
            if (properties.getOffHand() == null || properties.getMainHand() == null) return 0; // one of the hands is empty, no need to buff
            if (WeightClass.getWeightClass(properties.getOffHand().getMeta()) == WeightClass.LIGHT &&
                    WeightClass.getWeightClass(properties.getMainHand().getMeta()) == WeightClass.LIGHT) return attackSpeedBuff;
        }
        return 0;
    }
}
