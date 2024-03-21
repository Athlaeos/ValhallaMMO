package me.athlaeos.valhallammo.persistence.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
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
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class SQLite extends ProfilePersistence implements Database, LeaderboardCompatible {
    private final Map<UUID, Map<Class<? extends Profile>, Profile>> persistentProfiles = new HashMap<>();
    private final Map<UUID, Map<Class<? extends Profile>, Profile>> skillProfiles = new HashMap<>();
    private Connection conn;
    @Override
    public Connection getConnection() {
        File dataFolder = new File(ValhallaMMO.getInstance().getDataFolder(), "player_data.db");
        if (!dataFolder.exists()){
            try {
                if (dataFolder.createNewFile()) ValhallaMMO.logInfo("New player_data.db file created!");
            } catch (IOException e) {
                ValhallaMMO.logSevere("Could not create SQLite database file player_data.db");
            }
        }

        try {
            if(conn != null && !conn.isClosed()){
                return conn;
            }
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            ValhallaMMO.logFine("SQLite connection created! Deleting this file will reset everyone's progress, so back this file up or ignore it in case you want to delete/reset the configs.");
            return conn;
        } catch (SQLException ex) {
            ValhallaMMO.logSevere("SQLite exception on initialize " + ex);
        } catch (ClassNotFoundException ex) {
            ValhallaMMO.logInfo("You do not have the SQLite JDBC library on your server, defaulted to Persistent Data Container storage");
        }
        return null;
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
    public void loadProfile(Player p) {
        if (persistentProfiles.containsKey(p.getUniqueId())) return; // stats are presumably already loaded in, and so they do not
        // need to be loaded in from the database again
        Database database = this;
        new BukkitRunnable(){
            @Override
            public void run() {
                Map<Class<? extends Profile>, Profile> profs = persistentProfiles.getOrDefault(p.getUniqueId(), new HashMap<>());
                boolean runPersistentStartingPerks = false;
                for (Profile pr : ProfileRegistry.getRegisteredProfiles().values()){
                    try {
                        Profile profile = pr.fetchProfile(p, database);
                        if (profile == null) {
                            profile = ProfileRegistry.getBlankProfile(p, pr.getClass());
                            runPersistentStartingPerks = true;
                        }
                        profs.put(profile.getClass(), profile);
                    } catch (SQLException e){
                        ValhallaMMO.logSevere("SQLException when trying to fetch " + p.getName() + "'s profile of type " + pr.getClass().getSimpleName() + ". ");
                        e.printStackTrace();
                    }
                }
                persistentProfiles.put(p.getUniqueId(), profs);
                Utils.sendMessage(p, TranslationManager.getTranslation("status_profiles_loaded"));

                SkillRegistry.updateSkillProgression(p, runPersistentStartingPerks);
            }
        }.runTaskAsynchronously(ValhallaMMO.getInstance());
    }

    @Override
    public void saveAllProfiles() {
        for (UUID p : new HashSet<>(persistentProfiles.keySet())){
            Player player = ValhallaMMO.getInstance().getServer().getPlayer(p);
            for (Profile profile : persistentProfiles.get(p).values()){
                if (!shouldPersist(profile)) continue;
                try {
                    profile.insertOrUpdateProfile(this);
                } catch (SQLException e){
                    ValhallaMMO.getInstance().getServer().getLogger().severe("SQLException when trying to save profile for profile type " + profile.getClass().getName() + ". ");
                    e.printStackTrace();
                }
            }
            if (player == null || !player.isOnline()) persistentProfiles.remove(p);
        }
    }

    @Override
    public void saveProfile(Player p) {
        if (persistentProfiles.containsKey(p.getUniqueId())){
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
                for (Profile profile : persistentProfiles.get(p.getUniqueId()).values()){
                    if (!shouldPersist(profile)) continue;
                    try {
                        profile.insertOrUpdateProfile(this);
                    } catch (SQLException e){
                        ValhallaMMO.getInstance().getServer().getLogger().severe("SQLException when trying to save profile for profile type " + profile.getClass().getName() + ". ");
                        e.printStackTrace();
                    }
                }
                persistentProfiles.remove(p.getUniqueId());
            });
        }
    }

    @Override
    public Map<Integer, LeaderboardEntry> queryLeaderboardEntries(LeaderboardManager.Leaderboard leaderboard) {
        Map<Integer, LeaderboardEntry> entries = new HashMap<>();
        Profile profile = ProfileRegistry.getRegisteredProfiles().get(leaderboard.profile());
        try {
            PreparedStatement stmt = conn.prepareStatement(SQL.leaderboardQuery(profile, leaderboard.mainStat(), leaderboard.extraStats().values()));
            ResultSet set = stmt.executeQuery();
            int rank = 1;
            while (set.next()){
                double value = set.getDouble("main_stat");
                UUID uuid = UUID.fromString(set.getString("owner"));
                OfflinePlayer player = ValhallaMMO.getInstance().getServer().getOfflinePlayer(uuid);
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
