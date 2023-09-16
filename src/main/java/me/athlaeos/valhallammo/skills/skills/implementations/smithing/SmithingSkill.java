package me.athlaeos.valhallammo.skills.skills.implementations.smithing;

import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.skills.skills.Skill;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class SmithingSkill extends Skill {
    public SmithingSkill(String type) {
        super(type);
        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/smithing.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/smithing_progression.yml").get();

        loadCommonConfig(skillConfig, progressionConfig);
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return SmithingProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return 5;
    }

    @Override
    public boolean isExperienceScaling() {
        return true;
    }

    @Override
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            amount *= AccumulativeStatManager.getStats("SMITHING_EXP_GAIN", p, true);
        }
        super.addEXP(p, amount, silent, reason);
    }
}
