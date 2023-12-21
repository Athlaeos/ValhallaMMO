package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.BooleanProperties;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.FishingSkill;
import me.athlaeos.valhallammo.skills.skills.implementations.MiningSkill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class FishingProfile extends Profile {
    {
        floatStat("fishingDrops", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("fishingLuck", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("fishingEssenceMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("fishingSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("baitSaveChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());

        doubleStat("fishingEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).min(0).perkReward().create());
    }

    public float getFishingDrops() { return getFloat("fishingDrops"); }
    public void setFishingDrops(float value) { setFloat("fishingDrops", value); }
    public float getFishingLuck() { return getFloat("fishingLuck"); }
    public void setFishingLuck(float value) { setFloat("fishingLuck", value); }
    public float getFishingEssenceMultiplier() { return getFloat("fishingEssenceMultiplier"); }
    public void setFishingEssenceMultiplier(float value) { setFloat("fishingEssenceMultiplier", value); }
    public float getFishingSpeedBonus() { return getFloat("fishingSpeedBonus"); }
    public void setFishingSpeedBonus(float value) { setFloat("fishingSpeedBonus", value); }
    public float getBaitSaveChance() { return getFloat("baitSaveChance"); }
    public void setBaitSaveChance(float value) { setFloat("baitSaveChance", value); }

    public double getFishingEXPMultiplier(){ return getDouble("fishingEXPMultiplier");}
    public void setFishingEXPMultiplier(double value){ setDouble("fishingEXPMultiplier", value);}

    public FishingProfile(Player owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
        return "profiles_fishing";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "profile_fishing");

    @Override
    public FishingProfile getBlankProfile(Player owner) {
        return new FishingProfile(owner);
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return FishingSkill.class;
    }
}
