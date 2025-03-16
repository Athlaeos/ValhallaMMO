package me.athlaeos.valhallammo.resourcepack;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.utility.Zipper;
import org.bukkit.entity.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ResourcePack {
    /*
     * I copied most of this from thesheepdev's Simple Resourcepack, so code credit for resource pack hosting goes to them
     */
    private static File pack;
    private static final Map<MinecraftVersion, ResourcePackDetails> resourcePacks = new HashMap<>();
    private static final String legacyPackLink = "https://github.com/user-attachments/files/19273979/ValhallaMMO_1.21.2-.zip";
    private static final String legacySha1 = "a0925253a4691f262572281178c3c7d75247c3ef";
    static {
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_26, null, null);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_25, null, null);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_24, null, null);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_23, null, null);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_22_3, "https://github.com/user-attachments/files/19273981/ValhallaMMO_1.21.4%2B.zip", "097c7e403e265dd523ca342ec024ba5488bf830b");
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_22_2, "https://github.com/user-attachments/files/19273981/ValhallaMMO_1.21.4%2B.zip", "097c7e403e265dd523ca342ec024ba5488bf830b");
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_22_1, "https://github.com/user-attachments/files/19273981/ValhallaMMO_1.21.4%2B.zip", "097c7e403e265dd523ca342ec024ba5488bf830b");
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_22, "https://github.com/user-attachments/files/19273981/ValhallaMMO_1.21.4%2B.zip", "097c7e403e265dd523ca342ec024ba5488bf830b");
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_21_5, "https://github.com/user-attachments/files/19273981/ValhallaMMO_1.21.4%2B.zip", "097c7e403e265dd523ca342ec024ba5488bf830b");
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_21_4, "https://github.com/user-attachments/files/19273981/ValhallaMMO_1.21.4%2B.zip", "097c7e403e265dd523ca342ec024ba5488bf830b");
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_21_3, "https://github.com/user-attachments/files/19273980/ValhallaMMO_1.21.3.zip", "9e1216aeaabeb04b2983f0aec3fb94cae6baa490");
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_21_2, legacyPackLink, legacySha1);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_21_1, legacyPackLink, legacySha1);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_21, legacyPackLink, legacySha1);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_20_6, legacyPackLink, legacySha1);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_20_5, legacyPackLink, legacySha1);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_20_4, legacyPackLink, legacySha1);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_20_3, legacyPackLink, legacySha1);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_20_2, legacyPackLink, legacySha1);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_20_1, legacyPackLink, legacySha1);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_20, legacyPackLink, legacySha1);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_19_4, legacyPackLink, legacySha1);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_19_3, legacyPackLink, legacySha1);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_19_2, legacyPackLink, legacySha1);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_19_1, legacyPackLink, legacySha1);
        mapResourcePackDetails(MinecraftVersion.MINECRAFT_1_19, legacyPackLink, legacySha1);
    }

    private static void mapResourcePackDetails(MinecraftVersion version, String packLink, String packSha1){
        resourcePacks.put(version, new ResourcePackDetails(packLink, packSha1));
    }

    public static File getPack() {
        return pack;
    }

    public static String getVersion(){
        return ValhallaMMO.getPluginConfig().getString("resourcepack_version", "1");
    }

    public static void tryStart(){
        if (Host.start()){
            if (ResourcePack.generate()){
                ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new ResourcePackListener(), ValhallaMMO.getInstance());
            }
        } else if (!ValhallaMMO.isResourcePackConfigForced()) {
            ValhallaMMO.logWarning("No valid host for the resource pack configured! You may ignore this warning if you've not set up the resource pack.");
        }
    }

    public static void sendUpdate(Player player) {
        if (Host.getData() != null) {
            try {
                player.setResourcePack("http://" + Host.getIp() + ":" + Host.getPort() + "/ValhallaMMO_" + getVersion());
            } catch (Exception ignored) { }
        }
    }

    public static String getResourcePackURL(){
        ResourcePackDetails details = resourcePacks.get(MinecraftVersion.getServerVersion());
        if (details == null) {
            ValhallaMMO.logWarning("Warning! An up-to-date resource pack for this version has not yet been made! Using default version. This may not work well or at all");
            return legacyPackLink;
        }
        return details.link;
    }

    public static String getResourcePackSha1(){
        ResourcePackDetails details = resourcePacks.get(MinecraftVersion.getServerVersion());
        if (details == null) return legacySha1;
        return details.sha1;
    }

    public static boolean downloadDefault(){
        try (BufferedInputStream in = new BufferedInputStream(new URL(getResourcePackURL()).openStream());
             FileOutputStream out = new FileOutputStream("ValhallaMMO_default.zip")) {
            byte[] data = new byte[1024];
            int read;
            while ((read = in.read(data, 0, 1024)) != -1){
                out.write(data, 0, read);
            }
        } catch (IOException ex){
            ValhallaMMO.logWarning("Could not download default resource pack");
            ex.printStackTrace();
            return false;
        }
        Zipper.unzipFolder("ValhallaMMO_default.zip", new File(ValhallaMMO.getInstance().getDataFolder(), "/resourcepack"));
        return true;
    }

    @SuppressWarnings("all")
    public static boolean generate() {
        File dataFolder = ValhallaMMO.getInstance().getDataFolder();

        File resourcePackDirectory = new File(dataFolder.getPath() + "/resourcepack");
        if (!resourcePackDirectory.exists()) {
            resourcePackDirectory.mkdirs();
        }

        File cacheDirectory = new File(dataFolder.getPath() + "/cache");
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs();
        }

        pack = new File(cacheDirectory.getPath() + "/ValhallaMMO_" + getVersion() + ".zip");
        if (pack.exists()) {
            boolean ds = pack.delete();
            if (!ds) {
                ValhallaMMO.logWarning("Could not delete old resource pack");
                return false;
            }
        }

        Zipper.zipFolder(resourcePackDirectory.getPath(), pack.getPath());

        try {
            Host.setData(Files.readAllBytes(pack.toPath()));
        } catch (IOException ex) {
            ValhallaMMO.logWarning("Could not cache resource pack");
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public static String getLegacyPackLink() {
        return legacyPackLink;
    }

    public static String getLegacySha1() {
        return legacySha1;
    }

    private static record ResourcePackDetails(String link, String sha1){}
}
