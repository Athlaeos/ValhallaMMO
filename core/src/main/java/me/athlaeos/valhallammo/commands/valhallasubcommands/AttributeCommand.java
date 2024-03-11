package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class AttributeCommand implements Command {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player target)){
			sender.sendMessage(Utils.chat("&cOnly players may perform this command for themselves."));
			return true;
		}

		if (args.length >= 3){
			String attribute = args[1];
			Double defaultValue = null;
			if (!args[2].equalsIgnoreCase("same")){
				try {
					defaultValue = StringUtils.parseDouble(args[2]);
				} catch (NumberFormatException ignored){
					Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_number")));
					return true;
				}
			}
			Double actualValue = null;
			if (args.length >= 4){
				if (!args[3].equalsIgnoreCase("same")){
					try {
						actualValue = Double.valueOf(args[3]);
					} catch(NumberFormatException ignored) {
						Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_number")));
						return true;
					}
				}
			}
			boolean hidden = false;
			if (args.length >= 5){
				hidden = args[4].equalsIgnoreCase("true");
			}
			if (!ItemAttributesRegistry.getRegisteredAttributes().containsKey(attribute.toUpperCase())){
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_attribute")));
				return true;
			}
			if (ItemUtils.isEmpty(target.getInventory().getItemInMainHand())){
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_item_required")));
				return true;
			}

			ItemBuilder result = new ItemBuilder(target.getInventory().getItemInMainHand());
			if (defaultValue != null){
				AttributeWrapper defaultWrapper = ItemAttributesRegistry.getCopy(attribute.toUpperCase()).setValue(defaultValue);
				ItemAttributesRegistry.addDefaultStat(result.getMeta(), defaultWrapper);
			}
			if (actualValue != null){
				ItemAttributesRegistry.setStat(result.getMeta(), attribute.toUpperCase(), actualValue, hidden, false);
			}
			target.getInventory().setItemInMainHand(result.get());
			return true;
		}
		return false;
	}

	@Override
	public String getFailureMessage(String[] args) {
		return "&4/valhalla attribute [attribute] [default] <actual>";
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[]{"valhalla.attribute"};
	}

	@Override
	public String getDescription() {
		return TranslationManager.getTranslation("description_command_attribute");
	}

	@Override
	public String getCommand() {
		return "/valhalla attribute [attribute] [default] <actual>";
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission("valhalla.attribute");
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		if (args.length == 2) return new ArrayList<>(ItemAttributesRegistry.getRegisteredAttributes().values().stream().map(a -> a.getAttribute().toLowerCase()).sorted().toList());
		if (args.length == 3) return List.of("<default_value>", "same");
		if (args.length == 4) return List.of("<actual_value>", "same");
		if (args.length == 5) return List.of("true", "false");
		return Command.noSubcommandArgs();
	}
}
