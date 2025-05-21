package me.athlaeos.valhallammo.version;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicCookingRecipe;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.BlockUtils;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.Recipe;

public class FurnaceStartSmeltListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFurnaceStart(FurnaceStartSmeltEvent e){
        if (e.getBlock().getState() instanceof Furnace){
            DynamicCookingRecipe recipe = CustomRecipeRegistry.getCookingRecipesByKey().get(e.getRecipe().getKey());
            if (recipe == null) {
                if (CustomRecipeRegistry.getDisabledRecipes().contains(e.getRecipe().getKey())) {
                    e.setTotalCookTime(Integer.MAX_VALUE);
                    return;
                }
            } else {
                Recipe r = ValhallaMMO.getInstance().getServer().getRecipe(recipe.getKey());
                if (!(r instanceof CookingRecipe<?>)) throw new IllegalStateException("Recipe linked to dynamic cooking recipe key is not a cooking recipe");
            }
            Player owner = BlockUtils.getOwner(e.getBlock());
            double cookingSpeedBonus = 1 + (owner == null ? 0 : AccumulativeStatManager.getCachedStats("COOKING_SPEED_BONUS", owner, 10000, true));
            e.setTotalCookTime((int) (cookingSpeedBonus <= 0 ? Integer.MAX_VALUE : e.getRecipe().getCookingTime() / cookingSpeedBonus));
        }
    }
}
