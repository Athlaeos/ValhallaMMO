package me.athlaeos.valhallammo.commands;

import me.athlaeos.valhallammo.parties.PartyManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartySpyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("valhalla.manageparties") && !sender.hasPermission("valhalla.partyspy")){
            PartyManager.ErrorStatus.NO_PERMISSION.sendErrorMessage(sender);
            return true;
        }
        if (!(sender instanceof Player p)) return true;
        if (PartyManager.togglePartySpy(p)) Utils.sendMessage(sender, "status_command_party_spy_enabled");
        else Utils.sendMessage(sender, "status_command_party_spy_disabled");
        return true;
    }
}
