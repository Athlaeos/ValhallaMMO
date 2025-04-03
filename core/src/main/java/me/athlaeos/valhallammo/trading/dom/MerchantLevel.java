package me.athlaeos.valhallammo.trading.dom;

public enum MerchantLevel {
    NOVICE(1, 0),
    APPRENTICE(2, 10),
    JOURNEYMAN(3, 70),
    EXPERT(4, 150),
    MASTER(5, 250);
    private final int level;
    private final int defaultExpRequirement;

    MerchantLevel(int level, int defaultExpRequirement){
        this.level = level;
        this.defaultExpRequirement = defaultExpRequirement;
    }

    public int getLevel() {
        return level;
    }

    public int getDefaultExpRequirement() {
        return defaultExpRequirement;
    }
}
