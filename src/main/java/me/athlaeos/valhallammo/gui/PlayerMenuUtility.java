package me.athlaeos.valhallammo.gui;

import org.bukkit.entity.Player;

public class PlayerMenuUtility {
    private final Player owner;
    private Menu previousMenu = null;

    public PlayerMenuUtility(Player owner) {
        this.owner = owner;
    }

    public void setPreviousMenu(Menu previousMenu) {
        this.previousMenu = previousMenu;
    }

    public Menu getPreviousMenu() {
        return previousMenu;
    }

    public Player getOwner() {
        return owner;
    }
}
