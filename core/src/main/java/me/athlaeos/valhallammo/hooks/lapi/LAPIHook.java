package me.athlaeos.valhallammo.hooks.lapi;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.hooks.PluginHook;

public class LAPIHook extends PluginHook {

    public LAPIHook() {
        super("LoreAPI");
    }

    @Override
    public void whenPresent() {
        ValhallaMMO.getInstance().saveConfig("lapi_configuration.yml");

    }
}
