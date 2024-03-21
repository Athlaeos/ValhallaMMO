package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.RelationalItemModifier;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ModifyCommand implements Command {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Collection<Player> targets = new HashSet<>();
        if (args.length >= 2){
            DynamicItemModifier modifier;
            try {
                modifier = ModifierRegistry.createModifier(args[1]);
            } catch (IllegalArgumentException ignored){
                Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_invalid_modifier"));
                return true;
            }
            if (modifier instanceof RelationalItemModifier){
                Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_advanced_modifier_unusable"));
                return true;
            }
            if (args.length >= 2 + modifier.commandArgsRequired()){
                String modifierError = modifier.parseCommand(sender, Arrays.copyOfRange(args, 2, args.length > 2 + modifier.commandArgsRequired() ? args.length - 1 : args.length));
                if (modifierError != null){
                    Utils.sendMessage(sender, modifierError);
                    return true;
                }

                if (args.length == 3 + modifier.commandArgsRequired()){
                    targets.addAll(Utils.selectPlayers(sender, args[2 + modifier.commandArgsRequired()]));
                } else if (sender instanceof Player p) {
                    targets.add(p);
                    ItemStack hand = p.getInventory().getItemInMainHand();
                    if (ItemUtils.isEmpty(hand)) {
                        Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_item_required"));
                        return true;
                    }
                } else {
                    Utils.sendMessage(sender, "&cOnly players may perform this command.");
                    return true;
                }

                if (targets.isEmpty()){
                    Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_player_offline"));
                    return true;
                }

                for (Player target : targets){
                    ItemStack hand = target.getInventory().getItemInMainHand();
                    if (ItemUtils.isEmpty(hand)) continue;
                    ItemBuilder builder = new ItemBuilder(hand);
                    modifier.processItem(target, builder, true, true);
                    if (ItemUtils.isEmpty(builder.getItem()) || CustomFlag.hasFlag(builder.getMeta(), CustomFlag.UNCRAFTABLE)){
                        Utils.sendMessage(sender,
                                TranslationManager.getTranslation("error_command_modifier_failed")
                                        .replace("%item%", ItemUtils.getItemName(builder.getMeta()))
                                        .replace("%player%", target.getName())
                        );
                        return true;
                    }
                    target.getInventory().setItemInMainHand(builder.get());
                }
                Utils.sendMessage(sender,
                        TranslationManager.getTranslation("status_command_modify_executed").replace("%count%", String.valueOf(targets.size()))
                );
                return true;
            } // not enough command args to fill modifier properties
        }
        return false;
    }

    @Override
    public String getFailureMessage(String[] args) {
        return "&4/valhalla modify [modifier] <args> <player>";
    }

    @Override
    public String getDescription() {
        return TranslationManager.getTranslation("description_command_modify");
    }

    @Override
    public String getCommand() {
        return "/valhalla modify [modifier] <args> <player>";
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[]{"valhalla.modify"};
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("valhalla.modify");
    }

    @Override
    public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
        if (args.length == 2) return ModifierRegistry.getModifiers().values().stream().filter(m -> !(m instanceof RelationalItemModifier)).map(DynamicItemModifier::getName).collect(Collectors.toList());
        else if (args.length >= 3) {
            DynamicItemModifier modifier;
            try {
                modifier = ModifierRegistry.createModifier(args[1]);
            } catch (IllegalArgumentException ignored){
                return List.of("INVALID_MODIFIER");
            }
            if (args.length > modifier.commandArgsRequired() + 2) return null;
            return modifier.commandSuggestions(sender, args.length - 3);
        }
        return null;
    }
}
