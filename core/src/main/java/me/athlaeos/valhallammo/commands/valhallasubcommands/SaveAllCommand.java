package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.item.ArmorSetRegistry;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.parties.PartyManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.utility.GlobalEffect;
import me.athlaeos.valhallammo.utility.Scheduling;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SaveAllCommand implements Command {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        ProfileRegistry.saveAll();
        CustomRecipeRegistry.saveRecipes(true);

        Scheduling.runTaskAsync(ValhallaMMO.getInstance(), () -> {
            PartyManager.saveParties();
            GlobalEffect.saveActiveGlobalEffects();
            CustomItemRegistry.saveItems();
            ArmorSetRegistry.saveArmorSets();
            LootTableRegistry.saveAll();

            Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_saveall_done"));
        });
        return true;
    }

    @Override
    public String getFailureMessage(String[] args) {
        return "/val saveall";
    }

    @Override
    public String getDescription() {
        return TranslationManager.getTranslation("description_command_saveall");
    }

    @Override
    public String getCommand() {
        return "/val saveall";
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[]{"valhalla.saveall"};
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("valhalla.saveall");
    }

    @Override
    public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
        return null;
    }
}
