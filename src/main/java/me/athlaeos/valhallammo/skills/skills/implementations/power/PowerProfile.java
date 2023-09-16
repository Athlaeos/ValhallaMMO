package me.athlaeos.valhallammo.skills.skills.implementations.power;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Collection;

@SuppressWarnings("unused")
public class PowerProfile extends Profile {

    {
        intStat("spendableSkillPoints", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        intStat("spentSkillPoints", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        intStat("itemCounterLimit", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        intStat("redeemableLevelTokens", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create()); // generic level tokens that can be used to acquire more levels in any skill (except Power)
        doubleStat("redeemableExperience", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create()); // generic experience points that can be invested into any skill (except Power)
        doubleStat("allSkillEXPGain", 1D, new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
        stringSetStat("unlockedPerks", false);
        stringSetStat("fakeUnlockedPerks", true); // if a perk is "fake unlocked" it will be excluded from stat calculation, as if the player hasn't unlocked it at all
        stringSetStat("permanentlyLockedPerks", true); // permanently locked perks will be considered unlocked regardless if it's actually in unlockedPerks, essentially permanently preventing it from unlocking
        stringSetStat("unlockedRecipes", false);
        floatStat("healthBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("healthMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("movementSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("knockbackResistanceBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("armorBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("armorMultiplierBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("toughnessBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("attackReachBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("attackDamageBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("attackDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("meleeAttackDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("attackSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("attackKnockbackBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("luckBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("healthRegenerationBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("hungerSaveChance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("damageResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("meleeResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("projectileResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("fireResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("explosionResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("magicResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("poisonResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("fallDamageResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("cooldownReduction", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("craftingTimeReduction", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        intStat("immunityFrameBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        floatStat("immunityFrameMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("stunResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bleedResistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bleedChance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bleedDamage", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("bleedDuration", new PropertyBuilder().format(StatFormat.DIFFERENCE_TIME_SECONDS_BASE_1000_P1).perkReward().create());
        floatStat("durabilityMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("entityDropMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("dodgeChance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("reflectChance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("reflectFraction", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("jumpHeightBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        intStat("jumpBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).min(0).perkReward().create());
    } // TODO an annotation to automate getters and setters for all these properties?

    public int getSpendableSkillPoints(){ return getInt("spendableSkillPoints");}
    public void setSpendableSkillPoints(int value){ setInt("spendableSkillPoints", value);}

    public int getSpentSkillPoints(){ return getInt("spentSkillPoints"); }
    public void setSpentSkillPoints(int points){ setInt("spentSkillPoints", points); }

    public int getRedeemableLevelTokens(){ return getInt("redeemableLevelTokens");}
    public void setRedeemableLevelTokens(int value){ setInt("redeemableLevelTokens", value);}

    public int getItemCounterLimit(){ return getInt("itemCounterLimit");}
    public void setItemCounterLimit(int value){ setInt("itemCounterLimit", value);}

    public int getImmunityFrameBonus(){ return getInt("immunityFrameBonus");}
    public void setImmunityFrameBonus(int value){ setInt("immunityFrameBonus", value);}

    public int getJumpBonus(){ return getInt("jumpBonus");}
    public void setJumpBonus(int value){ setInt("jumpBonus", value);}

    public float getJumpHeightBonus(){ return getFloat("jumpHeightBonus");}
    public void setJumpHeightBonus(float value){ setFloat("jumpHeightBonus", value);}

    public float getImmunityFrameMultiplier(){ return getFloat("immunityFrameMultiplier");}
    public void setImmunityFrameMultiplier(float value){ setFloat("immunityFrameMultiplier", value);}

    public float getHealthBonus(){ return getFloat("healthBonus");}
    public void setHealthBonus(float value){ setFloat("healthBonus", value);}

    public float getHealthMultiplier(){ return getFloat("healthMultiplier");}
    public void setHealthMultiplier(float value){ setFloat("healthMultiplier", value);}

    public float getMovementSpeedBonus(){ return getFloat("movementSpeedBonus");}
    public void setMovementSpeedBonus(float value){ setFloat("movementSpeedBonus", value);}

    public float getKnockbackResistanceBonus(){ return getFloat("knockbackResistanceBonus");}
    public void setKnockbackResistanceBonus(float value){ setFloat("knockbackResistanceBonus", value);}

    public float getArmorBonus(){ return getFloat("armorBonus");}
    public void setArmorBonus(float value){ setFloat("armorBonus", value);}

    public float getArmorMultiplierBonus(){ return getFloat("armorMultiplierBonus");}
    public void setArmorMultiplierBonus(float value){ setFloat("armorMultiplierBonus", value);}

    public float getToughnessBonus(){ return getFloat("toughnessBonus");}
    public void setToughnessBonus(float value){ setFloat("toughnessBonus", value);}

    public float getAttackReachBonus(){ return getFloat("attackReachBonus");}
    public void setAttackReachBonus(float value){ setFloat("attackReachBonus", value);}

    public float getAttackDamageBonus(){ return getFloat("attackDamageBonus");}
    public void setAttackDamageBonus(float value){ setFloat("attackDamageBonus", value);}

    public float getAttackDamageMultiplier(){ return getFloat("attackDamageMultiplier");}
    public void setAttackDamageMultiplier(float value){ setFloat("attackDamageMultiplier", value);}

    public float getMeleeAttackDamageMultiplier(){ return getFloat("meleeAttackDamageMultiplier");}
    public void setMeleeAttackDamageMultiplier(float value){ setFloat("meleeAttackDamageMultiplier", value);}

    public float getAttackSpeedBonus(){ return getFloat("attackSpeedBonus");}
    public void setAttackSpeedBonus(float value){ setFloat("attackSpeedBonus", value);}

    public float getAttackKnockbackBonus(){ return getFloat("attackKnockbackBonus");}
    public void setAttackKnockbackBonus(float value){ setFloat("attackKnockbackBonus", value);}

    public float getLuckBonus(){ return getFloat("luckBonus");}
    public void setLuckBonus(float value){ setFloat("luckBonus", value);}

    public float getHealthRegenerationBonus(){ return getFloat("healthRegenerationBonus");}
    public void setHealthRegenerationBonus(float value){ setFloat("healthRegenerationBonus", value);}

    public float getHungerSaveChance(){ return getFloat("hungerSaveChance");}
    public void setHungerSaveChance(float value){ setFloat("hungerSaveChance", value);}

    public float getDamageResistance(){ return getFloat("damageResistance");}
    public void setDamageResistance(float value){ setFloat("damageResistance", value);}

    public float getMeleeResistance(){ return getFloat("meleeResistance");}
    public void setMeleeResistance(float value){ setFloat("meleeResistance", value);}

    public float getProjectileResistance(){ return getFloat("projectileResistance");}
    public void setProjectileResistance(float value){ setFloat("projectileResistance", value);}

    public float getFireResistance(){ return getFloat("fireResistance");}
    public void setFireResistance(float value){ setFloat("fireResistance", value);}

    public float getExplosionResistance(){ return getFloat("explosionResistance");}
    public void setExplosionResistance(float value){ setFloat("explosionResistance", value);}

    public float getMagicResistance(){ return getFloat("magicResistance");}
    public void setMagicResistance(float value){ setFloat("magicResistance", value);}

    public float getPoisonResistance(){ return getFloat("immunityFrameBonus");}
    public void setPoisonResistance(float value){ setFloat("immunityFrameBonus", value);}

    public float getFallDamageResistance(){ return getFloat("fallDamageResistance");}
    public void setFallDamageResistance(float value){ setFloat("fallDamageResistance", value);}

    public float getCooldownReduction(){ return getFloat("cooldownReduction");}
    public void setCooldownReduction(float value){ setFloat("cooldownReduction", value);}

    public float getCraftingTimeReduction(){ return getFloat("craftingTimeReduction");}
    public void setCraftingTimeReduction(float value){ setFloat("craftingTimeReduction", value);}

    public float getStunResistance(){ return getFloat("stunResistance");}
    public void setStunResistance(float value){ setFloat("stunResistance", value);}

    public float getBleedChance(){ return getFloat("bleedChance");}
    public void setBleedChance(float value){ setFloat("bleedChance", value);}

    public float getDodgeChance(){ return getFloat("dodgeChance");}
    public void setDodgeChance(float value){ setFloat("dodgeChance", value);}

    public float getBleedDamage(){ return getFloat("bleedDamage");}
    public void setBleedDamage(float value){ setFloat("bleedDamage", value);}

    public float getBleedDuration(){ return getFloat("bleedDuration");}
    public void setBleedDuration(float value){ setFloat("bleedDuration", value);}

    public float getEntityDropMultiplier(){ return getFloat("entityDropMultiplier");}
    public void setEntityDropMultiplier(float value){ setFloat("entityDropMultiplier", value);}

    public float getDurabilityMultiplier(){ return getFloat("durabilityMultiplier");}
    public void setDurabilityMultiplier(float value){ setFloat("durabilityMultiplier", value);}

    public double getAllSkillEXPGain(){ return getDouble("allSkillEXPGain");}
    public void setAllSkillEXPGain(double value){ setDouble("allSkillEXPGain", value);}

    public double getRedeemableExperiencePoints(){ return getDouble("redeemableExperience");}
    public void setRedeemableExperiencePoints(double value){ setDouble("redeemableExperience", value);}

    public Collection<String> getUnlockedPerks(){ return getStringSet("unlockedPerks");}
    public void setUnlockedPerks(Collection<String> value){ setStringSet("unlockedPerks", value);}

    public Collection<String> getFakeUnlockedPerks(){ return getStringSet("fakeUnlockedPerks");}
    public void setFakeUnlockedPerks(Collection<String> value){ setStringSet("fakeUnlockedPerks", value);}

    public Collection<String> getUnlockedRecipes(){ return getStringSet("unlockedRecipes");}
    public void setUnlockedRecipes(Collection<String> value){ setStringSet("unlockedRecipes", value);}

    public Collection<String> getPermanentlyLockedPerks(){ return getStringSet("permanentlyLockedPerks");}
    public void setPermanentlyLockedPerks(Collection<String> value){ setStringSet("permanentlyLockedPerks", value);}

    public PowerProfile(Player owner) {
        super(owner);
    }

    @Override
    protected String getTableName() {
        return "profiles_power";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "profile_power");

    @Override
    public PowerProfile getBlankProfile(Player owner) {
        return new PowerProfile(owner);
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return PowerSkill.class;
    }
}
