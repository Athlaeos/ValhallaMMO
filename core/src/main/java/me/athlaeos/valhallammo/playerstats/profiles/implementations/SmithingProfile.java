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
        floatStat("genericCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P1).min(0).perkReward().create());
        floatStat("bowCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P1).min(0).perkReward().create());
        floatStat("crossbowCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P1).min(0).perkReward().create());
        floatStat("woodCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P1).min(0).perkReward().create());
        floatStat("leatherCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P1).min(0).perkReward().create());
        floatStat("stoneCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P1).min(0).perkReward().create());
        floatStat("chainCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P1).min(0).perkReward().create());
        floatStat("goldCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P1).min(0).perkReward().create());
        floatStat("ironCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P1).min(0).perkReward().create());
        floatStat("diamondCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P1).min(0).perkReward().create());
        floatStat("netheriteCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P1).min(0).perkReward().create());
        floatStat("prismarineCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P1).min(0).perkReward().create());
        floatStat("endericCraftingSkill", new PropertyBuilder().format(StatFormat.FLOAT_P1).min(0).perkReward().create());

        floatStat("genericCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("bowCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("crossbowCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("woodCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("leatherCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("stoneCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("chainCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("goldCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("ironCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("diamondCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("netheriteCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("prismarineCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("endericCraftingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());

        doubleStat("genericEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("bowEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("crossbowEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("woodEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("leatherEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("stoneEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("chainEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("goldEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("ironEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("diamondEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("netheriteEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("prismarineEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        doubleStat("endericEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
    }

    public float getCraftingSkill(MaterialClass material){
        if (material == null) return getFloat("genericCraftingSkill");
        return switch (material){
            case BOW -> getFloat("bowCraftingSkill");
            case CROSSBOW -> getFloat("crossbowCraftingSkill");
            case WOOD -> getFloat("woodCraftingSkill");
            case LEATHER -> getFloat("leatherCraftingSkill");
            case STONE -> getFloat("stoneCraftingSkill");
            case CHAINMAIL -> getFloat("chainCraftingSkill");
            case GOLD ->getFloat("goldCraftingSkill");
            case IRON -> getFloat("ironCraftingSkill");
            case DIAMOND -> getFloat("diamondCraftingSkill");
            case NETHERITE -> getFloat("netheriteCraftingSkill");
            case PRISMARINE -> getFloat("prismarineCraftingSkill");
            case ENDERIC -> getFloat("endericCraftingSkill");
            case OTHER -> getFloat("genericCraftingSkill");
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
                case CHAINMAIL -> setFloat("chainCraftingSkill", value);
                case GOLD -> setFloat("goldCraftingSkill", value);
                case IRON -> setFloat("ironCraftingSkill", value);
                case DIAMOND -> setFloat("diamondCraftingSkill", value);
                case NETHERITE -> setFloat("netheriteCraftingSkill", value);
                case PRISMARINE -> setFloat("prismarineCraftingSkill", value);
                case ENDERIC -> setFloat("endericCraftingSkill", value);
                case OTHER -> setFloat("genericCraftingSkill", value);
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
            case CHAINMAIL -> getFloat("chainCraftingSkillFractionBonus");
            case GOLD ->getFloat("goldCraftingSkillFractionBonus");
            case IRON -> getFloat("ironCraftingSkillFractionBonus");
            case DIAMOND -> getFloat("diamondCraftingSkillFractionBonus");
            case NETHERITE -> getFloat("netheriteCraftingSkillFractionBonus");
            case PRISMARINE -> getFloat("prismarineCraftingSkillFractionBonus");
            case ENDERIC -> getFloat("endericCraftingSkillFractionBonus");
            case OTHER -> getFloat("genericCraftingSkillFractionBonus");
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
                case CHAINMAIL -> setFloat("chainCraftingSkillFractionBonus", value);
                case GOLD -> setFloat("goldCraftingSkillFractionBonus", value);
                case IRON -> setFloat("ironCraftingSkillFractionBonus", value);
                case DIAMOND -> setFloat("diamondCraftingSkillFractionBonus", value);
                case NETHERITE -> setFloat("netheriteCraftingSkillFractionBonus", value);
                case PRISMARINE -> setFloat("prismarineCraftingSkillFractionBonus", value);
                case ENDERIC -> setFloat("endericCraftingSkillFractionBonus", value);
                case OTHER -> setFloat("genericCraftingSkillFractionBonus", value);
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
            case CHAINMAIL -> getDouble("chainEXPMultiplier");
            case GOLD ->getDouble("goldEXPMultiplier");
            case IRON -> getDouble("ironEXPMultiplier");
            case DIAMOND -> getDouble("diamondEXPMultiplier");
            case NETHERITE -> getDouble("netheriteEXPMultiplier");
            case PRISMARINE -> getDouble("prismarineEXPMultiplier");
            case ENDERIC -> getDouble("endericEXPMultiplier");
            case OTHER -> getDouble("genericEXPMultiplier");
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
                case STONE -> setDouble("stoneEXPMultiplier", value);
                case CHAINMAIL -> setDouble("chainEXPMultiplier", value);
                case GOLD -> setDouble("goldEXPMultiplier", value);
                case IRON -> setDouble("ironEXPMultiplier", value);
                case DIAMOND -> setDouble("diamondEXPMultiplier", value);
                case NETHERITE -> setDouble("netheriteEXPMultiplier", value);
                case PRISMARINE -> setDouble("prismarineEXPMultiplier", value);
                case ENDERIC -> setDouble("endericEXPMultiplier", value);
                case OTHER -> setDouble("genericEXPMultiplier", value);
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
