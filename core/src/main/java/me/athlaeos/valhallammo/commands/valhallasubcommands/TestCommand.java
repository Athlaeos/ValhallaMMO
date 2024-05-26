package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TestCommand implements Command {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player p){
            if (args[1].equalsIgnoreCase("a"))
                sender.sendMessage(Utils.chat("stat " + args[2] + " : " + AccumulativeStatManager.getCachedAttackerRelationalStats(args[2], p, p, 10000, true)));
            if (args[1].equalsIgnoreCase("v"))
                sender.sendMessage(Utils.chat("stat " + args[2] + " : " + AccumulativeStatManager.getCachedRelationalStats(args[2], p, p, 10000, true)));
        }
        return true;
    }

    @Override
    public String getFailureMessage(String[] args) {
        return "&4/valhalla profile <type> <name>";
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[]{"valhalla.profile" , "valhalla.profile.other"};
    }

    @Override
    public String getCommand() {
        return "/valhalla profile <type> <name>";
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("valhalla.profile") || sender.hasPermission("valhalla.profile.other");
    }

    @Override
    public String getDescription() {
        return TranslationManager.getTranslation("description_command_profile");
    }

    @Override
    public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
        if (args.length == 2) return List.of("a", "v");
        if (args.length == 3) return new ArrayList<>(AccumulativeStatManager.getSources().keySet());
        return null;
    }

//    private void registerSkillProfileFormats(){
//        for (Skill s : SkillRegistry.getAllSkills().values()){
//            List<String> format = TranslationManager.getListTranslation("profile_format_" + s.getType().toLowerCase());
//            if (format == null) format = new ArrayList<>();
//            profileFormats.put(s, format);
//        }
//    }
}
