package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.BooleanProperties;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.MiningSkill;
import me.athlaeos.valhallammo.skills.skills.implementations.WoodcuttingSkill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Collection;

@SuppressWarnings("unused")
public class WoodcuttingProfile extends Profile {
    {
        floatStat("woodcuttingDrops", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("woodcuttingLuck", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("woodcuttingSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("blockExperienceRate", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("instantGrowthRate", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());

        booleanStat("treeCapitatorUnlocked", new BooleanProperties(true, true));
        booleanStat("treeCapitatorInstantPickup", new BooleanProperties(true, true));
        intStat("treeCapitatorCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("treeCapitatorLimit", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        stringSetStat("treeCapitatorValidBlocks");

        doubleStat("woodcuttingEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
    }

    public boolean isTreeCapitatorUnlocked() { return getBoolean("treeCapitatorUnlocked"); }
    public void setTreeCapitatorUnlocked(boolean value) { setBoolean("treeCapitatorUnlocked", value); }
    public boolean isTreeCapitatorInstantPickup() { return getBoolean("treeCapitatorInstantPickup"); }
    public void setTreeCapitatorInstantPickup(boolean value) { setBoolean("treeCapitatorInstantPickup", value); }

    public int getTreeCapitatorCooldown() { return getInt("treeCapitatorCooldown"); }
    public void setTreeCapitatorCooldown(int value) { setInt("treeCapitatorCooldown", value); }
    public int getTreeCapitatorLimit() { return getInt("treeCapitatorLimit"); }
    public void setTreeCapitatorLimit(int value) { setInt("treeCapitatorLimit", value); }

    public Collection<String> getTreeCapitatorValidBlocks() { return getStringSet("treeCapitatorValidBlocks"); }
    public void setTreeCapitatorValidBlocks(Collection<String> value) { setStringSet("treeCapitatorValidBlocks", value); }

    public float getWoodcuttingDrops() { return getFloat("woodcuttingDrops"); }
    public void setWoodcuttingDrops(float value) { setFloat("woodcuttingDrops", value); }
    public float getWoodcuttingLuck() { return getFloat("woodcuttingLuck"); }
    public void setWoodcuttingLuck(float value) { setFloat("woodcuttingLuck", value); }
    public float getWoodcuttingSpeedBonus() { return getFloat("woodcuttingSpeedBonus"); }
    public void setWoodcuttingSpeedBonus(float value) { setFloat("woodcuttingSpeedBonus", value); }
    public float getBlockExperienceRate() { return getFloat("blockExperienceRate"); }
    public void setBlockExperienceRate(float value) { setFloat("blockExperienceRate", value); }
    public float getInstantGrowthRate() { return getFloat("instantGrowthRate"); }
    public void setInstantGrowthRate(float value) { setFloat("instantGrowthRate", value); }

    public double getWoodcuttingEXPMultiplier(){ return getDouble("woodcuttingEXPMultiplier");}
    public void setWoodcuttingEXPMultiplier(double value){ setDouble("woodcuttingEXPMultiplier", value);}

    public WoodcuttingProfile(Player owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
        return "profiles_woodcutting";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "profile_woodcutting");

    @Override
    public WoodcuttingProfile getBlankProfile(Player owner) {
        return new WoodcuttingProfile(owner);
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return WoodcuttingSkill.class;
    }
}
