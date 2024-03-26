package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.FoodClass;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.BooleanProperties;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.PowerSkill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Collection;

@SuppressWarnings("unused")
public class PowerProfile extends Profile {
    {
        intStat("spendableSkillPoints", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        intStat("spentSkillPoints", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        intStat("spendablePrestigePoints", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        intStat("spentPrestigePoints", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        intStat("redeemableLevelTokens", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create()); // generic level tokens that can be used to acquire more levels in any skill (except Power)
        doubleStat("redeemableExperience", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create()); // generic experience points that can be invested into any skill (except Power)
        intStat("itemCounterLimit", new PropertyBuilder().format(StatFormat.INT).perkReward().create());

        doubleStat("allSkillEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        floatStat("healthBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("healthMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("movementSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("sneakMovementSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("sprintMovementSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("knockbackResistanceBonus", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("armorBonus", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());
        floatStat("armorlessArmor", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());
        floatStat("armorMultiplierBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("toughnessBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("attackReachBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P2).perkReward().create());
        floatStat("attackReachMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("blockReachBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P2).perkReward().create());
        floatStat("stepHeightBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P2).perkReward().create());
        floatStat("scaleMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("gravity", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("safeFallingDistance", new PropertyBuilder().format(StatFormat.DIFFERENCE_FLOAT_P1).perkReward().create());
        floatStat("attackDamageBonus", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("fireDamageBonus", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("explosionDamageBonus", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("poisonDamageBonus", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("bludgeoningDamageBonus", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("magicDamageBonus", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("radiantDamageBonus", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("necroticDamageBonus", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("freezingDamageBonus", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("lightningDamageBonus", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("powerAttackDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create()); // "power attack" hits are what Minecraft normally considers "critical hits", aka falling while hitting. These hits can now do configurable damage, a fraction of which is shared to other mobs in the area.
        floatStat("powerAttackFraction", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("powerAttackRadius", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("attackDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("meleeAttackDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("rangedAttackDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("unarmedAttackDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("magicDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("fireDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("poisonDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bludgeoningDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("radiantDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("necroticDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("freezingDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("lightningDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("explosionDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("attackSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("attackKnockbackBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("luckBonus", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());
        floatStat("healthRegenerationBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("hungerSaveChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("damageResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("meleeResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("projectileResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("fireResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bludgeoningResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("explosionResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("magicResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("poisonResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("radiantResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("necroticResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("freezingResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("lightningResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("fallDamageResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("critChanceResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("critDamageResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("cooldownReduction", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("craftingTimeReduction", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("cookingSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        intStat("immunityFrameBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        floatStat("immunityFrameMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("stunResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bleedResistance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("stunChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("stunDurationBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_TIME_SECONDS_BASE_20_P1).perkReward().create());
        floatStat("critChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("critDamage", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bleedChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bleedDamage", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("bleedDuration", new PropertyBuilder().format(StatFormat.DIFFERENCE_TIME_SECONDS_BASE_20_P1).perkReward().create());
        floatStat("durabilityMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("entityDropMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("entityRareDropMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("dodgeChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("reflectChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("reflectFraction", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("jumpHeightBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        intStat("jumpBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).min(0).perkReward().create());
        intStat("crossbowMagazine", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());
        intStat("parryEffectiveDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).min(0).perkReward().create());
        intStat("parryVulnerableDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).min(0).perkReward().create());
        intStat("parryEnemyDebuffDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).min(0).perkReward().create());
        intStat("parrySelfDebuffDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).min(0).perkReward().create());
        intStat("parryCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).min(0).perkReward().create());
        floatStat("parryCooldownSuccessReduction", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("parryDamageReduction", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("shieldDisarming", new PropertyBuilder().format(StatFormat.DIFFERENCE_TIME_SECONDS_BASE_20_P1).perkReward().create());
        floatStat("lifeSteal", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P2).perkReward().create());
        floatStat("dismountChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("foodBonusVegetable", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("foodBonusSeasoning", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("foodBonusAlcoholic", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("foodBonusBeverage", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("foodBonusSpoiled", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("foodBonusSeafood", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("foodBonusMagical", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("foodBonusSweet", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("foodBonusGrain", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("foodBonusFruit", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("foodBonusNuts", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("foodBonusDairy", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("foodBonusMeat", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("foodBonusFats", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        booleanStat("badFoodImmune", new BooleanProperties(true, true));
        booleanStat("miningAffinityWater", new BooleanProperties(true, true));
        booleanStat("miningAffinityAir", new BooleanProperties(true, true));

        stringSetStat("unlockedPerks");
        stringSetStat("fakeUnlockedPerks"); // if a perk is "fake unlocked" it will be excluded from stat calculation, as if the player hasn't unlocked it at all
        stringSetStat("permanentlyLockedPerks"); // permanently locked perks will be considered unlocked regardless if it's actually in unlockedPerks, essentially permanently preventing it from unlocking
        stringSetStat("unlockedRecipes");
        stringSetStat("unlockedBlockConversions");
        stringSetStat("permanentPotionEffects");

        booleanStat("hidePotionEffectBar");
        booleanStat("hideCraftingEffects");
        booleanStat("hideExperienceGain");
        booleanStat("hideGlobalBuffs");
        floatStat("craftingEffectVolume", 1F, new PropertyBuilder().min(0).max(10).create());
    } // TODO an annotation to automate getters and setters for all these properties?

    public float getShieldDisarming() { return getFloat("shieldDisarming"); }
    public void setShieldDiarming(float value) { setFloat("shieldDisarming", value); }

    public float getLifeSteal() { return getFloat("lifeSteal"); }
    public void setLifeSteal(float value) { setFloat("lifeSteal", value); }

    public float getFoodBonus(FoodClass foodClass){
        return getFloat(switch (foodClass){
            case VEGETABLE -> "foodBonusVegetable";
            case SEASONING -> "foodBonusSeasoning";
            case ALCOHOLIC -> "foodBonusAlcoholic";
            case BEVERAGE -> "foodBonusBeverage";
            case SPOILED -> "foodBonusSpoiled";
            case SEAFOOD -> "foodBonusSeafood";
            case MAGICAL -> "foodBonusMagical";
            case SWEET -> "foodBonusSweet";
            case GRAIN -> "foodBonusGrain";
            case FRUIT -> "foodBonusFruit";
            case DAIRY -> "foodBonusNuts";
            case NUTS -> "foodBonusDairy";
            case MEAT -> "foodBonusMeat";
            case FATS -> "foodBonusFats";
        });
    }
    public void setFoodBonus(FoodClass foodClass, float value){
        setFloat(switch (foodClass){
            case VEGETABLE -> "foodBonusVegetable";
            case SEASONING -> "foodBonusSeasoning";
            case ALCOHOLIC -> "foodBonusAlcoholic";
            case BEVERAGE -> "foodBonusBeverage";
            case SPOILED -> "foodBonusSpoiled";
            case SEAFOOD -> "foodBonusSeafood";
            case MAGICAL -> "foodBonusMagical";
            case SWEET -> "foodBonusSweet";
            case GRAIN -> "foodBonusGrain";
            case FRUIT -> "foodBonusFruit";
            case DAIRY -> "foodBonusNuts";
            case NUTS -> "foodBonusDairy";
            case MEAT -> "foodBonusMeat";
            case FATS -> "foodBonusFats";
        }, value);
    }

    public boolean hasAquaAffinity() { return getBoolean("miningAffinityWater"); }
    public void setAquaAffinity(boolean affinity) { setBoolean("miningAffinityWater", affinity); }

    public boolean hasAerialAffinity() { return getBoolean("miningAffinityAir"); }
    public void setAerialAffinity(boolean affinity) { setBoolean("miningAffinityAir", affinity); }

    public boolean isBadFoodImmune() { return getBoolean("badFoodImmune"); }
    public void setBadFoodImmune(boolean immune) { setBoolean("badFoodImmune", immune); }

    public boolean hidePotionEffectBar() { return getBoolean("hidePotionEffectBar"); }
    public void togglePotionEffectBar() { setBoolean("hidePotionEffectBar", !hidePotionEffectBar()); }

    public boolean hideCraftingEffects() { return getBoolean("hideCraftingEffects"); }
    public void toggleCraftingEffects() { setBoolean("hideCraftingEffects", !hideCraftingEffects()); }

    @SuppressWarnings("all")
    public boolean hideExperienceGain() { return getBoolean("hideExperienceGain"); }
    public void toggleExperienceGain() { setBoolean("hideExperienceGain", !hideExperienceGain()); }

    public boolean hideGlobalBuffs() { return getBoolean("hideGlobalBuffs"); }
    public void toggleGlobalBuffs() { setBoolean("hideGlobalBuffs", !hideGlobalBuffs()); }

    public float getCraftingSoundVolume() { return getFloat("craftingEffectVolume"); }
    public void setCraftingSoundVolume(float volume) { setFloat("craftingEffectVolume", volume); }

    public float getArmorlessArmor() { return getFloat("armorlessArmor"); }
    public void setArmorlessArmor(float volume) { setFloat("armorlessArmor", volume); }

    public int getSpendableSkillPoints(){ return getInt("spendableSkillPoints");}
    public void setSpendableSkillPoints(int value){ setInt("spendableSkillPoints", value);}

    public int getSpentSkillPoints(){ return getInt("spentSkillPoints"); }
    public void setSpentSkillPoints(int points){ setInt("spentSkillPoints", points); }

    public int getSpendablePrestigePoints(){ return getInt("spendablePrestigePoints");}
    public void setSpendablePrestigePoints(int value){ setInt("spendablePrestigePoints", value);}

    public int getCrossbowMagazine(){ return getInt("crossbowMagazine"); }
    public void setCrossbowMagazine(int points){ setInt("crossbowMagazine", points); }

    public int getSpentPrestigePoints(){ return getInt("spentPrestigePoints"); }
    public void setSpentPrestigePoints(int points){ setInt("spentPrestigePoints", points); }

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

    public float getBlockReachBonus(){ return getFloat("blockReachBonus");}
    public void setBlockReachBonus(float value){ setFloat("blockReachBonus", value);}

    public float getStepHeightBonus(){ return getFloat("stepHeightBonus");}
    public void setStepHeightBonus(float value){ setFloat("stepHeightBonus", value);}

    public float getScaleMultiplier(){ return getFloat("scaleMultiplier");}
    public void setScaleMultiplier(float value){ setFloat("scaleMultiplier", value);}

    public float getAttackReachMultiplier(){ return getFloat("attackReachMultiplier");}
    public void setAttackReachMultiplier(float value){ setFloat("attackReachMultiplier", value);}

    public float getDismountChance(){ return getFloat("dismountChance");}
    public void setDismountChance(float value){ setFloat("dismountChance", value);}

    public int getParryEffectiveDuration(){ return getInt("parryEffectiveDuration");}
    public void setParryEffectiveDuration(int value){ setInt("parryEffectiveDuration", value);}

    public int getParryVulnerableDuration(){ return getInt("parryVulnerableDuration");}
    public void setParryVulnerableDuration(int value){ setInt("parryVulnerableDuration", value);}

    public int getParryEnemyDebuffDuration(){ return getInt("parryEnemyDebuffDuration");}
    public void setParryEnemyDebuffDuration(int value){ setInt("parryEnemyDebuffDuration", value);}

    public int getParrySelfDebuffDuration(){ return getInt("parrySelfDebuffDuration");}
    public void setParrySelfDebuffDuration(int value){ setInt("parrySelfDebuffDuration", value);}

    public float getParryDamageReduction(){ return getFloat("parryDamageReduction");}
    public void setParryDamageReduction(int value){ setFloat("parryDamageReduction", value);}

    public int getParryCooldown(){ return getInt("parryCooldown");}
    public void setParryCooldown(int value){ setInt("parryCooldown", value);}

    public float getParryCooldownSuccessReduction(){ return getFloat("parryCooldownSuccessReduction");}
    public void setParryCooldownSuccessReduction(int value){ setFloat("parryCooldownSuccessReduction", value);}

    public float getImmunityFrameMultiplier(){ return getFloat("immunityFrameMultiplier");}
    public void setImmunityFrameMultiplier(float value){ setFloat("immunityFrameMultiplier", value);}

    public float getHealthBonus(){ return getFloat("healthBonus");}
    public void setHealthBonus(float value){ setFloat("healthBonus", value);}

    public float getHealthMultiplier(){ return getFloat("healthMultiplier");}
    public void setHealthMultiplier(float value){ setFloat("healthMultiplier", value);}

    public float getMovementSpeedBonus(){ return getFloat("movementSpeedBonus");}
    public void setMovementSpeedBonus(float value){ setFloat("movementSpeedBonus", value);}

    public float getSneakMovementSpeedBonus(){ return getFloat("sneakMovementSpeedBonus");}
    public void setSneakMovementSpeedBonus(float value){ setFloat("sneakMovementSpeedBonus", value);}

    public float getSprintMovementSpeedBonus(){ return getFloat("sprintMovementSpeedBonus");}
    public void setSprintMovementSpeedBonus(float value){ setFloat("sprintMovementSpeedBonus", value);}

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

    public float getFireDamageBonus(){ return getFloat("fireDamageBonus");}
    public void setFireDamageBonus(float value){ setFloat("fireDamageBonus", value);}

    public float getExplosionDamageBonus(){ return getFloat("explosionDamageBonus");}
    public void setExplosionDamageBonus(float value){ setFloat("explosionDamageBonus", value);}

    public float getBludgeoningDamageBonus(){ return getFloat("bludgeoningDamageBonus");}
    public void setBludgeoningDamageBonus(float value){ setFloat("bludgeoningDamageBonus", value);}

    public float getLightningDamageBonus(){ return getFloat("lightningDamageBonus");}
    public void setLightningDamageBonus(float value){ setFloat("lightningDamageBonus", value);}

    public float getFreezingingDamageBonus(){ return getFloat("freezingDamageBonus");}
    public void setFreezingDamageBonus(float value){ setFloat("freezingDamageBonus", value);}

    public float getRadiantDamageBonus(){ return getFloat("radiantDamageBonus");}
    public void setRadiantDamageBonus(float value){ setFloat("radiantDamageBonus", value);}

    public float getNecroticDamageBonus(){ return getFloat("necroticDamageBonus");}
    public void setNecroticDamageBonus(float value){ setFloat("necroticDamageBonus", value);}

    public float getPoisonDamageBonus(){ return getFloat("poisonDamageBonus");}
    public void setPoisonDamageBonus(float value){ setFloat("poisonDamageBonus", value);}

    public float getMagicDamageBonus(){ return getFloat("magicDamageBonus");}
    public void setMagicDamageBonus(float value){ setFloat("magicDamageBonus", value);}

    public float getAttackDamageMultiplier(){ return getFloat("attackDamageMultiplier");}
    public void setAttackDamageMultiplier(float value){ setFloat("attackDamageMultiplier", value);}

    public float getMeleeAttackDamageMultiplier(){ return getFloat("meleeAttackDamageMultiplier");}
    public void setMeleeAttackDamageMultiplier(float value){ setFloat("meleeAttackDamageMultiplier", value);}

    public float getUnarmedAttackDamageMultiplier(){ return getFloat("unarmedAttackDamageMultiplier");}
    public void setUnarmedAttackDamageMultiplier(float value){ setFloat("unarmedAttackDamageMultiplier", value);}

    public float getRangedAttackDamageMultiplier(){ return getFloat("rangedAttackDamageMultiplier");}
    public void setRangedAttackDamageMultiplier(float value){ setFloat("rangedAttackDamageMultiplier", value);}

    public float getMagicDamageMultiplier(){ return getFloat("magicDamageMultiplier");}
    public void setMagicDamageMultiplier(float value){ setFloat("magicDamageMultiplier", value);}

    public float getFireDamageMultiplier(){ return getFloat("fireDamageMultiplier");}
    public void setFireDamageMultiplier(float value){ setFloat("fireDamageMultiplier", value);}

    public float getPoisonDamageMultiplier(){ return getFloat("poisonDamageMultiplier");}
    public void setPoisonDamageMultiplier(float value){ setFloat("poisonDamageMultiplier", value);}

    public float getBludgeoningDamageMultiplier(){ return getFloat("bludgeoningDamageMultiplier");}
    public void setBludgeoningDamageMultiplier(float value){ setFloat("bludgeoningDamageMultiplier", value);}

    public float getLightningDamageMultiplier(){ return getFloat("lightningDamageMultiplier");}
    public void setLightningDamageMultiplier(float value){ setFloat("lightningDamageMultiplier", value);}

    public float getFreezingDamageMultiplier(){ return getFloat("freezingDamageMultiplier");}
    public void setFreezingDamageMultiplier(float value){ setFloat("freezingDamageMultiplier", value);}

    public float getRadiantDamageMultiplier(){ return getFloat("radiantDamageMultiplier");}
    public void setRadiantDamageMultiplier(float value){ setFloat("radiantDamageMultiplier", value);}

    public float getNecroticDamageMultiplier(){ return getFloat("necroticDamageMultiplier");}
    public void setNecroticDamageMultiplier(float value){ setFloat("necroticDamageMultiplier", value);}

    public float getExplosionDamageMultiplier(){ return getFloat("explosionDamageMultiplier");}
    public void setExplosionDamageMultiplier(float value){ setFloat("explosionDamageMultiplier", value);}

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

    public float getCritDamageResistance(){ return getFloat("critDamageResistance");}
    public void setCritDamageResistance(float value){ setFloat("critDamageResistance", value);}

    public float getCritChanceResistance(){ return getFloat("critChanceResistance");}
    public void setCritChanceResistance(float value){ setFloat("critChanceResistance", value);}

    public float getBludgeoningResistance(){ return getFloat("bludgeoningResistance");}
    public void setBludgeoningResistance(float value){ setFloat("bludgeoningResistance", value);}

    public float getLightningResistance(){ return getFloat("lightningResistance");}
    public void setLightningResistance(float value){ setFloat("lightningResistance", value);}

    public float getFreezingResistance(){ return getFloat("freezingResistance");}
    public void setFreezingResistance(float value){ setFloat("freezingResistance", value);}

    public float getRadiantResistance(){ return getFloat("radiantResistance");}
    public void setRadiantResistance(float value){ setFloat("radiantResistance", value);}

    public float getNecroticResistance(){ return getFloat("necroticResistance");}
    public void setNecroticResistance(float value){ setFloat("necroticResistance", value);}

    public float getCooldownReduction(){ return getFloat("cooldownReduction");}
    public void setCooldownReduction(float value){ setFloat("cooldownReduction", value);}

    public float getCraftingTimeReduction(){ return getFloat("craftingTimeReduction");}
    public void setCraftingTimeReduction(float value){ setFloat("craftingTimeReduction", value);}

    public float getCookingSpeedBonus(){ return getFloat("cookingSpeedBonus");}
    public void setCookingSpeedBonus(float value){ setFloat("cookingSpeedBonus", value);}

    public float getStunResistance(){ return getFloat("stunResistance");}
    public void setStunResistance(float value){ setFloat("stunResistance", value);}

    public void setStunChance(float value){ setFloat("stunChance", value);}
    public float getStunChance(){ return getFloat("stunChance");}

    public void setStunDurationBonus(float value){ setFloat("stunDurationBonus", value);}
    public float getStunDurationBonus(){ return getFloat("stunDurationBonus");}

    public void setCritChance(float value){ setFloat("critChance", value);}
    public float getCritChance(){ return getFloat("critChance");}

    public void setCritDamage(float value){ setFloat("critDamage", value);}
    public float getCritDamage(){ return getFloat("critDamage");}

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

    public float getEntityRareDropMultiplier(){ return getFloat("entityRareDropMultiplier");}
    public void setEntityRareDropMultiplier(float value){ setFloat("entityRareDropMultiplier", value);}

    public float getDurabilityMultiplier(){ return getFloat("durabilityMultiplier");}
    public void setDurabilityMultiplier(float value){ setFloat("durabilityMultiplier", value);}

    public double getAllSkillEXPMultiplier(){ return getDouble("allSkillEXPMultiplier");}
    public void setAllSkillEXPMultiplier(double value){ setDouble("allSkillEXPMultiplier", value);}

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

    public Collection<String> getUnlockedBlockConversions(){ return getStringSet("unlockedBlockConversions");}
    public void setUnlockedBlockConversions(Collection<String> value){ setStringSet("unlockedBlockConversions", value);}

    public Collection<String> getPermanentPotionEffects(){ return getStringSet("permanentPotionEffects");}
    public void setPermanentPotionEffects(Collection<String> value){ setStringSet("permanentPotionEffects", value);}

    public PowerProfile(Player owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
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
