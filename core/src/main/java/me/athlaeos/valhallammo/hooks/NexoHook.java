package me.athlaeos.valhallammo.hooks;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOptionRegistry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.NexoChoice;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class NexoHook extends PluginHook {
    public NexoHook() {
        super("Nexo");
    }

    @Override
    public void whenPresent() {
        // Register NexoChoice, but don't try to load any Nexo classes yet
        RecipeOptionRegistry.registerOption(new NexoChoice());
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new NexoWrapper(), ValhallaMMO.getInstance());
        ValhallaMMO.logInfo("Registered Nexo compatibility - It may now recognize custom items and blocks in its configurations");
    }

    public static String getNexoItemID(ItemStack item) {
        return NexoWrapper.getNexoItemID(item);
    }

    public static ItemStack getNexoItem(String type) {
        return NexoWrapper.getNexoItem(type);
    }

    public static String getNexoBlock(Block b){
        return NexoWrapper.getNexoBlock(b);
    }

    public static boolean setNexoBlock(Block b, String type){
        return NexoWrapper.setNexoBlock(b, type);
    }
}