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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

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
                ConfigManager.getConfig("config.yml").reload();
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
            } else if (args[1].equalsIgnoreCase("stophost")) {
                ConfigManager.getConfig("config.yml").set("resourcepack_port", null);
                ConfigManager.getConfig("config.yml").set("server_ip", null);
                ConfigManager.getConfig("config.yml").save();
                ConfigManager.getConfig("config.yml").reload();
                Host.setData(null);
                Host.stop();
                Utils.sendMessage(sender, "&aHost stopped! Player resource packs will be updated upon re-logging");
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
                        ConfigManager.getConfig("config.yml").set("resourcepack_port", port);
                        ConfigManager.getConfig("config.yml").set("server_ip", ip);
                    } catch (IllegalArgumentException ignored){
                        Utils.sendMessage(sender, Utils.chat(TranslationManager.getTranslation("error_command_invalid_number")));
                        return true;
                    }
                    ConfigManager.getConfig("config.yml").set("resourcepack_version", String.valueOf(System.currentTimeMillis()));
                    ConfigManager.getConfig("config.yml").set("resource_pack_config_override", true);
                    ConfigManager.getConfig("config.yml").save();
                    ResourcePack.generate();
                    sender.sendMessage(Utils.chat(TranslationManager.getTranslation("status_command_resourcepack_setup")));
                } else {
                    if (!modify("resource-pack-sha1", ResourcePack.getDefaultSha1())) {
                        ValhallaMMO.logSevere("Could not set resource pack sha1 to server.properties");
                        Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_resourcepack"));
                        return true;
                    }

                    if (!modify("resource-pack", ResourcePack.getDefaultPackLink())) {
                        ValhallaMMO.logSevere("Could not set resource pack link to server.properties");
                        Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_resourcepack"));
                        return true;
                    }
                    sender.sendMessage(Utils.chat(TranslationManager.getTranslation("status_command_resourcepack_setup")));
                    return true;
                }
            } else if (args[1].equalsIgnoreCase("download")){
                if (!ResourcePack.downloadDefault()) {
                    Utils.sendMessage(sender, "&cCould not download default resource pack. View console for more details");
                    return true;
                }
                ResourcePack.generate();
                sender.sendMessage(Utils.chat(TranslationManager.getTranslation("status_command_resourcepack_setup")));
            } else {
                boolean enabled;
                if (args[1].equalsIgnoreCase("enable")) {
                    enabled = true;
                } else if (args[1].equalsIgnoreCase("disable")) {
                    enabled = false;
                } else return false;
                ConfigManager.getConfig("config.yml").set("resource_pack_config_override", enabled);
                ConfigManager.getConfig("config.yml").save();
                ValhallaMMO.setResourcePackConfigForced(enabled);
                sender.sendMessage(Utils.chat(enabled ? TranslationManager.getTranslation("status_command_resourcepack_enabled") : TranslationManager.getTranslation("status_command_resourcepack_disabled")));

            }
        }
        return true;
    }

    @Override
    public String getFailureMessage(String[] args) {
        return "/valhalla resourcepack <enable/disable/setup/download/stophost/resetplayer/reload>";
    }

    @Override
    public String getDescription() {
        return TranslationManager.getTranslation("description_command_resourcepack");
    }

    @Override
    public String getCommand() {
        return "/valhalla resourcepack <enable/disable/setup/download/stophost/resetplayer/reload>";
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
        if (args.length == 2) return List.of("enable", "disable", "resetplayer", "reload", "setup", "download", "stophost");
        if (args.length == 3 && args[1].equalsIgnoreCase("setup")) return List.of("<your_server_ip>");
        if (args.length == 4 && args[1].equalsIgnoreCase("setup")) return List.of("<available_port>", "30005");
        return null;
    }

    private String sha1Code(File file) throws IOException, NoSuchAlgorithmException {
        FileInputStream fileInputStream = new FileInputStream(file);
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, digest);
        byte[] bytes = new byte[1024];
        // read all file content
        while (digestInputStream.read(bytes) > 0) digest = digestInputStream.getMessageDigest();
        byte[] resultByteArray = digest.digest();
        return bytesToHexString(resultByteArray);
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int value = b & 0xFF;
            if (value < 16) sb.append("0");
            sb.append(Integer.toHexString(value).toUpperCase());
        }
        return sb.toString();
    }

    @SuppressWarnings("all")
    private boolean modify(String configKey, String configSetting) {
        File serverProperties;
        try {
            serverProperties = new File(Paths.get(ValhallaMMO.getInstance().getDataFolder().getParentFile().getCanonicalFile().getParentFile().toString() + File.separatorChar + "server.properties").toString());
            if (!serverProperties.exists()) {
                ValhallaMMO.logSevere("Could not find server.properties");
                return false;
            }
        } catch (Exception exception) {
            ValhallaMMO.logSevere("Could not access server.properties");
            exception.printStackTrace();
            return false;
        }
        try {
            FileInputStream in = new FileInputStream(serverProperties);
            Properties props = new Properties();
            props.load(in);
            in.close();

            java.io.FileOutputStream out = new java.io.FileOutputStream(serverProperties);
            props.setProperty(configKey, configSetting);
            props.store(out, null);
            out.close();
        } catch (Exception ex) {
            ValhallaMMO.logSevere("Could not write to server.properties");
            return false;
        }
        return true;
    }
}
