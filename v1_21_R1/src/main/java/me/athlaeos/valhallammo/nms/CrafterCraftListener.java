package me.athlaeos.valhallammo.nms;

import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicGridRecipe;
import org.bukkit.Keyed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.inventory.*;

public class CrafterCraftListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraft(CrafterCraftEvent e){
        if (e.getRecipe() instanceof ShapedRecipe || e.getRecipe() instanceof ShapelessRecipe) {
            DynamicGridRecipe recipe = CustomRecipeRegistry.getGridRecipesByKey().get(((Keyed) e.getRecipe()).getKey());
            if (recipe == null) return;
            e.setCancelled(true);
        }
    }
}
