package me.athlaeos.valhallammo.hooks;

import me.athlaeos.valhallammo.ValhallaMMO;

public abstract class PluginHook {
    private final boolean isPresent;
    public PluginHook(String name){
        this.isPresent = ValhallaMMO.getInstance().getServer().getPluginManager().getPlugin(name) != null;
    }

    public boolean isPresent(){
        return isPresent;
    }
}
