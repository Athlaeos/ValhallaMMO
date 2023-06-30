package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileManager;
import me.athlaeos.valhallammo.playerstats.profiles.ResetType;
import me.athlaeos.valhallammo.progression.skills.Skill;
import me.athlaeos.valhallammo.progression.skills.SkillRegistry;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ResetProfilesCommand implements Command {

	private Long timeConsoleAttemptedReset = 0L;

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player target;
		boolean resetSingleSkill = false;
		if (args.length >= 2) resetSingleSkill = args[1].equalsIgnoreCase("skill");
		if (args.length >= 3){
			if (!sender.hasPermission("valhalla.reset.other")) {
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_no_permission")));
				return true;
			}
			target = ValhallaMMO.getInstance().getServer().getPlayer(resetSingleSkill ? args[3] : args[2]);
		} else if (sender instanceof Player){
			target = (Player) sender;
		} else {
			Utils.sendMessage(sender, Utils.chat("&cOnly players can do this"));
			return true;
		}

		if (args.length < 2) return false;
		boolean overwriteConfirmation = args.length > 3 && args[3].equalsIgnoreCase("confirm");

		try {
			ResetType type = resetSingleSkill ? null : ResetType.valueOf(args[1].toUpperCase());
			if (sender.hasPermission("valhalla.reset") ||
					sender.hasPermission("valhalla.reset.other") ||
					sender.hasPermission("valhalla.reset." + (type == null ? "skill" : type.toString().toLowerCase()))) {
				if (target == null){
					Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_player_required")));
					return true;
				}

				if (!overwriteConfirmation && type != null) {
					if (sender instanceof Player && type.shouldAskForConfirmation() && Timer.isCooldownPassed(((Player) sender).getUniqueId(), "reset_command_attempt")){
						Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("warning_profile_reset")));
						Timer.setCooldown(((Player) sender).getUniqueId(), 10000, "reset_command_attempt");
						return true;
					} else if (type.shouldAskForConfirmation() && timeConsoleAttemptedReset <= System.currentTimeMillis()){
						Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("warning_profile_reset")));
						timeConsoleAttemptedReset = System.currentTimeMillis() + 10000;
						return true;
					}
				}

				if (type == null) {
					Skill skillToReset = SkillRegistry.getSkill(args[2]);
					if (skillToReset == null) {
						Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_invalid_skill"));
						return true;
					}
					ProfileManager.reset(target, skillToReset.getClass());
					Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_hard_reset_success"));
				} else {
					ProfileManager.reset(target, type);
					Utils.sendMessage(sender, type.shouldAskForConfirmation() ? TranslationManager.getTranslation("status_command_hard_reset_success") : TranslationManager.getTranslation("status_command_soft_reset_success"));
				}
			} else {
				Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_no_permission"));
			}
		} catch (IllegalArgumentException ignored) {
			Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_invalid_option"));
		}

		return true;
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[]{"valhalla.reset.other", "valhalla.reset"};
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission("valhalla.reset") ||
				sender.hasPermission("valhalla.reset.other") ||
				Arrays.stream(ResetType.values()).anyMatch(t -> sender.hasPermission("valhalla.reset." + t.toString().toLowerCase()));
	}

	@Override
	public String getFailureMessage(String[] args) {
		return "&c/valhalla reset [type] <player> <confirm>";
	}

	@Override
	public String getDescription() {
		return TranslationManager.getTranslation("description_command_reset");
	}

	@Override
	public String getCommand() {
		return "&c/valhalla reset [type] <player|skill> <confirm|player>";
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		if (args.length == 2) {
			List<String> values = new ArrayList<>(Arrays.stream(ResetType.values()).map(ResetType::toString).toList());
			values.add("skill");
			return values.stream().map(String::toLowerCase).collect(Collectors.toList());
		}
		if (args.length == 3) {
			if (args[1].equalsIgnoreCase("skill"))
				return SkillRegistry.getAllSkills().values().stream().map(Skill::getType).map(String::toLowerCase).collect(Collectors.toList());
			return null;
		}
		if (args.length == 4) {
			return args[1].equalsIgnoreCase("skill") ? null : List.of("confirm");
		}
		return Command.noSubcommandArgs();
	}
}
