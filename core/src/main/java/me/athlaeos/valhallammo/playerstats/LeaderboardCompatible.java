package me.athlaeos.valhallammo.playerstats;

import java.util.Map;

public interface LeaderboardCompatible {

    Map<Integer, LeaderboardEntry> queryLeaderboardEntries(LeaderboardManager.Leaderboard leaderboard);
}
