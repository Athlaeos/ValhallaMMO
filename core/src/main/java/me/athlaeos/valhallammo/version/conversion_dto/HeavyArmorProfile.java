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

public class HeavyArmorProfile extends Profile implements Serializable {
    private static final NamespacedKey heavyArmorProfileKey = ValhallaMMO.key("valhalla_profile_heavy_armor");

    private float movementspeedpenalty = 0F; // movement speed penalty per piece of heavy armor
    private float damageresistance = 0F; // damage resistance per piece of heavy armor
    private float meleedamageresistance = 0F; // melee damage resistance per piece of heavy armor
    private float projectiledamageresistance = 0F; // projectile damage resistance per piece of heavy armor
    private float fireresistance = 0F; // fire damage resistance per piece of heavy armor
    private float magicresistance = 0F; // magic damage resistance per piece of heavy armor
    private float explosionresistance = 0F; // explosion damage resistance per piece of heavy armor
    private float falldamageresistance = 0F; // fall damage resistance per piece of heavy armor
    private float poisonresistance = 0F; // poison/wither damage resistance per piece of heavy armor
    private float knockbackresistance = 0F; // knockback reduction per piece of heavy armor
    private float bleedresistance = 0F; // bleed resistance per piece of heavy armor
    private float heavyarmormultiplier = 1F; // armor multiplier for worn heavy armor pieces
    private float fullarmormultiplierbonus = 0F; // armor multiplier when player wearing full heavy armor
    private float fullarmorhungersavechance = 0F; // chance to not consume hunger points when wearing full heavy armor
    private float fullarmorreflectchance = 0F; // chance reflect damage back to an entity when wearing full heavy armor
    private float fullarmorhealingbonus = 0F; // additional healing when wearing full heavy armor
    private float fullarmorbleedresistance = 0F; // additional bleeding resistance when wearing full heavy armor
    private float reflectfraction = 0F; // fraction of original damage that should be reflected back to the attacker
    private int armorpiecesforbonusses = 4; // amount of armor pieces the player needs to wear to benefit from
    // "full set" bonusses
    private Collection<String> immunepotioneffects = new HashSet<>(); // potion effect immunity types
    private float ragethreshold = 0F; // fraction of health the player must reach to activate rage
    private int ragecooldown = -1; // cooldown of rage after activation
    private int ragelevel = 0; // level of rage, impact defined in skill config

    private double expmultiplier = 100D;

    public HeavyArmorProfile(Player owner){
        super(owner);
        if (owner == null) return;
        this.key = heavyArmorProfileKey;
    }

    @Override
    public Profile fetchProfile(Player p, DatabaseConnection conn) throws SQLException {
        PreparedStatement stmt = conn.getConnection().prepareStatement("SELECT * FROM profiles_heavy_armor WHERE owner = ?;");
        stmt.setString(1, p.getUniqueId().toString());
        ResultSet result = stmt.executeQuery();
        if (result.next()){
            HeavyArmorProfile profile = new HeavyArmorProfile(p);
            profile.setLevel(result.getInt("level"));
            profile.setExp(result.getDouble("exp"));
            profile.setLifetimeEXP(result.getDouble("exp_total"));
            result.getFloat("movementspeedpenalty");
            if (result.wasNull()) return null;
            return profile;
        }
        return null;
    }

    @Override
    public NamespacedKey getKey() {
        return heavyArmorProfileKey;
    }

    @Override
    public HeavyArmorProfile clone() throws CloneNotSupportedException {
        return (HeavyArmorProfile) super.clone();
    }
}
