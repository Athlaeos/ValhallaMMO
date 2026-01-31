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

public class LandscapingProfile extends Profile implements Serializable {
    private static final NamespacedKey landscapingProfileKey = ValhallaMMO.key("valhalla_profile_landscaping");

    private float woodcuttingraredropratemultiplier = 1F; // rare drop rate multiplier for cut logs
    private float woodcuttingdropmultiplier = 1F; // drop multiplier for cut logs
    private float diggingraredropratemultiplier = 1F; // rare drop rate multiplier for diggable blocks
    private float diggingdropmultiplier = 1F; // drop multiplier for diggable blocks
    private float woodstrippingraredropratemultiplier = 1F; // rare drop rate multiplier for stripping wood
    private int treecapitatorcooldown = -1; // cooldown of the tree capitator special ability, if <0 the ability is disabled
    private float instantgrowthrate = 0; // the amount of stages saplings grow immediately upon planting
    private boolean replacesaplings = false; // if true, saplings are automatically planted when breaking trees
    private float blockplacereachbonus = 0; // unused
    private Collection<String> unlockedconversions = new HashSet<>(); // the block conversion interactions the player has unlocked
    private Collection<String> validtreecapitatorblocks = new HashSet<>();
    private float woodcuttingexperiencerate = 0F;
    private float diggingexperiencerate = 0F;

    private double woodcuttingexpmultiplier = 100D;
    private double diggingexpmultiplier = 100D;
    private double woodstrippingexpmultiplier = 100D;
    private double generalexpmultiplier = 100D;

    public LandscapingProfile(Player owner){
        super(owner);
        if (owner == null) return;
        this.key = landscapingProfileKey;
    }

    @Override
    public Profile fetchProfile(Player p, DatabaseConnection conn) throws SQLException {
        PreparedStatement stmt = conn.getConnection().prepareStatement("SELECT * FROM profiles_landscaping WHERE owner = ?;");
        stmt.setString(1, p.getUniqueId().toString());
        ResultSet result = stmt.executeQuery();
        if (result.next()){
            LandscapingProfile profile = new LandscapingProfile(p);
            profile.setLevel(result.getInt("level"));
            profile.setExp(result.getDouble("exp"));
            profile.setLifetimeEXP(result.getDouble("exp_total"));
            result.getFloat("woodcuttingraredropratemultiplier");
            if (result.wasNull()) return null;
            return profile;
        }
        return null;
    }

    @Override
    public NamespacedKey getKey() {
        return landscapingProfileKey;
    }

    @Override
    public LandscapingProfile clone() throws CloneNotSupportedException {
        return (LandscapingProfile) super.clone();
    }
}
