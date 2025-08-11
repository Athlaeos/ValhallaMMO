package me.athlaeos.valhallammo.playerstats.format.formats;

import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;

public class ScalarBase1Format extends StatFormat {
    String precision;
    public ScalarBase1Format(int precision){
        this.precision = "%,." + precision + "fx";
    }

    @Override
    public String format(Number stat) {
        return StringUtils.trimTrailingZeroes(String.format(precision, Utils.round6Decimals(stat.doubleValue())));
    }
}
