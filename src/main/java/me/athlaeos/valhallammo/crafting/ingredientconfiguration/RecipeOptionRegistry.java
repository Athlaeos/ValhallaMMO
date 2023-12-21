package me.athlaeos.valhallammo.crafting.ingredientconfiguration;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.*;

import java.util.HashMap;
import java.util.Map;

public class RecipeOptionRegistry {
    private static final Map<String, RecipeOption> options = new HashMap<>();

    static {
        registerOption(new ExactChoice());
        registerOption(new MaterialChoice());
        registerOption(new MaterialWithDataChoice());
        registerOption(new ArmorChoice());
        registerOption(new SimilarMaterialsChoice());
        registerOption(new SimilarTypeChoice());
        registerOption(new ConfigurableMaterialsChoice());
        registerOption(new PotionChoice());
        registerOption(new MaterialWithIDChoice());
    }

    public static void registerOption(RecipeOption c) {
        options.put(c.getName(), c);
    }

    public static Map<String, RecipeOption> getOptions() {
        return options;
    }

    public static RecipeOption createOption(String name){
        if (!options.containsKey(name)) throw new IllegalArgumentException("Recipe option " + name + " doesn't exist");
        return options.get(name).getNew();
    }
}
