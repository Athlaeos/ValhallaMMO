package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardRegistry;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class PerkRewardCommand implements Command {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Collection<Player> targets = new HashSet<>();
		if (args.length < 4){
			if (sender instanceof Player){
				targets.add((Player) sender);
			} else {
				sender.sendMessage(Utils.chat("&cOnly players may perform this command for themselves."));
				return true;
			}
		}

		if (args.length >= 2){
			PerkReward baseReward = PerkRewardRegistry.getRegisteredRewards().get(args[1]);
			if (baseReward == null){
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_reward")));
				return true;
			}
			PerkRewardArgumentType expectedType = baseReward.getRequiredType();
			if (args.length >= 3 || expectedType == PerkRewardArgumentType.NONE){
				if (args.length >= 4 || (expectedType == PerkRewardArgumentType.NONE && args.length > 2)){
					targets.clear();
					targets.addAll(Utils.selectPlayers(sender, expectedType == PerkRewardArgumentType.NONE ? args[2] : args[3]));
				}
				Object arg;
				try {
					arg = switch (expectedType){
						case NONE -> null;
						case DOUBLE -> StringUtils.parseDouble(args[2]);
						case FLOAT -> StringUtils.parseDouble(args[2]);
						case INTEGER -> Integer.parseInt(args[2]);
						case STRING -> args[2];
						case STRING_LIST -> List.of(args[2].split(";"));
						case BOOLEAN -> Boolean.parseBoolean(args[2]);
					};
				} catch (IllegalArgumentException ignored){
					sender.sendMessage(Utils.chat(TranslationManager.getTranslation("error_command_invalid_argument_type")
							.replace("%type%", expectedType.toString())
							.replace("%arg%", args[2])));
					return true;
				}
				PerkReward createdReward = PerkRewardRegistry.createReward(args[1], arg);

				if (createdReward == null){
					Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_reward")));
				} else if (targets.isEmpty()){
					Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_player_offline")));
				} else {
					createdReward.setPersistent(true);
					for (Player target : targets){
						createdReward.apply(target);
					}
					sender.sendMessage(Utils.chat(TranslationManager.getTranslation("status_command_reward_executed")));
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public String getFailureMessage(String[] args) {
		return "&4/valhalla reward [reward] [argument] <player>";
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[]{"valhalla.reward"};
	}

	@Override
	public String getDescription() {
		return TranslationManager.getTranslation("description_command_reward");
	}

	@Override
	public String getCommand() {
		return "/valhalla reward [reward] [argument] <player>";
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission("valhalla.reward");
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		if (args.length == 2) return new ArrayList<>(PerkRewardRegistry.getRegisteredRewards().keySet());

		if (args.length >= 3){
			PerkReward reward = PerkRewardRegistry.getRegisteredRewards().get(args[1]);
			if (reward != null){
				if (args.length == 3) {
					if (!reward.getTabAutoComplete(args[2]).isEmpty()) return reward.getTabAutoComplete(args[2]);

					return switch (reward.getRequiredType()) {
						case BOOLEAN -> Arrays.asList("true", "false");
						case STRING -> Collections.singletonList("string_arg");
						case STRING_LIST -> args[2].equalsIgnoreCase("") || args[2].equalsIgnoreCase(" ") ?
								Collections.singletonList("string_arg;") :
								Collections.singletonList(args[2] + ";");
						case INTEGER -> Arrays.asList("1", "2", "3", "int_arg");
						case DOUBLE, FLOAT -> Arrays.asList("1.0", "2.0", "3.0", "double_arg");
						default -> Command.noSubcommandArgs();
					};
				} else if (args.length == 4) return null;
			}
		}
		return Command.noSubcommandArgs();
	}
}
