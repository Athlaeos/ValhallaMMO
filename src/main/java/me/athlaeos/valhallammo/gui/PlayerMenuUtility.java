package me.athlaeos.valhallammo.gui;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerMenuUtility {
    private final Player owner;
    private Menu previousMenu = null;
    private final Map<String, RecipeOption> options = new HashMap<>();

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
    public void addOption(RecipeOption option){
        options.put(option.getName(), option.getNew());
    }
    public void clearOptions(){
        options.clear();
    }

    public Map<String, RecipeOption> getOptions() {
        return options;
    }
}
