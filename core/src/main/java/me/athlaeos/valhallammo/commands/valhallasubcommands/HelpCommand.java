package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.commands.CommandManager;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HelpCommand implements Command {
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Map<Integer, List<String>> pages;
		List<String> helpLines = new ArrayList<>();
		
		for (Command c : CommandManager.getCommands().values()) {
			if (c.hasPermission(sender)) {
				for (String line : TranslationManager.getListTranslation("command_help_format")){
					helpLines.add(line
							.replace("%description%", c.getDescription())
							.replace("%permissions%", String.join("|", c.getRequiredPermissions()))
							.replace("%command%", c.getCommand()));
				}
			}
		}

		pages = Utils.paginate(TranslationManager.getListTranslation("command_help_format").size() * 3, helpLines);
		
		if (pages.isEmpty()) return true;

		int page = 1;
		if (args.length >= 2){
			try {
				page = Integer.parseInt(args[1]);
			} catch (NumberFormatException nfe) {
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_number")));
				return true;
			}
		}

		page = Math.max(1, Math.min(pages.size(), page));
		for (String line : pages.get(page - 1)) {
			sender.sendMessage(Utils.chat(line));
		}
		Utils.chat("&8&m                                             ");
		sender.sendMessage(Utils.chat(String.format("&8[&e%s&8/&e%s&8]", page, pages.size())));
		return true;
	}

	@Override
	public String getFailureMessage(String[] args) {
		return "/valhalla help <page>";
	}

	@Override
	public String getCommand() {
		return "/valhalla help <page>";
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[]{"valhalla.help"};
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission("valhalla.help");
	}

	@Override
	public String getDescription() {
		return TranslationManager.getTranslation("description_command_help");
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		if (args.length == 2) return List.of("1", "2", "3", "...");
		return Command.noSubcommandArgs();
	}
}
