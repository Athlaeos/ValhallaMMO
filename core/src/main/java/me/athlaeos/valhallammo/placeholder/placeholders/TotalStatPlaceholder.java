package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.placeholder.Placeholder;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.StatCollector;
import org.bukkit.entity.Player;

public class TotalStatPlaceholder extends Placeholder {
    private final String statSource;

    public TotalStatPlaceholder(String placeholder, String statSource) {
        super(placeholder);
        this.statSource = statSource.toUpperCase(java.util.Locale.US);
    }

    @Override
    public String parse(String s, Player p) {
        StatCollector collector = AccumulativeStatManager.getStatCollector(statSource);
        double stat = AccumulativeStatManager.getCachedStats(statSource, p, 10000, false);
        return s.replace(this.placeholder, collector.getFormat().format(stat));
    }
}
