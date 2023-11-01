package me.athlaeos.valhallammo.playerstats.format.formats;

import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.utility.Utils;

public class TimeFormat extends StatFormat {
    private final String precision;
    private final int base;
    public TimeFormat(int precision, int base){
        this.precision = "%," + precision + "ds";
        this.base = base;
    }

    @Override
    public String format(Number stat) {
        double val = Utils.round6Decimals(stat.doubleValue());
        int ms = (int) Math.round(val / base);
        return String.format(precision, ms);
    }
}
