package me.athlaeos.valhallammo.playerstats.format.formats;

import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.utility.StringUtils;

public class RomanNumeralFormat extends StatFormat {
    @Override
    public String format(Number stat) {
        return StringUtils.toRoman((int) stat.doubleValue());
    }
}
