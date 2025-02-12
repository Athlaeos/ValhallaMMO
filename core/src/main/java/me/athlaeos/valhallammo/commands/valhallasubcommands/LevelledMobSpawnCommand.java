package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.entities.EntityClassification;
import me.athlaeos.valhallammo.entities.MonsterScalingManager;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class LevelledMobSpawnCommand implements Command {
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length < 7) return false;
		EntityType typeToSpawn = Catch.catchOrElse(() -> EntityType.valueOf(args[1]), null);
		int level = Catch.catchOrElse(() -> Integer.parseInt(args[2]), -1);
		if (level < 1){
			Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_number")));
			return true;
		}
		if (typeToSpawn == null || typeToSpawn == EntityType.UNKNOWN || typeToSpawn == EntityType.PLAYER
				|| EntityClassification.matchesClassification(typeToSpawn, EntityClassification.UNALIVE)){
			Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_option")));
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

		Entity spawned = spawnWorld.spawnEntity(reference, typeToSpawn);
		if (!(spawned instanceof LivingEntity l)) {
			Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_option")));
			return true;
		}

		AttributeInstance maxHealth = l.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		MonsterScalingManager.setLevel(l, level);
		if (maxHealth != null) l.setHealth(maxHealth.getValue());

		return true;
	}

	@Override
	public String getFailureMessage(String[] args) {
		return "&c/valhalla spawn <entityType> <level> <x> <y> <z> <world>";
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[]{"valhalla.spawn"};
	}

	@Override
	public String getDescription() {
		return TranslationManager.getTranslation("description_command_spawn");
	}

	@Override
	public String getCommand() {
		return "/valhalla spawn <entityType> <level> <x> <y> <z> <world>";
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission("valhalla.spawn");
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		if (args.length == 2) return Stream.of(EntityType.values()).filter(t ->
				t != EntityType.UNKNOWN && t != EntityType.PLAYER
						&& !EntityClassification.matchesClassification(t, EntityClassification.UNALIVE)
		).map(EntityType::toString).toList();
		if (args.length == 3) return List.of("<level>", "1", "2", "3", "...");
		if (args.length == 4) return List.of("<x>");
		if (args.length == 5) return List.of("<y>");
		if (args.length == 6) return List.of("<z>");
		if (args.length == 7) return Stream.concat(Stream.of("reference"), ValhallaMMO.getInstance().getServer().getWorlds().stream().map(World::getName)).toList();
		return null;
	}
}
