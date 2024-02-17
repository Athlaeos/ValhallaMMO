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

public class ManageLootTablesCommand implements Command {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player player){
            new LootTableOverviewMenu(PlayerMenuUtilManager.getPlayerMenuUtility(player)).open();
        } else Utils.sendMessage(sender, "&cOnly players may manage loot tables");
        return true;
    }

    @Override
    public String getFailureMessage(String[] args) {
        return "&4/val loot";
    }

    @Override
    public String getDescription() {
        return TranslationManager.getTranslation("description_command_loottables");
    }

    @Override
    public String getCommand() {
        return "/val loot";
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[]{"valhalla.loottables"};
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("valhalla.loottables");
    }

    @Override
    public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
        return Command.noSubcommandArgs();
    }
}
