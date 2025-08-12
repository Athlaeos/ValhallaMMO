package me.athlaeos.valhallammo.dom;

public enum ItemRarityWrapper {
    COMMON("&f"),
    UNCOMMON("&e"),
    RARE("&b"),
    EPIC("&d");
    private final String color;

    ItemRarityWrapper(String color){
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
