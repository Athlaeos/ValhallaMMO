package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.content.ContentPackageManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExportCommand implements Command {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_import_no_path_given"));
            return true;
        }
        String file = args[1];

        List<ContentPackageManager.ExportMode> exportModes = new ArrayList<>();
        if (args.length > 2){
            String[] modes = Arrays.copyOfRange(args, 2, args.length);
            for (String mode : modes){
                ContentPackageManager.ExportMode m = Catch.catchOrElse(() -> ContentPackageManager.ExportMode.valueOf(mode.toUpperCase(java.util.Locale.US)), null);
                if (m == null) Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_import_invalid_mode").replace("%mode%", mode));
                else exportModes.add(m);
            }
        } else exportModes.addAll(List.of(ContentPackageManager.ExportMode.values()));

        if (ContentPackageManager.exportContent(file, exportModes.toArray(new ContentPackageManager.ExportMode[0])))
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
        if (args.length >= 3) return Arrays.stream(ContentPackageManager.ExportMode.values()).map(ContentPackageManager.ExportMode::toString).map(String::toLowerCase).toList();
        return null;
    }
}
