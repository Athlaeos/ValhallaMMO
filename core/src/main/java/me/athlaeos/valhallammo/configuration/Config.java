package me.athlaeos.valhallammo.configuration;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Config {
    private final String name;
    private File file;
    private YamlConfiguration config;
    private final ValhallaMMO plugin = ValhallaMMO.getInstance();

    public Config(String name) {
        this.name = name;
        save();
    }

    public Config save() {
        if ((this.config == null) || (this.file == null) || file.exists())
            return this;
        try {
            ConfigurationSection section = config.getConfigurationSection("");
            if (section != null && !section.getKeys(true).isEmpty())
                config.save(this.file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return this;
    }

    public YamlConfiguration get() {
        if (this.config == null)
            reload();

        return this.config;
    }

    public Config saveDefaultConfig() {
        file = new File(plugin.getDataFolder(), this.name);

        plugin.saveResource(this.name, false);

        return this;
    }

    public Config reload() {
        if (file == null)
            this.file = new File(plugin.getDataFolder(), this.name);

        this.config = YamlConfiguration.loadConfiguration(file);

        return this;
    }

    public Config copyDefaults(boolean force) {
        get().options().copyDefaults(force);
        return this;
    }

    public Config set(String key, Object value) {
        get().set(key, value);
        return this;
    }

    public Object get(String key) {
        return get().get(key);
    }
}