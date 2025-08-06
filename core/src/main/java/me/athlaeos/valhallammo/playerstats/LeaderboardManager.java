package me.athlaeos.valhallammo.playerstats;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.placeholder.PlaceholderRegistry;
import me.athlaeos.valhallammo.placeholder.placeholders.LeaderboardPlaceholder;
import me.athlaeos.valhallammo.placeholder.placeholders.LeaderboardPlacementPlaceholder;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

public class LeaderboardManager {
    private static final Map<String, Leaderboard> leaderboards = new HashMap<>();
    private static final Map<Integer, String> placementPrefixes = new HashMap<>();
    private static final Map<String, Map<Integer, LeaderboardEntry>> cachedLeaderboardsByRank = new HashMap<>();
    private static final Map<String, Map<UUID, LeaderboardEntry>> cachedLeaderboardsByPlayer = new HashMap<>();
    private static final Collection<String> excludedPlayers = new HashSet<>(ConfigManager.getConfig("leaderboards.yml").get().getStringList("excluded"));
    private static int pageEntryLimit = 10;
    private static String personalEntryPrefix;

    public static void loadFile(){
        YamlConfiguration config = ConfigManager.getConfig("leaderboards.yml").reload().get();

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
            String placeholderDisplay = config.getString("leaderboards." + leaderboard + ".placeholder_display");
            String format = config.getString("leaderboards." + leaderboard + ".entry_display");
            double lowerLimit = config.getDouble("leaderboards." + leaderboard + ".lower_limit", -999999);
            String mainStat = config.getString("leaderboards." + leaderboard + ".main_stat");
            if (mainStat == null) continue;
            Pair<String, Double> mainStatRequirement;
            if (mainStat.contains(":")) {
                String[] split = mainStat.split(":");
                Double minimumValue = Double.parseDouble(split[1]);
                String stat = split[0];
                mainStatRequirement = new Pair<>(stat, minimumValue);
            } else {
                mainStatRequirement = new Pair<>(mainStat, null);
            }
            Map<String, Pair<String, Double>> extraStats = new LinkedHashMap<>();
            ConfigurationSection extraStatSection = config.getConfigurationSection("leaderboards." + leaderboard + ".extra_stats");
            if (extraStatSection != null) {
                for (String statName : extraStatSection.getKeys(false)) {
                    String statTotal = config.getString("leaderboards." + leaderboard + ".extra_stats." + statName);
                    if (statTotal == null) continue;
                    Double minimum = null;
                    String stat = statTotal;
                    if (statTotal.contains(":")) {
                        String[] split = statTotal.split(":");
                        stat = split[0];
                        minimum = Catch.catchOrElse(() -> Double.parseDouble(split[1]), null, "Invalid minimum " + split[0] + " value passed to leaderboard " + leaderboard + ", " + split[1] + " is not a number");
                    }
                    extraStats.put(statName, new Pair<>(stat, minimum));
                }
            }
            leaderboards.put(leaderboard, new Leaderboard(leaderboard, profile, mainStatRequirement, displayName, placeholderDisplay, format, extraStats, lowerLimit));

            // placeholder format leaderboard_<leaderboard>_<place>
            // place from 1-10
            for (int i = 1; i <= 10; i++){
                PlaceholderRegistry.registerPlaceholder(new LeaderboardPlaceholder("%leaderboard_" + leaderboard + "_" + i + "%", leaderboard, i));
            }
            PlaceholderRegistry.registerPlaceholder(new LeaderboardPlacementPlaceholder("%leaderboard_" + leaderboard + "_placement%", leaderboard));
        }
    }

    public static Map<String, Map<Integer, LeaderboardEntry>> getCachedLeaderboardsByRank() {
        return cachedLeaderboardsByRank;
    }

    public static Map<String, Map<UUID, LeaderboardEntry>> getCachedLeaderboardsByPlayer() {
        return cachedLeaderboardsByPlayer;
    }

    public static void cache(Leaderboard l, Map<Integer, LeaderboardEntry> ranks){
        cachedLeaderboardsByRank.put(l.key, ranks);
        Map<UUID, LeaderboardEntry> personalEntries = new HashMap<>();
        for (LeaderboardEntry entry : ranks.values()) {
            personalEntries.put(entry.playerUUID(), entry);
        }
        cachedLeaderboardsByPlayer.put(l.key, personalEntries);
    }

    public static void refreshLeaderboards(){
        for (String leaderboard : LeaderboardManager.getLeaderboards().keySet()){
            LeaderboardManager.fetchLeaderboard(leaderboard, false, (map) -> {
                cachedLeaderboardsByRank.put(leaderboard, new HashMap<>());
                cachedLeaderboardsByPlayer.put(leaderboard, new HashMap<>());
                cache(leaderboards.get(leaderboard), map);
            }, true);
        }
    }

    public static void fetchLeaderboard(String leaderboard, boolean cache, Consumer<Map<Integer, LeaderboardEntry>> callback, boolean reload){
        Leaderboard l = leaderboards.get(leaderboard);
        if (l == null) return;
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
            if (cachedLeaderboardsByRank.containsKey(l.key) && !reload) {
                if (callback != null) callback.accept(cachedLeaderboardsByRank.get(l.key));
            } else {
                Map<Integer, LeaderboardEntry> results = ProfileRegistry.getPersistence().queryLeaderboardEntries(l);
                if (cache) cache(l, results);
                if (callback != null) callback.accept(results);
            }
        });
    }

    public static void fetchLeaderboardEntry(UUID uuid, String leaderboard, Consumer<LeaderboardEntry> callback){
        Leaderboard l = leaderboards.get(leaderboard);
        if (l == null) return;
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
            if (!cachedLeaderboardsByRank.containsKey(l.key)) cache(l, ProfileRegistry.getPersistence().queryLeaderboardEntries(l));
            LeaderboardEntry entry = cachedLeaderboardsByPlayer.getOrDefault(l.key, new HashMap<>()).get(uuid);
            if (entry == null) return;
            callback.accept(entry);
        });
    }

    public static void sendLeaderboard(CommandSender s, String leaderboard, int page){
        Leaderboard l = leaderboards.get(leaderboard);
        fetchLeaderboard(l.key, true, (r) -> {
            int realPage = Math.min((int) Math.ceil(r.size() / (double) pageEntryLimit), page);
            Utils.sendMessage(s, TranslationManager.translatePlaceholders(l.displayName).replace("%page%", String.valueOf(realPage)));
            for (int rank = ((realPage - 1) * pageEntryLimit) + 1; rank <= (realPage) * pageEntryLimit; rank++){
                LeaderboardEntry entry = cachedLeaderboardsByRank.get(l.key).get(rank);
                if (entry == null) break;
                Utils.sendMessage(s, entryString(l, entry));
            }

            if (s instanceof Player p){
                fetchLeaderboardEntry(p.getUniqueId(), l.key, (e) -> {
                    if (e == null) return;
                    Utils.sendMessage(s, " ");
                    Utils.sendMessage(s, TranslationManager.translatePlaceholders(personalEntryPrefix).replace("%player%", p.getName()) + entryString(l, e));
                });
            }
        }, false);
    }

    private static Class<? extends Profile> profileFromString(String s){
        if (s == null) return null;
        for (Class<? extends Profile> clazz : ProfileRegistry.getRegisteredProfiles().keySet()){
            if (clazz.getSimpleName().equals(s)) return clazz;
        }
        return null;
    }

    private static String entryString(Leaderboard stat, LeaderboardEntry entry){
        Profile profile = ProfileRegistry.getRegisteredProfiles().get(stat.profile);
        StatFormat format = profile.getStatFormat(stat.mainStat.getOne());
        String finalEntry = stat.entryFormat
                .replace("%rank%", String.valueOf(entry.place()))
                .replace("%prefix%", placementPrefixes.getOrDefault(entry.place(), ""))
                .replace("%player%", entry.playerName())
                .replace("%main_stat%", format == null ? "" : format.format(entry.mainStat()));
        for (String e : entry.extraStats().keySet()){
            double value = entry.extraStats().get(e);
            StatFormat f = profile.getStatFormat(e);
            finalEntry = finalEntry.replace("%" + e + "%", f == null ? "" : f.format(value));
        }
        return finalEntry;
    }

    public static void resetLeaderboard(){
        cachedLeaderboardsByRank.clear();
    }

    public static Map<String, Leaderboard> getLeaderboards() {
        return leaderboards;
    }

    public static int getPageEntryLimit() {
        return pageEntryLimit;
    }

    public record Leaderboard(String key, Class<? extends Profile> profile, Pair<String, Double> mainStat, String displayName, String placeholderDisplay, String entryFormat, Map<String, Pair<String, Double>> extraStats, double lowerLimit){}

    public static Collection<String> getExcludedPlayers() {
        return excludedPlayers;
    }
}
