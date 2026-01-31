package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.MiningSpeed;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.properties.BooleanProperties;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.MiningSkill;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.util.Collection;
import java.util.UUID;

@SuppressWarnings("unused")
public class MiningProfile extends Profile {
    {
        floatStat("miningDrops", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("miningLuck", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("blastingDrops", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("blastingLuck", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("blockExperienceRate", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("blockExperienceMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("tntBlastRadius", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("tntDamageReduction", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("miningSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        stringSetStat("unbreakableBlocks");
        intStat("blastFortuneLevel", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        booleanStat("blastingInstantPickup", new BooleanProperties(true, true));
        booleanStat("blastingItemImmunity", new BooleanProperties(true, true));

        booleanStat("veinMiningUnlocked", new BooleanProperties(true, true));
        booleanStat("veinMiningInstantPickup", new BooleanProperties(true, true));
        intStat("veinMiningCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        stringSetStat("veinMinerValidBlocks");

        booleanStat("drillingUnlocked", new BooleanProperties(true, true));
        floatStat("drillingSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        intStat("drillingCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("drillingDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());

        doubleStat("miningEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());

        stringSetStat("emptyHandToolMaterial");
        intStat("emptyHandToolFortune", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        floatStat("emptyHandToolMiningStrength", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
    }

    public boolean isVeinMiningUnlocked() { return getBoolean("veinMiningUnlocked"); }
    public void setVeinMiningUnlocked(boolean value) { setBoolean("veinMiningUnlocked", value); }
    public boolean isDrillingUnlocked() { return getBoolean("drillingUnlocked"); }
    public void setDrillingUnlocked(boolean value) { setBoolean("drillingUnlocked", value); }
    public boolean isBlastingInstantPickup() { return getBoolean("blastingInstantPickup"); }
    public void setBlastingInstantPickup(boolean value) { setBoolean("blastingInstantPickup", value); }
    public boolean isBlastingItemImmunity() { return getBoolean("blastingItemImmunity"); }
    public void setBlastingItemImmunity(boolean value) { setBoolean("blastingItemImmunity", value); }
    public boolean isVeinMiningInstantPickup() { return getBoolean("veinMiningInstantPickup"); }
    public void setVeinMiningInstantPickup(boolean value) { setBoolean("veinMiningInstantPickup", value); }

    public int getBlastFortuneLevel() { return getInt("blastFortuneLevel"); }
    public void setBlastFortuneLevel(int value) { setInt("blastFortuneLevel", value); }
    public int getVeinMiningCooldown() { return getInt("veinMiningCooldown"); }
    public void setVeinMiningCooldown(int value) { setInt("veinMiningCooldown", value); }
    public int getDrillingCooldown() { return getInt("drillingCooldown"); }
    public void setDrillingCooldown(int value) { setInt("drillingCooldown", value); }
    public int getDrillingDuration() { return getInt("drillingDuration"); }
    public void setDrillingDuration(int value) { setInt("drillingDuration", value); }
    public int getEmptyHandToolFortune() { return getInt("emptyHandToolFortune"); }
    public void setEmptyHandToolFortune(int value) { setInt("emptyHandToolFortune", value); }

    public Collection<String> getUnbreakableBlocks() { return getStringSet("unbreakableBlocks"); }
    public void setUnbreakableBlocks(Collection<String> value) { setStringSet("unbreakableBlocks", value); }
    public Collection<String> getVeinMinerValidBlocks() { return getStringSet("veinMinerValidBlocks"); }
    public void setVeinMinerValidBlocks(Collection<String> value) { setStringSet("veinMinerValidBlocks", value); }
    public Collection<String> getEmptyHandToolMaterial() { return getStringSet("emptyHandToolMaterial"); }
    public void setEmptyHandToolMaterial(Collection<String> value) { setStringSet("emptyHandToolMaterial", value); }

    public float getMiningDrops() { return getFloat("miningDrops"); }
    public void setMiningDrops(float value) { setFloat("miningDrops", value); }
    public float getMiningLuck() { return getFloat("miningLuck"); }
    public void setMiningLuck(float value) { setFloat("miningLuck", value); }
    public float getBlastingDrops() { return getFloat("blastingDrops"); }
    public void setBlastingDrops(float value) { setFloat("blastingDrops", value); }
    public float getBlastingLuck() { return getFloat("blastingLuck"); }
    public void setBlastingLuck(float value) { setFloat("blastingLuck", value); }
    public float getTntBlastRadius() { return getFloat("tntBlastRadius"); }
    public void setTntBlastRadius(float value) { setFloat("tntBlastRadius", value); }
    public float getTntDamageReduction() { return getFloat("tntDamageReduction"); }
    public void setTntDamageReduction(float value) { setFloat("tntDamageReduction", value); }
    public float getMiningSpeedBonus() { return getFloat("miningSpeedBonus"); }
    public void setMiningSpeedBonus(float value) { setFloat("miningSpeedBonus", value); }
    public float getDrillingSpeedBonus() { return getFloat("drillingSpeedBonus"); }
    public void setDrillingSpeedBonus(float value) { setFloat("drillingSpeedBonus", value); }
    public float getBlockExperienceRate() { return getFloat("blockExperienceRate"); }
    public void setBlockExperienceRate(float value) { setFloat("blockExperienceRate", value); }
    public float getBlockExperienceMultiplier() { return getFloat("blockExperienceMultiplier"); }
    public void setBlockExperienceMultiplier(float value) { setFloat("blockExperienceMultiplier", value); }
    public float getEmptyHandToolMiningStrength() { return getFloat("emptyHandToolMiningStrength"); }
    public void setEmptyHandToolMiningStrength(float value) { setFloat("emptyHandToolMiningStrength", value); }

    public double getMiningEXPMultiplier(){ return getDouble("miningEXPMultiplier");}
    public void setMiningEXPMultiplier(double value){ setDouble("miningEXPMultiplier", value);}

    private ItemBuilder emptyHandTool = null;

    public ItemBuilder getEmptyHandTool() {
        return emptyHandTool;
    }

    @Override
    public void onCacheRefresh() {
        String value = getEmptyHandToolMaterial().stream().findFirst().orElse(null);
        if (value == null) {
            emptyHandTool = null;
            return;
        }
        Material material = ItemUtils.stringToMaterial(value, null);
        if (material == null) {
            emptyHandTool = null;
            return;
        }
        int level = getEmptyHandToolFortune();
        ItemBuilder item = new ItemBuilder(material);
        if (level != 0) item.enchant(level < 0 ? Enchantment.SILK_TOUCH : EnchantmentMappings.FORTUNE.getEnchantment(), level < 0 ? 1 : level);
        if (getEmptyHandToolMiningStrength() > 0) MiningSpeed.setMultiplier(item.getMeta(), getEmptyHandToolMiningStrength());
        emptyHandTool = new ItemBuilder(item.get());
    }

    public MiningProfile(UUID owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
        return "profiles_mining";
    }

    private static final NamespacedKey key = ValhallaMMO.key("profile_mining");

    @Override
    public Profile getBlankProfile(UUID owner) {
        return ProfileRegistry.copyDefaultStats(new MiningProfile(owner));
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return MiningSkill.class;
    }
}
