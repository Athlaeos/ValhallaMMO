package me.athlaeos.valhallammo.content;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.crafting.recipetypes.*;
import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.dom.Weighted;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.persistence.GsonAdapter;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ContentPackageManager {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RecipeOption.class, new GsonAdapter<RecipeOption>("OPTION"))
            .registerTypeAdapter(IngredientChoice.class, new GsonAdapter<IngredientChoice>("CHOICE"))
            .registerTypeAdapter(DynamicItemModifier.class, new GsonAdapter<DynamicItemModifier>("MOD_TYPE"))
            .registerTypeAdapter(ValhallaRecipe.class, new GsonAdapter<ValhallaRecipe>("RECIPE_TYPE"))
            .registerTypeAdapter(Validation.class, new GsonAdapter<Validation>("VALIDATION_TYPE"))
            .registerTypeAdapter(LootPredicate.class, new GsonAdapter<LootPredicate>("PRED_TYPE"))
            .registerTypeAdapter(Weighted.class, new GsonAdapter<Weighted>("WEIGHTED_IMPL"))
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ItemStackGSONAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();

    public static ContentPackage fromFile(String path){
        File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/" + path + ".json");
        if (!f.exists()) {
            ValhallaMMO.logWarning("Could not load content package from " + path + ".json, it doesn't exist!");
            return null;
        }
        try (BufferedReader setsReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))){
            return gson.fromJson(setsReader, ContentPackage.class);
        } catch (IOException | JsonSyntaxException | NoClassDefFoundError exception){
            ValhallaMMO.logSevere("Could not load content package from " + path + ".json, " + exception.getMessage());
        }
        return null;
    }

    public static void importContent(ContentPackage contentPackage, List<String> recipes, ExportMode... importModes){
        List<ExportMode> modes = List.of(importModes);
        if (modes.contains(ExportMode.ITEMS)) contentPackage.getCustomItems().values().forEach(i -> CustomItemRegistry.register(i.getId(), i));
        if (modes.contains(ExportMode.LOOT_TABLES)) contentPackage.getLootTables().values().forEach(l -> LootTableRegistry.registerLootTable(l, true));
        if (modes.contains(ExportMode.LOOT_CONFIGURATION)) LootTableRegistry.applyConfiguration(contentPackage.getLootTableConfiguration());
        if (modes.contains(ExportMode.REPLACEMENT_TABLES)) contentPackage.getReplacementTables().values().forEach(l -> LootTableRegistry.registerReplacementTable(l, true));
        if (modes.contains(ExportMode.REPLACEMENT_CONFIGURATION)) LootTableRegistry.applyConfiguration(contentPackage.getReplacementTableConfiguration());
        if (modes.contains(ExportMode.RECIPES_IMMERSIVE)) contentPackage.getImmersiveRecipes().values().stream().filter(r -> recipes.isEmpty() || recipes.contains(r.getName())).forEach(r -> CustomRecipeRegistry.register(r, true));
        if (modes.contains(ExportMode.RECIPES_SMITHING)) contentPackage.getSmithingRecipes().values().stream().filter(r -> recipes.isEmpty() || recipes.contains(r.getName())).forEach(r -> CustomRecipeRegistry.register(r, true));
        if (modes.contains(ExportMode.RECIPES_GRID)) contentPackage.getGridRecipes().values().stream().filter(r -> recipes.isEmpty() || recipes.contains(r.getName())).forEach(r -> CustomRecipeRegistry.register(r, true));
        if (modes.contains(ExportMode.RECIPES_BREWING)) contentPackage.getBrewingRecipes().values().stream().filter(r -> recipes.isEmpty() || recipes.contains(r.getName())).forEach(r -> CustomRecipeRegistry.register(r, true));
        if (modes.contains(ExportMode.RECIPES_COOKING)) contentPackage.getCookingRecipes().values().stream().filter(r -> recipes.isEmpty() || recipes.contains(r.getName())).forEach(r -> CustomRecipeRegistry.register(r, true));
        if (modes.contains(ExportMode.RECIPES_CAULDRON)) contentPackage.getCauldronRecipes().values().stream().filter(r -> recipes.isEmpty() || recipes.contains(r.getName())).forEach(r -> CustomRecipeRegistry.register(r, true));
        CustomRecipeRegistry.setChangesMade();
    }

    public static void importContent(ContentPackage contentPackage){
        importContent(contentPackage, new ArrayList<>(), ExportMode.values());
    }

    public static boolean exportContent(String packageName, List<ValhallaRecipe> recipes, boolean add, ExportMode... exportModes){
        ContentPackage contentPackage = add ? Utils.thisorDefault(fromFile(packageName), new ContentPackage()) : new ContentPackage();
        for (ExportMode mode : exportModes) mode.act(contentPackage);
        for (ValhallaRecipe recipe : recipes){
            if (recipe instanceof ImmersiveCraftingRecipe r) contentPackage.getImmersiveRecipes().put(r.getName(), r);
            else if (recipe instanceof DynamicCauldronRecipe r) contentPackage.getCauldronRecipes().put(r.getName(), r);
            else if (recipe instanceof DynamicGridRecipe r) contentPackage.getGridRecipes().put(r.getName(), r);
            else if (recipe instanceof DynamicCookingRecipe r) contentPackage.getCookingRecipes().put(r.getName(), r);
            else if (recipe instanceof DynamicBrewingRecipe r) contentPackage.getBrewingRecipes().put(r.getName(), r);
            else if (recipe instanceof DynamicSmithingRecipe r) contentPackage.getSmithingRecipes().put(r.getName(), r);
        }

        File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/export/" + packageName + ".json");
        try {
            if (!f.getParentFile().exists() && !f.getParentFile().mkdirs()) {
                ValhallaMMO.logWarning("Could not save items to export/" + packageName + ".json, file directory could not be created");
                return false;
            }
            if (!f.exists() && !f.createNewFile()) {
                ValhallaMMO.logWarning("Could not save items to export/" + packageName + ".json, file could not be created");
                return false;
            }
        } catch (IOException ignored){}
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8))){
            JsonElement element = gson.toJsonTree(contentPackage, ContentPackage.class);
            gson.toJson(element, writer);
            writer.flush();
            return true;
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not save items to export/" + packageName + ".json, " + exception.getMessage());
            return false;
        }
    }

    public static void exportContent(String path){
        exportContent(path, new ArrayList<>(), false, ExportMode.values());
    }

    public enum ExportMode{
        RECIPES_BREWING(p -> p.getBrewingRecipes().putAll(CustomRecipeRegistry.getBrewingRecipes())),
        RECIPES_CAULDRON(p -> p.getCauldronRecipes().putAll(CustomRecipeRegistry.getCauldronRecipes())),
        RECIPES_COOKING(p -> p.getCookingRecipes().putAll(CustomRecipeRegistry.getCookingRecipes())),
        RECIPES_GRID(p -> p.getGridRecipes().putAll(CustomRecipeRegistry.getGridRecipes())),
        RECIPES_SMITHING(p -> p.getSmithingRecipes().putAll(CustomRecipeRegistry.getSmithingRecipes())),
        RECIPES_IMMERSIVE(p -> p.getImmersiveRecipes().putAll(CustomRecipeRegistry.getImmersiveRecipes())),
        LOOT_TABLES(p -> p.getLootTables().putAll(LootTableRegistry.getLootTables())),
        LOOT_CONFIGURATION(p -> p.setLootTableConfiguration(LootTableRegistry.getLootTableConfiguration())),
        REPLACEMENT_TABLES(p -> p.getReplacementTables().putAll(LootTableRegistry.getReplacementTables())),
        REPLACEMENT_CONFIGURATION(p -> p.setReplacementTableConfiguration(LootTableRegistry.getReplacementTableConfiguration())),
        ITEMS(p -> p.getCustomItems().putAll(CustomItemRegistry.getItems()));

        private final Action<ContentPackage> action;
        ExportMode(Action<ContentPackage> action){
            this.action = action;
        }

        private void act(ContentPackage contentPackage){
            action.act(contentPackage);
        }
    }
}
