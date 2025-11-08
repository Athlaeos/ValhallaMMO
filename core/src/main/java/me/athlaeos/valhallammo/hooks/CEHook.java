package me.athlaeos.valhallammo.hooks;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOptionRegistry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.CEChoice;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class CEHook extends PluginHook {
    public CEHook() {
        super("CraftEngine");
    }

    @Override
    public void whenPresent() {
        RecipeOptionRegistry.registerOption(new CEChoice());
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new CEWrapper(), ValhallaMMO.getInstance());
        ValhallaMMO.logInfo("Registered CraftEngine compatibility - It may now recognize custom items and blocks in its configurations");
    }
    // Get the Nexo ID from reflection or return false if there is an error
    public static String getCraftEngineItemID(ItemStack item) {
        return CEWrapper.getCraftEngineItemID(item);
    }

    public static ItemStack getCraftEngineItem(String type) {
        return CEWrapper.getCraftEngineItem(type);
    }

    public static String getCraftEngineBlock(Block b){
        return CEWrapper.getCraftEngineBlock(b);
    }

    public static boolean setCraftEngineBlock(Block b, String type){
        return CEWrapper.setCraftEngineBlock(b, type);
    }
}