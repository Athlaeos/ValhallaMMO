package me.athlaeos.valhallammo.playerstats.format.formats;

import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.utility.StringUtils;

public class RomanNumeralFormat extends StatFormat {
    private final int offset;
    public RomanNumeralFormat(int offset){
        this.offset = offset;
    }

    public RomanNumeralFormat(){
        this.offset = 0;
    }

    @Override
    public String format(Number stat) {
        return StringUtils.toRoman((int) stat.doubleValue() + offset);
    }
}
