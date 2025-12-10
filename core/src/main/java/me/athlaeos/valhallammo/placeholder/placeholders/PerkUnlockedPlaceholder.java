package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.placeholder.Placeholder;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

public class PerkUnlockedPlaceholder extends Placeholder {
    public PerkUnlockedPlaceholder(String placeholder) {
        super(placeholder);
    }

    @Override
    public String parse(String s, Player p) {
        String base = placeholder.replace("%", "");
        String subString = StringUtils.substringBetween(s,"%" + base, "%");
        if (subString == null) return s;
        PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
        boolean result = profile.getUnlockedPerks().contains(subString) || profile.getPermanentlyUnlockedPerks().contains(subString) || profile.getFakeUnlockedPerks().contains(subString);
        return s.replace(String.format("%%%s%s%%", base, subString), String.format("%s", result));
    }

    @Override
    public boolean matchString(String string) {
        String base = placeholder.replace("%", "");
        String subString = StringUtils.substringBetween(string,"%" + base, "%");
        return subString != null;
    }
}
