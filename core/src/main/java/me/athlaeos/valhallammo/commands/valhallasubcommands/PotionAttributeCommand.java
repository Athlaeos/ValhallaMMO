package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PotionAttributeCommand implements Command {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player target)){
			sender.sendMessage(Utils.chat("&cOnly players may perform this command for themselves."));
			return true;
		}

		if (args.length >= 4){
			String effect = args[1];
			double defaultValue;
			int defaultDuration;
			int charges = -1;
			try {
				defaultValue = StringUtils.parseDouble(args[2]);
				defaultDuration = Integer.parseInt(args[3]);
				if (args.length == 5) charges = Integer.parseInt(args[4]);
			} catch (NumberFormatException ignored){
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_number")));
				return true;
			}
			Double actualValue = null;
			Integer actualDuration = null;
			if (args.length >= 6){
				try {
					actualValue = Double.valueOf(args[4]);
					actualDuration = Integer.valueOf(args[5]);
					if (args.length >= 7) charges = Integer.parseInt(args[6]);
				} catch(NumberFormatException ignored) {
					Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_number")));
					return true;
				}
			}
			if (!PotionEffectRegistry.getRegisteredEffects().containsKey(effect.toUpperCase())){
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_effect")));
				return true;
			}
			if (ItemUtils.isEmpty(target.getInventory().getItemInMainHand())){
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_item_required")));
				return true;
			}

			ItemBuilder result = new ItemBuilder(target.getInventory().getItemInMainHand());
			if (result.getItem().getType().isEdible()) charges = -1; // consumable items can't really have charges, so they're always "infinite"

			PotionEffectWrapper defaultWrapper = PotionEffectRegistry.getEffect(effect.toUpperCase()).setAmplifier(defaultValue).setDuration(defaultDuration).setCharges(charges);
			PotionEffectRegistry.addDefaultEffect(result.getMeta(), defaultWrapper);
			if (actualValue != null){
				PotionEffectRegistry.setStoredEffect(result.getMeta(), effect.toUpperCase(), actualValue, actualDuration, charges, false);
			}
			PotionEffectRegistry.updateItemName(result.getMeta(), false, false);
			target.getInventory().setItemInMainHand(result.get());
			return true;
		}
		return false;
	}

	@Override
	public String getFailureMessage(String[] args) {
		return "&4/valhalla potionattribute [effect] [default_amplifier] [default_duration] <actual_amplifier> <actual_duration> <charges>";
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[]{"valhalla.potionattribute"};
	}

	@Override
	public String getDescription() {
		return TranslationManager.getTranslation("description_command_potionattribute");
	}

	@Override
	public String getCommand() {
		return "/valhalla potionattribute [effect] [default_amplifier] [default_duration] <actual_amplifier> <actual_duration> <charges>";
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission("valhalla.potionattribute");
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		if (args.length == 2) return new ArrayList<>(PotionEffectRegistry.getRegisteredEffects().values().stream().map(e -> e.getEffect().toLowerCase()).sorted().toList());
		if (args.length == 3) return List.of("<default_value>");
		if (args.length == 4) return List.of("<default_duration>");
		if (args.length == 5) return List.of("<actual_value_or_charges>");
		if (args.length == 6) return List.of("<actual_duration>");
		if (args.length == 7) return List.of("<charges>");
		return Command.noSubcommandArgs();
	}
}
