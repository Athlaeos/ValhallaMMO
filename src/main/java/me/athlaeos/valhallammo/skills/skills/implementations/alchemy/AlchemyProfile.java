package me.athlaeos.valhallammo.skills.skills.implementations.alchemy;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.MaterialClass;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.smithing.SmithingSkill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class AlchemyProfile extends Profile {

    {
        intStat("genericBrewingSkill", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());
        intStat("buffBrewingSkill", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());
        intStat("debuffBrewingSkill", new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());

        floatStat("genericBrewingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("buffBrewingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("debuffBrewingSkillFractionBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());

        floatStat("brewingTimeReduction", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("potionSaveChance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("ingredientSaveChance", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("potionVelocity", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());

        doubleStat("brewingEXPMultiplier", 1D, new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).min(0).perkReward().create());
    }

    public AlchemyProfile(Player owner) {
        super(owner);
    }

    @Override
    protected String getTableName() {
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
