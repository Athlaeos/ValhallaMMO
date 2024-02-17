package me.athlaeos.valhallammo.playerstats.profiles.properties;

import me.athlaeos.valhallammo.playerstats.format.StatFormat;

public class PropertyBuilder {
    private final StatProperties properties;

    public PropertyBuilder() {
        this.properties = new StatProperties();
    }

    public StatProperties create() {
        return properties;
    }

    public PropertyBuilder mergePrioritizePositive(boolean positive) {
        properties.setAddWhenMerged(false);
        properties.setShouldPrioritizePositive(positive);
        return this;
    }

    public PropertyBuilder format(StatFormat format) {
        properties.setFormat(format);
        return this;
    }

    public PropertyBuilder perkReward() {
        properties.setGeneratePerkRewards(true);
        return this;
    }
    public PropertyBuilder min(double min){
        properties.setMin(min);
        return this;
    }

    public PropertyBuilder max(double max){
        properties.setMax(max);
        return this;
    }
}