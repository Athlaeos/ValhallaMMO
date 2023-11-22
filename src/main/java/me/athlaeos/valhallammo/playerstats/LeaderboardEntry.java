package me.athlaeos.valhallammo.playerstats;

import java.util.Map;
import java.util.UUID;

public record LeaderboardEntry(String playerName, UUID playerUUID, Double mainStat, int place, Map<String, Double> extraStats) {
}
