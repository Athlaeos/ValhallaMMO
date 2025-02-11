package me.athlaeos.valhallammo.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PAPIHook extends PluginHook{

    public PAPIHook() {
        super("PlaceholderAPI");
    }

    public static String parse(Player player, String string){
        return PlaceholderAPI.setPlaceholders(player, string);
    }

    @Override
    public void whenPresent() {
        new PAPIRelationalPlaceholder().register();
    }
}
