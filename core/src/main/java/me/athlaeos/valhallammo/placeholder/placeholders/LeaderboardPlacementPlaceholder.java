package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.placeholder.Placeholder;
import me.athlaeos.valhallammo.playerstats.LeaderboardEntry;
import me.athlaeos.valhallammo.playerstats.LeaderboardManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class LeaderboardPlacementPlaceholder extends Placeholder {
    private final String leaderboard;

    public LeaderboardPlacementPlaceholder(String placeholder, String leaderboard) {
        super(placeholder);
        this.leaderboard = leaderboard;
    }

    @Override
    public String parse(String s, Player p) {
        LeaderboardManager.Leaderboard l = LeaderboardManager.getLeaderboards().get(leaderboard);
        LeaderboardEntry entry = LeaderboardManager.getCachedLeaderboardsByPlayer().getOrDefault(leaderboard, new HashMap<>()).get(p.getUniqueId());
        if (entry == null || l == null || entry.playerName() == null) return s.replace(this.placeholder, "");
        StatFormat format = Profile.getFormat(l.profile(), l.mainStat());
        String finalEntry = l.placeholderDisplay()
                .replace("%rank%", String.valueOf(entry.place()))
                .replace("%player%", entry.playerName())
                .replace("%main_stat%", format == null ? "" : format.format(entry.mainStat()));
        for (String e : entry.extraStats().keySet()){
            double value = entry.extraStats().get(e);
            StatFormat f = Profile.getFormat(l.profile(), e);
            finalEntry = finalEntry.replace("%" + e + "%", f == null ? "" : f.format(value));
        }
        return s.replace(this.placeholder, finalEntry);
    }
}
