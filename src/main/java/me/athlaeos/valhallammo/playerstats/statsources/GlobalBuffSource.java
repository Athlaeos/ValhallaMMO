package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.utility.GlobalEffect;
import org.bukkit.entity.Entity;

public class GlobalBuffSource implements AccumulativeStatSource {
    private final String buff;
    public GlobalBuffSource(String buff){
        this.buff = buff;
        GlobalEffect.addValidEffect(buff);
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (GlobalEffect.isActive(buff)) return GlobalEffect.getAmplifier(buff);
        return 0;
    }
}
