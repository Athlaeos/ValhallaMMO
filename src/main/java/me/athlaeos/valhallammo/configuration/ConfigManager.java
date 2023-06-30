package me.athlaeos.valhallammo.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

//All credit to spigotmc.org user Bimmr for this manager
@SuppressWarnings("unused")
public class ConfigManager {

    private static final Map<String, Config> configs = new HashMap<>();

    public Map<String, Config> getConfigs() {
        return configs;
    }

    public static Config getConfig(String name) {
        if (!configs.containsKey(name))
            configs.put(name, new Config(name));

        return configs.get(name);
    }

    public static Config saveConfig(String name) {
        return getConfig(name).save();
    }

    public static Config reloadConfig(String name) {
        return getConfig(name).reload();
    }

    public static boolean doesPathExist(YamlConfiguration config, String root, String key){
        ConfigurationSection section = config.getConfigurationSection(root);
        return section != null && section.getKeys(false).contains(key);
    }
}
