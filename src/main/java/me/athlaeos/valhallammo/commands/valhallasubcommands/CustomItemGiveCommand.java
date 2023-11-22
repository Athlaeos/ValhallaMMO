package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.CustomItemManagementMenu;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CustomItemGiveCommand implements Command {
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Collection<Player> targets = new HashSet<>();
		if (args.length < 4){
			if (sender instanceof Player){
				targets.add((Player) sender);
			} else {
				Utils.sendMessage(sender, Utils.chat("&cOnly players can perform this command for themselves."));
				return true;
			}
		}
		if (args.length >= 3){
			if (args.length >= 4){
				targets.addAll(Utils.selectPlayers(sender, args[3]));
			}
			int amount;

			try {
				amount = Math.max(1, Integer.parseInt(args[2]));
			} catch (IllegalArgumentException ignored){
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_number")));
				return true;
			}

			if (targets.isEmpty()){
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_player_offline")));
				return true;
			}

			ItemStack item = CustomItemRegistry.getItem(args[1]);
			if (item == null) {
				Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_item")));
				return true;
			}
			List<ItemStack> decompressed = ItemUtils.decompressStacks(Map.of(item, amount));
			for (Player target : targets){
				decompressed.forEach(i -> ItemUtils.addItem(target, i, true));
			}
			return true;
		}
		return false;
	}

	@Override
	public String getFailureMessage(String[] args) {
		return "&c/valhalla give <item> <amount> <players>";
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[]{"valhalla.give"};
	}

	@Override
	public String getDescription() {
		return TranslationManager.getTranslation("description_command_give");
	}

	@Override
	public String getCommand() {
		return "/valhalla give <item> <amount> <players>";
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission("valhalla.give");
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		if (args.length == 2) return new ArrayList<>(CustomItemRegistry.getItems().keySet());
		if (args.length == 3) return List.of("1", "2", "3", "...");
		return null;
	}
}
