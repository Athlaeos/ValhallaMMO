package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.placeholder.Placeholder;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import org.bukkit.entity.Player;

public class TotalRawStatPlaceholder extends Placeholder {
    private final String statSource;

    public TotalRawStatPlaceholder(String placeholder, String statSource) {
        super(placeholder);
        this.statSource = statSource.toUpperCase(java.util.Locale.US);
    }

    @Override
    public String parse(String s, Player p) {
        double stat = AccumulativeStatManager.getCachedStats(statSource, p, 10000, false);
        return s.replace(this.placeholder, String.format("%.6f", stat));
    }
}
