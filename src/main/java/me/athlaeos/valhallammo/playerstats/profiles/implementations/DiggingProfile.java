package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.DiggingSkill;
import me.athlaeos.valhallammo.skills.skills.implementations.MiningSkill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class DiggingProfile extends Profile {
    {
        floatStat("diggingDrops", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("diggingLuck", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("diggingSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("blockExperienceRate", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());

        doubleStat("diggingEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
    }

    public float getDiggingDrops() { return getFloat("diggingDrops"); }
    public void setDiggingDrops(float value) { setFloat("diggingDrops", value); }
    public float getDiggingLuck() { return getFloat("diggingLuck"); }
    public void setDiggingLuck(float value) { setFloat("diggingLuck", value); }
    public float getDiggingSpeedBonus() { return getFloat("diggingSpeedBonus"); }
    public void setDiggingSpeedBonus(float value) { setFloat("diggingSpeedBonus", value); }
    public float getBlockExperienceRate() { return getFloat("blockExperienceRate"); }
    public void setBlockExperienceRate(float value) { setFloat("blockExperienceRate", value); }

    public double getDiggingEXPMultiplier(){ return getDouble("diggingEXPMultiplier");}
    public void setDiggingEXPMultiplier(double value){ setDouble("diggingEXPMultiplier", value);}

    public DiggingProfile(Player owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
        return "profiles_digging";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "profile_digging");

    @Override
    public DiggingProfile getBlankProfile(Player owner) {
        return new DiggingProfile(owner);
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return DiggingSkill.class;
    }
}
