package me.athlaeos.valhallammo.version.conversion_dto;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

public class AlchemyProfile extends Profile implements Serializable {
    private static final NamespacedKey alchemyProfileKey = new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_profile_alchemy");

    private float brewingtimereduction = 1F; // reduction in brewing time to brew anything
    private float brewingingredientsavechance = 0F; // chance for ingredient to not be consumed
    private float potionvelocity = 1F; // velocity multiplier of thrown potions
    private float potionsavechance = 0F; // chance for thrown/drank potion to not be consumed

    private int brewingquality_general = 0;
    private int brewingquality_buffs = 0;
    private int brewingquality_debuffs = 0;

    private double brewingexpmultiplier = 100D;

    private Collection<String> unlockedTransmutations = new HashSet<>();

    public AlchemyProfile(Player owner){
        super(owner);
        if (owner == null) return;
        this.key = alchemyProfileKey;
    }

    @Override
    public NamespacedKey getKey() {
        return alchemyProfileKey;
    }

    @Override
    public Profile fetchProfile(Player p, DatabaseConnection conn) throws SQLException {
        PreparedStatement stmt = conn.getConnection().prepareStatement("SELECT * FROM profiles_alchemy WHERE owner = ?;");
        stmt.setString(1, p.getUniqueId().toString());
        ResultSet result = stmt.executeQuery();
        if (result.next()){
            AlchemyProfile profile = new AlchemyProfile(p);
            profile.setLevel(result.getInt("level"));
            profile.setExp(result.getDouble("exp"));
            profile.setLifetimeEXP(result.getDouble("exp_total"));
            result.getFloat("brewing_time_reduction");
            if (result.wasNull()) return null; // if this property did not exist, then it can be assumed this profile was already in beta format
            return profile;
        }
        return null;
    }

    @Override
    public AlchemyProfile clone() throws CloneNotSupportedException {
        return (AlchemyProfile) super.clone();
    }
}
