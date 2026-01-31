package me.athlaeos.valhallammo.version.conversion_dto;

import com.google.gson.Gson;
import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class AlphaPDCAdapter {
    private final Gson gson = new Gson();
    private final Map<String, NamespacedKey> alphaKeyMappings = new HashMap<>();
    private final Map<String, Class<? extends Profile>> alphaProfileMappings = new HashMap<>();

    {
        alphaKeyMappings.put("ALCHEMY", ValhallaMMO.key("valhalla_profile_alchemy"));
        alphaKeyMappings.put("ARCHERY", ValhallaMMO.key("valhalla_profile_archery"));
        alphaKeyMappings.put("ENCHANTING", ValhallaMMO.key("valhalla_profile_enchanting"));
        alphaKeyMappings.put("FARMING", ValhallaMMO.key("valhalla_profile_farming"));
        alphaKeyMappings.put("HEAVYARMOR", ValhallaMMO.key("valhalla_profile_heavy_armor"));
        alphaKeyMappings.put("HEAVYWEAPONS", ValhallaMMO.key("valhalla_profile_heavy_weapons"));
        alphaKeyMappings.put("LANDSCAPING", ValhallaMMO.key("valhalla_profile_landscaping"));
        alphaKeyMappings.put("LIGHTARMOR", ValhallaMMO.key("valhalla_profile_light_armor"));
        alphaKeyMappings.put("LIGHTWEAPONS", ValhallaMMO.key("valhalla_profile_light_weapons"));
        alphaKeyMappings.put("MINING", ValhallaMMO.key("valhalla_profile_mining"));
        alphaKeyMappings.put("SMITHING", ValhallaMMO.key("valhalla_profile_smithing"));

        alphaProfileMappings.put("ALCHEMY", AlchemyProfile.class);
        alphaProfileMappings.put("ARCHERY", ArcheryProfile.class);
        alphaProfileMappings.put("ENCHANTING", EnchantingProfile.class);
        alphaProfileMappings.put("FARMING", FarmingProfile.class);
        alphaProfileMappings.put("HEAVYARMOR", HeavyArmorProfile.class);
        alphaProfileMappings.put("HEAVYWEAPONS", HeavyWeaponsProfile.class);
        alphaProfileMappings.put("LANDSCAPING", LandscapingProfile.class);
        alphaProfileMappings.put("LIGHTARMOR", LightArmorProfile.class);
        alphaProfileMappings.put("LIGHTWEAPONS", LightWeaponsProfile.class);
        alphaProfileMappings.put("MINING", MiningProfile.class);
        alphaProfileMappings.put("SMITHING", SmithingProfile.class);
    }

    public Profile getProfile(Player p, String type) {
        if (p == null) return null;
        NamespacedKey key = alphaKeyMappings.get(type);
        if (key == null) return null;
        if (!p.getPersistentDataContainer().has(key, PersistentDataType.STRING)) return null;
        String jsonProfile = p.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (jsonProfile == null) {
            ValhallaMMO.logSevere("Profile is still null after creation, this should never occur. Notify plugin author");
            return null;
        }
        return gson.fromJson(jsonProfile, alphaProfileMappings.get(type));
    }
}
