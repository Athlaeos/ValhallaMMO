package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class EXPCommand implements Command {
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Collection<Player> targets = new HashSet<>();
		if (args.length < 4){
			if (sender instanceof Player){
				targets.add((Player) sender);
			} else {
				Utils.sendMessage(sender, Utils.chat("&cOnly players can perform this command for themselves."));
				return true;
			}
		}
		if (args.length >= 3){
			if (args.length >= 4){
				targets.addAll(Utils.selectPlayers(sender, args[3]));
			}
			double amount;

			try {
				amount = StringUtils.parseDouble(args[2]);
			} catch (IllegalArgumentException ignored){
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_number")));
				return true;
			}

			if (targets.isEmpty()){
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_player_offline")));
				return true;
			}

			Skill skill = SkillRegistry.getSkill(args[1].toUpperCase());
			if (skill == null) {
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_skill")));
				return true;
			}
			for (Player target : targets){
				skill.addEXP(target, amount, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.COMMAND);
				Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_exp_success")
						.replace("%player%", target.getName())
						.replace("%amount%", String.format("%.2f", amount))
						.replace("%skill%", skill.getDisplayName()));
			}
			return true;
		}
		return false;
	}

	@Override
	public String getFailureMessage(String[] args) {
		return "&4/valhalla exp [skilltype] [amount] <player>";
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[]{"valhalla.exp"};
	}

	@Override
	public String getDescription() {
		return TranslationManager.getTranslation("description_command_exp");
	}

	@Override
	public String getCommand() {
		return "/valhalla exp [skilltype] [amount] <player>";
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission("valhalla.exp");
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		if (args.length == 2){
			return SkillRegistry.getAllSkills().values().stream().map(Skill::getType).map(String::toLowerCase).collect(Collectors.toList());
		}
		return null;
	}
}
