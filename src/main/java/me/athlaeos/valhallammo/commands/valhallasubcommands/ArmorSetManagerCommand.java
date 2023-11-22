package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.ArmorSetEditor;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ArmorSetManagerCommand implements Command {
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player p)) {
			Utils.sendMessage(sender, Utils.chat("&cOnly players can perform this command."));
			return true;
		}
		new ArmorSetEditor(PlayerMenuUtilManager.getPlayerMenuUtility(p)).open();
		return true;
	}

	@Override
	public String getFailureMessage(String[] args) {
		return "&c/valhalla armorsets";
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[]{"valhalla.armorsets"};
	}

	@Override
	public String getDescription() {
		return TranslationManager.getTranslation("description_command_armorsets");
	}

	@Override
	public String getCommand() {
		return "/valhalla armorsets";
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender instanceof Player && (sender.hasPermission("valhalla.armorsets"));
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		return null;
	}
}
