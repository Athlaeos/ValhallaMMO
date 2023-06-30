package me.athlaeos.valhallammo.playerstats;

import me.athlaeos.valhallammo.playerstats.format.StatFormat;

import java.util.Collection;
import java.util.HashSet;

public class StatCollector {
    private boolean attackerPossessive = false;
    private StatFormat format = null;
    private final Collection<AccumulativeStatSource> statSources = new HashSet<>();
    protected StatCollector(){}

    public void setFormat(StatFormat format) {
        this.format = format;
    }

    public void setAttackerPossessive(boolean attackerPossessive) {
        this.attackerPossessive = attackerPossessive;
    }

    public StatFormat getFormat() {
        return format;
    }

    public Collection<AccumulativeStatSource> getStatSources() {
        return statSources;
    }

    public boolean isAttackerPossessive() {
        return attackerPossessive;
    }
}
