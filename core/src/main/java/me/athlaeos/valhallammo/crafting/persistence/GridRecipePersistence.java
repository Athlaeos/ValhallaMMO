package me.athlaeos.valhallammo.crafting.persistence;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicGridRecipe;
import me.athlaeos.valhallammo.crafting.recipetypes.ValhallaRecipe;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GridRecipePersistence extends RecipePersistence {
    public GridRecipePersistence(Gson gson) {
        super(gson);
    }

    @Override
    public Map<String, ValhallaRecipe> getRecipesFromFile(File f){
        Map<String, ValhallaRecipe> recipes = new HashMap<>();
        if (f.exists()){
            try (BufferedReader recipesReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))){
                DynamicGridRecipe[] collectedRecipes = gson.fromJson(recipesReader, DynamicGridRecipe[].class);
                for (DynamicGridRecipe recipe : collectedRecipes) if (recipe != null) recipes.put(recipe.getName(), recipe);
            } catch (IOException | JsonSyntaxException exception){
                ValhallaMMO.logSevere("Could not load recipes file " + f.getPath() + ", " + exception.getMessage());
            } catch (NoClassDefFoundError ignored){}
        } else {
            ValhallaMMO.logWarning("File " + f.getPath() + " does not exist!");
        }

        return recipes;
    }

    @Override
    public void saveRecipesToFile(String fileName, Collection<? extends ValhallaRecipe> recipes){
        if (!fileName.endsWith(".json")) {
            ValhallaMMO.logWarning("The selected file to save recipes to (" + fileName + ") is not a valid .json file!");
            return;
        }
        recipes = new ArrayList<>(recipes.stream().filter(r -> r instanceof DynamicGridRecipe).toList());
        File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/" + fileName);
        if (!f.exists()) ValhallaMMO.getInstance().saveResource(fileName, false);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8))){
            JsonElement element = gson.toJsonTree(new ArrayList<>(recipes), new TypeToken<ArrayList<DynamicGridRecipe>>(){}.getType());
            gson.toJson(element, writer);
            writer.flush();
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not save grid recipes file " + fileName+ ", " + exception.getMessage());
        }
    }
}
