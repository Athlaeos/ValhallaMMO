package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantType;
import me.athlaeos.valhallammo.trading.menu.CustomTradeManagementMenu;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ManageMerchantsCommand implements Command {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player player){
            if (args.length > 2 && args[1].equalsIgnoreCase("summonitem")) {
                MerchantType type = CustomMerchantManager.getMerchantType(args[2]);
                if (type == null) Utils.sendMessage(sender, "&cInvalid merchant type");
                else {
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    if (ItemUtils.isEmpty(hand)) Utils.sendMessage(sender, "&cYou should be holding an item");
                    else {
                        ItemMeta meta = hand.getItemMeta();
                        if (meta == null) return true;
                        CustomMerchantManager.convertToMerchantSummonItem(meta, type);
                        hand.setItemMeta(meta);
                        Utils.sendMessage(sender, "&aConverted to merchant type changing item! Set to &2" + type.getType());
                        Utils.sendMessage(sender, "&7If this is a villager spawn egg, spawned villagers will immediately have the given type and custom trades");
                        Utils.sendMessage(sender, "&7If it isn't, then it will need to be used on an existing villager to apply the type");
                    }
                }
            } else if (args.length > 1 && args[1].equalsIgnoreCase("blockeritem")) {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (ItemUtils.isEmpty(hand)) Utils.sendMessage(sender, "&cYou should be holding an item");
                else {
                    ItemMeta meta = hand.getItemMeta();
                    if (meta == null) return true;
                    CustomMerchantManager.convertToBlockerItem(meta, !CustomMerchantManager.isBlockerItem(meta));
                    hand.setItemMeta(meta);
                    if (CustomMerchantManager.isBlockerItem(meta)) {
                        Utils.sendMessage(sender, "&aConverted to merchant blocking item!");
                        Utils.sendMessage(sender, "&7Right click on villager to prevent them from using the custom trading system, or to re-enable it. Rather, ValhallaMMO will stop interfering when you interact with them.");
                        Utils.sendMessage(sender, "&7Useful if you have villagers that serve alternative functionality when right clicked, such as questgivers or shopkeepers from other plugins");
                    } else {
                        Utils.sendMessage(sender, "&aRemoved merchant blocking status from item!");
                    }
                }
            } else {
                new CustomTradeManagementMenu(PlayerMenuUtilManager.getPlayerMenuUtility(player)).open();
            }
        } else Utils.sendMessage(sender, "&cOnly players may manage custom merchants");
        return true;
    }

    @Override
    public String getFailureMessage(String[] args) {
        return "&4/val merchants";
    }

    @Override
    public String getDescription() {
        return TranslationManager.getTranslation("description_command_merchants");
    }

    @Override
    public String getCommand() {
        return "/val merchants";
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[]{"valhalla.merchants"};
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("valhalla.merchants");
    }

    @Override
    public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
        if (args.length == 2) return List.of("summonitem", "blockeritem");
        if (args.length == 3 && !args[2].equalsIgnoreCase("blockeritem")) return new ArrayList<>(CustomMerchantManager.getRegisteredMerchantTypes().keySet());
        return Command.noSubcommandArgs();
    }
}
