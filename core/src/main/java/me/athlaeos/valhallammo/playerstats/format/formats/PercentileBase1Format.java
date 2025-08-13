package me.athlaeos.valhallammo.playerstats.format.formats;

import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;

public class PercentileBase1Format extends StatFormat {
    String precision;
    public PercentileBase1Format(int precision){
        this.precision = "%,." + precision + "f";
    }

    @Override
    public String format(Number stat) {
        return StringUtils.trimTrailingZeroes(String.format(precision, Utils.round6Decimals(stat.doubleValue()) * 100)) + "%";
    }
}
