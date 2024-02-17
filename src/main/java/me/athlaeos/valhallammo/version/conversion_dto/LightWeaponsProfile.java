package me.athlaeos.valhallammo.version.conversion_dto;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LightWeaponsProfile extends Profile implements Serializable {
    private static final NamespacedKey lightWeaponsProfileKey = new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_profile_light_weapons");

    private float damagemultiplier = 1F;
    private float attackspeedbonus = 0F;
    private float knockbackbonus = 0F;
    private float damagebonuslightarmor = 0F;
    private float damagebonusheavyarmor = 0F;
    private float flatlightarmorignored = 0F;
    private float flatheavyarmorignored = 0F;
    private float flatarmorignored = 0F;
    private float fractionlightarmorignored = 0F;
    private float fractionheavyarmorignored = 0F;
    private float fractionarmorignored = 0F;
    private float immunityframereduction = 0F;
    private int parrytimeframe = 0;
    private int parryvulnerableframe = 0;
    private float parrydamagereduction = 0F;
    private int parrycooldown = -1;
    private int enemydebuffduration = 0;
    private int faileddebuffduration = 0;
    private boolean bleedoncrit = false;
    private float bleedchance = 0F;
    private float bleeddamage = 0F;
    private int bleedduration = 0;
    private float critchance = 0F;
    private boolean critonbleed = false;
    private float critdamagemultiplier = 1F;
    private boolean critonstealth = false;
    private boolean critonstun = false;
    private boolean unlockedweaponcoating = false; // determines if the player can coat their light weapons with potion effects
    private float selfpotiondurationmultiplier = 0F; // the multiplier of the original potion duration determining how long the weapon will stay coated for
    private float enemypotiondurationmultiplier = 0F; // the multiplier of the original potion duration determining the debuff inflicted on the enemy
    private float enemypotionamplifiermultiplier = 0F; // the multiplier of the original potion amplifier determining the debuff inflicted on the enemy
    private float stunchance = 0F;
    private int stunduration = 0;
    private float dropbonus = 0F;
    private float raredropmultiplier = 0F;

    private double expmultiplier = 100D;

    public LightWeaponsProfile(Player owner){
        super(owner);
        if (owner == null) return;
        this.key = lightWeaponsProfileKey;
    }

    @Override
    public Profile fetchProfile(Player p, DatabaseConnection conn) throws SQLException {
        PreparedStatement stmt = conn.getConnection().prepareStatement("SELECT * FROM profiles_light_weapons WHERE owner = ?;");
        stmt.setString(1, p.getUniqueId().toString());
        ResultSet result = stmt.executeQuery();
        if (result.next()){
            LightWeaponsProfile profile = new LightWeaponsProfile(p);
            profile.setLevel(result.getInt("level"));
            profile.setExp(result.getDouble("exp"));
            profile.setLifetimeEXP(result.getDouble("exp_total"));
            result.getFloat("damagemultiplier");
            if (result.wasNull()) return null;
            return profile;
        }
        return null;
    }

    @Override
    public NamespacedKey getKey() {
        return lightWeaponsProfileKey;
    }

    @Override
    public LightWeaponsProfile clone() throws CloneNotSupportedException {
        return (LightWeaponsProfile) super.clone();
    }
}
