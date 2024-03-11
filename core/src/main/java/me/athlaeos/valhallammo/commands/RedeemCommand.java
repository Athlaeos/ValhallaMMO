package me.athlaeos.valhallammo.commands;

import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.skills.skills.implementations.PowerSkill;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class RedeemCommand implements TabExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command cmd, @NotNull String name, String[] args) {
		if (!(sender instanceof Player p)) {
			Utils.sendMessage(sender, Utils.chat("&cOnly players can perform this command."));
			return true;
		}
		if (!sender.hasPermission("valhalla.redeemlevels") && !sender.hasPermission("valhalla.redeemexp")) {
			Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_no_permission")));
			return true;
		}
		if (args.length > 2){
			Skill skill = SkillRegistry.getSkill(args[1].toUpperCase());
			if (skill == null || skill instanceof PowerSkill) {
				Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_invalid_skill"));
				return true;
			}
			boolean levels = args[0].equalsIgnoreCase("levels");
			boolean exp = args[0].equalsIgnoreCase("exp");
			if (!levels && !exp) return false;
			PowerProfile profile = ProfileRegistry.getPersistentProfile(p, PowerProfile.class);

			try {
				if (levels) {
					int amountToRedeem = Math.max(0, Integer.parseInt(args[2]));
					if (profile.getRedeemableLevelTokens() < amountToRedeem){
						Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_insufficient_level_tokens"));
						return true;
					}
					if (amountToRedeem == 0) return true;
					skill.addLevels(p, amountToRedeem, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.REDEEM);
					profile.setRedeemableLevelTokens(profile.getRedeemableLevelTokens() - amountToRedeem);
					Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_levels_redeemed"));
				} else {
					double amountToRedeem = Math.max(0, StringUtils.parseDouble(args[2]));
					if (profile.getRedeemableExperiencePoints() < amountToRedeem){
						Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_insufficient_exp_tokens"));
						return true;
					}
					if (amountToRedeem == 0) return true;
					skill.addEXP(p, amountToRedeem, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.REDEEM);
					profile.setRedeemableExperiencePoints(profile.getRedeemableExperiencePoints() - amountToRedeem);
					Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_exp_redeemed"));
				}
				ProfileRegistry.setPersistentProfile(p, profile, PowerProfile.class);
				return true;
			} catch (NumberFormatException ignored){
				Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_invalid_number"));
				return true;
			}
		}
		return false;
	}

	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
		if (strings.length == 1) return List.of("levels", "exp");
		if (strings.length == 2) return
				SkillRegistry.getAllSkills().values().stream()
						.filter(skill -> !(skill instanceof PowerSkill))
						.map(skill -> skill.getType().toLowerCase())
						.collect(Collectors.toList());
		if (strings.length == 3) return List.of("<amount>", "1", "2", "3", "...");
		return null;
	}
}
