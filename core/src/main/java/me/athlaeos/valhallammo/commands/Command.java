package me.athlaeos.valhallammo.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface Command {
	boolean execute(CommandSender sender, String[] args);
	String getFailureMessage(String[] args);
	String getDescription();
	String getCommand();
	String[] getRequiredPermissions();

	boolean hasPermission(CommandSender sender);
	List<String> getSubcommandArgs(CommandSender sender, String[] args);

	static List<String> noSubcommandArgs() {
		return List.of(" ");
	}
}
