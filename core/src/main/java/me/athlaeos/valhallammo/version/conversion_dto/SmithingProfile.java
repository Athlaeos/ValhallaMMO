package me.athlaeos.valhallammo.version.conversion_dto;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SmithingProfile extends Profile implements Serializable {
    private static final NamespacedKey smithingProfileKey = ValhallaMMO.key("valhalla_profile_smithing");

    private int craftingquality_all = 0;
    private int craftingquality_bow = 0;
    private int craftingquality_crossbow = 0;
    private int craftingquality_wood = 0;
    private int craftingquality_leather = 0;
    private int craftingquality_stone = 0;
    private int craftingquality_chain = 0;
    private int craftingquality_gold = 0;
    private int craftingquality_iron = 0;
    private int craftingquality_diamond = 0;
    private int craftingquality_netherite = 0;
    private int craftingquality_prismarine = 0;
    private int craftingquality_membrane = 0;
    private double craftingexpmultiplier_all = 100D;
    private double craftingexpmultiplier_bow = 100D;
    private double craftingexpmultiplier_crossbow = 100D;
    private double craftingexpmultiplier_wood = 100D;
    private double craftingexpmultiplier_leather = 100D;
    private double craftingexpmultiplier_stone = 100D;
    private double craftingexpmultiplier_chain = 100D;
    private double craftingexpmultiplier_gold = 100D;
    private double craftingexpmultiplier_iron = 100D;
    private double craftingexpmultiplier_diamond = 100D;
    private double craftingexpmultiplier_netherite = 100D;
    private double craftingexpmultiplier_prismarine = 100D;
    private double craftingexpmultiplier_membrane = 100D;
    private float craftingtimereduction = 0F;

    public SmithingProfile(Player owner){
        super(owner);
        if (owner == null) return;
        this.key = smithingProfileKey;
    }

    @Override
    public Profile fetchProfile(Player p, DatabaseConnection conn) throws SQLException {
        PreparedStatement stmt = conn.getConnection().prepareStatement("SELECT * FROM profiles_smithing WHERE owner = ?;");
        stmt.setString(1, p.getUniqueId().toString());
        ResultSet result = stmt.executeQuery();
        if (result.next()){
            SmithingProfile profile = new SmithingProfile(p);
            profile.setLevel(result.getInt("level"));
            profile.setExp(result.getDouble("exp"));
            profile.setLifetimeEXP(result.getDouble("exp_total"));
            result.getFloat("craftingquality_all");
            if (result.wasNull()) return null;
            return profile;
        }
        return null;
    }

    @Override
    public NamespacedKey getKey() {
        return smithingProfileKey;
    }
}
