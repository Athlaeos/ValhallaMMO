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

    public static MerchantLevel getLevel(int exp){
        if (exp < 10) return NOVICE;
        else if (exp < 70) return APPRENTICE;
        else if (exp < 150) return JOURNEYMAN;
        else if (exp < 250) return EXPERT;
        else return MASTER;
    }

    public static MerchantLevel getNextLevel(MerchantLevel level){
        return switch (level) {
            case NOVICE -> APPRENTICE;
            case APPRENTICE -> JOURNEYMAN;
            case JOURNEYMAN -> EXPERT;
            case EXPERT -> MASTER;
            case MASTER -> null;
        };
    }

    public static MerchantLevel getPreviousLevel(MerchantLevel level){
        return switch (level) {
            case NOVICE -> null;
            case APPRENTICE -> NOVICE;
            case JOURNEYMAN -> APPRENTICE;
            case EXPERT -> JOURNEYMAN;
            case MASTER -> EXPERT;
        };
    }

    public int getDefaultExpRequirement() {
        return defaultExpRequirement;
    }
}
