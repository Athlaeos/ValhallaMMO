package me.athlaeos.valhallammo.commands;

import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.LeaderboardManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardCommand implements TabExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String name, String[] args) {
		if (!sender.hasPermission("valhalla.top")) {
			Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_no_permission")));
			return true;
		}
		if (args.length == 0) return false;
		int page = 1;
		if (args.length > 1){
			try {
				page = Math.max(1, Integer.parseInt(args[1]));
			} catch (IllegalArgumentException ignored){
				Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_invalid_number"));
				return true;
			}
		}
		LeaderboardManager.Leaderboard leaderboard = LeaderboardManager.getLeaderboards().get(args[0]);
		if (leaderboard == null){
			Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_invalid_leaderboard"));
			return true;
		}
		LeaderboardManager.sendLeaderboard(sender, args[0], page);
		return true;
	}

	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
		if (strings.length == 1) return new ArrayList<>(LeaderboardManager.getLeaderboards().keySet());
		if (strings.length == 2) return List.of("1", "2", "3", "...");
		return null;
	}
}
