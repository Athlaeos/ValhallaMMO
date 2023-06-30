package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.placeholder.Placeholder;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import org.bukkit.entity.Player;

public class TotalArmorPlaceholder extends Placeholder {
    private final boolean ignoreLight;
    private final boolean ignoreHeavy;
    private final boolean ignoreWeightless;
    public TotalArmorPlaceholder(String placeholder, boolean ignoreLight, boolean ignoreHeavy, boolean ignoreWeightless) {
        super(placeholder);
        this.ignoreLight = ignoreLight;
        this.ignoreHeavy = ignoreHeavy;
        this.ignoreWeightless = ignoreWeightless;
    }

    @Override
    public String parse(String s, Player p) {
        double lightArmor = ignoreLight ? 0 : Math.max(0, AccumulativeStatManager.getCachedStats("LIGHT_ARMOR", p, 500,true));
        double heavyArmor = ignoreHeavy ? 0 : Math.max(0, AccumulativeStatManager.getCachedStats("HEAVY_ARMOR", p, 500,true));
        double nonEquipmentArmor = ignoreWeightless ? 0 : Math.max(0, AccumulativeStatManager.getCachedStats("NON_EQUIPMENT_ARMOR", p, 500,true));

        double lightArmorMultiplier = ignoreLight ? 1 : Math.max(0, AccumulativeStatManager.getCachedStats("LIGHT_ARMOR_MULTIPLIER", p, 500,true));
        double heavyArmorMultiplier = ignoreHeavy ? 1 : Math.max(0, AccumulativeStatManager.getCachedStats("HEAVY_ARMOR_MULTIPLIER", p, 500,true));
        double armorMultiplierBonus = Math.max(0, AccumulativeStatManager.getCachedStats("ARMOR_MULTIPLIER_BONUS", p, 500,true));

        double totalLightArmor = Math.max(0, (lightArmor * lightArmorMultiplier));
        double totalHeavyArmor = Math.max(0, (heavyArmor * heavyArmorMultiplier));

        double totalArmor = Math.max(0, ((totalLightArmor + totalHeavyArmor + nonEquipmentArmor) * (1 + armorMultiplierBonus)));

        return s.replace(this.placeholder, String.format("%.1f", totalArmor));
    }
}
