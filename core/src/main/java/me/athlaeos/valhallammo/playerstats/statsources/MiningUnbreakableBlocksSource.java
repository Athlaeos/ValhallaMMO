package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.MiningProfile;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Reduces mining speed by a great amount if looking at a block the player can't mine
 */
public class MiningUnbreakableBlocksSource implements AccumulativeStatSource {
    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof Player p){
            Block b = p.getTargetBlockExact(8);
            if (b == null) return 0;
            MiningProfile profile = ProfileCache.getOrCache(p, MiningProfile.class);
            if (profile.getUnbreakableBlocks().contains(b.getType().toString())) return -999999;
        }
        return 0;
    }
}
