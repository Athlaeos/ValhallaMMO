package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.placeholder.Placeholder;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import org.bukkit.entity.Player;

public class ProfileNextLevelPlaceholder extends Placeholder {
    private StatFormat format;
    private final Class<? extends Profile> type;

    public ProfileNextLevelPlaceholder(String placeholder, Class<? extends Profile> type, StatFormat format) {
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
        Profile profile = ProfileCache.getOrCache(p, type);
        return s.replace(placeholder, format.format(Math.min(SkillRegistry.getSkill(profile.getSkillType()).getMaxLevel(), profile.getLevel() + 1)));
    }
}
