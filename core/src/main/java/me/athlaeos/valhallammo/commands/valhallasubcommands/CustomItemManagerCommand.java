package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.CustomItemManagementMenu;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CustomItemManagerCommand implements Command {
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player p)) {
			Utils.sendMessage(sender, Utils.chat("&cOnly players can perform this command."));
			return true;
		}
		new CustomItemManagementMenu(PlayerMenuUtilManager.getPlayerMenuUtility(p)).open();
		return true;
	}

	@Override
	public String getFailureMessage(String[] args) {
		return "&c/valhalla items";
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[]{"valhalla.items"};
	}

	@Override
	public String getDescription() {
		return TranslationManager.getTranslation("description_command_items");
	}

	@Override
	public String getCommand() {
		return "/valhalla items";
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender instanceof Player && (sender.hasPermission("valhalla.items"));
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		return null;
	}
}
