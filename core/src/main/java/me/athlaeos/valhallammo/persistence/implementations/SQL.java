package me.athlaeos.valhallammo.persistence.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.listeners.JoinLeaveListener;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.persistence.Database;
import me.athlaeos.valhallammo.playerstats.LeaderboardCompatible;
import me.athlaeos.valhallammo.playerstats.LeaderboardEntry;
import me.athlaeos.valhallammo.persistence.ProfilePersistence;
import me.athlaeos.valhallammo.playerstats.LeaderboardManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class SQL extends ProfilePersistence implements Database, LeaderboardCompatible {
    private final Map<UUID, Map<Class<? extends Profile>, Profile>> persistentProfiles = new HashMap<>();
    private final Map<UUID, Map<Class<? extends Profile>, Profile>> skillProfiles = new HashMap<>();

    private Connection conn;
    @Override
    public Connection getConnection() {
        YamlConfiguration config = ConfigManager.getConfig("config.yml").reload().get();
        String host = config.getString("db_host");
        String database = config.getString("db_database");
        String username = config.getString("db_username");
        String password = config.getString("db_password");
        int port = config.getInt("db_port");
        int ping_delay = config.getInt("db_ping_delay");
        try {
            if (conn != null && !conn.isClosed()) {
                return conn;
            }

            synchronized (ValhallaMMO.getInstance()) {
                if (conn != null && !conn.isClosed()) {
                    return conn;
                }
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);

                ValhallaMMO.logFine("Database connection created!");
            }
        } catch (Exception e) {
            ValhallaMMO.logInfo("Database connection failed, attempting SQLite for profile persistence");
            return null;
        }
        if (conn != null){
            new BukkitRunnable(){
                @Override
                public void run() {
                    try {
                        conn.prepareStatement("/* ping */ SELECT 1;").execute();
                    } catch (SQLException ex){
                        ValhallaMMO.logWarning("Database ping failed");
                        cancel();
                    }
                }
            }.runTaskTimerAsynchronously(ValhallaMMO.getInstance(), ping_delay, ping_delay);
        }
        return conn;
    }

    @Override
    public void addColumnIfNotExists(String tableName, String columnName, String columnType) {
        try {
            PreparedStatement procedureCreationStatement = conn.prepareStatement(
                    "SELECT " + columnName + " FROM " + tableName + ";");
            procedureCreationStatement.execute();
        } catch (SQLException e){
            try {
                PreparedStatement procedureCreationStatement = conn.prepareStatement(
                        "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType + ";");
                procedureCreationStatement.execute();
            } catch (SQLException ex){
                ValhallaMMO.logSevere("SQLException when trying to add column " + columnName + " " + columnType + " to " + tableName + ". ");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setPersistentProfile(Player p, Profile profile, Class<? extends Profile> type) {
        Map<Class<? extends Profile>, Profile> profiles = persistentProfiles.getOrDefault(p.getUniqueId(), new HashMap<>());
        profiles.put(type, profile);
        persistentProfiles.put(p.getUniqueId(), profiles);
        ProfilePersistence.scheduleProfilePersisting(p, type);
    }

    @Override
    public void setSkillProfile(Player p, Profile profile, Class<? extends Profile> type) {
        Map<Class<? extends Profile>, Profile> profiles = skillProfiles.getOrDefault(p.getUniqueId(), new HashMap<>());
        profiles.put(type, profile);
        skillProfiles.put(p.getUniqueId(), profiles);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Profile> T getPersistentProfile(Player p, Class<T> type) {
        return (T) persistentProfiles.getOrDefault(p.getUniqueId(), new HashMap<>()).get(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Profile> T getSkillProfile(Player p, Class<T> type) {
        return (T) skillProfiles.getOrDefault(p.getUniqueId(), new HashMap<>()).get(type);
    }

    @Override
    public void onProfileRegistration(Profile profile) {
        createTable(profile, this);
    }

    public static void createTable(Profile type, Database database) {
        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        query.append(type.getTableName()).append(" (");
        query.append("owner VARCHAR(40) PRIMARY KEY");

        // prepare table with all non-update stat names
        for (String s : type.getAllStatNames()){
            if (type.getTablesToUpdate().contains(s)) continue;
            String lower = s.toLowerCase(java.util.Locale.US);
            if (type.getInts().contains(s)) query.append(", ").append(lower).append(" INTEGER default ").append(type.getDefaultInt(s));
            if (type.getDoubles().contains(s)) query.append(", ").append(lower).append(" DOUBLE(24,12) default ").append(type.getDefaultDouble(s));
            if (type.getFloats().contains(s)) query.append(", ").append(lower).append(" FLOAT default ").append(type.getDefaultFloat(s));
            if (type.getStringSets().contains(s)) query.append(", ").append(lower).append(" TEXT");
            if (type.getBooleans().contains(s)) query.append(", ").append(lower).append(" BOOLEAN default ").append(type.getDefaultBoolean(s));
        }
        query.append(");");

        try (PreparedStatement stmt = database.getConnection().prepareStatement(query.toString())){
            stmt.execute();
        } catch (SQLException ex){
            ex.printStackTrace();
        }

        // edit table with new columns
        for (String s : type.getAllStatNames()){
            String lower = s.toLowerCase(java.util.Locale.US);
            if (type.getInts().contains(s)) database.addColumnIfNotExists(type.getTableName(), lower, "INTEGER default " + type.getDefaultInt(s));
            if (type.getDoubles().contains(s)) database.addColumnIfNotExists(type.getTableName(), lower, "DOUBLE default " + type.getDefaultDouble(s));
            if (type.getFloats().contains(s)) database.addColumnIfNotExists(type.getTableName(), lower, "FLOAT default " + type.getDefaultFloat(s));
            if (type.getStringSets().contains(s)) database.addColumnIfNotExists(type.getTableName(), lower, "TEXT");
            if (type.getBooleans().contains(s)) database.addColumnIfNotExists(type.getTableName(), lower, "BOOLEAN default " + type.getDefaultBoolean(s));
        }
    }

    @Override
    public void loadProfile(Player p) {
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
            boolean runPersistentStartingPerks = false;
            Map<Class<? extends Profile>, Profile> profs = persistentProfiles.getOrDefault(p.getUniqueId(), new HashMap<>());
            for (Class<? extends Profile> pr : ProfileRegistry.getRegisteredProfiles().keySet()){
                Profile profile = queryProfile(p, conn, pr);
                if (profile == null) {
                    profile = ProfileRegistry.getBlankProfile(p, pr);
                    runPersistentStartingPerks = true;
                }
                profs.put(profile.getClass(), profile);
            }
            persistentProfiles.put(p.getUniqueId(), profs);
            p.sendMessage(Utils.chat(TranslationManager.getTranslation("status_profiles_loaded")));

            JoinLeaveListener.getLoadedProfiles().add(p.getUniqueId());
            SkillRegistry.updateSkillProgression(p, runPersistentStartingPerks);
        });
    }

    public static <T extends Profile> T queryProfile(Player player, Connection conn, Class<T> clazz){
        try {
            T profile = ProfileRegistry.getBlankProfile(player, clazz);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + profile.getTableName() + " WHERE owner = ?;");
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet result = stmt.executeQuery();
            if (result.next()){
                for (String s : profile.getAllStatNames()){
                    String lower = s.toLowerCase(java.util.Locale.US);
                    if (profile.getInts().contains(s)) {
                        profile.setInt(s, result.getInt(lower));
                        if (result.wasNull()) profile.setInt(s, profile.getDefaultInt(s));
                    } else if (profile.getDoubles().contains(s)) {
                        profile.setDouble(s, result.getDouble(lower));
                        if (result.wasNull()) profile.setDouble(s, profile.getDefaultDouble(s));
                    } else if (profile.getFloats().contains(s)) {
                        profile.setFloat(s, result.getFloat(lower));
                        if (result.wasNull()) profile.setFloat(s, profile.getDefaultFloat(s));
                    } else if (profile.getStringSets().contains(s)) {
                        profile.setStringSet(s, ProfilePersistence.deserializeStringSet(Objects.requireNonNullElse(result.getString(lower), "")));
                    } else if (profile.getBooleans().contains(s)) {
                        profile.setBoolean(s, result.getBoolean(lower));
                        if (result.wasNull()) profile.setBoolean(s, profile.getDefaultBoolean(s));
                    } else ValhallaMMO.logWarning("Stat " + s + " in " + clazz.getSimpleName() + " was not found in database");
                }
                return profile;
            }
        } catch (SQLException ex){
            ValhallaMMO.logSevere("SQLException when trying to fetch " + player.getName() + "'s profile of type " + clazz.getSimpleName() + ". ");
            ex.printStackTrace();
        }
        return null;
    }

    public static void insertOrUpdateProfile(UUID owner, Connection conn, Profile profile){
        StringBuilder query = new StringBuilder("REPLACE INTO ").append(profile.getTableName()).append(" (owner");
        // stat names
        Map<Integer, String> indexMap = new HashMap<>();
        int index = 2;
        for (String s : profile.getAllStatNames()){
            query.append(", ").append(s);
            indexMap.put(index, s);
            index++;
        }
        query.append(") VALUES (?");
        // param placeholders
        query.append(", ?".repeat(profile.getAllStatNames().size()));
        query.append(");");
        // populating param placeholders
        try (PreparedStatement stmt = conn.prepareStatement(query.toString())) {
            stmt.setString(1, owner.toString());
            for (int i : indexMap.keySet()){
                String s = indexMap.get(i);
                if (profile.getInts().contains(s)) stmt.setInt(i, profile.getInt(s));
                else if (profile.getDoubles().contains(s)) stmt.setDouble(i, profile.getDouble(s));
                else if (profile.getFloats().contains(s)) stmt.setFloat(i, profile.getFloat(s));
                else if (profile.getStringSets().contains(s)) stmt.setString(i, ProfilePersistence.serializeStringSet(profile.getStringSet(s)));
                else if (profile.getBooleans().contains(s)) stmt.setBoolean(i, profile.getBoolean(s));
                else ValhallaMMO.logWarning("Stat " + s + " from " + profile.getClass().getSimpleName() + " did not belong to a valid data type");
            }
            stmt.execute();
        } catch (SQLException exception){
            ValhallaMMO.getInstance().getServer().getLogger().severe("SQLException when trying to save profile for profile type " + profile.getClass().getName() + ". ");
            exception.printStackTrace();
        }
    }

    @Override
    public void saveAllProfiles() {
        for (UUID p : new HashSet<>(persistentProfiles.keySet())){
            if (!persistentProfiles.containsKey(p)) continue;
            Player player = ValhallaMMO.getInstance().getServer().getPlayer(p);
            for (Profile profile : persistentProfiles.getOrDefault(p, new HashMap<>()).values()){
                insertOrUpdateProfile(p, conn, profile);
            }
            if (player == null || !player.isOnline()) persistentProfiles.remove(p);
        }
    }

    @Override
    public void saveProfile(Player p) {
        if (persistentProfiles.containsKey(p.getUniqueId())){
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
                if (!JoinLeaveListener.getLoadedProfiles().contains(p.getUniqueId())) return;
                for (Profile profile : persistentProfiles.getOrDefault(p.getUniqueId(), new HashMap<>()).values()){
                    insertOrUpdateProfile(p.getUniqueId(), conn, profile);
                }
            });
        }
    }

    public static String leaderboardQuery(Profile p, String stat, Collection<String> extraStats){
        String query = """
                SELECT %s.owner AS owner%s, %s.%s AS main_stat
                FROM %s ORDER BY %s DESC%s;
                """;
        String t = p.getTableName();
        String c = stat.toLowerCase(java.util.Locale.US);
        return String.format(query,
                t, extraStats.stream().map(e -> String.format(", %s.%s", t, e)).collect(Collectors.joining()),
                t, c, t, c, extraStats.stream().map(e -> String.format(", %s DESC", e)).collect(Collectors.joining()));
//        return String.format("SELECT %s.owner%s, %s.%s AS score FROM %s ORDER BY score DESC LIMIT %d, %d;", t,
//                extraStats.stream().map(e -> String.format(", %s.%s", t, e)).collect(Collectors.joining()), t, c, t,
//                page * LeaderboardManager.getPageEntryLimit(),
//                (page + 1) * LeaderboardManager.getPageEntryLimit());
    }



    @Override
    public Map<Integer, LeaderboardEntry> queryLeaderboardEntries(LeaderboardManager.Leaderboard leaderboard) {
        Map<Integer, LeaderboardEntry> entries = new HashMap<>();
        Profile profile = ProfileRegistry.getRegisteredProfiles().get(leaderboard.profile());
        try {
            PreparedStatement stmt = conn.prepareStatement(leaderboardQuery(profile, leaderboard.mainStat(), leaderboard.extraStats().values()));
            ResultSet set = stmt.executeQuery();
            int rank = 1;
            while (set.next()){
                double value = set.getDouble("main_stat");
                UUID uuid = UUID.fromString(set.getString("owner"));
                OfflinePlayer player = ValhallaMMO.getInstance().getServer().getOfflinePlayer(uuid);
                if (LeaderboardManager.getExcludedPlayers().contains(player.getName())) continue;
                Map<String, Double> extraStat = new HashMap<>();
                for (String e : leaderboard.extraStats().values()) extraStat.put(e, set.getDouble(e));
                entries.put(rank, new LeaderboardEntry(player.getName(), player.getUniqueId(), value, rank, extraStat));
                rank++;
            }
        } catch (SQLException ex){
            ValhallaMMO.logWarning("Could not fetch leaderboard due to an exception: ");
            ex.printStackTrace();
        }
        return entries;
    }
}
