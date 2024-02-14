package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.BooleanProperties;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.LightWeaponsSkill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class LightWeaponsProfile extends Profile {
    {
        floatStat("damageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("powerAttackDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create()); // "overhead" hits are what Minecraft normally considers "critical hits", aka falling while hitting. These hits can now do configurable damage, a fraction of which is shared to other mobs in the area.
        floatStat("powerAttackFraction", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("powerAttackRadius", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("attackSpeedMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("attackReachBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P2).perkReward().create());
        floatStat("attackReachMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("knockbackMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("damageToLightArmorMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("damageToHeavyArmorMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("penetrationFlatLight", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());
        floatStat("penetrationFlatHeavy", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());
        floatStat("penetrationFlat", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());
        floatStat("penetrationFractionLight", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("penetrationFractionHeavy", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("penetrationFraction", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("immunityReductionFraction", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bleedChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bleedDamage", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("critChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("critDamage", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("stunChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("coatingDurationMultiplier", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("coatingAmplifierMultiplier", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("dropsMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("rareDropsMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("shieldDisarming", new PropertyBuilder().format(StatFormat.DIFFERENCE_TIME_SECONDS_BASE_20_P1).perkReward().create());
        floatStat("lifeSteal", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P2).perkReward().create());

        floatStat("parryDamageReduction", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("parryCooldownSuccessReduction", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        intStat("bleedDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("stunDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("parryEffectiveDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("parryVulnerableDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("parryCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("parryEnemyDebuffDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("parrySelfDebuffDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("coatingCharges", new PropertyBuilder().format(StatFormat.INT).perkReward().create());

        booleanStat("bleedOnCrit", new BooleanProperties(true, true));
        booleanStat("critOnBleed", new BooleanProperties(true, true));
        booleanStat("critOnStealth", new BooleanProperties(true, true));
        booleanStat("stunOnCrit", new BooleanProperties(true, true));
        booleanStat("critOnStun", new BooleanProperties(true, true));
        booleanStat("coatingUnlocked", new BooleanProperties(true, true));

        doubleStat("lightWeaponsEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
    }

    public float getShieldDisarming() { return getFloat("shieldDisarming"); }
    public void setShieldDiarming(float value) { setFloat("shieldDisarming", value); }

    public float getLifeSteal() { return getFloat("lifeSteal"); }
    public void setLifeSteal(float value) { setFloat("lifeSteal", value); }

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

    public boolean isCoatingUnlocked() { return getBoolean("coatingUnlocked"); }
    public void setCoatingUnlocked(boolean value) { setBoolean("coatingUnlocked", value); }

    public int getBleedDuration(){ return getInt("bleedDuration");}
    public void setBleedDuration(int value){ setInt("bleedDuration", value);}

    public int getStunDuration(){ return getInt("stunDuration");}
    public void setStunDuration(int value){ setInt("stunDuration", value);}

    public int getParryEffectiveDuration(){ return getInt("parryEffectiveDuration");}
    public void setParryEffectiveDuration(int value){ setInt("parryEffectiveDuration", value);}

    public int getParryVulnerableDuration(){ return getInt("parryVulnerableDuration");}
    public void setParryVulnerableDuration(int value){ setInt("parryVulnerableDuration", value);}

    public int getParryCooldown(){ return getInt("parryCooldown");}
    public void setParryCooldown(int value){ setInt("parryCooldown", value);}

    public int getParryEnemyDebuffDuration(){ return getInt("parryEnemyDebuffDuration");}
    public void setParryEnemyDebuffDuration(int value){ setInt("parryEnemyDebuffDuration", value);}

    public int getParrySelfDebuffDuration(){ return getInt("parrySelfDebuffDuration");}
    public void setParrySelfDebuffDuration(int value){ setInt("parrySelfDebuffDuration", value);}

    public int getCoatingCharges(){ return getInt("coatingCharges");}
    public void setCoatingCharges(int value){ setInt("coatingCharges", value);}

    public float getDamageMultiplier() { return getFloat("damageMultiplier"); }
    public void setDamageMultiplier(float value) { setFloat("damageMultiplier", value); }

    public float getPowerAttackDamageMultiplier() { return getFloat("overheadDamageMultiplier"); }
    public void setPowerAttackDamageMultiplier(float value) { setFloat("overheadDamageMultiplier", value); }
    public float getAttackReachMultiplier() { return getFloat("attackReachMultiplier"); }
    public void setAttackReachMultiplier(float value) { setFloat("attackReachMultiplier", value); }

    public float getPowerAttackFraction() { return getFloat("overheadFraction"); }
    public void setPowerAttackFraction(float value) { setFloat("overheadFraction", value); }

    public float getParryDamageReduction() { return getFloat("parryDamageReduction"); }
    public void setParryDamageReduction(float value) { setFloat("parryDamageReduction", value); }

    public float getParryCooldownSuccessReduction() { return getFloat("parryCooldownSuccessReduction"); }
    public void setParryCooldownSuccessReduction(float value) { setFloat("parryCooldownSuccessReduction", value); }

    public float getPowerAttackRadius() { return getFloat("overheadRadius"); }
    public void setPowerAttackRadius(float value) { setFloat("overheadRadius", value); }

    public float getAttackSpeedMultiplier() { return getFloat("attackSpeedMultiplier"); }
    public void setAttackSpeedMultiplier(float value) { setFloat("attackSpeedMultiplier", value); }

    public float getKnockbackMultiplier() { return getFloat("knockbackMultiplier"); }
    public void setKnockbackMultiplier(float value) { setFloat("knockbackMultiplier", value); }

    public float getDamageToLightArmorMultiplier() { return getFloat("damageToLightArmorMultiplier"); }
    public void setDamageToLightArmorMultiplier(float value) { setFloat("damageToLightArmorMultiplier", value); }

    public float getDamageToHeavyArmorMultiplier() { return getFloat("damageToHeavyArmorMultiplier"); }
    public void setDamageToHeavyArmorMultiplier(float value) { setFloat("damageToHeavyArmorMultiplier", value); }

    public float getPenetrationFlatLight() { return getFloat("penetrationFlatLight"); }
    public void setPenetrationFlatLight(float value) { setFloat("penetrationFlatLight", value); }

    public float getPenetrationFlatHeavy() { return getFloat("penetrationFlatHeavy"); }
    public void setPenetrationFlatHeavy(float value) { setFloat("penetrationFlatHeavy", value); }

    public float getPenetrationFlat() { return getFloat("penetrationFlat"); }
    public void setPenetrationFlat(float value) { setFloat("penetrationFlat", value); }

    public float getPenetrationFractionLight() { return getFloat("penetrationFractionLight"); }
    public void setPenetrationFractionLight(float value) { setFloat("penetrationFractionLight", value); }

    public float getPenetrationFractionHeavy() { return getFloat("penetrationFractionHeavy"); }
    public void setPenetrationFractionHeavy(float value) { setFloat("penetrationFractionHeavy", value); }

    public float getPenetrationFraction() { return getFloat("penetrationFraction"); }
    public void setPenetrationFraction(float value) { setFloat("penetrationFraction", value); }

    public float getImmunityReductionFraction() { return getFloat("immunityReductionFraction"); }
    public void setImmunityReductionFraction(float value) { setFloat("immunityReductionFraction", value); }

    public float getBleedChance() { return getFloat("bleedChance"); }
    public void setBleedChance(float value) { setFloat("bleedChance", value); }

    public float getBleedDamage() { return getFloat("bleedDamage"); }
    public void setBleedDamage(float value) { setFloat("bleedDamage", value); }

    public float getCritChance() { return getFloat("critChance"); }
    public void setCritChance(float value) { setFloat("critChance", value); }

    public float getCritDamage() { return getFloat("critDamage"); }
    public void setCritDamage(float value) { setFloat("critDamage", value); }

    public float getStunChance() { return getFloat("stunChance"); }
    public void setStunChance(float value) { setFloat("stunChance", value); }

    public float getCoatingDurationMultiplier() { return getFloat("coatingDurationMultiplier"); }
    public void setCoatingDurationMultiplier(float value) { setFloat("coatingDurationMultiplier", value); }

    public float getCoatingAmplifierMultiplier() { return getFloat("coatingAmplifierMultiplier"); }
    public void setCoatingAmplifierMultiplier(float value) { setFloat("coatingAmplifierMultiplier", value); }

    public float getDropsMultiplier() { return getFloat("dropsMultiplier"); }
    public void setDropsMultiplier(float value) { setFloat("dropsMultiplier", value); }

    public float getRareDropsMultiplier() { return getFloat("rareDropsMultiplier"); }
    public void setRareDropsMultiplier(float value) { setFloat("rareDropsMultiplier", value); }

    public double getLightWeaponsEXPGain(){ return getDouble("lightWeaponsEXPMultiplier");}
    public void setLightWeaponsEXPGain(double value){ setDouble("lightWeaponsEXPMultiplier", value);}

    public LightWeaponsProfile(Player owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
        return "profiles_light_weapons";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "profile_light_weapons");

    @Override
    public LightWeaponsProfile getBlankProfile(Player owner) {
        return new LightWeaponsProfile(owner);
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return LightWeaponsSkill.class;
    }
}
