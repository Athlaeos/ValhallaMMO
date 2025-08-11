package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.DiggingSkill;
import org.bukkit.NamespacedKey;

import java.util.UUID;

@SuppressWarnings("unused")
public class DiggingProfile extends Profile {
    {
        floatStat("diggingDrops", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("diggingLuck", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("diggingSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("blockExperienceRate", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());

        floatStat("archaeologyRepeatChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create()); // chance for suspicious block to regenerate after brushing
        floatStat("archaeologyLuck", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create()); // extra luck for archaeology loot tables
        floatStat("archaeologySandGenerationChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P6).perkReward().create()); // chance for adjacent blocks to a mined block to turn into suspicious sand
        floatStat("archaeologyGravelGenerationChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P6).perkReward().create()); // same with gravel
        floatStat("archaeologySandNearStructureGenerationChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P6).perkReward().create()); // same as generation chance, but only if block is near a structure
        floatStat("archaeologyGravelNearStructureGenerationChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P6).perkReward().create()); // same with gravel
        floatStat("archaeologyDefaultRareLootChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create()); // chance for generated brushable block to contain a rare loot table if no other is specified

        doubleStat("diggingEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
    }

    public float getDiggingDrops() { return getFloat("diggingDrops"); }
    public void setDiggingDrops(float value) { setFloat("diggingDrops", value); }
    public float getDiggingLuck() { return getFloat("diggingLuck"); }
    public void setDiggingLuck(float value) { setFloat("diggingLuck", value); }
    public float getDiggingSpeedBonus() { return getFloat("diggingSpeedBonus"); }
    public void setDiggingSpeedBonus(float value) { setFloat("diggingSpeedBonus", value); }
    public float getBlockExperienceRate() { return getFloat("blockExperienceRate"); }
    public void setBlockExperienceRate(float value) { setFloat("blockExperienceRate", value); }

    public float getArchaeologyRepeatChance() { return getFloat("archaeologyRepeatChance"); }
    public void setArchaeologyRepeatChance(float value) { setFloat("archaeologyRepeatChance", value); }
    public float getArchaeologyLuck() { return getFloat("archaeologyLuck"); }
    public void setArchaeologyLuck(float value) { setFloat("archaeologyLuck", value); }
    public float getArchaeologySandGenerationChance() { return getFloat("archaeologySandGenerationChance"); }
    public void setArchaeologySandGenerationChance(float value) { setFloat("archaeologySandGenerationChance", value); }
    public float getArchaeologyGravelGenerationChance() { return getFloat("archaeologyGravelGenerationChance"); }
    public void setArchaeologyGravelGenerationChance(float value) { setFloat("archaeologyGravelGenerationChance", value); }
    public float getArchaeologySandNearStructureGenerationChance() { return getFloat("archaeologySandNearStructureGenerationChance"); }
    public void setArchaeologySandNearStructureGenerationChance(float value) { setFloat("archaeologySandNearStructureGenerationChance", value); }
    public float getArchaeologyGravelNearStructureGenerationChance() { return getFloat("archaeologyGravelNearStructureGenerationChance"); }
    public void setArchaeologyGravelNearStructureGenerationChance(float value) { setFloat("archaeologyGravelNearStructureGenerationChance", value); }
    public float getArchaeologyDefaultRareLootChance() { return getFloat("archaeologyDefaultRareLootChance"); }
    public void setArchaeologyDefaultRareLootChance(float value) { setFloat("archaeologyDefaultRareLootChance", value); }

    public double getDiggingEXPMultiplier(){ return getDouble("diggingEXPMultiplier");}
    public void setDiggingEXPMultiplier(double value){ setDouble("diggingEXPMultiplier", value);}

    public DiggingProfile(UUID owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
        return "profiles_digging";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "profile_digging");

    @Override
    public Profile getBlankProfile(UUID owner) {
        return ProfileRegistry.copyDefaultStats(new DiggingProfile(owner));
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return DiggingSkill.class;
    }
}
