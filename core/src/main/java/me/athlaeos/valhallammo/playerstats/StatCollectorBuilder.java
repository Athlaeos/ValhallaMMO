package me.athlaeos.valhallammo.playerstats;

import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.statsources.ProfileStatSource;

import java.util.Arrays;

public class StatCollectorBuilder {
    private final StatCollector collector = new StatCollector();

    public StatCollector build(){
        if (collector.getFormat() != null) return collector;
        ProfileStatSource profileSource = (ProfileStatSource) collector.getStatSources().stream().filter(p -> p instanceof ProfileStatSource).findAny().orElse(null);
        if (profileSource != null && profileSource.getFormat() != null) collector.setFormat(profileSource.getFormat());
        else collector.setFormat(StatFormat.FLOAT_P2);
        return collector;
    }

    public StatCollectorBuilder setAttackerPossessive(){
        collector.setAttackerPossessive(true);
        return this;
    }

    public StatCollectorBuilder addSources(AccumulativeStatSource... sources){
        collector.getStatSources().addAll(Arrays.asList(sources));
        return this;
    }

    public StatCollectorBuilder setStatFormat(StatFormat format){
        collector.setFormat(format);
        return this;
    }
}
