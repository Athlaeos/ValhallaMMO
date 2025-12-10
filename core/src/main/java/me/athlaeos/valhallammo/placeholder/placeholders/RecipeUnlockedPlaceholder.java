package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.ValhallaRecipe;
import me.athlaeos.valhallammo.placeholder.Placeholder;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

public class RecipeUnlockedPlaceholder extends Placeholder {
    public RecipeUnlockedPlaceholder(String placeholder) {
        super(placeholder);
    }

    @Override
    public String parse(String s, Player p) {
        String base = placeholder.replace("%", "");
        String subString = StringUtils.substringBetween(s,"%" + base, "%");
        if (subString == null) return s;
        ValhallaRecipe recipe = CustomRecipeRegistry.getAllValhallaRecipes().get(subString);
        if (recipe == null) return s;
        PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
        boolean result = recipe.isUnlockedForEveryone() || p.hasPermission("valhalla.allrecipes") || profile.getUnlockedRecipes().contains(recipe.getName())
                || p.hasPermission("valhalla.recipe." + recipe.getName());
        return s.replace(String.format("%%%s%s%%", base, subString), String.format("%s", result));
    }

    @Override
    public boolean matchString(String string) {
        String base = placeholder.replace("%", "");
        String subString = StringUtils.substringBetween(string,"%" + base, "%");
        return subString != null;
    }
}
