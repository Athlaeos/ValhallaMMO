package me.athlaeos.valhallammo.commands;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.*;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ProfileCommand implements CommandExecutor {
    private final Class<? extends Profile> profile;

    public ProfileCommand(Class<? extends Profile> profile){
        this.profile = profile;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player target;
        if (args.length >= 1){
            if (!sender.hasPermission("valhalla.profile.other")) {
                Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_no_permission")));
                return true;
            }
            target = ValhallaMMO.getInstance().getServer().getPlayer(args[0]);
        } else if (sender instanceof Player){
            target = (Player) sender;
        } else {
            Utils.sendMessage(sender, "&cOnly players can do this");
            return true;
        }

        displayProfile(sender, target, profile);
        return true;
    }

    public static void displayProfile(CommandSender displayTo, Player of, Class<? extends Profile> type) {
        Profile profile = ProfileCache.getOrCache(of, type);
        for (String i : profile.intStatNames()){
            StatFormat format = profile.getNumberStatProperties().get(i).getFormat();
            displayTo.sendMessage(Utils.chat(String.format("&f> %s: %s", format == null ? profile.getInt(i) : format.format(profile.getInt(i)), i)));
        }
        for (String i : profile.floatStatNames()){
            StatFormat format = profile.getNumberStatProperties().get(i).getFormat();
            displayTo.sendMessage(Utils.chat(String.format("&f> %s: %s", format == null ? profile.getFloat(i) : format.format(profile.getFloat(i)), i)));
        }
        for (String i : profile.doubleStatNames()){
            StatFormat format = profile.getNumberStatProperties().get(i).getFormat();
            displayTo.sendMessage(Utils.chat(String.format("&f> %s: %s", format == null ? profile.getDouble(i) : format.format(profile.getDouble(i)), i)));
        }
        for (String i : profile.stringSetStatNames()){
            displayTo.sendMessage(Utils.chat(String.format("&f> %s: %s", i, String.join(", ", profile.getStringSet(i)))));
        }
        for (String i : profile.booleanStatNames()){
            displayTo.sendMessage(Utils.chat(String.format("&f> %s: %s", i, profile.getBoolean(i))));
        }
    }
}
