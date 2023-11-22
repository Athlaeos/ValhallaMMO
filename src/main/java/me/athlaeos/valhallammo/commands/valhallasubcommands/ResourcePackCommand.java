package me.athlaeos.valhallammo.commands.valhallasubcommands;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.resourcepack.Host;
import me.athlaeos.valhallammo.resourcepack.ResourcePack;
import me.athlaeos.valhallammo.resourcepack.ResourcePackListener;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public class ResourcePackCommand implements Command {

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2){
            return false;
        } else {
            if (args[1].equalsIgnoreCase("reload")){
                String newVersion = String.valueOf(System.currentTimeMillis());
                ConfigManager.getConfig("config.yml").set("resourcepack_version", newVersion);
                ConfigManager.getConfig("config.yml").save();
                ResourcePack.generate();
                ResourcePack.tryStart();
                Utils.sendMessage(sender, "&aReloaded resource pack!");
                return true;
            } else if (args[1].equalsIgnoreCase("resetplayer")){
                if (args.length > 2){
                    Collection<Player> targets = Utils.selectPlayers(sender, args[2]);

                    if (targets.isEmpty()){
                        Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_player_offline")));
                        return true;
                    }
                    for (Player target : targets){
                        ResourcePackListener.resetPackVersion(target);
                        ResourcePack.sendUpdate(target);
                    }
                    Utils.sendMessage(sender, "&aSent update!");
                    return true;
                } else return false;
            } else if (args[1].equalsIgnoreCase("setup")){
                if (args.length > 3){
                    if (!ResourcePack.downloadDefault()) {
                        Utils.sendMessage(sender, "&cCould not download default resource pack. View console for more details");
                        return true;
                    }
                    try {
                        String ip = args[2];
                        int port = Integer.parseInt(args[3]);

                        Host.setIp(ip);
                        Host.setPort(port);
                    } catch (IllegalArgumentException ignored){
                        Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_number")));
                        return true;
                    }
                    ConfigManager.getConfig("config.yml").set("resourcepack_version", String.valueOf(System.currentTimeMillis()));
                    ConfigManager.getConfig("config.yml").set("resource_pack_config_override", true);
                    ConfigManager.getConfig("config.yml").save();
                    ResourcePack.generate();
                    sender.sendMessage(Utils.chat(TranslationManager.getTranslation("status_command_resourcepack_setup")));
                } else return false;
            } else {
                boolean enabled;
                if (args[1].equalsIgnoreCase("enable")) {
                    enabled = true;
                } else if (args[1].equalsIgnoreCase("disable")) {
                    enabled = false;
                } else return false;
                ConfigManager.getConfig("config.yml").set("resource_pack_config_override", enabled);
                ConfigManager.getConfig("config.yml").save();
                sender.sendMessage(Utils.chat(enabled ? TranslationManager.getTranslation("status_command_resourcepack_enabled") : TranslationManager.getTranslation("status_command_resourcepack_disabled")));
            }
        }
        return true;
    }

    @Override
    public String getFailureMessage(String[] args) {
        return "&c/valhalla resourcepack";
    }

    @Override
    public String getDescription() {
        return TranslationManager.getTranslation("description_command_resourcepack");
    }

    @Override
    public String getCommand() {
        return "/valhalla resourcepack <download/enable/disable/setup>";
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[]{"valhalla.resourcepack"};
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("valhalla.resourcepack");
    }

    @Override
    public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
        if (args.length == 2) return List.of("enable", "disable", "resetplayer", "reload", "setup");
        if (args.length == 3 && args[1].equalsIgnoreCase("setup")) return List.of("<your_server_ip>");
        if (args.length == 4 && args[1].equalsIgnoreCase("setup")) return List.of("<available_port>", "30005");
        return null;
    }
}
