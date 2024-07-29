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
import me.athlaeos.valhallammo.crafting.recipetypes.ValhallaRecipe;
import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.dom.Weighted;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.persistence.GsonAdapter;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

    public static void importContent(ContentPackage contentPackage, ExportMode... importModes){
        List<ExportMode> modes = List.of(importModes);
        if (modes.contains(ExportMode.RECIPES_IMMERSIVE)) contentPackage.getImmersiveRecipes().values().forEach(r -> CustomRecipeRegistry.register(r, true));
        if (modes.contains(ExportMode.RECIPES_SMITHING)) contentPackage.getSmithingRecipes().values().forEach(r -> CustomRecipeRegistry.register(r, true));
        if (modes.contains(ExportMode.RECIPES_GRID)) contentPackage.getGridRecipes().values().forEach(r -> CustomRecipeRegistry.register(r, true));
        if (modes.contains(ExportMode.RECIPES_BREWING)) contentPackage.getBrewingRecipes().values().forEach(r -> CustomRecipeRegistry.register(r, true));
        if (modes.contains(ExportMode.RECIPES_COOKING)) contentPackage.getCookingRecipes().values().forEach(r -> CustomRecipeRegistry.register(r, true));
        if (modes.contains(ExportMode.RECIPES_CAULDRON)) contentPackage.getCauldronRecipes().values().forEach(r -> CustomRecipeRegistry.register(r, true));
        if (modes.contains(ExportMode.LOOT_TABLES)) contentPackage.getLootTables().values().forEach(l -> LootTableRegistry.registerLootTable(l, true));
        if (modes.contains(ExportMode.LOOT_CONFIGURATION)) LootTableRegistry.applyConfiguration(contentPackage.getLootTableConfiguration());
        if (modes.contains(ExportMode.ITEMS)) contentPackage.getCustomItems().values().forEach(i -> CustomItemRegistry.register(i.getId(), i));
    }

    public static void importContent(ContentPackage contentPackage){
        importContent(contentPackage, ExportMode.values());
    }

    public static boolean exportContent(String packageName, ExportMode... exportModes){
        ContentPackage contentPackage = new ContentPackage();
        for (ExportMode mode : exportModes) mode.act(contentPackage);

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
        exportContent(path, ExportMode.values());
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
