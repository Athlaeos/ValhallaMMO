package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.trading.menu.CustomTradeManagementMenu;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ManageMerchantsCommand implements Command {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player player){
            new CustomTradeManagementMenu(PlayerMenuUtilManager.getPlayerMenuUtility(player)).open();
        } else Utils.sendMessage(sender, "&cOnly players may manage custom merchants");
        return true;
    }

    @Override
    public String getFailureMessage(String[] args) {
        return "&4/val merchants";
    }

    @Override
    public String getDescription() {
        return TranslationManager.getTranslation("description_command_merchants");
    }

    @Override
    public String getCommand() {
        return "/val merchants";
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[]{"valhalla.merchants"};
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("valhalla.merchants");
    }

    @Override
    public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
        return Command.noSubcommandArgs();
    }
}
