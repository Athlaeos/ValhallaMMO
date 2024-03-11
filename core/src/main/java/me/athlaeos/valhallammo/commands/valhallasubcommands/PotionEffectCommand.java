package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPotionEffectEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class PotionEffectCommand implements Command {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Collection<Player> targets = new HashSet<>();
		if (args.length <= 4){
			if (sender instanceof Player){
				targets.add((Player) sender);
			} else {
				sender.sendMessage(Utils.chat("&cOnly players may perform this command for themselves."));
				return true;
			}
		}

		if (args.length >= 4){
			String effect = args[1].toUpperCase();
			double amplifier;
			int duration;
			try {
				amplifier = StringUtils.parseDouble(args[2]);
				duration = Integer.parseInt(args[3]);
			} catch (NumberFormatException ignored){
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_number")));
				return true;
			}
			if (args.length > 4){
				targets = Utils.selectPlayers(sender, args[4]);
			}

			if (!PotionEffectRegistry.getRegisteredEffects().containsKey(effect)){
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_effect")));
				return true;
			}
			PotionEffectWrapper wrapper = PotionEffectRegistry.getEffect(effect);
			if (wrapper.isVanilla()) {
				Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_vanilla_effects_illegal"));
				return true;
			}
			wrapper.setDuration(duration);
			wrapper.setAmplifier(amplifier);
			if (duration == -1)
				targets.forEach(p -> PotionEffectRegistry.addEffect(p, null, new CustomPotionEffect(wrapper, -1L, amplifier), true, 1, EntityPotionEffectEvent.Cause.COMMAND));
			else
				targets.forEach(p -> PotionEffectRegistry.addEffect(p, null, new CustomPotionEffect(wrapper, duration, amplifier), true, 1, EntityPotionEffectEvent.Cause.COMMAND));

			Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_durable_effect_applied").replace("%effect%", wrapper.getEffectName()
					.replace("%icon%", wrapper.getEffectIcon())
					.replace("%value%", wrapper.getFormat().format(wrapper.getAmplifier()))
					.replace("%duration%", String.format("(%s)", StringUtils.toTimeStamp(wrapper.getDuration(), 20)))
					.trim()));

			return true;
		}
		return false;
	}

	@Override
	public String getFailureMessage(String[] args) {
		return "&4/valhalla effect [effect] [amplifier] [duration] <player>";
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[]{"valhalla.effect"};
	}

	@Override
	public String getDescription() {
		return TranslationManager.getTranslation("description_command_effect");
	}

	@Override
	public String getCommand() {
		return "/valhalla effect [effect] [amplifier] [duration] <player>";
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission("valhalla.effect");
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		if (args.length == 2) return new ArrayList<>(PotionEffectRegistry.getRegisteredEffects().values().stream().filter(e -> !e.isVanilla()).map(e -> e.getEffect().toLowerCase()).sorted().toList());
		if (args.length == 3) return List.of("<amplifier>");
		if (args.length == 4) return List.of("<duration>");
		return Command.noSubcommandArgs();
	}
}
