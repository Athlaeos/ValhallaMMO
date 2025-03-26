package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.content.ContentPackageManager;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.ValhallaRecipe;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.item.CustomItem;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;

import java.util.*;

public class ExportCommand implements Command {
    private static final Collection<String> preparedItems = new HashSet<>();

    public static boolean prepareOrUnprepareItem(String item){
        if (preparedItems.contains(item)) {
            preparedItems.remove(item);
            return false;
        } else {
            preparedItems.add(item);
            return true;
        }
    }

    public static boolean isPrepared(String item){
        return preparedItems.contains(item);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_import_no_path_given"));
            return true;
        }
        String file = args[1];

        boolean addToExisting = false;
        List<ContentPackageManager.ExportMode> exportModes = new ArrayList<>();
        List<ValhallaRecipe> exportRecipes = new ArrayList<>();
        List<CustomItem> exportItems = new ArrayList<>();
        if (args.length > 2){
            List<String> invalidModes = new ArrayList<>();
            List<String> modes = new ArrayList<>(List.of(Arrays.copyOfRange(args, 2, args.length)));
            for (String mode : modes){
                if (mode.equalsIgnoreCase("add")) {
                    addToExisting = true;
                    continue;
                } else if (mode.equalsIgnoreCase("prepared")) {
                    invalidModes.addAll(preparedItems);
                    preparedItems.clear();
                    continue;
                }
                ContentPackageManager.ExportMode m = Catch.catchOrElse(() -> ContentPackageManager.ExportMode.valueOf(mode.toUpperCase(java.util.Locale.US)), null);
                if (m == null) invalidModes.add(mode);
                else exportModes.add(m);
            }
            for (String r : invalidModes){
                // Invalid modes are attempted to be parsed as recipes instead
                ValhallaRecipe recipe = null;
                if (CustomRecipeRegistry.getCauldronRecipes().containsKey(r))
                    recipe = CustomRecipeRegistry.getCauldronRecipes().get(r);
                else if (CustomRecipeRegistry.getCookingRecipes().containsKey(r))
                    recipe = CustomRecipeRegistry.getCookingRecipes().get(r);
                else if (CustomRecipeRegistry.getGridRecipes().containsKey(r))
                    recipe = CustomRecipeRegistry.getGridRecipes().get(r);
                else if (CustomRecipeRegistry.getBrewingRecipes().containsKey(r))
                    recipe = CustomRecipeRegistry.getBrewingRecipes().get(r);
                else if (CustomRecipeRegistry.getSmithingRecipes().containsKey(r))
                    recipe = CustomRecipeRegistry.getSmithingRecipes().get(r);
                else if (CustomRecipeRegistry.getImmersiveRecipes().containsKey(r))
                    recipe = CustomRecipeRegistry.getImmersiveRecipes().get(r);
                if (recipe != null) exportRecipes.add(recipe);
                else {
                    CustomItem item = CustomItemRegistry.getItem(r);
                    if (item != null) exportItems.add(item);
                }
            }
            if (exportRecipes.isEmpty() && !invalidModes.isEmpty()){
                Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_import_invalid_mode").replace("%mode%", String.join(", ", invalidModes)));
            }
        } else exportModes.addAll(List.of(ContentPackageManager.ExportMode.values()));

        if (ContentPackageManager.exportContent(file, exportRecipes, exportItems, addToExisting, exportModes.toArray(new ContentPackageManager.ExportMode[0])))
            Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_export_success").replace("%path%", "/export/" + file + " .json"));
        else Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_export"));
        return true;
    }

    @Override
    public String getFailureMessage(String[] args) {
        return "/val export";
    }

    @Override
    public String getDescription() {
        return TranslationManager.getTranslation("description_command_export");
    }

    @Override
    public String getCommand() {
        return "/val export";
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[]{"valhalla.export"};
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("valhalla.export");
    }

    @Override
    public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
        if (args.length == 2) return List.of("name");
        if (args.length >= 3) {
            List<String> returns = new ArrayList<>(Arrays.stream(ContentPackageManager.ExportMode.values()).map(ContentPackageManager.ExportMode::toString).map(String::toLowerCase).toList());
            returns.add("<item_or_recipe>");
            returns.add("prepared");
            returns.add("add");
            return returns;
        }
        return null;
    }
}
