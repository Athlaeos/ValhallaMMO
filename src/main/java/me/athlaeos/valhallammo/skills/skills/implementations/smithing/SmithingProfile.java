package me.athlaeos.valhallammo.skills.skills.implementations.smithing;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.MaterialClass;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class SmithingProfile extends Profile {

    {
        intStat("genericCraftingSkill", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());
        intStat("bowCraftingSkill", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());
        intStat("crossbowCraftingSkill", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());
        intStat("woodCraftingSkill", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());
        intStat("leatherCraftingSkill", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());
        intStat("stoneCraftingSkill", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());
        intStat("chainCraftingSkill", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());
        intStat("goldCraftingSkill", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());
        intStat("ironCraftingSkill", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());
        intStat("diamondCraftingSkill", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());
        intStat("netheriteCraftingSkill", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());
        intStat("prismarineCraftingSkill", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());
        intStat("endericCraftingSkill", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());

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

        doubleStat("genericEXPMultiplier", 1D, new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
        doubleStat("bowEXPMultiplier", 1D, new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
        doubleStat("crossbowEXPMultiplier", 1D, new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
        doubleStat("woodEXPMultiplier", 1D, new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
        doubleStat("leatherEXPMultiplier", 1D, new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
        doubleStat("stoneEXPMultiplier", 1D, new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
        doubleStat("chainEXPMultiplier", 1D, new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
        doubleStat("goldEXPMultiplier", 1D, new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
        doubleStat("ironEXPMultiplier", 1D, new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
        doubleStat("diamondEXPMultiplier", 1D, new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
        doubleStat("netheriteEXPMultiplier", 1D, new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
        doubleStat("prismarineEXPMultiplier", 1D, new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
        doubleStat("endericEXPMultiplier", 1D, new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
    }

    public int getCraftingSkill(MaterialClass material){
        if (material == null) return getInt("genericCraftingSkill");
        return switch (material){
            case BOW -> getInt("bowCraftingSkill");
            case CROSSBOW -> getInt("crossbowCraftingSkill");
            case WOOD -> getInt("woodCraftingSkill");
            case LEATHER -> getInt("leatherCraftingSkill");
            case STONE -> getInt("stoneCraftingSkill");
            case CHAINMAIL -> getInt("chainCraftingSkill");
            case GOLD ->getInt("goldCraftingSkill");
            case IRON -> getInt("ironCraftingSkill");
            case DIAMOND -> getInt("diamondCraftingSkill");
            case NETHERITE -> getInt("netheriteCraftingSkill");
            case PRISMARINE -> getInt("prismarineCraftingSkill");
            case ENDERIC -> getInt("endericCraftingSkill");
            case OTHER -> getInt("genericCraftingSkill");
        };
    }
    public void setCraftingSkill(MaterialClass material, int value){
        if (material == null) setInt("genericCraftingSkill", value);
        else
            switch (material) {
                case BOW -> setInt("bowCraftingSkill", value);
                case CROSSBOW -> setInt("crossbowCraftingSkill", value);
                case WOOD -> setInt("woodCraftingSkill", value);
                case LEATHER -> setInt("leatherCraftingSkill", value);
                case STONE -> setInt("stoneCraftingSkill", value);
                case CHAINMAIL -> setInt("chainCraftingSkill", value);
                case GOLD -> setInt("goldCraftingSkill", value);
                case IRON -> setInt("ironCraftingSkill", value);
                case DIAMOND -> setInt("diamondCraftingSkill", value);
                case NETHERITE -> setInt("netheriteCraftingSkill", value);
                case PRISMARINE -> setInt("prismarineCraftingSkill", value);
                case ENDERIC -> setInt("endericCraftingSkill", value);
                case OTHER -> setInt("genericCraftingSkill", value);
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

    public SmithingProfile(Player owner) {
        super(owner);
    }

    @Override
    protected String getTableName() {
        return "profiles_smithing";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "profile_smithing");

    @Override
    public SmithingProfile getBlankProfile(Player owner) {
        return new SmithingProfile(owner);
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return SmithingSkill.class;
    }
}
