package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.commands.ProfileCommand;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class ProfileStatsCommand implements Command {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 3){
            if (!sender.hasPermission("valhalla.profile.other")) {
                Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_no_permission")));
                return true;
            }
            target = ValhallaMMO.getInstance().getServer().getPlayer(args[2]);
        } else if (sender instanceof Player){
            target = (Player) sender;
        } else {
            sender.sendMessage(Utils.chat("&cOnly players can do this"));
            return true;
        }

        if (args.length < 2) return false;
        if (target == null){
            Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_player_offline")));
            return true;
        }

        Skill skill = SkillRegistry.getSkill(args[1]);
        if (skill == null) {
            Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_skill")));
            return true;
        }

        ProfileCommand.displayProfile(sender, target, skill.getProfileType());

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
        if (args.length == 2){
            return SkillRegistry.getAllSkills().values().stream().map(s -> s.getType().toLowerCase()).collect(Collectors.toList());
        }
        return null;
    }
}
