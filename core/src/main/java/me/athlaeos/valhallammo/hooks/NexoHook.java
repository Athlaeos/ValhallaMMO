package me.athlaeos.valhallammo.hooks;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOptionRegistry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.NexoChoice;

public class NexoHook extends PluginHook {
    private static boolean hookInitialized = false;

    public NexoHook() {
        super("Nexo");
    }

    @Override
    public void whenPresent() {
        // Register NexoChoice, but don't try to load any Nexo classes yet
        RecipeOptionRegistry.registerOption(new NexoChoice());
        ValhallaMMO.logInfo("Registered Nexo compatibility - hook will initialize when needed");
    }

    // Get the Nexo ID from reflection or return false if there is an error
    public static String getNexoId(org.bukkit.inventory.ItemStack item) {
        if (item == null) return null;

        try {
            // Attempt to access Nexo classes when this method is called
            if (!hookInitialized) {
                initializeHook();
            }

            // If initialization failed or item is null, return null
            if (!hookInitialized) {
                return null;
            }

            // Use reflection to call the method
            Class<?> nexoItemsClass = Class.forName("com.nexomc.nexo.api.NexoItems");
            Object result = nexoItemsClass.getMethod("idFromItem", org.bukkit.inventory.ItemStack.class)
                    .invoke(null, item);
            return (String) result;
        } catch (Exception e) {
            // If errors occur, return null
            return null;
        }
    }

    private static void initializeHook() {
        try {
            // Try calling a method to make sure it works
            Class.forName("com.nexomc.nexo.api.NexoItems");
            hookInitialized = true;
        } catch (Exception e) {
            ValhallaMMO.logWarning("Failed to initialize Nexo hook: " + e.getMessage());
            hookInitialized = false;
        }
    }

    // Checks that the hook is functioning
    public static boolean isHookFunctional() {
        // Simple presence check without trying to load any classes
        return ValhallaMMO.getInstance().getServer().getPluginManager().getPlugin("Nexo") != null;
    }
}