package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import org.bukkit.entity.Entity;

/**
 * Yes, the name is intentionally bad, lol
 */
public class SourceSource implements AccumulativeStatSource {
    private final String source;

    public SourceSource(String source){
        this.source = source;
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        return AccumulativeStatManager.getCachedStats(source, statPossessor, 10000, true);
    }
}
