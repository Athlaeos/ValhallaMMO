package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.GlobalEffect;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public class GlobalEffectCommand implements Command {
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length >= 3){
			String effect = args[2];
			if (args[1].equalsIgnoreCase("remove")){
				if (GlobalEffect.isActive(effect)){
					GlobalEffect.getActiveGlobalEffects().remove(effect);
					Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("status_command_global_buff_removed")));
				} else {
					Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_global_buff_expired")));
				}
				return true;
			} else if (args[1].equalsIgnoreCase("add")){
				if (args.length >= 5){
					long duration;
					double amplifier;
					GlobalEffect.EffectAdditionMode mode = GlobalEffect.EffectAdditionMode.OVERWRITE;
					try {
						duration = Long.parseLong(args[3].replace("w", "").replace("d", "").replace("h", "")
								.replace("m", "").replace("s", ""));
						amplifier = StringUtils.parseDouble(args[4]);
					} catch (IllegalArgumentException ignored){
						Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_number")));
						return true;
					}
					if (args[3].endsWith("w")){
						duration *= (7 * 24 * 60 * 60);
					} else if (args[3].endsWith("d")){
						duration *= (24 * 60 * 60);
					} else if (args[3].endsWith("h")){
						duration *= (60 * 60);
					} else if (args[3].endsWith("m")){
						duration *= (60);
					}
					duration *= 1000;

					String bossBar = null;
					BarColor color = null;
					BarStyle style = null;
					if (args.length >= 6){
						try {
							mode = GlobalEffect.EffectAdditionMode.valueOf(args[5]);
						} catch (IllegalArgumentException ignored){
							Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_option")));
							return true;
						}

						if (args.length >= 9){
							try {
								color = BarColor.valueOf(args[6]);
								style = BarStyle.valueOf(args[7]);
							} catch (IllegalArgumentException ignored){
								Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_option")));
								return true;
							}
							bossBar = String.join(" ", Arrays.copyOfRange(args, 8, args.length));
						}
					}
					GlobalEffect.addEffect(effect, duration, amplifier, mode, bossBar, color, style);
					double newAmplifier = GlobalEffect.getAmplifier(effect);
					double newDuration = GlobalEffect.getDuration(effect);
					String reply;
					if (GlobalEffect.getValidEffects().contains(effect)) {
						reply = TranslationManager.getTranslation("status_command_global_buff_applied");
					} else {
						reply = TranslationManager.getTranslation("status_command_global_buff_warning");
					}
					Utils.sendMessage(sender, Utils.chat(reply
							.replace("%duration_timestamp%", StringUtils.toTimeStamp((int) newDuration, 1000))
							.replace("%duration_timestamp2%", StringUtils.toTimeStamp2((int) newDuration, 1000))
							.replace("%duration_seconds%", "" + Math.round(newDuration))
							.replace("%duration_minutes%", String.format("%.1f", newDuration / 60D))
							.replace("%duration_hours%", String.format("%.1f", newDuration / 3600D))
							.replace("%duration_days%", String.format("%.1f", newDuration / 3600D))
							.replace("%amplifier%", String.format("%.2f", newAmplifier))
							.replace("%effect%", effect)));
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[]{"valhalla.globalbuffs"};
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission("valhalla.globalbuffs");
	}

	@Override
	public String getFailureMessage(String[] args) {
		return "&c/valhalla globalbuff remove/add [effect] [duration] [amplifier] <mode>";
	}

	@Override
	public String getDescription() {
		return TranslationManager.getTranslation("description_command_skills");
	}

	@Override
	public String getCommand() {
		return "/valhalla globalbuff remove/add [effect] [duration] [amplifier] <mode>";
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		if (args.length == 2){
			return Arrays.asList("add", "remove");
		} else if (args.length == 3){
			return new ArrayList<>(GlobalEffect.getValidEffects());
		} else if (args.length == 4) {
			List<String> validSuffixes = Arrays.asList("s", "m", "h", "d", "w");
			if (!(args[3].endsWith("w") || args[3].endsWith("d") || args[3].endsWith("h") || args[3].endsWith("m") || args[3].endsWith("s"))){
				return validSuffixes.stream().map(s -> args[3] + s).collect(Collectors.toList());
			}
			return Collections.singletonList("<duration>");
		} else if (args.length == 5) {
			return Collections.singletonList("<amplifier>");
		} else if (args.length == 6){
			return Arrays.stream(GlobalEffect.EffectAdditionMode.values()).map(GlobalEffect.EffectAdditionMode::toString).collect(Collectors.toList());
		} else if (args.length == 7){
			return Arrays.stream(BarColor.values()).map(Objects::toString).collect(Collectors.toList());
		} else if (args.length == 8){
			return Arrays.stream(BarStyle.values()).map(Objects::toString).collect(Collectors.toList());
		} else if (args.length == 9){
			return List.of("<bar_title>", "placeholders:", "%time1%", "%time2%");
		}
		return null;
	}
}
