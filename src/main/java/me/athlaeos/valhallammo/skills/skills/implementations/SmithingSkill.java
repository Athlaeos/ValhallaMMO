package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.MaterialClass;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.SmithingProfile;
import me.athlaeos.valhallammo.skills.skills.Skill;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class SmithingSkill extends Skill {
    public SmithingSkill(String type) {
        super(type);
        ValhallaMMO.getInstance().save("skills/smithing_progression.yml");
        ValhallaMMO.getInstance().save("skills/smithing.yml");

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
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_SMITHING)) return;
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            double multiplier = Math.max(0, 1 + AccumulativeStatManager.getStats("SMITHING_EXP_GAIN_GENERAL", p, true));
            amount *= multiplier;
        }
        super.addEXP(p, amount, silent, reason);
    }

    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason, MaterialClass material) {
        double multiplier = (1 + AccumulativeStatManager.getStats("SMITHING_EXP_GAIN_" + material, p, true));
        amount *= multiplier;
        addEXP(p, amount, silent, reason);
    }
}
