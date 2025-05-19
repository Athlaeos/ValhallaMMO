package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.TradingProfile;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.trading.dom.MerchantLevel;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class TradingSkill extends Skill implements Listener {
    private double expDiscountMultiplier = 4;
    private double noviceExpMultiplier = 1;
    private double apprenticeExpMultiplier = 1.1;
    private double journeymanExpMultiplier = 1.3;
    private double expertExpMultiplier = 1.6;
    private double masterExpMultiplier = 2;

    public TradingSkill(String type) {
        super(type);
    }

    @Override
    public void loadConfiguration() {
        ValhallaMMO.getInstance().save("skills/trading_progression.yml");
        ValhallaMMO.getInstance().save("skills/trading.yml");

        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/trading.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/trading_progression.yml").get();

        this.expDiscountMultiplier = progressionConfig.getDouble("experience.exp_discount_multiplier", 4);
        this.noviceExpMultiplier = progressionConfig.getDouble("experience.exp_multiplier_novice", 1);
        this.apprenticeExpMultiplier = progressionConfig.getDouble("experience.exp_multiplier_apprentice", 1.1);
        this.journeymanExpMultiplier = progressionConfig.getDouble("experience.exp_multiplier_journeyman", 1.3);
        this.expertExpMultiplier = progressionConfig.getDouble("experience.exp_multiplier_expert", 1.6);
        this.masterExpMultiplier = progressionConfig.getDouble("experience.exp_multiplier_master", 2);

        loadCommonConfig(skillConfig, progressionConfig);
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return TradingProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return 47;
    }

    @Override
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_TRADING)) return;
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            double multiplier = Math.max(0, 1 + AccumulativeStatManager.getCachedStats("TRADING_EXP_GAIN", p, 10000, true));
            amount *= multiplier;
        }
        super.addEXP(p, amount, silent, reason);
    }

    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason, MerchantLevel level, double ratio) {
        double multiplier = level == null ? 1 : switch (level) {
            case NOVICE -> noviceExpMultiplier;
            case APPRENTICE -> apprenticeExpMultiplier;
            case JOURNEYMAN -> journeymanExpMultiplier;
            case EXPERT -> expertExpMultiplier;
            case MASTER -> masterExpMultiplier;
        };
        amount *= multiplier;
        amount *= Math.max(0, expDiscountMultiplier * ratio);
        addEXP(p, amount, silent, reason);
    }
}
