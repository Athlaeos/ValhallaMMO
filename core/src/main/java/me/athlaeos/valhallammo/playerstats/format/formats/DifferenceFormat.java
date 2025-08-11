package me.athlaeos.valhallammo.playerstats.format.formats;

import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;

public class DifferenceFormat extends StatFormat {
    String precision;
    public DifferenceFormat(int precision){
        this.precision = "%,." + precision + "f";
    }

    @Override
    public String format(Number stat) {
        double val = Utils.round6Decimals(stat.doubleValue());
        return (val >= 0 ? "+" : "") + StringUtils.trimTrailingZeroes(String.format(precision, val));
    }
}
