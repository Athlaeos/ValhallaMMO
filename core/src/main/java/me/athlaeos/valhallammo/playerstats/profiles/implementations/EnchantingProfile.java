package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.EnchantmentClassification;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.EnchantingSkill;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.util.Collection;
import java.util.UUID;

@SuppressWarnings("unused")
public class EnchantingProfile extends Profile {
    {
        floatStat("enchantingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("anvilSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());

        floatStat("enchantingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("anvilSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("enchantmentAmplificationChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("lapisSaveChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("essenceRefundChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("essenceRefundFraction", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("essenceMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());

        floatStat("passiveElementalDamageConversion", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create()); // passive fraction of damage converted converted to elemental damage
        floatStat("activeElementalDamageConversion", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create()); // active fraction of damage converted to elemental damage (active meaning the ability is toggled on, and now costs experience per hit) this is added on top of the passive conversion
        floatStat("activeElementalDamageMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create()); // the converted fraction of damage while the ability is active is further enhanced by this value
        floatStat("essenceCostPerHit", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create()); // avg amount of experience spent per hit while elemental damage conversion is active
        stringSetStat("elementalDamageTypes"); // the damage type(s) this fraction of damage will do

        intStat("levelBonusSharpness", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusSmite", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusBOA", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusFortune", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusEfficiency", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusKnockback", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusLooting", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusUnbreaking", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusFireAspect", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusPower", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusBlastProtection", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusFeatherFalling", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusFireProtection", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusProjectileProtection", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusProtection", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusSoulSpeed", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusThorns", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusLure", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusLoTS", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusImpaling", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusSwiftSneak", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());

        intStat("levelBonusGenericDefensive", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusGenericOffensive", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());
        intStat("levelBonusGenericUtility", new PropertyBuilder().format(StatFormat.DIFFERENCE_INT).perkReward().create());

        doubleStat("enchantingEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
    }

    public int getEnchantmentBonus(EnchantmentClassification e){
        return switch(e){
            case DEFENSIVE -> getInt("levelBonusGenericDefensive");
            case OFFENSIVE -> getInt("levelBonusGenericOffensive");
            case UTILITY -> getInt("levelBonusGenericUtility");
            default -> 0;
        };
    }

    public int setEnchantmentBonus(EnchantmentClassification e, int value){
        return switch(e){
            case DEFENSIVE -> getInt("levelBonusGenericDefensive");
            case OFFENSIVE -> getInt("levelBonusGenericOffensive");
            case UTILITY -> getInt("levelBonusGenericUtility");
            default -> 0;
        };
    }

    public int getEnchantmentBonus(Enchantment e){
        return switch(e.getKey().getKey()){
            case "sharpness" -> getInt("levelBonusSharpness");
            case "smite" -> getInt("levelBonusSmite");
            case "bane_of_arthropods" -> getInt("levelBonusBOA");
            case "fortune" -> getInt("levelBonusFortune");
            case "efficiency" -> getInt("levelBonusEfficiency");
            case "knockback" -> getInt("levelBonusKnockback");
            case "unbreaking" -> getInt("levelBonusUnbreaking");
            case "looting" -> getInt("levelBonusLooting");
            case "fire_aspect" -> getInt("levelBonusFireAspect");
            case "power" -> getInt("levelBonusPower");
            case "blast_protection" -> getInt("levelBonusBlastProtection");
            case "feather_falling" -> getInt("levelBonusFeatherFalling");
            case "fire_protection" -> getInt("levelBonusFireProtection");
            case "projectile_protection" -> getInt("levelBonusProjectileProtection");
            case "protection" -> getInt("levelBonusProtection");
            case "soul_speed" -> getInt("levelBonusSoulSpeed");
            case "thorns" -> getInt("levelBonusThorns");
            case "lure" -> getInt("levelBonusLure");
            case "luck_of_the_sea" -> getInt("levelBonusLoTS");
            case "impaling" -> getInt("levelBonusImpaling");
            case "swift_sneak" -> getInt("levelBonusSwiftSneak");
            default -> 0;
        };
    }

    public void setEnchantmentBonus(Enchantment e, int value){
        switch(e.getKey().getKey()){
            case "sharpness" -> setInt("levelBonusSharpness", value);
            case "smite" -> setInt("levelBonusSmite", value);
            case "bane_of_arthropods" -> setInt("levelBonusBOA", value);
            case "fortune" -> setInt("levelBonusFortune", value);
            case "efficiency" -> setInt("levelBonusEfficiency", value);
            case "knockback" -> setInt("levelBonusKnockback", value);
            case "unbreaking" -> setInt("levelBonusUnbreaking", value);
            case "looting" -> setInt("levelBonusLooting", value);
            case "fire_aspect" -> setInt("levelBonusFireAspect", value);
            case "power" -> setInt("levelBonusPower", value);
            case "blast_protection" -> setInt("levelBonusBlastProtection", value);
            case "feather_falling" -> setInt("levelBonusFeatherFalling", value);
            case "fire_protection" -> setInt("levelBonusFireProtection", value);
            case "projectile_protection" -> setInt("levelBonusProjectileProtection", value);
            case "protection" -> setInt("levelBonusProtection", value);
            case "soul_speed" -> setInt("levelBonusSoulSpeed", value);
            case "thorns" -> setInt("levelBonusThorns", value);
            case "lure" -> setInt("levelBonusLure", value);
            case "luck_of_the_sea" -> setInt("levelBonusLoTS", value);
            case "impaling" -> setInt("levelBonusImpaling", value);
            case "swift_sneak" -> setInt("levelBonusSwiftSneak", value);
        }
    }

    public float getEnchantingSkill(){ return getFloat("enchantingSkill"); }
    public void setEnchantingSkill(float value) { setFloat("enchantingSkill", value); }

    public float getAnvilSkill(){ return getFloat("anvilSkill"); }
    public void setAnvilSkill(float value) { setFloat("anvilSkill", value); }

    public float getEnchantmentAmplificationChance(){ return getFloat("enchantmentAmplificationChance"); }
    public void setEnchantmentAmplificationChance(float value) { setFloat("enchantmentAmplificationChance", value); }

    public float getLapisSaveChance(){ return getFloat("lapisSaveChance"); }
    public void setLapisSaveChance(float value) { setFloat("lapisSaveChance", value); }

    public float getEssenceRefundFraction(){ return getFloat("essenceRefundFraction"); }
    public void setEssenceRefundFraction(float value) { setFloat("essenceRefundFraction", value); }

    public float getEssenceMultiplier(){ return getFloat("essenceMultiplier"); }
    public void setEssenceMultiplier(float value) { setFloat("essenceMultiplier", value); }

    public float getPassiveElementalDamageConversion(){ return getFloat("passiveElementalDamageConversion"); }
    public void setPassiveElementalDamageConversion(float value) { setFloat("passiveElementalDamageConversion", value); }

    public float getActiveElementalDamageConversion(){ return getFloat("activeElementalDamageConversion"); }
    public void setActiveElementalDamageConversion(float value) { setFloat("activeElementalDamageConversion", value); }

    public float getActiveElementalDamageMultiplier(){ return getFloat("activeElementalDamageMultiplier"); }
    public void setActiveElementalDamageMultiplier(float value) { setFloat("activeElementalDamageMultiplier", value); }

    public float getEssenceCostPerHit(){ return getFloat("essenceCostPerHit"); }
    public void setEssenceCostPerHit(float value) { setFloat("essenceCostPerHit", value); }

    public Collection<String> getElementalDamageTypes(){ return getStringSet("elementalDamageTypes");}
    public void setElementalDamageTypes(Collection<String> value){ setStringSet("elementalDamageTypes", value);}

    public double getEnchantingEXPMultiplier(){ return getDouble("enchantingEXPMultiplier");}
    public void setEnchantingEXPMultiplier(double value){ setDouble("enchantingEXPMultiplier", value);}

    public EnchantingProfile(UUID owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
        return "profiles_enchanting";
    }

    private static final NamespacedKey key = ValhallaMMO.key("profile_enchanting");

    @Override
    public Profile getBlankProfile(UUID owner) {
        return ProfileRegistry.copyDefaultStats(new EnchantingProfile(owner));
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return EnchantingSkill.class;
    }
}
