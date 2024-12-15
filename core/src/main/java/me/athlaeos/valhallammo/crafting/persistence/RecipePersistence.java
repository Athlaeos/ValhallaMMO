package me.athlaeos.valhallammo.crafting.persistence;

import com.google.gson.Gson;
import me.athlaeos.valhallammo.crafting.recipetypes.ValhallaRecipe;

import java.io.File;
import java.util.Collection;
import java.util.Map;

public abstract class RecipePersistence {
    protected final Gson gson;
    public RecipePersistence(Gson gson){
        this.gson = gson;
    }

    public abstract Map<String, ValhallaRecipe> getRecipesFromFile(File f);

    public abstract void saveRecipesToFile(String fileName, Collection<? extends ValhallaRecipe> recipes);
}
