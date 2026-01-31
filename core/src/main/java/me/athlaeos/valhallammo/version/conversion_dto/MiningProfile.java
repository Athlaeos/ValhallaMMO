package me.athlaeos.valhallammo.version.conversion_dto;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class MiningProfile extends Profile implements Serializable {
    private static final NamespacedKey miningProfileKey = ValhallaMMO.key("valhalla_profile_mining");

    private float miningraredropratemultiplier = 1F; // rare drop rate multiplier of mining
    private float blastminingraredropratemultiplier = 1F; // rare drop rate multiplier of blast mining
    private float miningdropmultiplier = 1F; // drop multiplier for mining
    private float blastminingdropmultiplier = 1F; // drop multiplier for blast mining
    private float blastradiusmultiplier = 1F; // blast radius multiplier
    private float tntexplosiondamagemultiplier = 1F; // damage multiplier of damage taken from tnt
    private int veinminingcooldown = -1; // cooldown of the vein mining ability, which harvests a large area of the same ore at once. if -1, ability is unusable
    private Collection<String> validveinminerblocks = new HashSet<>();
    private Collection<String> unbreakableblocks = new HashSet<>();
    private int quickminecooldown = -1; // cooldown of the quick mine ability, which allows blocks to be instantly mined at the cost of hunger and health.
    private int quickminehungerdrainspeed = -1; // amount of blocks the player can instantly mine before losing 1 saturation/hunger/health point. if <0 this ability is disabled
    // some blocks can be valued higher, which are defined in skill_mining.yml
    // this is to make it so blocks like obsidian can't be mined as frequently as any other block. undefined block values are valued as 1
    private float quickminedurabilitylossrate = 1F; // durability damage multiplier of tools while quickmine mode is enabled. 1 is equal to a normal rate, anything below 1 means a decreased rate, anything above 1 is an increased rate
    private float oreexperiencemultiplier = 1F;
    private float blockmineexperiencerate = 0F;
    private int explosionfortunelevel = 0; // the fortune level at which exploded blocks will be "mined". if 0 blocks will be broken as if they were mined with a plain iron pickaxe, if -1 they will be broken as if broken with a silk touch pickaxe
    private int tunnelfatiguelevel = -1; // mining fatigue level applied on the player when tunnel mode is activated, if <0 tunnel mode is locked, if 0 no fatigue is applied

    private double generalexpmultiplier = 100D;
    private double blastminingexpmultiplier = 100D;
    private double miningexpmultiplier = 100D;

    @Override
    public Profile fetchProfile(Player p, DatabaseConnection conn) throws SQLException {
        PreparedStatement stmt = conn.getConnection().prepareStatement("SELECT * FROM profiles_mining WHERE owner = ?;");
        stmt.setString(1, p.getUniqueId().toString());
        ResultSet result = stmt.executeQuery();
        if (result.next()){
            MiningProfile profile = new MiningProfile(p);
            profile.setLevel(result.getInt("level"));
            profile.setExp(result.getDouble("exp"));
            profile.setLifetimeEXP(result.getDouble("exp_total"));
            result.getFloat("miningraredropratemultiplier");
            if (result.wasNull()) return null;
            return profile;
        }
        return null;
    }

    public MiningProfile(Player owner){
        super(owner);
        if (owner == null) return;
        this.key = miningProfileKey;
    }

    @Override
    public NamespacedKey getKey() {
        return miningProfileKey;
    }

    @Override
    public MiningProfile clone() throws CloneNotSupportedException {
        return (MiningProfile) super.clone();
    }
}
