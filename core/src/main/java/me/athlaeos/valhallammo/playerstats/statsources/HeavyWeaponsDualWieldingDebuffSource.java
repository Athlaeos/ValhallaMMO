package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class HeavyWeaponsDualWieldingDebuffSource implements AccumulativeStatSource {
    private final boolean enabled;
    private final double attackSpeedDebuffT1;
    private final double attackSpeedDebuffT2;
    private final String attackSpeedDebuffMessage;

    private static final Collection<UUID> messaged = new HashSet<>();

    public HeavyWeaponsDualWieldingDebuffSource(){
        YamlConfiguration config = ConfigManager.getConfig("skills/heavy_weapons.yml").reload().get();
        enabled = config.getBoolean("require_empty_offhand", true);
        attackSpeedDebuffT1 = config.getDouble("attack_speed_reduction_occupied_offhand", -0.5D);
        attackSpeedDebuffT2 = config.getDouble("attack_speed_reduction_heavyweapon_in_offhand", -0.35);
        attackSpeedDebuffMessage = TranslationManager.translatePlaceholders(config.getString("warning_dual_wielding"));
    }

    @Override
    public double fetch(Entity p, boolean use) {
        if (p instanceof Player pl && enabled){
            EntityProperties properties = EntityCache.getAndCacheProperties(pl);
            if (properties.getOffHand() == null || properties.getMainHand() == null) return 0; // one of the hands is empty, no need to debuff
            double debuff = 0;
            WeightClass mainHandWeight = WeightClass.getWeightClass(properties.getMainHand().getMeta());
            WeightClass offHandWeight = WeightClass.getWeightClass(properties.getOffHand().getMeta());
            if (mainHandWeight == WeightClass.HEAVY || offHandWeight == WeightClass.HEAVY) debuff += attackSpeedDebuffT1;
            if (mainHandWeight == WeightClass.HEAVY && offHandWeight == WeightClass.HEAVY) debuff += attackSpeedDebuffT2;
            if (debuff < 0 && !messaged.contains(p.getUniqueId())){
                messaged.add(p.getUniqueId());
                Utils.sendMessage(pl, attackSpeedDebuffMessage);
            }
            return debuff;
        }
        return 0;
    }
}
