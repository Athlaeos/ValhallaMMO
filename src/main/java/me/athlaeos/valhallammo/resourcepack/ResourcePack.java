package me.athlaeos.valhallammo.resourcepack;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.entity.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

public class ResourcePack {
    /*
     * I copied most of this from thesheepdev's Simple Resourcepack, so code credit for resource pack hosting goes to them
     */
    private static File pack;
    private static final String defaultPackLink = "https://download.mc-packs.net/pack/b489a41cba07969b3611658941abb90755445a51.zip";

    public static File getPack() {
        return pack;
    }

    public static String getVersion(){
        return ValhallaMMO.getPluginConfig().getString("resourcepack_version", "1.0");
    }

    public static void tryStart(){
        if (Host.start()){
            if (ResourcePack.generate()){
                ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new ResourcePackListener(), ValhallaMMO.getInstance());
            }
        } else {
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

    public static boolean downloadDefault(){
        try (BufferedInputStream in = new BufferedInputStream(new URL(defaultPackLink).openStream());
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
}
