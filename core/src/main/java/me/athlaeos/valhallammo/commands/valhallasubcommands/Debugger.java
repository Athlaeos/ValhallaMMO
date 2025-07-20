package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class Debugger implements Command {
	private static final Map<String, String> validModes = Map.of(
			"tree_capitating", "Sends messages explaining why the player can't use tree capitator. Please tell your player to attempt tree capitator. If this doesn't work, forward the messages sent to Athlaeos"
	);
	private static final Map<UUID, String> debugModes = new HashMap<>();

	public static void send(Player player, String mode, String message){
		String playerOption = debugModes.get(player.getUniqueId());
		if (playerOption == null || !playerOption.equalsIgnoreCase(mode)) return; // debugger for this mode disabled for this player
		for (Player p : ValhallaMMO.getInstance().getServer().getOnlinePlayers())
			if (p.hasPermission("valhalla.debugger")) Utils.sendMessage(player, "&fDEBUG >>> " + player.getName() + ": " + message);
		Utils.sendMessage(ValhallaMMO.getInstance().getServer().getConsoleSender(), message);
	}

	public static boolean isDebuggerEnabled(Player player, String mode){
		String playerOption = debugModes.get(player.getUniqueId());
		return playerOption != null && playerOption.equalsIgnoreCase(mode);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length < 3) return false;
		String mode = args[1];
		Player target = ValhallaMMO.getInstance().getServer().getPlayer(args[2]);

		if (target == null || !target.isOnline()){
			Utils.sendMessage(sender, "&cInvalid target, player not found");
			return true;
		}
		if (!mode.equalsIgnoreCase("stop") && !mode.equalsIgnoreCase("clear")){
			if (!validModes.containsKey(mode)) {
				Utils.sendMessage(sender, "&cInvalid mode, valid modes are currently &f" + String.join(", ", validModes.keySet()));
				return true;
			}
		} else {
			debugModes.remove(target.getUniqueId());
			return true;
		}

		debugModes.put(target.getUniqueId(), mode);
		Utils.sendMessage(sender, "&aPlayer " + target.getName() + " debugging enabled with mode " + mode);
		Utils.sendMessage(sender, "&7" + validModes.get(mode));
		return true;
	}

	@Override
	public String getFailureMessage(String[] args) {
		return "&4/valhalla debug <mode> <target>";
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[]{"valhalla.debugger"};
	}

	@Override
	public String getDescription() {
		return "Should only really be used if I (Athlaeos) tell you to. This command exists to allow for debugging bugs that are hard to reproduce by me, without constantly spamming console.";
	}

	@Override
	public String getCommand() {
		return "/valhalla debug <mode> <target>";
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission("valhalla.debugger");
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		if (args.length == 2) {
			List<String> modes = new ArrayList<>(validModes.keySet());
			modes.add("clear");
			modes.add("stop");
			return modes;
		}
		if (args.length == 3) return null;
		return Command.noSubcommandArgs();
	}
}
