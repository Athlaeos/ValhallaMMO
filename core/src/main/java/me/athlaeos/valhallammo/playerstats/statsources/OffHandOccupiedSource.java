package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Returns a flat stat value depending on whether the stat possessor is holding anything in their off hand.
 * Both values are read from the plugin config and default to 0, so the source is inert until configured.
 * Negative values are allowed, which is how an off hand penalty is expressed.
 */
public class OffHandOccupiedSource implements AccumulativeStatSource {
    private final String occupiedConfigKey;
    private final String emptyConfigKey;

    public OffHandOccupiedSource(String occupiedConfigKey, String emptyConfigKey){
        this.occupiedConfigKey = occupiedConfigKey;
        this.emptyConfigKey = emptyConfigKey;
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (!(statPossessor instanceof Player p)) return 0;
        boolean occupied = !ItemUtils.isEmpty(p.getInventory().getItemInOffHand());
        return ValhallaMMO.getPluginConfig().getDouble(occupied ? occupiedConfigKey : emptyConfigKey, 0);
    }
}
