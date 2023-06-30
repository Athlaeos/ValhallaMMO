package me.athlaeos.valhallammo.playerstats.format.formats;

import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.utility.Utils;

public class DifferencePercentileBase1Format extends StatFormat {
    String precision;
    public DifferencePercentileBase1Format(int precision){
        this.precision = "%,." + precision + "f%%";
    }

    @Override
    public String format(Number stat) {
        double val = Utils.round6Decimals(stat.doubleValue());
        return (val >= 0 ? "+" : "") + String.format(precision, val * 100);
    }
}
