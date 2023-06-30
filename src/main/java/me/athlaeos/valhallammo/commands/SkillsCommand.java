package me.athlaeos.valhallammo.commands;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.SkillTreeMenu;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SkillsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.@NotNull Command cmd, @NotNull String name, String[] args) {
		if (!(sender instanceof Player p)) {
			Utils.sendMessage(sender, Utils.chat("&cOnly players can perform this command."));
			return true;
		}
		if (!sender.hasPermission("valhalla.skills") && sender.hasPermission("valhalla.skills.other")) {
			Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_no_permission")));
			return true;
		}
		if (args.length > 1){
			Player target = ValhallaMMO.getInstance().getServer().getPlayer(args[0]);
			if (target == null) {
				Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_player_offline"));
				return true;
			}
			if (!target.equals(p) && !sender.hasPermission("valhalla.skills.other")) {
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_no_permission")));
				return true;
			}
			new SkillTreeMenu(PlayerMenuUtilManager.getPlayerMenuUtility(p), target).open();
		} else {
			new SkillTreeMenu(PlayerMenuUtilManager.getPlayerMenuUtility(p)).open();
		}
		return true;
	}
}
