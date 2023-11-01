package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.BooleanProperties;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.MiningSkill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Collection;

@SuppressWarnings("unused")
public class MiningProfile extends Profile {
    {
        floatStat("miningDrops", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("miningLuck", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("blastingDrops", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("blastingLuck", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("blockExperienceRate", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("blockExperienceMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("tntBlastRadius", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("tntDamageReduction", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("miningSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        stringSetStat("unbreakableBlocks");
        intStat("blastFortuneLevel", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        booleanStat("blastingInstantPickup", new BooleanProperties(true, true));

        booleanStat("veinMiningUnlocked", new BooleanProperties(true, true));
        booleanStat("veinMiningInstantPickup", new BooleanProperties(true, true));
        intStat("veinMiningCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        stringSetStat("veinMinerValidBlocks");

        booleanStat("drillingUnlocked", new BooleanProperties(true, true));
        floatStat("drillingSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        intStat("drillingCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("drillingDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());

        doubleStat("miningEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
    }

    public boolean isVeinMiningUnlocked() { return getBoolean("veinMiningUnlocked"); }
    public void setVeinMiningUnlocked(boolean value) { setBoolean("veinMiningUnlocked", value); }
    public boolean isDrillingUnlocked() { return getBoolean("drillingUnlocked"); }
    public void setDrillingUnlocked(boolean value) { setBoolean("drillingUnlocked", value); }
    public boolean isBlastingInstantPickup() { return getBoolean("blastingInstantPickup"); }
    public void setBlastingInstantPickup(boolean value) { setBoolean("blastingInstantPickup", value); }
    public boolean isVeinMiningInstantPickup() { return getBoolean("veinMiningInstantPickup"); }
    public void setVeinMiningInstantPickup(boolean value) { setBoolean("veinMiningInstantPickup", value); }

    public int getBlastFortuneLevel() { return getInt("blastFortuneLevel"); }
    public void setBlastFortuneLevel(int value) { setInt("blastFortuneLevel", value); }
    public int getVeinMiningCooldown() { return getInt("veinMiningCooldown"); }
    public void setVeinMiningCooldown(int value) { setInt("veinMiningCooldown", value); }
    public int getDrillingCooldown() { return getInt("drillingCooldown"); }
    public void setDrillingCooldown(int value) { setInt("drillingCooldown", value); }
    public int getDrillingDuration() { return getInt("drillingDuration"); }
    public void setDrillingDuration(int value) { setInt("drillingDuration", value); }

    public Collection<String> getUnbreakableBlocks() { return getStringSet("unbreakableBlocks"); }
    public void setUnbreakableBlocks(Collection<String> value) { setStringSet("unbreakableBlocks", value); }
    public Collection<String> getVeinMinerValidBlocks() { return getStringSet("veinMinerValidBlocks"); }
    public void setVeinMinerValidBlocks(Collection<String> value) { setStringSet("veinMinerValidBlocks", value); }

    public float getMiningDrops() { return getFloat("miningDrops"); }
    public void setMiningDrops(float value) { setFloat("miningDrops", value); }
    public float getMiningLuck() { return getFloat("miningLuck"); }
    public void setMiningLuck(float value) { setFloat("miningLuck", value); }
    public float getBlastingDrops() { return getFloat("blastingDrops"); }
    public void setBlastingDrops(float value) { setFloat("blastingDrops", value); }
    public float getBlastingLuck() { return getFloat("blastingLuck"); }
    public void setBlastingLuck(float value) { setFloat("blastingLuck", value); }
    public float getTntBlastRadius() { return getFloat("tntBlastRadius"); }
    public void setTntBlastRadius(float value) { setFloat("tntBlastRadius", value); }
    public float getTntDamageReduction() { return getFloat("tntDamageReduction"); }
    public void setTntDamageReduction(float value) { setFloat("tntDamageReduction", value); }
    public float getMiningSpeedBonus() { return getFloat("miningSpeedBonus"); }
    public void setMiningSpeedBonus(float value) { setFloat("miningSpeedBonus", value); }
    public float getDrillingSpeedBonus() { return getFloat("drillingSpeedBonus"); }
    public void setDrillingSpeedBonus(float value) { setFloat("drillingSpeedBonus", value); }
    public float getBlockExperienceRate() { return getFloat("blockExperienceRate"); }
    public void setBlockExperienceRate(float value) { setFloat("blockExperienceRate", value); }
    public float getBlockExperienceMultiplier() { return getFloat("blockExperienceMultiplier"); }
    public void setBlockExperienceMultiplier(float value) { setFloat("blockExperienceMultiplier", value); }

    public double getMiningEXPMultiplier(){ return getDouble("miningEXPMultiplier");}
    public void setMiningEXPMultiplier(double value){ setDouble("miningEXPMultiplier", value);}

    public MiningProfile(Player owner) {
        super(owner);
    }

    @Override
    protected String getTableName() {
        return "profiles_mining";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "profile_mining");

    @Override
    public MiningProfile getBlankProfile(Player owner) {
        return new MiningProfile(owner);
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return MiningSkill.class;
    }
}
