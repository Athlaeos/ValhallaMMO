package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.properties.BooleanProperties;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.FarmingSkill;
import org.bukkit.NamespacedKey;

import java.util.UUID;

@SuppressWarnings("unused")
public class FarmingProfile extends Profile {
    {
        floatStat("farmingDrops", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("farmingLuck", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("farmingExperienceRate", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        booleanStat("instantHarvesting", new BooleanProperties(true, true));
        floatStat("instantGrowthRate", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("butcheryDrops", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("butcheryLuck", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("butcheryDamageMultiplier", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("growUpTimeMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("breedingExperienceMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("hiveHoneySaveChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create()); // impl
        booleanStat("beeAggroImmunity", new BooleanProperties(true, true));

        booleanStat("fieldHarvestUnlocked", new BooleanProperties(true, true));
        booleanStat("fieldHarvestInstantPickup", new BooleanProperties(true, true));
        intStat("fieldHarvestCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());

        doubleStat("farmingEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
    }

    public boolean isInstantHarvesting() { return getBoolean("instantHarvesting"); }
    public void setInstantHarvesting(boolean value) { setBoolean("instantHarvesting", value); }
    public boolean hasBeeAggroImmunity() { return getBoolean("beeAggroImmunity"); }
    public void setBeeAggroImmunity(boolean value) { setBoolean("beeAggroImmunity", value); }
    public boolean isFieldHarvestUnlocked() { return getBoolean("fieldHarvestUnlocked"); }
    public void setFieldHarvestUnlocked(boolean value) { setBoolean("fieldHarvestUnlocked", value); }
    public boolean isFieldHarvestInstantPickup() { return getBoolean("fieldHarvestInstantPickup"); }
    public void setFieldHarvestInstantPickup(boolean value) { setBoolean("fieldHarvestInstantPickup", value); }

    public int getFieldHarvestCooldown() { return getInt("fieldHarvestCooldown"); }
    public void setFieldHarvestCooldown(int value) { setInt("fieldHarvestCooldown", value); }

    public float getFarmingDrops() { return getFloat("farmingDrops"); }
    public void setFarmingDrops(float value) { setFloat("farmingDrops", value); }
    public float getFarmingLuck() { return getFloat("farmingLuck"); }
    public void setFarmingLuck(float value) { setFloat("farmingLuck", value); }
    public float getFarmingExperienceRate() { return getFloat("farmingExperienceRate"); }
    public void setFarmingExperienceRate(float value) { setFloat("farmingExperienceRate", value); }
    public float getInstantGrowthRate() { return getFloat("instantGrowthRate"); }
    public void setInstantGrowthRate(float value) { setFloat("instantGrowthRate", value); }
    public float getButcheryDrops() { return getFloat("butcheryDrops"); }
    public void setButcheryDrops(float value) { setFloat("butcheryDrops", value); }
    public float getButcheryLuck() { return getFloat("butcheryLuck"); }
    public void setButcheryLuck(float value) { setFloat("butcheryLuck", value); }
    public float getButcheryDamageMultiplier() { return getFloat("butcheryDamageMultiplier"); }
    public void setButcheryDamageMultiplier(float value) { setFloat("butcheryDamageMultiplier", value); }
    public float getGrowUpTimeMultiplier() { return getFloat("growUpTimeMultiplier"); }
    public void setGrowUpTimeMultiplier(float value) { setFloat("growUpTimeMultiplier", value); }
    public float getBreedingExpMultiplier() { return getFloat("breedingExperienceMultiplier"); }
    public void setBreedingExperienceMultiplier(float value) { setFloat("breedingExperienceMultiplier", value); }
    public float getHiveHoneySaveChance() { return getFloat("hiveHoneySaveChance"); }
    public void setHiveHoneySaveChance(float value) { setFloat("hiveHoneySaveChance", value); }

    public double getFarmingEXPMultiplier(){ return getDouble("farmingEXPMultiplier");}
    public void setFarmingEXPMultiplier(double value){ setDouble("farmingEXPMultiplier", value);}

    public FarmingProfile(UUID owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
        return "profiles_farming";
    }

    private static final NamespacedKey key = ValhallaMMO.key("profile_farming");

    @Override
    public Profile getBlankProfile(UUID owner) {
        return ProfileRegistry.copyDefaultStats(new FarmingProfile(owner));
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return FarmingSkill.class;
    }
}
