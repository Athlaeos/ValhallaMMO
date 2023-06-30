package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.SkillTreeMenu;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ViewSkillTreeCommand implements Command {
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player p)) {
			Utils.sendMessage(sender, Utils.chat("&cOnly players can perform this command."));
			return true;
		}
		if (args.length > 1){
			Player target = ValhallaMMO.getInstance().getServer().getPlayer(args[1]);
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

	@Override
	public String getFailureMessage(String[] args) {
		return "&c/valhalla skills";
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[]{"valhalla.skills", "valhalla.skills.other"};
	}

	@Override
	public String getDescription() {
		return TranslationManager.getTranslation("description_command_skills");
	}

	@Override
	public String getCommand() {
		return "/valhalla skills";
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender instanceof Player && (sender.hasPermission("valhalla.skills") || sender.hasPermission("valhalla.skills.other"));
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		return null;
	}
}
