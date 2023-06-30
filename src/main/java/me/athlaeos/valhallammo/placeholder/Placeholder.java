package me.athlaeos.valhallammo.placeholder;

import org.bukkit.entity.Player;

public abstract class Placeholder {
    protected String placeholder;

    public Placeholder(String placeholder){
        this.placeholder = placeholder;
    }
    public abstract String parse(String s, Player p);

    public String getPlaceholder() {
        return placeholder;
    }
}
