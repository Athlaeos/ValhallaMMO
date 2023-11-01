package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.tools.SpecializedToolRegistry;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SpecializedToolCommand implements Command {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)){
            sender.sendMessage(Utils.chat("&cOnly players can do this"));
            return true;
        }
        if (args.length < 2) return false;
        ItemStack item = SpecializedToolRegistry.getTools().get(args[1]);
        if (ItemUtils.isEmpty(item)){
            sender.sendMessage(Utils.chat(TranslationManager.getTranslation("error_command_invalid_option")));
            return true;
        }
        p.getInventory().addItem(item.clone());
        return true;
    }

    @Override
    public String getFailureMessage(String[] args) {
        return "/val tool <id>";
    }

    @Override
    public String getDescription() {
        return TranslationManager.getTranslation("description_command_tool");
    }

    @Override
    public String getCommand() {
        return "/val tool <id>";
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[]{"valhalla.tool"};
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("valhalla.tool");
    }

    @Override
    public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
        if (args.length == 2) return new ArrayList<>(SpecializedToolRegistry.getTools().keySet());
        return null;
    }
}
