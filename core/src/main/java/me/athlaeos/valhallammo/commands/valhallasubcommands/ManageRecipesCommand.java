package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.*;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.*;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManageRecipesCommand implements Command {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player player){
            if (args.length == 1){
                new RecipeOverviewMenu(PlayerMenuUtilManager.getPlayerMenuUtility(player)).open();
                return true;
            } else if (args.length >= 3){
                String method = args[1];
                if (method.equalsIgnoreCase("new")){
                    if (args.length == 3) return false;
                    String type = args[2].toLowerCase(java.util.Locale.US);
                    if (CustomRecipeRegistry.getAllRecipes().contains(args[3])) {
                        Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_recipe_exists"));
                        return true;
                    }
                    switch (type){
                        case "crafting_table" -> new GridRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), new DynamicGridRecipe(args[3])).open();
                        case "furnace" -> new CookingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), new DynamicCookingRecipe(args[3], DynamicCookingRecipe.CookingRecipeType.FURNACE)).open();
                        case "blasting" -> new CookingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), new DynamicCookingRecipe(args[3], DynamicCookingRecipe.CookingRecipeType.BLAST_FURNACE)).open();
                        case "smoking" -> new CookingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), new DynamicCookingRecipe(args[3], DynamicCookingRecipe.CookingRecipeType.SMOKER)).open();
                        case "campfire" -> new CookingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), new DynamicCookingRecipe(args[3], DynamicCookingRecipe.CookingRecipeType.CAMPFIRE)).open();
                        case "brewing" -> new BrewingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), new DynamicBrewingRecipe(args[3])).open();
                        case "cauldron" -> new CauldronRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), new DynamicCauldronRecipe(args[3])).open();
                        case "smithing" -> new SmithingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), new DynamicSmithingRecipe(args[3])).open();
                        case "immersive" -> new ImmersiveRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), new ImmersiveCraftingRecipe(args[3])).open();
                        default -> {
                            Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_invalid_recipe"));
                            return true;
                        }
                    }
                    return true;
                } else if (method.equalsIgnoreCase("edit")) {
                    String recipe = args[2].toLowerCase(java.util.Locale.US);
                    if (CustomRecipeRegistry.getCauldronRecipes().containsKey(recipe))
                        new CauldronRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), CustomRecipeRegistry.getCauldronRecipes().get(recipe)).open();
                    else if (CustomRecipeRegistry.getCookingRecipes().containsKey(recipe))
                        new CookingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), CustomRecipeRegistry.getCookingRecipes().get(recipe)).open();
                    else if (CustomRecipeRegistry.getGridRecipes().containsKey(recipe))
                        new GridRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), CustomRecipeRegistry.getGridRecipes().get(recipe)).open();
                    else if (CustomRecipeRegistry.getBrewingRecipes().containsKey(recipe))
                        new BrewingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), CustomRecipeRegistry.getBrewingRecipes().get(recipe)).open();
                    else if (CustomRecipeRegistry.getSmithingRecipes().containsKey(recipe))
                        new SmithingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), CustomRecipeRegistry.getSmithingRecipes().get(recipe)).open();
                    else if (CustomRecipeRegistry.getImmersiveRecipes().containsKey(recipe))
                        new ImmersiveRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), CustomRecipeRegistry.getImmersiveRecipes().get(recipe)).open();
                    else Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_recipe_missing"));
                    return true;
                } else if (method.equalsIgnoreCase("copy")) {
                    if (args.length == 3) return false;
                    String recipe = args[2].toLowerCase(java.util.Locale.US);
                    String newRecipe = args[3].toLowerCase(java.util.Locale.US);
                    if (!CustomRecipeRegistry.getAllRecipes().contains(recipe)) {
                        Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_recipe_missing"));
                        return true;
                    }
                    if (CustomRecipeRegistry.getAllRecipes().contains(newRecipe)){
                        Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_recipe_exists"));
                        return true;
                    }
                    if (CustomRecipeRegistry.getCauldronRecipes().containsKey(recipe))
                        new CauldronRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), CustomRecipeRegistry.getCauldronRecipes().get(recipe), newRecipe).open();
                    else if (CustomRecipeRegistry.getCookingRecipes().containsKey(recipe))
                        new CookingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), CustomRecipeRegistry.getCookingRecipes().get(recipe), newRecipe).open();
                    else if (CustomRecipeRegistry.getGridRecipes().containsKey(recipe))
                        new GridRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), CustomRecipeRegistry.getGridRecipes().get(recipe), newRecipe).open();
                    else if (CustomRecipeRegistry.getBrewingRecipes().containsKey(recipe))
                        new BrewingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), CustomRecipeRegistry.getBrewingRecipes().get(recipe), newRecipe).open();
                    else if (CustomRecipeRegistry.getSmithingRecipes().containsKey(recipe))
                        new SmithingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), CustomRecipeRegistry.getSmithingRecipes().get(recipe), newRecipe).open();
                    else if (CustomRecipeRegistry.getImmersiveRecipes().containsKey(recipe))
                        new ImmersiveRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(player), CustomRecipeRegistry.getImmersiveRecipes().get(recipe), newRecipe).open();
                    else Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_recipe_exists"));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getFailureMessage(String[] args) {
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("new")) return "&4/val recipes new <type> <new recipe name>";
            else if (args[1].equalsIgnoreCase("edit")) return "&4/val recipes edit <recipe name>";
            else if (args[1].equalsIgnoreCase("copy")) return "&4/val recipes copy <recipe name> <copied recipe name>";
        }
        return "&4/val recipes [new/edit/copy]";
    }

    @Override
    public String getDescription() {
        return TranslationManager.getTranslation("description_command_recipes");
    }

    @Override
    public String getCommand() {
        return "/val recipes [new/edit/copy]";
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[]{"valhalla.recipes"};
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("valhalla.recipes");
    }

    @Override
    public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
        if (args.length == 2) return Arrays.asList("new", "edit", "copy");
        if (args.length == 3){
            if (args[1].equalsIgnoreCase("new")){
                return Arrays.asList("immersive", "crafting_table", "brewing", "furnace", "blasting", "smoking", "campfire", "smithing", "cauldron");
            } else if (args[1].equalsIgnoreCase("edit") || args[1].equalsIgnoreCase("copy")){
                return new ArrayList<>(CustomRecipeRegistry.getAllRecipes());
            }
        }
        return Command.noSubcommandArgs();
    }
}
