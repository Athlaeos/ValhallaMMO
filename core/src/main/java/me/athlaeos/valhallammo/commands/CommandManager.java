package me.athlaeos.valhallammo.commands;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.commands.valhallasubcommands.*;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager implements TabExecutor {
	private static final Map<String, Command> commands = new HashMap<>();

	public CommandManager() {
		commands.put("help", new HelpCommand());
		commands.put("exp", new EXPCommand());
		commands.put("recipes", new ManageRecipesCommand());
		commands.put("profile", new ProfileStatsCommand());
		commands.put("reset", new ResetProfilesCommand());
		commands.put("skills", new ViewSkillTreeCommand());
  		commands.put("modify", new ModifyCommand());
		commands.put("reward", new PerkRewardCommand());
		commands.put("attribute", new AttributeCommand());
		commands.put("potionattribute", new PotionAttributeCommand());
		commands.put("effect", new PotionEffectCommand());
		commands.put("loot", new ManageLootTablesCommand());
		commands.put("tool", new SpecializedToolCommand());
		commands.put("armorsets", new ArmorSetManagerCommand());
		commands.put("items", new CustomItemManagerCommand());
		commands.put("give", new CustomItemGiveCommand());
		commands.put("globalbuff", new GlobalEffectCommand());
		commands.put("resourcepack", new ResourcePackCommand());
		commands.put("saveall", new SaveAllCommand());
		commands.put("hardness", new BlockHardnessCommand());
//		commands.put("reload", new ReloadCommand());
//		commands.put("import", new ImportCommand());
//		commands.put("toggleexp", new HideBossBarsCommand());
//		commands.put("revealrecipekeys", new RecipeRevealToggleCommand());
	}

	public static Map<String, Command> getCommands() {
		return commands;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command cmd, @NotNull String name, String[] args) {
		if (args.length == 0) {
			Utils.sendMessage(sender, Utils.chat(String.format("&eValhallaMMO v%s by Athlaeos", ValhallaMMO.getInstance().getDescription().getVersion())));
			Utils.sendMessage(sender, Utils.chat("&7/val help"));
			Utils.sendMessage(sender, Utils.chat("&7/skills"));
			return true;
		}

		Command command = commands.get(args[0].toLowerCase());
		if (command == null) {
			Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_command")));
			return true;
		}

		if (!command.hasPermission(sender)){
			Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_no_permission")));
			return true;
		}

		if (!command.execute(sender, args)) Utils.sendMessage(sender, Utils.chat(command.getFailureMessage(args)));
		return true;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command cmd, @NotNull String name, String[] args) {
		if (args.length == 1) {
			List<String> allowedCommands = new ArrayList<>();
			for (String arg : commands.keySet()) {
				Command command = commands.get(arg);
				if (command.hasPermission(sender)) {
					allowedCommands.add(arg);
				}
			}
			return allowedCommands;
		} else if (args.length > 1) {
			Command command = commands.get(args[0]);
			if (command == null || !command.hasPermission(sender)) return Command.noSubcommandArgs();
			return command.getSubcommandArgs(sender, args);
		}
		return null;
	}
}
