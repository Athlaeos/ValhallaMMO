package me.athlaeos.valhallammo.commands;

import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.parties.PartyManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartyChatCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            Utils.sendMessage(sender, "&cOnly players may execute this command");
            return true;
        }
        PartyManager.ErrorStatus openStatus = PartyManager.togglePartyChat(p);
        if (openStatus != null){
            openStatus.sendErrorMessage(p);
            return true;
        }
        if (PartyManager.getPartyChatPlayers().contains(p.getUniqueId())) Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_chat_enabled"));
        else Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_chat_disabled"));
        return true;
    }
}
