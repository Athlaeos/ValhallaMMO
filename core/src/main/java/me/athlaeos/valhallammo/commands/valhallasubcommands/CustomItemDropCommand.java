package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Stream;

public class CustomItemDropCommand implements Command {
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player referencePlayer = null;
		if (args.length >= 8){
			referencePlayer = ValhallaMMO.getInstance().getServer().getPlayer(args[7]);
			if (referencePlayer == null){
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_player_offline")));
				return true;
			}
		}
		if (args.length < 6) return false;
		String item = args[1];
		int amount = Catch.catchOrElse(() -> Integer.parseInt(args[2]), -1);
		if (amount < 1){
			Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_number")));
			return true;
		}
		World world = null;
		boolean worldRef = false;
		if (args[6].equalsIgnoreCase("reference")){
			worldRef = true;
		} else {
			world = ValhallaMMO.getInstance().getServer().getWorld(args[6]);
		}
		Location reference = sender instanceof Entity e ? e.getLocation() :
				sender instanceof BlockCommandSender b ? b.getBlock().getLocation() : null;

		World spawnWorld = worldRef ? (sender instanceof Entity e ? e.getWorld() :
				sender instanceof BlockCommandSender b ? b.getBlock().getWorld() : null) : world;

		if (spawnWorld == null) {
			Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_world")));
			return true;
		}
		try {
			String xStr = args[3];
			boolean xRelative = xStr.startsWith("~") && reference != null;
			xStr = xStr.replaceFirst("~", "");
			if (xStr.isEmpty()) xStr = "0";
			double x = Double.parseDouble(xStr);

			String yStr = args[4];
			boolean yRelative = yStr.startsWith("~") && reference != null;
			yStr = yStr.replaceFirst("~", "");
			if (yStr.isEmpty()) yStr = "0";
			double y = Double.parseDouble(yStr);

			String zStr = args[5];
			boolean zRelative = zStr.startsWith("~") && reference != null;
			zStr = zStr.replaceFirst("~", "");
			if (zStr.isEmpty()) zStr = "0";
			double z = Double.parseDouble(zStr);

			reference = new Location(spawnWorld,
					xRelative ? reference.getX() + x : x,
					yRelative ? reference.getY() + y : y,
					zRelative ? reference.getZ() + z : z
			);
		} catch (IllegalArgumentException ignored){
			Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_number")));
			return true;
		}
		ItemStack finalItem = CustomItemRegistry.getProcessedItem(item, referencePlayer);
		if (finalItem == null) {
			Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_item")));
			return true;
		}
		Location loc = reference;
		List<ItemStack> decompressed = ItemUtils.decompressStacks(Map.of(finalItem, amount));
		decompressed.forEach(i ->
				spawnWorld.dropItem(loc, i)
		);

		return true;
	}

	@Override
	public String getFailureMessage(String[] args) {
		return "&c/valhalla drop <item> <amount> <x> <y> <z> <world> <referencePlayer>";
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[]{"valhalla.drop"};
	}

	@Override
	public String getDescription() {
		return TranslationManager.getTranslation("description_command_drop");
	}

	@Override
	public String getCommand() {
		return "/valhalla drop <item> <amount> <x> <y> <z> <world> <referencePlayer>";
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission("valhalla.drop");
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		if (args.length == 2) return new ArrayList<>(CustomItemRegistry.getItems().keySet());
		if (args.length == 3) return List.of("<amount>", "1", "2", "3", "...");
		if (args.length == 4) return List.of("<x>");
		if (args.length == 5) return List.of("<y>");
		if (args.length == 6) return List.of("<z>");
		if (args.length == 7) return Stream.concat(Stream.of("reference"), ValhallaMMO.getInstance().getServer().getWorlds().stream().map(World::getName)).toList();
		return null;
	}
}
