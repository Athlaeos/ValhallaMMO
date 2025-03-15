package me.athlaeos.valhallammo.trading.dom;

public enum MerchantLevel {
    NOVICE(1),
    APPRENTICE(2),
    JOURNEYMAN(3),
    EXPERT(4),
    MASTER(5);
    private final int level;

    MerchantLevel(int level){
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
