package me.athlaeos.valhallammo.content;

import me.athlaeos.valhallammo.crafting.recipetypes.*;
import me.athlaeos.valhallammo.item.CustomItem;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ContentPackage {
    private final Map<String, DynamicBrewingRecipe> brewingRecipes = new HashMap<>();
    private final Map<String, DynamicCauldronRecipe> cauldronRecipes = new HashMap<>();
    private final Map<String, DynamicCookingRecipe> cookingRecipes = new HashMap<>();
    private final Map<String, DynamicGridRecipe> gridRecipes = new HashMap<>();
    private final Map<String, DynamicSmithingRecipe> smithingRecipes = new HashMap<>();
    private final Map<String, ImmersiveCraftingRecipe> immersiveRecipes = new HashMap<>();
    private final Map<String, LootTable> lootTables = new HashMap<>();
    private LootTableConfiguration lootTableConfiguration = null;
    private final Map<String, CustomItem> customItems = new HashMap<>();

    public LootTableConfiguration getLootTableConfiguration() { return lootTableConfiguration; }
    public void setLootTableConfiguration(LootTableConfiguration lootTableConfiguration) { this.lootTableConfiguration = lootTableConfiguration; }
    public Map<String, CustomItem> getCustomItems() { return customItems; }
    public Map<String, DynamicBrewingRecipe> getBrewingRecipes() { return brewingRecipes; }
    public Map<String, DynamicCauldronRecipe> getCauldronRecipes() { return cauldronRecipes; }
    public Map<String, DynamicCookingRecipe> getCookingRecipes() { return cookingRecipes; }
    public Map<String, DynamicGridRecipe> getGridRecipes() { return gridRecipes; }
    public Map<String, DynamicSmithingRecipe> getSmithingRecipes() { return smithingRecipes; }
    public Map<String, ImmersiveCraftingRecipe> getImmersiveRecipes() { return immersiveRecipes; }
    public Map<String, LootTable> getLootTables() { return lootTables; }
}
