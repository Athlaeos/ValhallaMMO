package me.athlaeos.valhallammo.version.conversion_dto;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FarmingProfile extends Profile implements Serializable {
    private static final NamespacedKey farmingProfileKey = ValhallaMMO.key("valhalla_profile_farming");

    private float raredropratemultiplier = 1F; // rare drop rate multiplier for crops
    private float dropmultiplier = 1F; // drop multiplier for crops
    private float animaldropmultiplier = 1F; // drop multiplier for animals
    private float animalraredropratemultiplier = 1F; // rare drop rate multiplier for killed animals
    private boolean instantharvesting = false; // if true, players can right click crops to harvest them and immediately replant them
    private float instantgrowthrate = 0F; // the amount of stages crops grow immediately upon planting
    private float fishingtimemultiplier = 1F; // the duration multiplier of fishing hooks until they catch a fish
    private float fishingrewardtier = 0F; // the reward tier of rewards the player fishes up
    private float farmingvanillaexpreward = 0F; // (vanilla) exp rewarded for harvesting fully grown crops
    private float breedingvanillaexpmultiplier = 1F; // (vanilla) experience multiplier for experience gained by breeding animals
    private float fishingvanillaexpmultiplier = 1F; // (vanilla) experience multiplier for experience gained by fishing
    private float babyanimalagemultiplier = 1F; // multiplier of baby animal age when born, causing them to mature faster or slower
    private float hivehoneysavechance = 0F; // chance for hives to not consume honey when harvested (either with shears or bottles)
    private boolean hivebeeaggroimmunity = false;
    private int ultraharvestingcooldown = -1; // cooldown of the ultra harvesting ability, which harvests a large area of crops at once (similar to veinminer except for crops). if -1, ability is unusable
    private float animaldamagemultiplier = 1F; // damage multiplier against livestock animals (cows, chickens, pigs, sheep, etc. NOT neutral animals like wolves or hoglins)

    private boolean isbadfoodimmune = false; // determines if the player is immune to the negative effects of foods like rotten flesh or pufferfish
    private float carnivoroushungermultiplier = 1F; // food/saturation multipliers of eating meat
    private float pescotarianhungermultiplier = 1F; // food/saturation multipliers of eating fish
    private float vegetarianhungermultiplier = 1F; // food/saturation multipliers of eating non-meat foods (plant-based, but including things like honey/cake)
    private float garbagehungermultiplier = 1F; // food/saturation multipliers of eating garbage (things that are usually not meant to be eaten like spider eyes and rotten flesh)
    private float magicalhungermultiplier = 1F; // food/saturation multipliers of eating magical foods like golden apples or golden carrots

    private double generalexpmultiplier = 100D;
    private double farmingexpmultiplier = 100D;
    private double breedingexpmultiplier = 100D;
    private double fishingexpmultiplier = 100D;

    @Override
    public Profile fetchProfile(Player p, DatabaseConnection conn) throws SQLException {
        PreparedStatement stmt = conn.getConnection().prepareStatement("SELECT * FROM profiles_farming WHERE owner = ?;");
        stmt.setString(1, p.getUniqueId().toString());
        ResultSet result = stmt.executeQuery();
        if (result.next()){
            FarmingProfile profile = new FarmingProfile(p);
            profile.setLevel(result.getInt("level"));
            profile.setExp(result.getDouble("exp"));
            profile.setLifetimeEXP(result.getDouble("exp_total"));
            result.getFloat("raredropratemultiplier");
            if (result.wasNull()) return null;
            return profile;
        }
        return null;
    }

    public FarmingProfile(Player owner){
        super(owner);
        if (owner == null) return;
        this.key = farmingProfileKey;
    }

    @Override
    public NamespacedKey getKey() {
        return farmingProfileKey;
    }

    @Override
    public FarmingProfile clone() throws CloneNotSupportedException {
        return (FarmingProfile) super.clone();
    }
}
