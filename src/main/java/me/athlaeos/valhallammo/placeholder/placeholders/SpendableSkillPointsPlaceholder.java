package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.placeholder.Placeholder;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import org.bukkit.entity.Player;

public class SpendableSkillPointsPlaceholder extends Placeholder {
    public SpendableSkillPointsPlaceholder(String placeholder) {
        super(placeholder);
    }

    @Override
    public String parse(String s, Player p) {
        PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
        int points = profile.getSpendableSkillPoints() - profile.getSpentSkillPoints();
        return s.replace(this.placeholder, String.format("%,d", points));
    }
}
