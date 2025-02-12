package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.BooleanProperties;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Collection;

@SuppressWarnings("unused")
public class MartialArtsProfile extends Profile {
    {
        floatStat("damageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("powerAttackDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create()); // "overhead" hits are what Minecraft normally considers "critical hits", aka falling while hitting. These hits can now do configurable damage, a fraction of which is shared to other mobs in the area.
        floatStat("powerAttackFraction", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("powerAttackRadius", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());

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
        floatStat("dropsMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("rareDropsMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("shieldDisarming", new PropertyBuilder().format(StatFormat.DIFFERENCE_TIME_SECONDS_BASE_20_P1).perkReward().create());
        floatStat("lifeSteal", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P2).perkReward().create());

        intStat("bleedDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("stunDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());

        booleanStat("bleedOnCrit", new BooleanProperties(true, true));
        booleanStat("critOnBleed", new BooleanProperties(true, true));
        booleanStat("critOnStealth", new BooleanProperties(true, true));
        booleanStat("stunOnCrit", new BooleanProperties(true, true));
        booleanStat("critOnStun", new BooleanProperties(true, true));

        doubleStat("martialArtsEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());

        /*
         * Grappling is an ability where the user repeatedly right-clicks an entity, applying debuffs
         * at the rate of interval to the entity and buffing themselves. The idea is that the martial
         * artist this way is attempting to take control over their attacker by manipulating
         * their movement. The grappler cannot attack while grappling, as this prevents them further grappling
         * for some time after.
         *
         * Reaching full stacks of grappling disarms the attacker, preventing them from using their current weapon
         * and inflicting further debuffs.
         *
         * Grappling power determines if other material artists can grapple you or not, missed punches
         * briefly make martial artists vulnerable to being grappled by setting their grappling power to 0
         */
        booleanStat("grapplingUnlocked", new BooleanProperties(true, true));
        floatStat("grapplingPower", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());
        intStat("grapplingEffectInterval", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("grapplingAttackCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("stacksUntilDisarming", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        intStat("stackDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("disarmingDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        stringSetStat("grapplingEnemyEffects"); // To be formatted like EFFECT:DURATION:AMPLIFIER:MAXSTACKS
        stringSetStat("grapplingSelfEffects"); // Same formatting
        stringSetStat("disarmingDebuffs"); // To be formatted like EFFECT:DURATION:AMPLIFIER
        stringSetStat("disarmingBuffs"); // Same formatting
        intStat("disarmingCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());

        /*
         * Delayed punches work by recording the time since the last attempted punch.
         * The martial artist gains bonus attack damage the longer it has been since their last
         * punch, up to a maximum. This is to make it so that spam punching is less efficient
         */
        floatStat("damageBonus", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());
        intStat("delayedPunchTimeMinimum", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("delayedPunchDamageCap", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        floatStat("delayedPunchDamagePerTick", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());

        /*
         * Martial artists get additional dodge chance while not holding weapons and not attacking, which is
         * further multiplied if they're not wearing any armor
         */
        intStat("attackDodgeChanceCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        floatStat("baseDodgeChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("defenselessDodgeChanceMultiplier", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());

        /*
         * Martial artists may meditate by right-clicking on a valid sitting material to sit still on it for a given duration.
         * If there's an elevation requirement or skylight requirement, the martial artist must meet them as well
         * Fully meditating grants the user several long-lasting buffs
         */
        booleanStat("meditationUnlocked", new BooleanProperties(true, true));
        stringSetStat("meditationBuffs");
        intStat("meditationElevationRequirement", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        stringSetStat("meditationSittingMaterials");
        booleanStat("meditationSkyLightRequirement", new BooleanProperties(false, true));
        intStat("meditationCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());

        booleanStat("uppercutUnlocked", new BooleanProperties(true, true));
        floatStat("uppercutKnockUpStrength", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("uppercutDamage", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());
        intStat("uppercutPVECooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("uppercutPVPCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());

        booleanStat("dropKickUnlocked", new BooleanProperties(true, true));
        floatStat("dropKickKnockBackStrength", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("dropKickDamage", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());
        floatStat("dropKickWallHitDamage", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());
        floatStat("dropKickWallHitDamagePerVelocity", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());
        stringSetStat("dropKickDamageType");
    }

    public Collection<String> getDropKickDamageType() { return getStringSet("dropKickDamageType"); }
    public void setDropKickDamageType(Collection<String> value) { setStringSet("dropKickDamageType", value); }

    public Collection<String> getGrapplingEnemyEffects() { return getStringSet("grapplingEnemyEffects"); }
    public void setGrapplingEnemyEffects(Collection<String> value) { setStringSet("grapplingEnemyEffects", value); }

    public Collection<String> getGrapplingSelfEffects() { return getStringSet("grapplingSelfEffects"); }
    public void setGrapplingSelfEffects(Collection<String> value) { setStringSet("grapplingSelfEffects", value); }

    public Collection<String> getDisarmingDebuffs() { return getStringSet("disarmingDebuffs"); }
    public void setDisarmingDebuffs(Collection<String> value) { setStringSet("disarmingDebuffs", value); }

    public Collection<String> getMeditationBuffs() { return getStringSet("meditationBuffs"); }
    public void setMeditationBuffs(Collection<String> value) { setStringSet("meditationBuffs", value); }

    public Collection<String> getDisarmingBuffs() { return getStringSet("disarmingBuffs"); }
    public void setDisarmingBuffs(Collection<String> value) { setStringSet("disarmingBuffs", value); }

    public Collection<String> getMeditationSittingMaterials() { return getStringSet("meditationSittingMaterials"); }
    public void setMeditationSittingMaterials(Collection<String> value) { setStringSet("meditationSittingMaterials", value); }

    public boolean isUppercutUnlocked() { return getBoolean("uppercutUnlocked"); }
    public void setUppercutUnlocked(boolean value) { setBoolean("uppercutUnlocked", value); }

    public boolean isMeditationUnlocked() { return getBoolean("meditationUnlocked"); }
    public void setMeditationUnlocked(boolean value) { setBoolean("meditationUnlocked", value); }

    public boolean isDropKickUnlocked() { return getBoolean("dropKickUnlocked"); }
    public void setDropKickUnlocked(boolean value) { setBoolean("dropKickUnlocked", value); }

    public boolean isGrapplingUnlocked() { return getBoolean("grapplingUnlocked"); }
    public void setGrapplingUnlocked(boolean value) { setBoolean("grapplingUnlocked", value); }

    public boolean isMeditationSkyLightRequirement() { return getBoolean("meditationSkyLightRequirement"); }
    public void setMeditationSkyLightRequirement(boolean value) { setBoolean("meditationSkyLightRequirement", value); }

    public int getUppercutPVECooldown(){ return getInt("uppercutPVECooldown");}
    public void setUppercutPVECooldown(int value){ setInt("uppercutPVECooldown", value);}

    public int getUppercutPVPCooldown(){ return getInt("uppercutPVPCooldown");}
    public void setUppercutPVPCooldown(int value){ setInt("uppercutPVPCooldown", value);}

    public int getMeditationCooldown(){ return getInt("meditationCooldown");}
    public void setMeditationCooldown(int value){ setInt("meditationCooldown", value);}

    public int getDisarmingCooldown(){ return getInt("disarmingCooldown");}
    public void setDisarmingCooldown(int value){ setInt("disarmingCooldown", value);}

    public int getGrapplingEffectInterval(){ return getInt("grapplingEffectInterval");}
    public void setGrapplingEffectInterval(int value){ setInt("grapplingEffectInterval", value);}

    public int getGrapplingAttackCooldown(){ return getInt("grapplingAttackCooldown");}
    public void setGrapplingAttackCooldown(int value){ setInt("grapplingAttackCooldown", value);}

    public int getStackDuration(){ return getInt("stackDuration");}
    public void setStackDuration(int value){ setInt("stackDuration", value);}

    public int getStacksUntilDisarming(){ return getInt("stacksUntilDisarming");}
    public void setStacksUntilDisarming(int value){ setInt("stacksUntilDisarming", value);}

    public int getDisarmingDuration(){ return getInt("disarmingDuration");}
    public void setDisarmingDuration(int value){ setInt("disarmingDuration", value);}

    public int getDelayedPunchTimeMinimum(){ return getInt("delayedPunchTimeMinimum");}
    public void setDelayedPunchTimeMinimum(int value){ setInt("delayedPunchTimeMinimum", value);}

    public int getDelayedPunchDamageCap(){ return getInt("delayedPunchDamageCap");}
    public void setDelayedPunchDamageCap(int value){ setInt("delayedPunchDamageCap", value);}

    public int getAttackDodgeChanceCooldown(){ return getInt("attackDodgeChanceCooldown");}
    public void setAttackDodgeChanceCooldown(int value){ setInt("attackDodgeChanceCooldown", value);}

    public int getMeditationElevationRequirement(){ return getInt("meditationElevationRequirement");}
    public void setMeditationElevationRequirement(int value){ setInt("meditationElevationRequirement", value);}

    public float getUppercutKnockUpStrength() { return getFloat("uppercutKnockUpStrength"); }
    public void setUppercutKnockUpStrength(float value) { setFloat("uppercutKnockUpStrength", value); }

    public float getDropKickWallHitDamagePerVelocity() { return getFloat("dropKickWallHitDamagePerVelocity"); }
    public void setDropKickWallHitDamagePerVelocity(float value) { setFloat("dropKickWallHitDamagePerVelocity", value); }

    public float getUppercutDamage() { return getFloat("uppercutDamage"); }
    public void setUppercutDamage(float value) { setFloat("uppercutDamage", value); }

    public float getDropKickKnockBackStrength() { return getFloat("dropKickKnockBackStrength"); }
    public void setDropKickKnockBackStrength(float value) { setFloat("dropKickKnockBackStrength", value); }

    public float getDropKickDamage() { return getFloat("dropKickDamage"); }
    public void setDropKickDamage(float value) { setFloat("dropKickDamage", value); }

    public float getDropKickWallHitDamage() { return getFloat("dropKickWallHitDamage"); }
    public void setDropKickWallHitDamage(float value) { setFloat("dropKickWallHitDamage", value); }

    public float getDamageBonus() { return getFloat("damageBonus"); }
    public void setDamageBonus(float value) { setFloat("damageBonus", value); }

    public float getGrapplingPower() { return getFloat("grapplingPower"); }
    public void setGrapplingPower(float value) { setFloat("grapplingPower", value); }

    public float getDelayedPunchDamagePerTick() { return getFloat("delayedPunchDamagePerTick"); }
    public void setDelayedPunchDamagePerTick(float value) { setFloat("delayedPunchDamagePerTick", value); }

    public float getBaseDodgeChance() { return getFloat("baseDodgeChance"); }
    public void setBaseDodgeChance(float value) { setFloat("baseDodgeChance", value); }

    public float getDefenselessDodgeChanceMultiplier() { return getFloat("defenselessDodgeChanceMultiplier"); }
    public void setDefenselessDodgeChanceMultiplier(float value) { setFloat("defenselessDodgeChanceMultiplier", value); }

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

    public int getBleedDuration(){ return getInt("bleedDuration");}
    public void setBleedDuration(int value){ setInt("bleedDuration", value);}

    public int getStunDuration(){ return getInt("stunDuration");}
    public void setStunDuration(int value){ setInt("stunDuration", value);}

    public float getDamageMultiplier() { return getFloat("damageMultiplier"); }
    public void setDamageMultiplier(float value) { setFloat("damageMultiplier", value); }

    public float getPowerAttackDamageMultiplier() { return getFloat("powerAttackDamageMultiplier"); }
    public void setPowerAttackDamageMultiplier(float value) { setFloat("powerAttackDamageMultiplier", value); }
    public float getAttackReachMultiplier() { return getFloat("attackReachMultiplier"); }
    public void setAttackReachMultiplier(float value) { setFloat("attackReachMultiplier", value); }

    public float getPowerAttackFraction() { return getFloat("powerAttackFraction"); }
    public void setPowerAttackFraction(float value) { setFloat("powerAttackFraction", value); }

    public float getPowerAttackRadius() { return getFloat("powerAttackRadius"); }
    public void setPowerAttackRadius(float value) { setFloat("powerAttackRadius", value); }

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

    public float getDropsMultiplier() { return getFloat("dropsMultiplier"); }
    public void setDropsMultiplier(float value) { setFloat("dropsMultiplier", value); }

    public float getRareDropsMultiplier() { return getFloat("rareDropsMultiplier"); }
    public void setRareDropsMultiplier(float value) { setFloat("rareDropsMultiplier", value); }

    public double getMartialArtsEXPGain(){ return getDouble("martialArtsEXPMultiplier");}
    public void setMartialArtsEXPGain(double value){ setDouble("martialArtsEXPMultiplier", value);}

    public MartialArtsProfile(Player owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
        return "profiles_martial_arts";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "profile_martial_arts");

    @Override
    public MartialArtsProfile getBlankProfile(Player owner) {
        return new MartialArtsProfile(owner);
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return MartialArtsSkill.class;
    }
}
