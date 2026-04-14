package me.athlaeos.valhallammo.commands;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.SkillTreeMenu;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.skills.skills.implementations.PowerSkill;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SkillsCommand implements TabExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command cmd, @NotNull String name, String[] args) {
		if (!(sender instanceof Player p)) {
			Utils.sendMessage(sender, Utils.chat("&cOnly players can perform this command."));
			return true;
		}
		if (!sender.hasPermission("valhalla.skills") && !sender.hasPermission("valhalla.skills.other")) {
			Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_no_permission")));
			return true;
		}
		if (args.length > 0){
			Player target = p;
			Skill targetSkill;
			if (args.length > 1) {
				target = ValhallaMMO.getInstance().getServer().getPlayer(args[0]);
				if (target == null) {
					Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_player_offline"));
					return true;
				}
				if (!target.equals(p) && !sender.hasPermission("valhalla.skills.other")) {
					Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_no_permission")));
					return true;
				}
				targetSkill = SkillRegistry.getSkill(args[1].toUpperCase(java.util.Locale.US));
				if (targetSkill == null) {
					Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_invalid_skill"));
					return true;
				}
			} else {
				targetSkill = SkillRegistry.getSkill(args[0].toUpperCase(java.util.Locale.US));
				if (targetSkill == null) {
					targetSkill = SkillRegistry.getSkill(PowerSkill.class);
					target = ValhallaMMO.getInstance().getServer().getPlayer(args[0]);
					if (target == null) {
						Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_player_offline"));
						return true;
					}
					if (!target.equals(p) && !sender.hasPermission("valhalla.skills.other")) {
						Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_no_permission")));
						return true;
					}
				}
			}
			new SkillTreeMenu(PlayerMenuUtilManager.getPlayerMenuUtility(p), target, targetSkill).open();
		} else {
			new SkillTreeMenu(PlayerMenuUtilManager.getPlayerMenuUtility(p)).open();
		}
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NonNull CommandSender commandSender, @NonNull Command command, @NonNull String s, @NonNull String[] args) {
		if (args.length == 1){
			List<String> suggestions = new ArrayList<>(List.of("<player_or_skill>"));
			suggestions.addAll(SkillRegistry.getAllSkills().values().stream().map(Skill::getType).map(String::toLowerCase).toList());
			return suggestions;
		}
		if (args.length == 2) return SkillRegistry.getAllSkills().values().stream().map(Skill::getType).map(String::toLowerCase).collect(Collectors.toList());
		return null;
	}
}
