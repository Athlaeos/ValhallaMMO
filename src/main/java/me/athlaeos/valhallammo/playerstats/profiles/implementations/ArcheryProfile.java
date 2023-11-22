package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.BooleanProperties;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.ArcherySkill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class ArcheryProfile extends Profile {
    {
        floatStat("bowDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("crossbowDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bowCritChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("crossbowCritChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("ammoSaveChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("critDamage", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("inaccuracy", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());
        floatStat("distanceDamageBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("distanceDamageBase", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bowStunChance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("crossbowStunChance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        intStat("stunDuration", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        floatStat("infinityDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());

        booleanStat("chargedShotUnlocked", new BooleanProperties(true, true));
        intStat("chargedShotCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("chargedShotCharges", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        intStat("chargedShotPiercing", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        intStat("chargedShotKnockback", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        floatStat("chargedShotDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("chargedShotAccuracy", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("chargedShotVelocityBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        booleanStat("chargedShotFullVelocity", new BooleanProperties(true, true));
        booleanStat("chargedShotCrossbowInstantReload", new BooleanProperties(true, true));
        booleanStat("chargedShotNoGravity", new BooleanProperties(true, true));

        booleanStat("bleedOnCrit", new BooleanProperties(true, true));
        booleanStat("critOnBleed", new BooleanProperties(true, true));
        booleanStat("critOnStealth", new BooleanProperties(true, true));
        booleanStat("stunOnCrit", new BooleanProperties(true, true));
        booleanStat("critOnStun", new BooleanProperties(true, true));

        doubleStat("archeryEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
    }

    public boolean isChargedShotAntiGravity() { return getBoolean("chargedShotNoGravity"); }
    public void setChargedShotAntiGravity(boolean value) { setBoolean("chargedShotNoGravity", value); }

    public boolean isChargedShotUnlocked() { return getBoolean("chargedShotUnlocked"); }
    public void setChargedShotUnlocked(boolean value) { setBoolean("chargedShotUnlocked", value); }

    public boolean doesBleedOnCrit() { return getBoolean("bleedOnCrit"); }
    public void setBleedOnCrit(boolean value) { setBoolean("bleedOnCrit", value); }

    public boolean doesCritOnBleed() { return getBoolean("critOnBleed"); }
    public void setCritOnBleed(boolean value) { setBoolean("critOnBleed", value); }

    public boolean doesCritOnStealth() { return getBoolean("critOnStealth"); }
    public void setCritOnStealth(boolean value) { setBoolean("critOnStealth", value); }

    public boolean doesStunOnCrit() { return getBoolean("stunOnCrit"); }
    public void setStunOnCrit(boolean value) { setBoolean("stunOnCrit", value); }

    public boolean doesCritOnStun() { return getBoolean("critOnStun"); }
    public void setCritOnStun(boolean value) { setBoolean("critOnStun", value); }

    public boolean doChargedShotsFireAtFullVelocity() { return getBoolean("chargedShotFullVelocity"); }
    public void setChargedShotFullVelocity(boolean value) { setBoolean("chargedShotFullVelocity", value); }

    public boolean doChargedShotCrossbowsInstantlyReload() { return getBoolean("chargedShotCrossbowInstantReload"); }
    public void setChargedShotCrossbowInstantReload(boolean value) { setBoolean("chargedShotCrossbowInstantReload", value); }

    public int getStunDuration() { return getInt("stunDuration"); }
    public void setStunDuration(int value) { setInt("stunDuration", value); }

    public int getChargedShotCooldown() { return getInt("chargedShotCooldown"); }
    public void setChargedShotCooldown(int value) { setInt("chargedShotCooldown", value); }

    public int getChargedShotCharges() { return getInt("chargedShotCharges"); }
    public void setChargedShotCharges(int value) { setInt("chargedShotCharges", value); }

    public int getChargedShotPiercing() { return getInt("chargedShotPiercing"); }
    public void setChargedShotPiercing(int value) { setInt("chargedShotPiercing", value); }

    public int getChargedShotKnockback() { return getInt("chargedShotKnockback"); }
    public void setChargedShotKnockback(int value) { setInt("chargedShotKnockback", value); }
    
    public float getBowDamageMultiplier() { return getFloat("bowDamageMultiplier"); }
    public void setBowDamageMultiplier(float value) { setFloat("bowDamageMultiplier", value); }

    public float getCrossbowDamageMultiplier() { return getFloat("crossbowDamageMultiplier"); }
    public void setCrossbowDamageMultiplier(float value) { setFloat("crossbowDamageMultiplier", value); }

    public float getBowCritChance() { return getFloat("bowCritChance"); }
    public void setBowCritChance(float value) { setFloat("bowCritChance", value); }

    public float getCrossbowCritChance() { return getFloat("crossbowCritChance"); }
    public void setCrossbowCritChance(float value) { setFloat("crossbowCritChance", value); }

    public float getAmmoSaveChance() { return getFloat("ammoSaveChance"); }
    public void setAmmoSaveChance(float value) { setFloat("ammoSaveChance", value); }

    public float getCritDamage() { return getFloat("critDamage"); }
    public void setCritDamage(float value) { setFloat("critDamage", value); }

    public float getInaccuracy() { return getFloat("inaccuracy"); }
    public void setInaccuracy(float value) { setFloat("inaccuracy", value); }

    public float getDistanceDamageBonus() { return getFloat("distanceDamageBonus"); }
    public void setDistanceDamageBonus(float value) { setFloat("distanceDamageBonus", value); }

    public float getDistanceDamageBase() { return getFloat("distanceDamageBase"); }
    public void setDistanceDamageBase(float value) { setFloat("distanceDamageBase", value); }

    public float getBowStunChance() { return getFloat("bowStunChance"); }
    public void setBowStunChance(float value) { setFloat("bowStunChance", value); }

    public float getCrossbowStunChance() { return getFloat("crossbowStunChance"); }
    public void setCrossbowStunChance(float value) { setFloat("crossbowStunChance", value); }

    public float getInfinityDamageMultiplier() { return getFloat("infinityDamageMultiplier"); }
    public void setInfinityDamageMultiplier(float value) { setFloat("infinityDamageMultiplier", value); }

    public float getChargedShotDamageMultiplier() { return getFloat("chargedShotDamageMultiplier"); }
    public void setChargedShotDamageMultiplier(float value) { setFloat("chargedShotDamageMultiplier", value); }

    public float getChargedShotAccuracy() { return getFloat("chargedShotAccuracy"); }
    public void setChargedShotAccuracy(float value) { setFloat("chargedShotAccuracy", value); }

    public float getChargedShotVelocityBonus() { return getFloat("chargedShotVelocityBonus"); }
    public void setChargedShotVelocityBonus(float value) { setFloat("chargedShotVelocityBonus", value); }

    public double getArcheryEXPMultiplier(){ return getDouble("archeryEXPMultiplier");}
    public void setArcheryEXPMultiplier(double value){ setDouble("archeryEXPMultiplier", value);}

    public ArcheryProfile(Player owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
        return "profiles_archery";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "profile_archery");

    @Override
    public ArcheryProfile getBlankProfile(Player owner) {
        return new ArcheryProfile(owner);
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return ArcherySkill.class;
    }
}
