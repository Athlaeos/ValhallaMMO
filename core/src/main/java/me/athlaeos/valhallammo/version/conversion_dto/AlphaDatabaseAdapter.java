package me.athlaeos.valhallammo.version.conversion_dto;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AlphaDatabaseAdapter {
    private final DatabaseConnection conn;
    private final AlphaPDCAdapter altPersistency;
    private final Map<String, Profile> alphaProfileMappings = new HashMap<>();

    {
        alphaProfileMappings.put("ALCHEMY", new AlchemyProfile(null));
        alphaProfileMappings.put("ARCHERY", new ArcheryProfile(null));
        alphaProfileMappings.put("ENCHANTING", new EnchantingProfile(null));
        alphaProfileMappings.put("FARMING", new FarmingProfile(null));
        alphaProfileMappings.put("HEAVYARMOR", new HeavyArmorProfile(null));
        alphaProfileMappings.put("HEAVYWEAPONS", new HeavyWeaponsProfile(null));
        alphaProfileMappings.put("LANDSCAPING", new LandscapingProfile(null));
        alphaProfileMappings.put("LIGHTARMOR", new LightArmorProfile(null));
        alphaProfileMappings.put("LIGHTWEAPONS", new LightWeaponsProfile(null));
        alphaProfileMappings.put("MINING", new MiningProfile(null));
        alphaProfileMappings.put("SMITHING", new SmithingProfile(null));
    }

    private final Map<UUID, Map<String, Profile>> profiles = new HashMap<>();

    public AlphaDatabaseAdapter(DatabaseConnection conn, AlphaPDCAdapter altPersistency){
        this.conn = conn;
        this.altPersistency = altPersistency;
    }

    public Profile getProfile(Player p, String type) {
        if (p == null) return null;
        if (conn == null || conn.getConnection() == null) return altPersistency.getProfile(p, type);
        try {
            Profile profile = alphaProfileMappings.get(type).fetchProfile(p, conn);
            // if no profile was found in the database, default to PersistentDataContainer implementation
            // anyway
            if (profile == null) profile = altPersistency.getProfile(p, type);
            return profile;
        } catch (SQLException e){
            ValhallaMMO.logSevere("SQLException when trying to fetch " + p.getName() + "'s profile for skill type " + type + ". ");
            e.printStackTrace();
        }
        return null;
    }
}
