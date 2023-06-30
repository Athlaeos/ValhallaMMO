package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.placeholder.Placeholder;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.progression.skills.Skill;
import me.athlaeos.valhallammo.progression.skills.SkillRegistry;
import org.bukkit.entity.Player;

public class ProfileNextLevelEXPPlaceholder extends Placeholder {
    private StatFormat format;
    private final Class<? extends Profile> type;

    public ProfileNextLevelEXPPlaceholder(String placeholder, Class<? extends Profile> type, StatFormat format) {
        super(placeholder);
        this.type = type;
        this.format = format;
    }

    public StatFormat getFormat() {
        return format;
    }

    public void setFormat(StatFormat format) {
        this.format = format;
    }

    @Override
    public String parse(String s, Player p) {
        Profile profile = ProfileCache.getOrCache(p, 10000, type);
        Skill skill = SkillRegistry.getSkill(profile.getSkillType());
        if (profile.getLevel() >= skill.getMaxLevel()) return s.replace(placeholder, TranslationManager.getTranslation("max_level"));
        return s.replace(placeholder, format.format(skill.expForLevel(profile.getLevel() + 1)));
    }
}
