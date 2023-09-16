package me.athlaeos.valhallammo.playerstats.format.formats;

import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.utility.Utils;

public class DifferenceTimeMillisecondsFormat extends StatFormat {
    String precision;
    public DifferenceTimeMillisecondsFormat(int precision){
        this.precision = "%," + precision + "ds";
    }

    @Override
    public String format(Number stat) {
        double val = Utils.round6Decimals(stat.doubleValue());
        int ms = (int) Math.round(val / 1000D);
        return (val >= 0 ? "+" : "") + String.format(precision, ms);
    }
}
