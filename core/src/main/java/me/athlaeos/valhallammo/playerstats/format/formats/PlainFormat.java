package me.athlaeos.valhallammo.playerstats.format.formats;

import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;

public class PlainFormat extends StatFormat {
    private final String precision;
    public PlainFormat(int precision){
        this.precision = "%,." + precision + "f";
    }

    @Override
    public String format(Number stat) {
        return StringUtils.trimTrailingZeroes(String.format(precision, Utils.round6Decimals(stat.doubleValue())));
    }
}
