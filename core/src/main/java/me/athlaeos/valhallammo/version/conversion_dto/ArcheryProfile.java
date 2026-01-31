package me.athlaeos.valhallammo.version.conversion_dto;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArcheryProfile extends Profile implements Serializable {
    private static final NamespacedKey archeryProfileKey = ValhallaMMO.key("valhalla_profile_archery");

    private float bowdamagemultiplier = 1f;
    private float crossbowdamagemultiplier = 1f;
    private float bowcritchance = 0f;
    private float crossbowcritchance = 0f;
    private float ammosavechance = 0f;
    private boolean critonfacingaway = false;
    private boolean critonstandingstill = false;
    private boolean critonstealth = false;
    private float critdamagemultiplier = 1f;
    private float inaccuracy = 0f;
    private float damagedistancebasemultiplier = 1f;
    private float damagedistancemultiplier = 0f;
    private float stunchance = 0f;
    private int stunduration = 0;
    private boolean stunoncrit = false;
    private float infinitydamagemultiplier = 1f;
    private int chargedshotcooldown = -1;
    private int chargedshotknockbackbonus = 0;
    private float chargedshotdamagemultiplier = 1f;
    private boolean chargedshotfullvelocity = false;
    private float chargedshotvelocitybonus = 0f;
    private int chargedshotpiercingbonus = 0;
    private int chargedshotcharges = 0;

    private double bowexpmultiplier = 100D;
    private double crossbowexpmultiplier = 100D;
    private double generalexpmultiplier = 100D;

    public ArcheryProfile(Player owner) {
        super(owner);
        if (owner == null) return;
        this.key = archeryProfileKey;
    }

    @Override
    public Profile fetchProfile(Player p, DatabaseConnection conn) throws SQLException {
        PreparedStatement stmt = conn.getConnection().prepareStatement("SELECT * FROM profiles_archery WHERE owner = ?;");
        stmt.setString(1, p.getUniqueId().toString());
        ResultSet result = stmt.executeQuery();
        if (result.next()) {
            ArcheryProfile profile = new ArcheryProfile(p);
            profile.setLevel(result.getInt("level"));
            profile.setExp(result.getDouble("exp"));
            profile.setLifetimeEXP(result.getDouble("exp_total"));
            result.getFloat("bowdamagemultiplier");
            if (result.wasNull()) return null;
            return profile;
        }
        return null;
    }

    @Override
    public NamespacedKey getKey() {
        return archeryProfileKey;
    }

    @Override
    public ArcheryProfile clone() throws CloneNotSupportedException {
        return (ArcheryProfile) super.clone();
    }
}
