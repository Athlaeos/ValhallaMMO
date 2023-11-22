package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Scaling;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;
import java.util.TreeMap;

public class ModifierScalingPresets {
    private static final Map<String, Scaling> scalings = new TreeMap<>();

    public static void loadScalings(){
        YamlConfiguration config = ConfigManager.getConfig("scaling_presets.yml").get();

        ConfigurationSection scalingSection = config.getConfigurationSection("scalings");
        if (scalingSection == null) return;
        for (String s : scalingSection.getKeys(false)){
            scalings.put(s, Scaling.fromConfig("scaling_presets.yml", "scalings." + s));
        }
    }

    public static Map<String, Scaling> getScalings() {
        return scalings;
    }
}
