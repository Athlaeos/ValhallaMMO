package me.athlaeos.valhallammo.crafting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.crafting.recipetypes.*;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.*;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.persistence.GsonAdapter;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import me.athlaeos.valhallammo.listeners.CookingListener;
import me.athlaeos.valhallammo.listeners.SmithingTableListener;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class CustomRecipeRegistry {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RecipeOption.class, new GsonAdapter<RecipeOption>("OPTION"))
            .registerTypeAdapter(IngredientChoice.class, new GsonAdapter<IngredientChoice>("CHOICE"))
            .registerTypeAdapter(DynamicItemModifier.class, new GsonAdapter<DynamicItemModifier>("MOD_TYPE"))
            .registerTypeAdapter(ValhallaRecipe.class, new GsonAdapter<ValhallaRecipe>("RECIPE_TYPE"))
            .registerTypeAdapter(Validation.class, new GsonAdapter<Validation>("VALIDATION_TYPE"))
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ItemStackGSONAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();

    private static final Collection<NamespacedKey> disabledRecipes = new HashSet<>();
    private static final Map<String, DynamicBrewingRecipe> brewingRecipes = new HashMap<>();
    private static final Map<Material, Collection<DynamicBrewingRecipe>> brewingRecipesByIngredient = new HashMap<>();
    private static final Map<String, DynamicCauldronRecipe> cauldronRecipes = new HashMap<>();
    private static final Map<String, DynamicCookingRecipe> cookingRecipes = new HashMap<>();
    private static final Map<String, DynamicGridRecipe> gridRecipes = new HashMap<>();
    private static final Map<String, DynamicSmithingRecipe> smithingRecipes = new HashMap<>();
    private static final Map<String, ImmersiveCraftingRecipe> immersiveRecipes = new HashMap<>();
    private static final Map<NamespacedKey, DynamicCookingRecipe> cookingRecipesByKey = new HashMap<>();
    private static final Map<NamespacedKey, DynamicGridRecipe> gridRecipesByKey = new HashMap<>();
    private static final Map<Integer, Collection<DynamicGridRecipe>> gridRecipesByIngredientQuantities = new HashMap<>();
    private static final Map<NamespacedKey, DynamicSmithingRecipe> smithingRecipesByKey = new HashMap<>();
    private static final Map<Material, Collection<ImmersiveCraftingRecipe>> immersiveRecipesByBlock = new HashMap<>();
    private static final Collection<String> allRecipes = new HashSet<>();
    private static final Map<NamespacedKey, ValhallaKeyedRecipe> allKeyedRecipes = new HashMap<>();
    private static final Map<String, ValhallaKeyedRecipe> allKeyedRecipesByName = new HashMap<>();

    private static boolean changesMade = false;

    public static void loadFiles(){
        loadBrewingRecipes(new File(ValhallaMMO.getInstance().getDataFolder(), "/recipes/brewing_recipes.json"), false);
        loadGridRecipes(new File(ValhallaMMO.getInstance().getDataFolder(), "/recipes/grid_recipes.json"), false);
        loadSmithingRecipes(new File(ValhallaMMO.getInstance().getDataFolder(), "/recipes/smithing_recipes.json"), false);
        loadImmersiveRecipes(new File(ValhallaMMO.getInstance().getDataFolder(), "/recipes/immersive_recipes.json"), false);
        loadCauldronRecipes(new File(ValhallaMMO.getInstance().getDataFolder(), "/recipes/cauldron_recipes.json"), false);
        loadCookingRecipes(new File(ValhallaMMO.getInstance().getDataFolder(), "/recipes/cooking_recipes.json"), false);

        YamlConfiguration disabled = ConfigManager.getConfig("recipes/disabled_recipes.yml").reload().get();
        for (String s : disabled.getStringList("disabled")){
            try {
                NamespacedKey recipeKey = NamespacedKey.minecraft(s.toLowerCase());
                disabledRecipes.add(recipeKey);
            } catch (IllegalArgumentException ignored){
                ValhallaMMO.logWarning("Invalid recipe key '" + s + "' found, recipe contains illegal characters. Allowed characters: [a-zA-Z0-9/. -], cancelled crafting recipe removal");
            }
        }
    }

    public static void removeDisabledRecipe(NamespacedKey key){
        disabledRecipes.remove(key);
    }

    public static void addDisabledRecipe(NamespacedKey key){
        disabledRecipes.add(key);
    }

    public static Collection<NamespacedKey> getDisabledRecipes() {
        return disabledRecipes;
    }

    public static void saveRecipes(boolean async){
        if (!changesMade) return;
        if (async) ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), CustomRecipeRegistry::saveAllRecipeTypes);
        else saveAllRecipeTypes();
    }

    private static void saveAllRecipeTypes(){
        new RecipePersistence<DynamicBrewingRecipe>().saveRecipesToFile("recipes/brewing_recipes.json", brewingRecipes.values());
        new RecipePersistence<DynamicGridRecipe>().saveRecipesToFile("recipes/grid_recipes.json", gridRecipes.values());
        new RecipePersistence<DynamicSmithingRecipe>().saveRecipesToFile("recipes/smithing_recipes.json", smithingRecipes.values());
        new RecipePersistence<ImmersiveCraftingRecipe>().saveRecipesToFile("recipes/immersive_recipes.json", immersiveRecipes.values());
        new RecipePersistence<DynamicCauldronRecipe>().saveRecipesToFile("recipes/cauldron_recipes.json", cauldronRecipes.values());
        new RecipePersistence<DynamicCookingRecipe>().saveRecipesToFile("recipes/cooking_recipes.json", cookingRecipes.values());

        YamlConfiguration disabled = ConfigManager.getConfig("recipes/disabled_recipes.yml").reload().get();
        disabled.set("disabled", disabledRecipes.stream().map(NamespacedKey::getKey).collect(Collectors.toList()));
        ConfigManager.saveConfig("recipes/disabled_recipes.yml");
    }

    public static void loadBrewingRecipes(File file, boolean overwrite){
        ValhallaMMO.logInfo("Loading brewing recipes from " + file.getPath());
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () ->
                new RecipePersistence<DynamicBrewingRecipe>().getRecipesFromFile(file, DynamicBrewingRecipe[].class).forEach((k, r) ->
                    ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> register(r, overwrite))
                )
        );
    }
    public static void loadCauldronRecipes(File file, boolean overwrite){
        ValhallaMMO.logInfo("Loading cauldron recipes from " + file.getPath());
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () ->
                new RecipePersistence<DynamicCauldronRecipe>().getRecipesFromFile(file, DynamicCauldronRecipe[].class).forEach((k, r) ->
                        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> register(r, overwrite))
                )
        );
    }
    public static void loadCookingRecipes(File file, boolean overwrite){
        ValhallaMMO.logInfo("Loading cooking recipes from " + file.getPath());
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () ->
                new RecipePersistence<DynamicCookingRecipe>().getRecipesFromFile(file, DynamicCookingRecipe[].class).forEach((k, r) ->
                        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> register(r, overwrite))
                )
        );
    }
    public static void loadGridRecipes(File file, boolean overwrite){
        ValhallaMMO.logInfo("Loading crafting grid recipes from " + file.getPath());
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () ->
                new RecipePersistence<DynamicGridRecipe>().getRecipesFromFile(file, DynamicGridRecipe[].class).forEach((k, r) ->
                        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> register(r, overwrite))
                )
        );
    }
    public static void loadSmithingRecipes(File file, boolean overwrite){
        ValhallaMMO.logInfo("Loading smithing recipes from " + file.getPath());
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () ->
                new RecipePersistence<DynamicSmithingRecipe>().getRecipesFromFile(file, DynamicSmithingRecipe[].class).forEach((k, r) ->
                        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> register(r, overwrite))
                )
        );
    }
    public static void loadImmersiveRecipes(File file, boolean overwrite){
        ValhallaMMO.logInfo("Loading immersive recipes from " + file.getPath());
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () ->
                new RecipePersistence<ImmersiveCraftingRecipe>().getRecipesFromFile(file, ImmersiveCraftingRecipe[].class).forEach((k, r) ->
                        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> register(r, overwrite))
                )
        );
    }

    public static void register(DynamicBrewingRecipe recipe, boolean overwrite){
        if (overwrite || !brewingRecipes.containsKey(recipe.getName().toLowerCase())) {
            brewingRecipes.put(recipe.getName().toLowerCase(), recipe);
            allRecipes.add(recipe.getName().toLowerCase());

            Collection<DynamicBrewingRecipe> existing = brewingRecipesByIngredient.getOrDefault(recipe.getIngredient().getItem().getType(), new HashSet<>());
            existing.add(recipe);
            brewingRecipesByIngredient.put(recipe.getIngredient().getItem().getType(), existing);
        }
    }
    public static void register(DynamicCauldronRecipe recipe, boolean overwrite){
        if (overwrite || !cauldronRecipes.containsKey(recipe.getName().toLowerCase())) {
            cauldronRecipes.put(recipe.getName().toLowerCase(), recipe);
            allRecipes.add(recipe.getName().toLowerCase());
        }
    }
    public static void register(ImmersiveCraftingRecipe recipe, boolean overwrite){
        if (overwrite || !immersiveRecipes.containsKey(recipe.getName().toLowerCase())) {
            immersiveRecipes.put(recipe.getName().toLowerCase(), recipe);
            allRecipes.add(recipe.getName().toLowerCase());

            Material base = ItemUtils.getBaseMaterial(recipe.getBlock());
            Collection<ImmersiveCraftingRecipe> recipesByBlock = immersiveRecipesByBlock.getOrDefault(base, new HashSet<>());
            recipesByBlock.add(recipe);
            immersiveRecipesByBlock.put(base, recipesByBlock);
        }
    }
    public static void register(DynamicCookingRecipe recipe, boolean overwrite){
        if (overwrite || !cookingRecipes.containsKey(recipe.getName().toLowerCase())) {
            cookingRecipes.put(recipe.getName().toLowerCase(), recipe);
            cookingRecipesByKey.put(recipe.getKey(), recipe);
            allRecipes.add(recipe.getName().toLowerCase());
            allKeyedRecipes.put(recipe.getKey(), recipe);
            allKeyedRecipesByName.put(recipe.getName(), recipe);
            recipe.registerRecipe();
            CookingListener.campfireRecipeCache.clear();
            CookingListener.furnaceRecipeCache.clear();
        }
    }
    public static void register(DynamicGridRecipe recipe, boolean overwrite){
        if (overwrite || !gridRecipes.containsKey(recipe.getName().toLowerCase())) {
            gridRecipes.put(recipe.getName().toLowerCase(), recipe);
            gridRecipesByKey.put(recipe.getKey(), recipe);
            gridRecipesByKey.put(recipe.getKey2(), recipe);
            Collection<DynamicGridRecipe> existing = gridRecipesByIngredientQuantities.getOrDefault(recipe.getItems().size(), new HashSet<>());
            existing.add(recipe);
            gridRecipesByIngredientQuantities.put(recipe.getItems().size(), existing);
            allRecipes.add(recipe.getName().toLowerCase());
            allKeyedRecipes.put(recipe.getKey(), recipe);
            allKeyedRecipesByName.put(recipe.getName(), recipe);
            recipe.registerRecipe();
        }
    }
    public static void register(DynamicSmithingRecipe recipe, boolean overwrite){
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20) &&
                (recipe.getTemplate() == null ||
                        recipe.getTemplate().getOption() == null ||
                        ItemUtils.isEmpty(recipe.getTemplate().getItem()) ||
                        recipe.getTemplate().getOption().getChoice(recipe.getTemplate().getItem()) == null
                )
        ) recipe.setTemplate(new SlotEntry(new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE), new MaterialChoice()));
        if (overwrite || !smithingRecipes.containsKey(recipe.getName().toLowerCase())) {
            smithingRecipes.put(recipe.getName().toLowerCase(), recipe);
            smithingRecipesByKey.put(recipe.getKey(), recipe);
            allRecipes.add(recipe.getName().toLowerCase());
            allKeyedRecipes.put(recipe.getKey(), recipe);
            allKeyedRecipesByName.put(recipe.getName(), recipe);
            recipe.registerRecipe();
            SmithingTableListener.smithingRecipeCache.clear();
        }
    }

    public static void setChangesMade() {
        changesMade = true;
    }

    public static void unregister(String recipe){
        allRecipes.remove(recipe);
        if (brewingRecipes.containsKey(recipe)) brewingRecipes.remove(recipe);
        else if (cauldronRecipes.containsKey(recipe)) cauldronRecipes.remove(recipe);
        else if (immersiveRecipes.containsKey(recipe)) {
            ImmersiveCraftingRecipe r = immersiveRecipes.get(recipe);
            Material base = ItemUtils.getBaseMaterial(r.getBlock());
            Collection<ImmersiveCraftingRecipe> recipesByBlock = immersiveRecipesByBlock.getOrDefault(base, new HashSet<>());
            recipesByBlock.remove(r);
            immersiveRecipesByBlock.put(base, recipesByBlock);
            immersiveRecipes.remove(recipe);
        }
        else {
            if (cookingRecipes.containsKey(recipe)) {
                cookingRecipes.get(recipe).unregisterRecipe();
                cookingRecipes.remove(recipe);
            } else if (gridRecipes.containsKey(recipe)) {
                gridRecipes.get(recipe).unregisterRecipe();
                gridRecipes.remove(recipe);
            } else if (smithingRecipes.containsKey(recipe)) {
                smithingRecipes.get(recipe).unregisterRecipe();
                smithingRecipes.remove(recipe);
            }
        }
    }

    private static class RecipePersistence<T extends ValhallaRecipe> {
        private Map<String, T> getRecipesFromFile(File f, Class<T[]> clazz){
            Map<String, T> recipes = new HashMap<>();
            if (f.exists()){
                try (BufferedReader recipesReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))){
                    T[] collectedRecipes = gson.fromJson(recipesReader, clazz);
                    for (T recipe : collectedRecipes) if (recipe != null) recipes.put(recipe.getName(), recipe);
                } catch (IOException | JsonSyntaxException exception){
                    ValhallaMMO.logSevere("Could not load recipes file " + f.getPath() + ", " + exception.getMessage());
                } catch (NoClassDefFoundError ignored){}
            } else {
                ValhallaMMO.logWarning("File " + f.getPath() + " does not exist!");
            }

            return recipes;
        }

        private void saveRecipesToFile(String fileName, Collection<? extends ValhallaRecipe> recipes){
            if (!fileName.endsWith(".json")) {
                ValhallaMMO.logWarning("The selected file to save recipes to (" + fileName + ") is not a valid .json file!");
                return;
            }
            File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/" + fileName);
            if (!f.exists()) ValhallaMMO.getInstance().saveResource(fileName, false);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8))){
                JsonElement element = gson.toJsonTree(new ArrayList<>(recipes), new TypeToken<ArrayList<T>>(){}.getType());
                gson.toJson(element, writer);
                writer.flush();
            } catch (IOException | JsonSyntaxException exception){
                ValhallaMMO.logSevere("Could not save recipes file " + fileName+ ", " + exception.getMessage());
            }
        }
    }

    public static Map<String, DynamicBrewingRecipe> getBrewingRecipes() {
        return brewingRecipes;
    }

    public static Map<String, DynamicCauldronRecipe> getCauldronRecipes() {
        return cauldronRecipes;
    }

    public static Map<String, DynamicCookingRecipe> getCookingRecipes() {
        return cookingRecipes;
    }

    public static Map<NamespacedKey, DynamicCookingRecipe> getCookingRecipesByKey() {
        return cookingRecipesByKey;
    }

    public static Map<String, DynamicGridRecipe> getGridRecipes() {
        return gridRecipes;
    }

    public static Map<NamespacedKey, DynamicGridRecipe> getGridRecipesByKey() {
        return gridRecipesByKey;
    }

    public static Map<Integer, Collection<DynamicGridRecipe>> getGridRecipesByIngredientQuantity() {
        return gridRecipesByIngredientQuantities;
    }

    public static Map<String, DynamicSmithingRecipe> getSmithingRecipes() {
        return smithingRecipes;
    }

    public static Map<NamespacedKey, DynamicSmithingRecipe> getSmithingRecipesByKey() {
        return smithingRecipesByKey;
    }

    public static Map<String, ImmersiveCraftingRecipe> getImmersiveRecipes() {
        return immersiveRecipes;
    }

    public static Collection<String> getAllRecipes() {
        return allRecipes;
    }

    public static Map<Material, Collection<ImmersiveCraftingRecipe>> getImmersiveRecipesByBlock() {
        return immersiveRecipesByBlock;
    }

    public static Map<NamespacedKey, ValhallaKeyedRecipe> getAllKeyedRecipes() {
        return allKeyedRecipes;
    }

    public static Map<String, ValhallaKeyedRecipe> getAllKeyedRecipesByName() {
        return allKeyedRecipesByName;
    }

    public static Map<Material, Collection<DynamicBrewingRecipe>> getBrewingRecipesByIngredient() {
        return brewingRecipesByIngredient;
    }
}
