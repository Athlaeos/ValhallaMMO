package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.BooleanProperties;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.HeavyArmorSkill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Collection;

@SuppressWarnings("unused")
public class HeavyArmorProfile extends Profile {
    {
        floatStat("heavyArmorMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());

        floatStat("movementSpeedPerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("damageResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("meleeResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("projectileResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("fireResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bludgeoningResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("explosionResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("magicResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("poisonResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("radiantResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("necroticResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("freezingResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("lightningResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("fallDamageResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("critChanceResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("critDamageResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("stunResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bleedResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("knockbackResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("pvpResistancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());

        floatStat("armorBonusPerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("armorMultiplierPerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("hungerSaveChancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("dodgeChancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("reflectChancePerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("reflectFractionPerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("healingBonusPerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("immunityFractionBonusPerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("immunityFlatBonusPerPiece", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());

        floatStat("setMovementSpeed", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setDamageResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setMeleeResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setProjectileResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setFireResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setBludgeoningResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setExplosionResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setMagicResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setPoisonResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setRadiantResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setNecroticResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setFreezingResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setLightningResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setFallDamageResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setCritChanceResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setCritDamageResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setStunResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setBleedResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setKnockbackResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setPvPResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());

        floatStat("setArmorBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("setArmorMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setHungerSaveChance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setDodgeChance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setReflectChance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setReflectFraction", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setHealingBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setImmunityFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("setImmunityFlatBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());

        intStat("setAmount", 4, new PropertyBuilder().format(StatFormat.INT).perkReward().create());

        stringSetStat("setImmunePotionEffects");

        booleanStat("rageUnlocked", new BooleanProperties(true, true));
        intStat("rageLevel", new PropertyBuilder().format(StatFormat.ROMAN).perkReward().create());
        intStat("rageCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        floatStat("rageThreshold", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());

        doubleStat("heavyArmorEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
    }

    public Collection<String> getImmuneEffects() { return getStringSet("setImmunePotionEffects"); }
    public void setImmuneEffects(Collection<String> value) { setStringSet("setImmunePotionEffects", value); }

    public boolean isRageUnlocked() { return getBoolean("rageUnlocked"); }
    public void setRageUnlocked(boolean value) { setBoolean("rageUnlocked", value); }

    public int getRageLevel() { return getInt("rageLevel"); }
    public void setRageLevel(int value) { setInt("rageLevel", value); }

    public int getRageCooldown() { return getInt("rageCooldown"); }
    public void setRageCooldown(int value) { setInt("rageCooldown", value); }

    public float getRageThreshold() { return getFloat("rageThreshold"); }
    public void setRageThreshold(float value) { setFloat("rageThreshold", value); }

    public int getSetCount() { return getInt("setAmount"); }
    public void setSetCount(int value) { setInt("setAmount", value); }

    public float getMovementSpeed(boolean setBonus) { return setBonus ? getFloat("setMovementSpeed") : getFloat("movementSpeedPerPiece"); }
    public float getDamageResistance(boolean setBonus) { return setBonus ? getFloat("setDamageResistance") : getFloat("damageResistancePerPiece"); }
    public float getMeleeResistance(boolean setBonus) { return setBonus ? getFloat("setMeleeResistance") : getFloat("meleeResistancePerPiece"); }
    public float getProjectileResistance(boolean setBonus) { return setBonus ? getFloat("setProjectileResistance") : getFloat("projectileResistancePerPiece"); }
    public float getFireResistance(boolean setBonus) { return setBonus ? getFloat("setFireResistance") : getFloat("fireResistancePerPiece"); }
    public float getBludgeoningResistance(boolean setBonus) { return setBonus ? getFloat("setBludgeoningResistance") : getFloat("bludgeoningResistancePerPiece"); }
    public float getExplosionResistance(boolean setBonus) { return setBonus ? getFloat("setExplosionResistance") : getFloat("explosionResistancePerPiece"); }
    public float getMagicResistance(boolean setBonus) { return setBonus ? getFloat("setMagicResistance") : getFloat("magicResistancePerPiece"); }
    public float getPoisonResistance(boolean setBonus) { return setBonus ? getFloat("setPoisonResistance") : getFloat("poisonResistancePerPiece"); }
    public float getRadiantResistance(boolean setBonus) { return setBonus ? getFloat("setRadiantResistance") : getFloat("radiantResistancePerPiece"); }
    public float getNecroticResistance(boolean setBonus) { return setBonus ? getFloat("setNecroticResistance") : getFloat("necroticResistancePerPiece"); }
    public float getFreezingResistance(boolean setBonus) { return setBonus ? getFloat("setFreezingResistance") : getFloat("freezingResistancePerPiece"); }
    public float getLightningResistance(boolean setBonus) { return setBonus ? getFloat("setLightningResistance") : getFloat("lightningResistancePerPiece"); }
    public float getFallDamageResistance(boolean setBonus) { return setBonus ? getFloat("setFallDamageResistance") : getFloat("fallDamageResistancePerPiece"); }
    public float getCritChanceResistance(boolean setBonus) { return setBonus ? getFloat("setCritChanceResistance") : getFloat("critChanceResistancePerPiece"); }
    public float getCritDamageResistance(boolean setBonus) { return setBonus ? getFloat("setCritDamageResistance") : getFloat("critDamageResistancePerPiece"); }
    public float getStunResistance(boolean setBonus) { return setBonus ? getFloat("setStunResistance") : getFloat("stunResistancePerPiece"); }
    public float getBleedResistance(boolean setBonus) { return setBonus ? getFloat("setBleedResistance") : getFloat("bleedResistancePerPiece"); }
    public float getKnockbackResistance(boolean setBonus) { return setBonus ? getFloat("setKnockbackResistance") : getFloat("knockbackResistancePerPiece"); }
    public float getPvPResistance(boolean setBonus) { return setBonus ? getFloat("setPvPResistance") : getFloat("pvpResistancePerPiece"); }
    public float getArmorBonus(boolean setBonus) { return setBonus ? getFloat("setArmorBonus") : getFloat("armorBonusPerPiece"); }
    public float getArmorMultiplier(boolean setBonus) { return setBonus ? getFloat("setArmorMultiplier") : getFloat("armorMultiplierPerPiece"); }
    public float getHungerSaveChance(boolean setBonus) { return setBonus ? getFloat("setHungerSaveChance") : getFloat("hungerSaveChancePerPiece"); }
    public float getDodgeChance(boolean setBonus) { return setBonus ? getFloat("setDodgeChance") : getFloat("dodgeChancePerPiece"); }
    public float getReflectChance(boolean setBonus) { return setBonus ? getFloat("setReflectChance") : getFloat("reflectChancePerPiece"); }
    public float getReflectFraction(boolean setBonus) { return setBonus ? getFloat("setReflectFraction") : getFloat("reflectFractionPerPiece"); }
    public float getHealingBonus(boolean setBonus) { return setBonus ? getFloat("setHealingBonus") : getFloat("healingBonusPerPiece"); }
    public float getImmunityFractionBonus(boolean setBonus) { return setBonus ? getFloat("setImmunityFractionBonus") : getFloat("immunityFractionBonusPerPiece"); }
    public float getImmunityFlatBonus(boolean setBonus) { return setBonus ? getFloat("setImmunityFlatBonus") : getFloat("immunityFlatBonusPerPiece"); }

    public void setMovementSpeed(boolean setBonus, float value) { setFloat(setBonus ? "setMovementSpeed" : "movementSpeedPerPiece", value); }
    public void setDamageResistance(boolean setBonus, float value) { setFloat(setBonus ? "setDamageResistance" : "damageResistancePerPiece", value); }
    public void setMeleeResistance(boolean setBonus, float value) { setFloat(setBonus ? "setMeleeResistance" : "meleeResistancePerPiece", value); }
    public void setProjectileResistance(boolean setBonus, float value) { setFloat(setBonus ? "setProjectileResistance" : "projectileResistancePerPiece", value); }
    public void setFireResistance(boolean setBonus, float value) { setFloat(setBonus ? "setFireResistance" : "fireResistancePerPiece", value); }
    public void setBludgeoningResistance(boolean setBonus, float value) { setFloat(setBonus ? "setBludgeoningResistance" : "bludgeoningResistancePerPiece", value); }
    public void setExplosionResistance(boolean setBonus, float value) { setFloat(setBonus ? "setExplosionResistance" : "explosionResistancePerPiece", value); }
    public void setMagicResistance(boolean setBonus, float value) { setFloat(setBonus ? "setMagicResistance" : "magicResistancePerPiece", value); }
    public void setPoisonResistance(boolean setBonus, float value) { setFloat(setBonus ? "setPoisonResistance" : "poisonResistancePerPiece", value); }
    public void setRadiantResistance(boolean setBonus, float value) { setFloat(setBonus ? "setRadiantResistance" : "radiantResistancePerPiece", value); }
    public void setNecroticResistance(boolean setBonus, float value) { setFloat(setBonus ? "setNecroticResistance" : "necroticResistancePerPiece", value); }
    public void setFreezingResistance(boolean setBonus, float value) { setFloat(setBonus ? "setFreezingResistance" : "freezingResistancePerPiece", value); }
    public void setLightningResistance(boolean setBonus, float value) { setFloat(setBonus ? "setLightningResistance" : "lightningResistancePerPiece", value); }
    public void setFallDamageResistance(boolean setBonus, float value) { setFloat(setBonus ? "setFallDamageResistance" : "fallDamageResistancePerPiece", value); }
    public void setCritChanceResistance(boolean setBonus, float value) { setFloat(setBonus ? "setCritChanceResistance" : "critChanceResistancePerPiece", value); }
    public void setCritDamageResistance(boolean setBonus, float value) { setFloat(setBonus ? "setCritDamageResistance" : "critDamageResistancePerPiece", value); }
    public void setStunResistance(boolean setBonus, float value) { setFloat(setBonus ? "setStunResistance" : "stunResistancePerPiece", value); }
    public void setBleedResistance(boolean setBonus, float value) { setFloat(setBonus ? "setBleedResistance" : "bleedResistancePerPiece", value); }
    public void setKnockbackResistance(boolean setBonus, float value) { setFloat(setBonus ? "setKnockbackResistance" : "knockbackResistancePerPiece", value); }
    public void setPvPResistance(boolean setBonus, float value) { setFloat(setBonus ? "setPvPResistance" : "pvpResistancePerPiece", value); }
    public void setArmorBonus(boolean setBonus, float value) { setFloat(setBonus ? "setArmorBonus" : "armorBonusPerPiece", value); }
    public void setArmorMultiplier(boolean setBonus, float value) { setFloat(setBonus ? "setArmorMultiplier" : "armorMultiplierPerPiece", value); }
    public void setHungerSaveChance(boolean setBonus, float value) { setFloat(setBonus ? "setHungerSaveChance" : "hungerSaveChancePerPiece", value); }
    public void setDodgeChance(boolean setBonus, float value) { setFloat(setBonus ? "setDodgeChance" : "dodgeChancePerPiece", value); }
    public void setReflectChance(boolean setBonus, float value) { setFloat(setBonus ? "setReflectChance" : "reflectChancePerPiece", value); }
    public void setReflectFraction(boolean setBonus, float value) { setFloat(setBonus ? "setReflectFraction" : "reflectFractionPerPiece", value); }
    public void setHealingBonus(boolean setBonus, float value) { setFloat(setBonus ? "setHealingBonus" : "healingBonusPerPiece", value); }
    public void setImmunityFractionBonus(boolean setBonus, float value) { setFloat(setBonus ? "setImmunityFractionBonus" : "immunityFractionBonusPerPiece", value); }
    public void setImmunityFlatBonus(boolean setBonus, float value) { setFloat(setBonus ? "setImmunityFlatBonus" : "immunityFlatBonusPerPiece", value); }

    public double getHeavyArmorEXPMultiplier(){ return getDouble("heavyArmorEXPMultiplier");}
    public void setHeavyArmorEXPMultiplier(double value){ setDouble("heavyArmorEXPMultiplier", value);}

    public HeavyArmorProfile(Player owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
        return "profiles_heavy_armor";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "profile_heavy_armor");

    @Override
    public HeavyArmorProfile getBlankProfile(Player owner) {
        return new HeavyArmorProfile(owner);
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return HeavyArmorSkill.class;
    }
}
