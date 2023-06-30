package me.athlaeos.valhallammo.playerstats.format.formats;

import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.utility.Utils;

public class PercentileBase100Format extends StatFormat {
    String precision;
    public PercentileBase100Format(int precision){
        this.precision = "%,." + precision + "f%%";
    }

    @Override
    public String format(Number stat) {
        return String.format(precision, Utils.round6Decimals(stat.doubleValue()));
    }
}
