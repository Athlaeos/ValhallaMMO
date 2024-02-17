package me.athlaeos.valhallammo.playerstats.profiles.properties;

import me.athlaeos.valhallammo.playerstats.format.StatFormat;

public class StatProperties {
    private boolean addWhenMerged = true;
    private boolean shouldPrioritizePositive = true; // does nothing if addWhenMerged is true
    private StatFormat format = null;
    private boolean generatePerkRewards = false;

    private double min = Double.NaN;
    private double max = Double.NaN;

    public StatFormat getFormat() {
        return format;
    }

    public void setFormat(StatFormat format) {
        this.format = format;
    }

    public boolean addWhenMerged() {
        return addWhenMerged;
    }

    public void setAddWhenMerged(boolean addWhenMerged) {
        this.addWhenMerged = addWhenMerged;
    }

    public void setGeneratePerkRewards(boolean generatePerkRewards) {
        this.generatePerkRewards = generatePerkRewards;
    }

    public void setShouldPrioritizePositive(boolean shouldPrioritizePositive) {
        this.shouldPrioritizePositive = shouldPrioritizePositive;
    }

    public boolean generatePerkRewards() {
        return generatePerkRewards;
    }

    public boolean shouldPrioritizePositive() {
        return shouldPrioritizePositive;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public void setMin(double min) {
        this.min = min;
    }
}