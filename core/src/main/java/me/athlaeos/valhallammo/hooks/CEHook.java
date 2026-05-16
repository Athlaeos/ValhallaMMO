package me.athlaeos.valhallammo.hooks;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOptionRegistry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.CEChoice;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class CEHook extends PluginHook {

    public CEHook() {
        super("CraftEngine");
    }

    @Override
    public void whenPresent() {
        RecipeOptionRegistry.registerOption(new CEChoice());
        Bukkit.getPluginManager().registerEvents(new CEWrapper(), ValhallaMMO.getInstance());
        ValhallaMMO.logInfo("Registered CraftEngine compatibility - It may now recognize custom items and blocks in its configurations");
    }

    public static String getCraftEngineItemID(ItemStack item) {
        return CEWrapper.getCraftEngineItemID(item);
    }

    public static ItemStack getCraftEngineItem(String id) {
        return CEWrapper.getCraftEngineItem(id);
    }

    public static String getCraftEngineBlock(Block block) {
        return CEWrapper.getCraftEngineBlock(block);
    }

    public static boolean setCraftEngineBlock(Block block, String id) {
        return CEWrapper.setCraftEngineBlock(block, id);
    }
}
