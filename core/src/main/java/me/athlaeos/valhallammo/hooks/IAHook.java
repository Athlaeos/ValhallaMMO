package me.athlaeos.valhallammo.hooks;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOptionRegistry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.IAChoice;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class IAHook extends PluginHook {
    public IAHook() {
        super("ItemsAdder");
    }

    @Override
    public void whenPresent() {
        RecipeOptionRegistry.registerOption(new IAChoice());
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new IAWrapper(), ValhallaMMO.getInstance());
        ValhallaMMO.logInfo("Registered ItemsAdder compatibility - It may now recognize custom items and blocks in its configurations");
    }
    // Get the Nexo ID from reflection or return false if there is an error
    public static String getItemsAdderItemID(ItemStack item) {
        return IAWrapper.getItemsAdderItemID(item);
    }

    public static ItemStack getItemsAdderItem(String type) {
        return IAWrapper.getItemsAdderItem(type);
    }

    public static String getItemsAdderBlock(Block b){
        return IAWrapper.getItemsAdderBlock(b);
    }

    public static boolean setItemsAdderBlock(Block b, String type){
        return IAWrapper.setItemsAdderBlock(b, type);
    }
}