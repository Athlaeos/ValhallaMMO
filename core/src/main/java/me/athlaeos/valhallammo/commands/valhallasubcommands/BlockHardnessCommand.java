package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.BlockUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BlockHardnessCommand implements Command {
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length >= 2){
			Float hardness = null;
			if (args.length > 2) {
				hardness = Catch.catchOrElse(() -> StringUtils.parseFloat(args[2]), null);
				if (hardness == null){
					Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_number")));
					return true;
				} else hardness = Math.max(-1, hardness);
			}

			Material block = Catch.catchOrElse(() -> Material.valueOf(args[1]), null);
			if (block == null) {
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_option")));
				return true;
			}

			ConfigManager.getConfig("default_block_hardnesses.yml").set(args[1], hardness);
			ConfigManager.getConfig("default_block_hardnesses.yml").save();
			ConfigManager.getConfig("default_block_hardnesses.yml").reload();
			BlockUtils.setDefaultHardness(block, hardness);

			if (hardness == null) Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_hardness_removed").replace("%block%", args[1]));
			else Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_hardness_set").replace("%block%", args[1]).replace("%hardness%", args[2]));
			return true;
		} else return false;
	}

	@Override
	public String getFailureMessage(String[] args) {
		return "&4/valhalla hardness [block] <hardness>";
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[]{"valhalla.hardness"};
	}

	@Override
	public String getDescription() {
		return TranslationManager.getTranslation("description_command_hardness");
	}

	@Override
	public String getCommand() {
		return "/valhalla hardness [block] <hardness>";
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission("valhalla.hardness");
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		if (args.length == 2) return Arrays.stream(Material.values()).filter(Material::isBlock).map(Material::toString).collect(Collectors.toList());
		return null;
	}
}
