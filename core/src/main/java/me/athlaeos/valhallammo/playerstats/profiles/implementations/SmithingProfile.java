package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.MaterialClass;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.SmithingSkill;
import org.bukkit.NamespacedKey;

import java.util.UUID;

@SuppressWarnings("unused")
public class SmithingProfile extends Profile {
    {
        floatStat("genericCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("bowCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("crossbowCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("woodCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("leatherCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("copperCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("stoneCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("chainCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("goldCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("ironCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("diamondCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("netheriteCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("prismarineCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("endericCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("custom1CraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("custom2CraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("custom3CraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("custom4CraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("custom5CraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("custom6CraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("custom7CraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("custom8CraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("custom9CraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("custom10CraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());
        floatStat("otherCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).perkReward().create());

        floatStat("genericCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bowCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("crossbowCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("woodCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("leatherCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("stoneCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("copperCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("chainCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("goldCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("ironCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("diamondCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("netheriteCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("prismarineCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("endericCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("custom1CraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("custom2CraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("custom3CraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("custom4CraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("custom5CraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("custom6CraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("custom7CraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("custom8CraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("custom9CraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("custom10CraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("otherCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());

        doubleStat("genericEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("bowEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("crossbowEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("woodEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("leatherEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("stoneEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("copperEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("chainEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("goldEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("ironEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("diamondEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("netheriteEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("prismarineEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("endericEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("custom1EXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("custom2EXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("custom3EXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("custom4EXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("custom5EXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("custom6EXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("custom7EXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("custom8EXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("custom9EXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("custom10EXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("otherEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
    }

    public float getCraftingSkill(MaterialClass material){
        if (material == null) return getFloat("genericCraftingSkill");
        return switch (material){
            case BOW -> getFloat("bowCraftingSkill");
            case CROSSBOW -> getFloat("crossbowCraftingSkill");
            case WOOD -> getFloat("woodCraftingSkill");
            case LEATHER -> getFloat("leatherCraftingSkill");
            case STONE -> getFloat("stoneCraftingSkill");
            case COPPER -> getFloat("copperCraftingSkill");
            case CHAINMAIL -> getFloat("chainCraftingSkill");
            case GOLD ->getFloat("goldCraftingSkill");
            case IRON -> getFloat("ironCraftingSkill");
            case DIAMOND -> getFloat("diamondCraftingSkill");
            case NETHERITE -> getFloat("netheriteCraftingSkill");
            case PRISMARINE -> getFloat("prismarineCraftingSkill");
            case ENDERIC -> getFloat("endericCraftingSkill");
            case CUSTOM_1 -> getFloat("custom1CraftingSkill");
            case CUSTOM_2 -> getFloat("custom2CraftingSkill");
            case CUSTOM_3 -> getFloat("custom3CraftingSkill");
            case CUSTOM_4 -> getFloat("custom4CraftingSkill");
            case CUSTOM_5 -> getFloat("custom5CraftingSkill");
            case CUSTOM_6 -> getFloat("custom6CraftingSkill");
            case CUSTOM_7 -> getFloat("custom7CraftingSkill");
            case CUSTOM_8 -> getFloat("custom8CraftingSkill");
            case CUSTOM_9 -> getFloat("custom9CraftingSkill");
            case CUSTOM_10 -> getFloat("custom10CraftingSkill");
            case OTHER -> getFloat("otherCraftingSkill");
        };
    }
    public void setCraftingSkill(MaterialClass material, float value){
        if (material == null) setFloat("genericCraftingSkill", value);
        else
            switch (material) {
                case BOW -> setFloat("bowCraftingSkill", value);
                case CROSSBOW -> setFloat("crossbowCraftingSkill", value);
                case WOOD -> setFloat("woodCraftingSkill", value);
                case LEATHER -> setFloat("leatherCraftingSkill", value);
                case STONE -> setFloat("stoneCraftingSkill", value);
                case COPPER -> setFloat("copperCraftingSkill", value);
                case CHAINMAIL -> setFloat("chainCraftingSkill", value);
                case GOLD -> setFloat("goldCraftingSkill", value);
                case IRON -> setFloat("ironCraftingSkill", value);
                case DIAMOND -> setFloat("diamondCraftingSkill", value);
                case NETHERITE -> setFloat("netheriteCraftingSkill", value);
                case PRISMARINE -> setFloat("prismarineCraftingSkill", value);
                case ENDERIC -> setFloat("endericCraftingSkill", value);
                case OTHER -> setFloat("otherCraftingSkill", value);
                case CUSTOM_1 -> setFloat("custom1CraftingSkill", value);
                case CUSTOM_2 -> setFloat("custom2CraftingSkill", value);
                case CUSTOM_3 -> setFloat("custom3CraftingSkill", value);
                case CUSTOM_4 -> setFloat("custom4CraftingSkill", value);
                case CUSTOM_5 -> setFloat("custom5CraftingSkill", value);
                case CUSTOM_6 -> setFloat("custom6CraftingSkill", value);
                case CUSTOM_7 -> setFloat("custom7CraftingSkill", value);
                case CUSTOM_8 -> setFloat("custom8CraftingSkill", value);
                case CUSTOM_9 -> setFloat("custom9CraftingSkill", value);
                case CUSTOM_10 -> setFloat("custom10CraftingSkill", value);
            }
    }

    public float getCraftingSkillFractionBonus(MaterialClass material){
        if (material == null) return getFloat("genericCraftingSkillFractionBonus");
        return switch (material){
            case BOW -> getFloat("bowCraftingSkillFractionBonus");
            case CROSSBOW -> getFloat("crossbowCraftingSkillFractionBonus");
            case WOOD -> getFloat("woodCraftingSkillFractionBonus");
            case LEATHER -> getFloat("leatherCraftingSkillFractionBonus");
            case STONE -> getFloat("stoneCraftingSkillFractionBonus");
            case COPPER -> getFloat("copperCraftingSkillFractionBonus");
            case CHAINMAIL -> getFloat("chainCraftingSkillFractionBonus");
            case GOLD ->getFloat("goldCraftingSkillFractionBonus");
            case IRON -> getFloat("ironCraftingSkillFractionBonus");
            case DIAMOND -> getFloat("diamondCraftingSkillFractionBonus");
            case NETHERITE -> getFloat("netheriteCraftingSkillFractionBonus");
            case PRISMARINE -> getFloat("prismarineCraftingSkillFractionBonus");
            case ENDERIC -> getFloat("endericCraftingSkillFractionBonus");
            case CUSTOM_1 -> getFloat("custom1CraftingSkillFractionBonus");
            case CUSTOM_2 -> getFloat("custom2CraftingSkillFractionBonus");
            case CUSTOM_3 -> getFloat("custom3CraftingSkillFractionBonus");
            case CUSTOM_4 -> getFloat("custom4CraftingSkillFractionBonus");
            case CUSTOM_5 -> getFloat("custom5CraftingSkillFractionBonus");
            case CUSTOM_6 -> getFloat("custom6CraftingSkillFractionBonus");
            case CUSTOM_7 -> getFloat("custom7CraftingSkillFractionBonus");
            case CUSTOM_8 -> getFloat("custom8CraftingSkillFractionBonus");
            case CUSTOM_9 -> getFloat("custom9CraftingSkillFractionBonus");
            case CUSTOM_10 -> getFloat("custom10CraftingSkillFractionBonus");
            case OTHER -> getFloat("otherCraftingSkillFractionBonus");
        };
    }
    public void setCraftingSkillFractionBonus(MaterialClass material, float value){
        if (material == null) setFloat("genericCraftingSkillFractionBonus", value);
        else
            switch (material) {
                case BOW -> setFloat("bowCraftingSkillFractionBonus", value);
                case CROSSBOW -> setFloat("crossbowCraftingSkillFractionBonus", value);
                case WOOD -> setFloat("woodCraftingSkillFractionBonus", value);
                case LEATHER -> setFloat("leatherCraftingSkillFractionBonus", value);
                case STONE -> setFloat("stoneCraftingSkillFractionBonus", value);
                case COPPER -> setFloat("copperCraftingSkillFractionBonus", value);
                case CHAINMAIL -> setFloat("chainCraftingSkillFractionBonus", value);
                case GOLD -> setFloat("goldCraftingSkillFractionBonus", value);
                case IRON -> setFloat("ironCraftingSkillFractionBonus", value);
                case DIAMOND -> setFloat("diamondCraftingSkillFractionBonus", value);
                case NETHERITE -> setFloat("netheriteCraftingSkillFractionBonus", value);
                case PRISMARINE -> setFloat("prismarineCraftingSkillFractionBonus", value);
                case ENDERIC -> setFloat("endericCraftingSkillFractionBonus", value);
                case OTHER -> setFloat("otherCraftingSkillFractionBonus", value);
                case CUSTOM_1 -> setFloat("custom1CraftingSkillFractionBonus", value);
                case CUSTOM_2 -> setFloat("custom2CraftingSkillFractionBonus", value);
                case CUSTOM_3 -> setFloat("custom3CraftingSkillFractionBonus", value);
                case CUSTOM_4 -> setFloat("custom4CraftingSkillFractionBonus", value);
                case CUSTOM_5 -> setFloat("custom5CraftingSkillFractionBonus", value);
                case CUSTOM_6 -> setFloat("custom6CraftingSkillFractionBonus", value);
                case CUSTOM_7 -> setFloat("custom7CraftingSkillFractionBonus", value);
                case CUSTOM_8 -> setFloat("custom8CraftingSkillFractionBonus", value);
                case CUSTOM_9 -> setFloat("custom9CraftingSkillFractionBonus", value);
                case CUSTOM_10 -> setFloat("custom10CraftingSkillFractionBonus", value);
            }
    }

    public double getAllSkillEXPGain(MaterialClass material){
        if (material == null) return getDouble("genericEXPMultiplier");
        return switch (material){
            case BOW -> getDouble("bowEXPMultiplier");
            case CROSSBOW -> getDouble("crossbowEXPMultiplier");
            case WOOD -> getDouble("woodEXPMultiplier");
            case LEATHER -> getDouble("leatherEXPMultiplier");
            case STONE -> getDouble("stoneEXPMultiplier");
            case COPPER -> getDouble("copperEXPMultiplier");
            case CHAINMAIL -> getDouble("chainEXPMultiplier");
            case GOLD ->getDouble("goldEXPMultiplier");
            case IRON -> getDouble("ironEXPMultiplier");
            case DIAMOND -> getDouble("diamondEXPMultiplier");
            case NETHERITE -> getDouble("netheriteEXPMultiplier");
            case PRISMARINE -> getDouble("prismarineEXPMultiplier");
            case ENDERIC -> getDouble("endericEXPMultiplier");
            case CUSTOM_1 -> getDouble("custom1EXPMultiplier");
            case CUSTOM_2 -> getDouble("custom2EXPMultiplier");
            case CUSTOM_3 -> getDouble("custom3EXPMultiplier");
            case CUSTOM_4 -> getDouble("custom4EXPMultiplier");
            case CUSTOM_5 -> getDouble("custom5EXPMultiplier");
            case CUSTOM_6 -> getDouble("custom6EXPMultiplier");
            case CUSTOM_7 -> getDouble("custom7EXPMultiplier");
            case CUSTOM_8 -> getDouble("custom8EXPMultiplier");
            case CUSTOM_9 -> getDouble("custom9EXPMultiplier");
            case CUSTOM_10 -> getDouble("custom10EXPMultiplier");
            case OTHER -> getDouble("otherEXPMultiplier");
        };
    }
    public void setAllSkillEXPGain(MaterialClass material, double value){
        if (material == null) setDouble("genericEXPMultiplier", value);
        else
            switch (material) {
                case BOW -> setDouble("bowEXPMultiplier", value);
                case CROSSBOW -> setDouble("crossbowEXPMultiplier", value);
                case WOOD -> setDouble("woodEXPMultiplier", value);
                case LEATHER -> setDouble("leatherEXPMultiplier", value);
                case COPPER -> setDouble("copperEXPMultiplier", value);
                case STONE -> setDouble("stoneEXPMultiplier", value);
                case CHAINMAIL -> setDouble("chainEXPMultiplier", value);
                case GOLD -> setDouble("goldEXPMultiplier", value);
                case IRON -> setDouble("ironEXPMultiplier", value);
                case DIAMOND -> setDouble("diamondEXPMultiplier", value);
                case NETHERITE -> setDouble("netheriteEXPMultiplier", value);
                case PRISMARINE -> setDouble("prismarineEXPMultiplier", value);
                case ENDERIC -> setDouble("endericEXPMultiplier", value);
                case OTHER -> setDouble("otherEXPMultiplier", value);
                case CUSTOM_1 -> setDouble("custom1EXPMultiplier", value);
                case CUSTOM_2 -> setDouble("custom2EXPMultiplier", value);
                case CUSTOM_3 -> setDouble("custom3EXPMultiplier", value);
                case CUSTOM_4 -> setDouble("custom4EXPMultiplier", value);
                case CUSTOM_5 -> setDouble("custom5EXPMultiplier", value);
                case CUSTOM_6 -> setDouble("custom6EXPMultiplier", value);
                case CUSTOM_7 -> setDouble("custom7EXPMultiplier", value);
                case CUSTOM_8 -> setDouble("custom8EXPMultiplier", value);
                case CUSTOM_9 -> setDouble("custom9EXPMultiplier", value);
                case CUSTOM_10 -> setDouble("custom10EXPMultiplier", value);
            }
    }

    public SmithingProfile(UUID owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
        return "profiles_smithing";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "profile_smithing");

    @Override
    public Profile getBlankProfile(UUID owner) {
        return ProfileRegistry.copyDefaultStats(new SmithingProfile(owner));
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return SmithingSkill.class;
    }
}
