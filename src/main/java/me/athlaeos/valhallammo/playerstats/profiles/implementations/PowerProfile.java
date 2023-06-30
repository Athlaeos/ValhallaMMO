package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.progression.skills.Skill;
import me.athlaeos.valhallammo.progression.skills.implementations.PowerSkill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Collection;

@SuppressWarnings("unused")
public class PowerProfile extends Profile {

    {
        intStat("spendableSkillPoints", false);
        intStat("spentSkillPoints", true);
        intStat("redeemableLevelTokens", true, new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create()); // generic level tokens that can be used to acquire more levels in any skill (except Power)
        doubleStat("redeemableExperience", true, new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create()); // generic experience points that can be invested into any skill (except Power)
        doubleStat("allSkillEXPGain", false, 100D, new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_100_P1).min(0).perkReward().create());
        stringSetStat("unlockedPerks", false);
        stringSetStat("fakeUnlockedPerks", true); // if a perk is "fake unlocked" it will be excluded from stat calculation, as if the player hasn't unlocked it at all
        stringSetStat("permanentlyLockedPerks", true); // permanently locked perks will be considered unlocked regardless if it's actually in unlockedPerks, essentially permanently preventing it from unlocking
        stringSetStat("unlockedRecipes", false);
        floatStat("healthBonus", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("healthMultiplier", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("movementSpeedBonus", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("knockbackResistanceBonus", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("armorBonus", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("armorMultiplierBonus", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("toughnessBonus", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("attackReachBonus", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("attackDamageBonus", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("attackDamageMultiplier", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("meleeAttackDamageMultiplier", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("attackSpeedBonus", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("attackKnockbackBonus", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("luckBonus", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("healthRegenerationBonus", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("hungerSaveChance", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("damageResistance", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("meleeResistance", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("projectileResistance", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("fireResistance", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("explosionResistance", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("magicResistance", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("poisonResistance", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("fallDamageResistance", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("cooldownReduction", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("craftingTimeReduction", false, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        intStat("immunityFrameBonus", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        floatStat("immunityFrameMultiplier", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("stunResistance", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bleedResistance", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bleedChance", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bleedDamage", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("bleedDuration", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_TIME_SECONDS_BASE_1000_P1).perkReward().create());
        floatStat("durabilityMultiplier", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("entityDropMultiplier", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("dodgeChance", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("reflectChance", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("reflectFraction", true, new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
    } // TODO an annotation to automate getters and setters for all these properties?

    public int getSpendableSkillPoints(){ return getInt("spendableSkillPoints");}
    public void setSpendableSkillPoints(int value){ setInt("spendableSkillPoints", value);}

    public int getSpentSkillPoints(){ return getInt("spentSkillPoints"); }
    public void setSpentSkillPoints(int points){ setInt("spentSkillPoints", points); }

    public int getRedeemableLevelTokens(){ return getInt("redeemableLevelTokens");}
    public void setRedeemableLevelTokens(int value){ setInt("redeemableLevelTokens", value);}

    public int getImmunityFrameBonus(){ return getInt("immunityFrameBonus");}
    public void setImmunityFrameBonus(int value){ setInt("immunityFrameBonus", value);}

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
        return "profiles_account";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_profile_account");

    @Override
    public PowerProfile getBlankProfile(Player owner) {
        return new PowerProfile(owner);
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return PowerSkill.class;
    }
}
