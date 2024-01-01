package me.athlaeos.valhallammo.playerstats.format.formats;

import me.athlaeos.valhallammo.playerstats.format.StatFormat;

public class None extends StatFormat {
    @Override
    public String format(Number stat) {
        return "";
    }
}
