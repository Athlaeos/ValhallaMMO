package me.athlaeos.valhallammo.playerstats;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.SmithingProfile;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class LeaderboardManager {
    private static final Map<String, Leaderboard> leaderboards = new HashMap<>();
    private static final Map<String, Map<Integer, Map<UUID, LeaderboardEntry>>> cachedLeaderboardEntryPages = new HashMap<>();
    private static final Map<String, Map<UUID, LeaderboardEntry>> cachedPersonalLeaderboardEntries = new HashMap<>();
    private static final Map<Integer, String> placementPrefixes = new HashMap<>();
    private static int pageEntryLimit = 10;
    private static String personalEntryPrefix;

    public static void loadFile(){
        YamlConfiguration config = ConfigManager.getConfig("leaderboards.yml").get();

        pageEntryLimit = config.getInt("page_limit", 10);
        personalEntryPrefix = config.getString("personal_entry_prefix", "");

        ConfigurationSection placementPrefixSection = config.getConfigurationSection("placement_prefixes");
        if (placementPrefixSection != null){
            for (String key : placementPrefixSection.getKeys(false)){
                int placement = Catch.catchOrElse(() -> Integer.parseInt(key), 0);
                if (placement <= 0) return;
                placementPrefixes.put(placement, config.getString("placement_prefixes." + key));
            }
        }

        ConfigurationSection leaderboardSection = config.getConfigurationSection("leaderboards");
        if (leaderboardSection == null) return;
        for (String leaderboard : leaderboardSection.getKeys(false)){
            Class<? extends Profile> profile = profileFromString(config.getString("leaderboards." + leaderboard + ".profile"));
            if (profile == null) {
                ValhallaMMO.logWarning("Profile in leaderboard " + leaderboard + " in leaderboards.yml is not a valid registered profile! Skipped this leaderboard");
                continue;
            }
            String displayName = config.getString("leaderboards." + leaderboard + ".display_name");
            String format = config.getString("leaderboards." + leaderboard + ".entry_display");
            double lowerLimit = config.getDouble("leaderboards." + leaderboard + ".lower_limit", -999999);
            String mainStat = config.getString("leaderboards." + leaderboard + ".main_stat");
            Map<String, String> extraStats = new HashMap<>();
            ConfigurationSection extraStatSection = config.getConfigurationSection("leaderboards." + leaderboard + ".extra_stats");
            if (extraStatSection != null) extraStatSection.getKeys(false).forEach(e -> extraStats.put(e, config.getString("leaderboards." + leaderboard + ".extra_stats." + e)));
            leaderboards.put(leaderboard, new Leaderboard(leaderboard, profile, mainStat, displayName, format, extraStats, lowerLimit));
        }
    }

    public static void sendLeaderboard(CommandSender s, String leaderboard, int page){
        Leaderboard l = leaderboards.get(leaderboard);
        if (cachedLeaderboardEntryPages.containsKey(leaderboard) && cachedLeaderboardEntryPages.get(leaderboard).containsKey(page)){
            Map<UUID, LeaderboardEntry> p = cachedLeaderboardEntryPages.get(leaderboard).get(page);
            Utils.sendMessage(s, TranslationManager.translatePlaceholders(l.displayName).replace("%page%", String.valueOf(page)));
            for (LeaderboardEntry entry : p.values()){
                if (entry.mainStat() < l.lowerLimit) continue;
                Utils.sendMessage(s, entryString(l, entry));
            }
        } else {
            ProfileRegistry.getPersistence().queryLeaderboardEntries(l.profile, "exp_total", page, (e) -> {
                Map<Integer, Map<UUID, LeaderboardEntry>> cachedPages = cachedLeaderboardEntryPages.getOrDefault(l.key, new HashMap<>());
                Map<UUID, LeaderboardEntry> cachedPage = cachedPages.getOrDefault(page, new HashMap<>());
                Utils.sendMessage(s, TranslationManager.translatePlaceholders(l.displayName).replace("%page%", String.valueOf(page)));
                e.forEach(le -> {
                    Utils.sendMessage(s, entryString(l, le));
                    cachedPage.put(le.playerUUID(), le);
                });
                cachedPages.put(page, cachedPage);
                cachedLeaderboardEntryPages.put(l.key, cachedPages);
            }, l.extraStats.values());
        }
    }

    private static StatFormat format(Class<? extends Profile> type, String stat){
        Profile profile = ProfileRegistry.getRegisteredProfiles().get(type);
        for (String i : profile.intStatNames()){
            if (!i.equals(stat)) continue;
            return profile.getNumberStatProperties().get(i).getFormat();
        }
        for (String i : profile.floatStatNames()){
            if (!i.equals(stat)) continue;
            return profile.getNumberStatProperties().get(i).getFormat();
        }
        for (String i : profile.doubleStatNames()){
            if (!i.equals(stat)) continue;
            return profile.getNumberStatProperties().get(i).getFormat();
        }
        return null;
    }

    private static Class<? extends Profile> profileFromString(String s){
        if (s == null) return null;
        for (Class<? extends Profile> clazz : ProfileRegistry.getRegisteredProfiles().keySet()){
            if (clazz.getSimpleName().equals(s)) return clazz;
        }
        return null;
    }

    private static String entryString(Leaderboard stat, LeaderboardEntry entry){
        StatFormat format = format(stat.profile, stat.mainStat);
        String finalEntry = stat.entryFormat
                .replace("%rank%", String.valueOf(entry.place()))
                .replace("%prefix%", placementPrefixes.getOrDefault(entry.place(), ""))
                .replace("%player%", entry.playerName())
                .replace("%main_stat%", format == null ? "" : format.format(entry.mainStat()));
        for (String e : entry.extraStats().keySet()){
            double value = entry.extraStats().get(e);
            StatFormat f = format(stat.profile, e);
            finalEntry = finalEntry.replace("%" + e + "%", f == null ? "" : f.format(value));
        }
        return finalEntry;
    }

    public static void resetLeaderboard(){
        cachedLeaderboardEntryPages.clear();
    }

    public static Map<String, Leaderboard> getLeaderboards() {
        return leaderboards;
    }

    public static int getPageEntryLimit() {
        return pageEntryLimit;
    }

    public record Leaderboard(String key, Class<? extends Profile> profile, String mainStat, String displayName, String entryFormat, Map<String, String> extraStats, double lowerLimit){}
}
