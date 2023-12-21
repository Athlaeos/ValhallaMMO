package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.BooleanProperties;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.potioneffects.EffectClass;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.AlchemySkill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Collection;

@SuppressWarnings("unused")
public class AlchemyProfile extends Profile {
    {
        floatStat("genericBrewingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P1).min(0).perkReward().create());
        floatStat("buffBrewingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P1).min(0).perkReward().create());
        floatStat("debuffBrewingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P1).min(0).perkReward().create());

        floatStat("genericBrewingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("buffBrewingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("debuffBrewingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());

        floatStat("brewingTimeReduction", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("potionSaveChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("brewingIngredientSaveChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("throwVelocity", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());

        floatStat("lingeringRadiusMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("lingeringDurationMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("splashIntensityMinimum", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());

        booleanStat("potionCombiningUnlocked", false, new BooleanProperties(true, true));
        intStat("potionCombiningMaxCombinations", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        floatStat("potionCombiningAmplifierMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("potionCombiningDurationMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());

        intStat("transmutationRadius", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        stringSetStat("unlockedTransmutations");

        doubleStat("alchemyEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).min(0).perkReward().create());
    }

    public int getTransmutationRadius() { return getInt("transmutationRadius"); }
    public void setTransmutationRadius(int value) { setInt("transmutationRadius", value); }

    public Collection<String> getUnlockedTransmutations() { return getStringSet("unlockedTransmutations"); }
    public void setUnlockedTransmutations(Collection<String> value) { setStringSet("unlockedTransmutations", value); }

    public boolean isPotionCombiningUnlocked(){ return getBoolean("potionCombiningUnlocked"); }
    public void setPotionCombiningUnlocked(boolean value) { setBoolean("potionCombiningUnlocked", value);}

    public float getPotionCombiningAmplifierMultiplier() { return getFloat("potionCombiningAmplifierMultiplier"); }
    public void setPotionCombiningAmplifierMultiplier(float volume) { setFloat("potionCombiningAmplifierMultiplier", volume); }

    public float getPotionCombiningDurationMultiplier() { return getFloat("potionCombiningDurationMultiplier"); }
    public void setPotionCombiningDurationMultiplier(float volume) { setFloat("potionCombiningDurationMultiplier", volume); }

    public int getPotionCombiningMaxCombinations() { return getInt("potionCombiningMaxCombinations"); }
    public void setPotionCombiningMaxCombinations(int volume) { setInt("potionCombiningMaxCombinations", volume); }

    public int getBrewingSkill(EffectClass effectClass){
        if (effectClass == null) return getInt("genericBrewingSkill");
        return switch (effectClass){
            case BUFF -> getInt("buffBrewingSkill");
            case DEBUFF -> getInt("debuffBrewingSkill");
            default -> 0;
        };
    }
    public void setBrewingSkill(EffectClass effectClass, int value){
        if (effectClass == null) setInt("genericBrewingSkill", value);
        else
            switch (effectClass) {
                case BUFF -> setInt("buffBrewingSkill", value);
                case DEBUFF -> setInt("debuffBrewingSkill", value);
            }
    }

    public float getBrewingSkillMultiplier(EffectClass effectClass){
        if (effectClass == null) return getFloat("genericBrewingSkillFractionBonus");
        return switch (effectClass){
            case BUFF -> getFloat("buffBrewingSkillFractionBonus");
            case DEBUFF -> getFloat("debuffBrewingSkillFractionBonus");
            default -> 0;
        };
    }
    public void setBrewingSkillMultiplier(EffectClass effectClass, int value){
        if (effectClass == null) setFloat("genericBrewingSkillFractionBonus", value);
        else
            switch (effectClass) {
                case BUFF -> setFloat("buffBrewingSkillFractionBonus", value);
                case DEBUFF -> setFloat("debuffBrewingSkillFractionBonus", value);
            }
    }

    public float getBrewingTimeReduction() { return getFloat("brewingTimeReduction"); }
    public void setBrewingTimeReduction(float volume) { setFloat("brewingTimeReduction", volume); }

    public float getPotionSaveChance() { return getFloat("potionSaveChance"); }
    public void setPotionSaveChance(float volume) { setFloat("potionSaveChance", volume); }

    public float getBrewingIngredientSaveChance() { return getFloat("brewingIngredientSaveChance"); }
    public void setBrewingIngredientSaveChance(float volume) { setFloat("brewingIngredientSaveChance", volume); }

    public float getThrowVelocity() { return getFloat("throwVelocity"); }
    public void setThrowVelocity(float volume) { setFloat("throwVelocity", volume); }

    public double getAlchemyEXPMultiplier(){ return getDouble("alchemyEXPMultiplier");}
    public void setAlchemyEXPMultiplier(double value){ setDouble("alchemyEXPMultiplier", value);}

    public float getLlingeringRadiusMultiplier() { return getFloat("lingeringRadiusMultiplier"); }
    public void setLlingeringRadiusMultiplier(float volume) { setFloat("lingeringRadiusMultiplier", volume); }

    public float getLingeringDurationMultiplier() { return getFloat("lingeringDurationMultiplier"); }
    public void setLingeringDurationMultiplier(float volume) { setFloat("lingeringDurationMultiplier", volume); }

    public float getSplashIntensityMinimum() { return getFloat("splashIntensityMinimum"); }
    public void setSplashIntensityMinimum(float volume) { setFloat("splashIntensityMinimum", volume); }

    public AlchemyProfile(Player owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
        return "profiles_alchemy";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "profile_alchemy");

    @Override
    public AlchemyProfile getBlankProfile(Player owner) {
        return new AlchemyProfile(owner);
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return AlchemySkill.class;
    }
}
