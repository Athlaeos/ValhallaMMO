package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.event.PlayerSkillLevelUpEvent;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.skills.skills.Skill;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PowerSkill extends Skill implements Listener {
    private double expPerLevelUp = 0;

    public PowerSkill(String type) {
        super(type);
    }

    @Override
    public void loadConfiguration() {
        ValhallaMMO.getInstance().save("skills/power_progression.yml");
        ValhallaMMO.getInstance().save("skills/power.yml");

        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/power.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/power_progression.yml").get();

        loadCommonConfig(skillConfig, progressionConfig);

        expPerLevelUp = progressionConfig.getDouble("experience.exp_gain");

        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLevelUp(PlayerSkillLevelUpEvent e){
        if (e.getSkill().equals(this)) return;
        int levelsLevelledUp = e.getLevelTo() - e.getLevelFrom();
        addEXP(e.getPlayer(), expPerLevelUp * levelsLevelledUp, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return PowerProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return 0;
    }

    @Override
    public boolean isExperienceScaling() {
        return false;
    }
}
