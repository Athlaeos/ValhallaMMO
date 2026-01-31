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

public class LightArmorProfile extends Profile implements Serializable {
    private static final NamespacedKey lightArmorProfileKey = ValhallaMMO.key("valhalla_profile_light_armor");

    private float movementspeedpenalty = 0F; // movement speed penalty per piece of light armor
    private float damageresistance = 0F; // damage resistance per piece of light armor
    private float meleedamageresistance = 0F; // melee damage resistance per piece of light armor
    private float projectiledamageresistance = 0F; // projectile damage resistance per piece of light armor
    private float fireresistance = 0F; // fire damage resistance per piece of light armor
    private float magicresistance = 0F; // magic damage resistance per piece of light armor
    private float explosionresistance = 0F; // explosion damage resistance per piece of light armor
    private float falldamageresistance = 0F; // fall damage resistance per piece of light armor
    private float poisonresistance = 0F; // poison/wither damage resistance per piece of light armor
    private float knockbackresistance = 0F; // knockback reduction per piece of light armor
    private float bleedresistance = 0F; // bleed resistance per piece of light armor
    private float lightarmormultiplier = 1F; // armor multiplier for worn light armor pieces
    private float fullarmormultiplierbonus = 0F; // armor multiplier when player wearing full light armor
    private float fullarmorhungersavechance = 0F; // chance to not consume hunger points when wearing full light armor
    private float fullarmordodgechance = 0F; // chance to avoid damage from an entity when wearing full light armor
    private float fullarmorhealingbonus = 0F; // additional healing when wearing full light armor
    private float fullarmorbleedresistance = 0F; // additional bleeding resistance when wearing full light armor
    private int armorpiecesforbonusses = 4; // amount of armor pieces the player needs to wear to benefit from
    // "full set" bonusses
    private Collection<String> immunepotioneffects = new HashSet<>(); // potion effect immunity types
    private float adrenalinethreshold = 0F; // fraction of health the player must reach to activate adrenaline
    private int adrenalinecooldown = -1; // cooldown of adrenaline after activation
    private int adrenalinelevel = 0; // level of adrenaline, impact defined in skill config

    private double expmultiplier = 100D;

    public LightArmorProfile(Player owner){
        super(owner);
        if (owner == null) return;
        this.key = lightArmorProfileKey;
    }

    @Override
    public Profile fetchProfile(Player p, DatabaseConnection conn) throws SQLException {
        PreparedStatement stmt = conn.getConnection().prepareStatement("SELECT * FROM profiles_light_armor WHERE owner = ?;");
        stmt.setString(1, p.getUniqueId().toString());
        ResultSet result = stmt.executeQuery();
        if (result.next()){
            LightArmorProfile profile = new LightArmorProfile(p);
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
        return lightArmorProfileKey;
    }

    @Override
    public LightArmorProfile clone() throws CloneNotSupportedException {
        return (LightArmorProfile) super.clone();
    }
}
