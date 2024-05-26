package me.athlaeos.valhallammo.crafting;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.ExactChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialCancelIfCosmeticsChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialWithDataChoice;

/**
 * The purpose of this enum is to apply a specific meta requirement to ingredients, where a RecipeOption would not be practical.
 */
public enum MetaRequirement {
    MATERIAL(new MaterialCancelIfCosmeticsChoice(), "&fIngredients need to match in material only.", "&fItems with custom names or lore are ignored"),
    EXACT(new ExactChoice(), "&fIngredients need to match exactly"),
    MATERIAL_WITH_DATA(new MaterialWithDataChoice(), "&fIngredients need to match in material", "&fAND custom model data");

    private final IngredientChoice choice;
    private final String[] description;

    MetaRequirement(IngredientChoice choice, String... description){
        this.choice = choice;
        this.description = description;
    }

    public IngredientChoice getChoice() {
        return choice;
    }

    public String[] getDescription() {
        return description;
    }
}
