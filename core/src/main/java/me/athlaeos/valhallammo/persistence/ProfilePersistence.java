package me.athlaeos.valhallammo.persistence;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.common.util.concurrent.Striped;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.listeners.JoinLeaveListener;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.LeaderboardEntry;
import me.athlaeos.valhallammo.playerstats.LeaderboardManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.ResetType;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.ConfigurableProfile;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.skills.perkresourcecost.ResourceExpense;
import me.athlaeos.valhallammo.skills.skills.Perk;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.skills.skills.implementations.ConfigurableSkill;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

public abstract class ProfilePersistence {
    private static final Map<UUID, Collection<Class<? extends Profile>>> PROFILES_TO_SAVE = new ConcurrentHashMap<>();
    private static final Map<UUID, Collection<String>> CONFIGURABLE_PROFILES_TO_SAVE = new ConcurrentHashMap<>();

    public final ScheduledExecutorService profileThreads;
    public final ScheduledExecutorService configurableProfileThreads;
    protected final AsyncLoadingCache<UUID, ClassToInstanceMap<Profile>> persistentProfiles;
    protected final AsyncLoadingCache<UUID, Map<String, ConfigurableProfile>> persistentConfigurableProfiles;
    protected final Map<UUID, ClassToInstanceMap<Profile>> skillProfiles;
    protected final Map<UUID, Map<String, ConfigurableProfile>> skillConfigurableProfiles;
    protected final Set<UUID> saving = ConcurrentHashMap.newKeySet();
    protected final Set<UUID> savingConfigurable = ConcurrentHashMap.newKeySet();

    // A queued asynchronous save must not suppress a synchronous save during logout or shutdown.
    private final Striped<Lock> saveLocks = Striped.lock(256);

    protected ProfilePersistence() {
        int timeout = ValhallaMMO.getPluginConfig().getInt("profile-thread-timeout", 180000);
        int threads = ValhallaMMO.getPluginConfig().getInt("profile-thread-count", 32);
        if (threads < minimumProfileThreadCount()) {
            ValhallaMMO.logWarning("Profile thread count is set to " + threads + ", but the minimum for your db type is " + minimumProfileThreadCount() + ". Setting to minimum.");
            threads = minimumProfileThreadCount();
        }

        profileThreads = Utils.threadPool("Profile", timeout, false, threads);
        persistentProfiles = Caffeine.newBuilder()
                .executor(profileThreads)
                .buildAsync((uuid, executor) -> CompletableFuture.supplyAsync(() -> loadProfile(uuid), executor));
        configurableProfileThreads = Utils.threadPool("ConfigurableProfile", timeout, false, threads);
        persistentConfigurableProfiles = Caffeine.newBuilder()
                .executor(configurableProfileThreads)
                .buildAsync((uuid, executor) -> CompletableFuture.supplyAsync(() -> loadConfigurableProfile(uuid), executor));
        skillProfiles = new ConcurrentHashMap<>();
        skillConfigurableProfiles = new ConcurrentHashMap<>();
    }

    public void requestProfile(UUID p) {
        persistentProfiles.get(p).exceptionally(ex -> {
            ValhallaMMO.logWarning("Exception when trying to load profile for " + p + ": ");
            ex.printStackTrace();
            Player player = Bukkit.getPlayer(p);
            if (player != null) {
                Utils.sendMessage(player, TranslationManager.getTranslation("status_profiles_load_failed"));
            }
            return null;
        });
        persistentConfigurableProfiles.get(p).exceptionally(ex -> {
            ValhallaMMO.logWarning("Exception when trying to load configurable profile for " + p + ": ");
            ex.printStackTrace();
            Player player = Bukkit.getPlayer(p);
            if (player != null) {
                Utils.sendMessage(player, TranslationManager.getTranslation("status_profiles_load_failed"));
            }
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends Profile> boolean fillProfile(UUID player, T profile) {
        Class<T> clazz = (Class<T>) profile.getClass();
        try(PreparedStatement stmt = getConnection().prepareStatement("SELECT * FROM " + profile.getTableName() + " WHERE owner = ?;")) {
            stmt.setString(1, player.toString());
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()){
                    for (String s : profile.getAllStatNames()){
                        String lower = s.toLowerCase(java.util.Locale.US);
                        if (profile.isInt(s)) {
                            profile.setInt(s, result.getInt(lower));
                            if (result.wasNull()) profile.setInt(s, profile.getDefaultInt(s));
                        } else if (profile.isDouble(s)) {
                            profile.setDouble(s, result.getDouble(lower));
                            if (result.wasNull()) profile.setDouble(s, profile.getDefaultDouble(s));
                        } else if (profile.isFloat(s)) {
                            profile.setFloat(s, result.getFloat(lower));
                            if (result.wasNull()) profile.setFloat(s, profile.getDefaultFloat(s));
                        } else if (profile.isStringSet(s)) {
                            profile.setStringSet(s, deserializeStringSet(Objects.requireNonNullElse(result.getString(lower), "")));
                        } else if (profile.isBoolean(s)) {
                            profile.setBoolean(s, result.getBoolean(lower));
                            if (result.wasNull()) profile.setBoolean(s, profile.getDefaultBoolean(s));
                        } else ValhallaMMO.logWarning("Stat " + s + " in " + clazz.getSimpleName() + " was not found in database");
                    }
                    return true;
                }
            }
        } catch (SQLException ex){
            ValhallaMMO.logSevere("SQLException when trying to fetch " + player + "'s profile of type " + clazz.getSimpleName() + ". ");
            throw new IllegalStateException("refusing a blank profile after SQL read failure for " + player, ex);
        }
        return false;
    }

    public <T extends Profile> void insertOrUpdateProfile(UUID player, T profile) {
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
        try (PreparedStatement stmt = getConnection().prepareStatement(query.toString())) {
            stmt.setString(1, player.toString());
            for (int i : indexMap.keySet()){
                String s = indexMap.get(i);
                if (profile.isInt(s)) stmt.setInt(i, profile.getInt(s));
                else if (profile.isDouble(s)) stmt.setDouble(i, profile.getDouble(s));
                else if (profile.isFloat(s)) stmt.setFloat(i, profile.getFloat(s));
                else if (profile.isStringSet(s)) stmt.setString(i, serializeStringSet(profile.getStringSet(s)));
                else if (profile.isBoolean(s)) stmt.setBoolean(i, profile.getBoolean(s));
                else ValhallaMMO.logWarning("Stat " + s + " from " + profile.getClass().getSimpleName() + " did not belong to a valid data type");
            }
            stmt.execute();
        } catch (SQLException exception){
            // Do not let a failed write look successful or evict the only in-memory profile.
            throw new IllegalStateException("SQL write failed for " + player + " / " + profile.getTableName(), exception);
        }
    }

    public static String leaderboardQuery(Profile p, Pair<String, Double> stat, Map<String, Pair<String, Double>> extraStats){
        String query = """
                SELECT %s.owner AS owner%s, %s.%s AS main_stat
                FROM %s%s ORDER BY %s DESC%s;
                """;
        String t = p.getTableName();
        String c = stat.getOne().toLowerCase(java.util.Locale.US);
        return String.format(query,
                t, extraStats.keySet().stream().map(e -> String.format(", %s.%s", t, e)).collect(Collectors.joining()),
                t, c, t, whereClause(stat, extraStats), c, extraStats.keySet().stream().map(e -> String.format(", %s DESC", e)).collect(Collectors.joining()));
    }

    private static String whereClause(Pair<String, Double> mainStat, Map<String, Pair<String, Double>> extraStats){
        Collection<String> whereClauses = new HashSet<>();
        if (mainStat.getTwo() != null)
            whereClauses.add(String.format("%s >= FORMAT(%s, 'D', 'en-US')", mainStat.getOne(), String.format("%.2f", mainStat.getTwo()).replace(",", ".")));
        for (String stat : extraStats.keySet()){
            Pair<String, Double> statWithMinimum = extraStats.get(stat);
            if (statWithMinimum == null || statWithMinimum.getTwo() == null) continue;
            whereClauses.add(String.format("%s >= FORMAT(%s, 'D', 'en-US')", stat, String.format("%.2f", statWithMinimum.getTwo()).replace(",", ".")));
        }
        return whereClauses.isEmpty() ? "" : (" WHERE " + String.join(" AND ", whereClauses));
    }

    public void createProfileTable(Profile type) {
        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        query.append(type.getTableName()).append(" (");
        query.append("owner VARCHAR(40) PRIMARY KEY");

        // prepare table with all non-update stat names
        for (String s : type.getAllStatNames()){
            if (type.getTablesToUpdate().contains(s)) continue;
            String lower = s.toLowerCase(java.util.Locale.US);
            if (type.isInt(s)) query.append(", ").append(lower).append(" INTEGER default ").append(type.getDefaultInt(s));
            if (type.isDouble(s)) query.append(", ").append(lower).append(" DOUBLE(24,12) default ").append(type.getDefaultDouble(s));
            if (type.isFloat(s)) query.append(", ").append(lower).append(" FLOAT default ").append(type.getDefaultFloat(s));
            if (type.isStringSet(s)) query.append(", ").append(lower).append(" TEXT");
            if (type.isBoolean(s)) query.append(", ").append(lower).append(" BOOLEAN default ").append(type.getDefaultBoolean(s));
        }
        query.append(");");

        try (PreparedStatement stmt = getConnection().prepareStatement(query.toString())){
            stmt.execute();
        } catch (SQLException ex){
            ex.printStackTrace();
        }

        // edit table with new columns
        for (String s : type.getAllStatNames()){
            String lower = s.toLowerCase(java.util.Locale.US);
            if (type.isInt(s)) addColumnIfNotExists(type.getTableName(), lower, "INTEGER default " + type.getDefaultInt(s));
            if (type.isDouble(s)) addColumnIfNotExists(type.getTableName(), lower, "DOUBLE default " + type.getDefaultDouble(s));
            if (type.isFloat(s)) addColumnIfNotExists(type.getTableName(), lower, "FLOAT default " + type.getDefaultFloat(s));
            if (type.isStringSet(s)) addColumnIfNotExists(type.getTableName(), lower, "TEXT");
            if (type.isBoolean(s)) addColumnIfNotExists(type.getTableName(), lower, "BOOLEAN default " + type.getDefaultBoolean(s));
        }
    }

    public boolean hasProfileTable(Profile type) {
        try (ResultSet rs = getConnection().getMetaData().getTables(null, null, type.getTableName(), null)) {
            return rs.next();
        } catch (SQLException e) {
            ValhallaMMO.logWarning("Could not check if profile table exists for " + type.getTableName() + " " + e);
            return false;
        }
    }

    public boolean deleteProfileTable(Profile profile) {
        try (PreparedStatement stmt = getConnection().prepareStatement("DROP TABLE IF EXISTS " + profile.getTableName() + ";")) {
            stmt.execute();
            return true;
        } catch (SQLException e) {
            ValhallaMMO.logSevere("SQLException when trying to delete profile table for " + profile.getTableName() + ". ");
            e.printStackTrace();
            return false;
        }
    }

    public ClassToInstanceMap<Profile> loadProfile(UUID uuid) {
        ClassToInstanceMap<Profile> profiles = MutableClassToInstanceMap.create();
        boolean runPersistentStartingPerks = false;
        for (Class<? extends Profile> clazz : ProfileRegistry.getRegisteredProfiles().keySet()) {
            Profile profile = ProfileRegistry.getBlankProfile(uuid, clazz);
            if (!fillProfile(uuid, profile)) {
                runPersistentStartingPerks = true;
            }
            profiles.put(clazz, profile);
        }
        updateProfileStats(uuid, runPersistentStartingPerks);
        return profiles;
    }

    public Map<String, ConfigurableProfile> loadConfigurableProfile(UUID uuid) {
        Map<String, ConfigurableProfile> profiles = new ConcurrentHashMap<>();
        boolean runPersistentStartingPerks = false;
        for (String type : ProfileRegistry.getRegisteredConfigurableProfiles().keySet()) {
            ConfigurableProfile profile = ProfileRegistry.getBlankConfigurableProfile(uuid, type);
            if (!fillProfile(uuid, profile)) {
                runPersistentStartingPerks = true;
            }
            profiles.put(type, profile);
        }
        updateConfigurableProfileStats(uuid, runPersistentStartingPerks);
        return profiles;
    }

    public void updateProfileStats(UUID uuid, boolean withPersistentPerks){
        AccumulativeStatManager.resetAllCaches(uuid);
        JoinLeaveListener.getLoadedProfiles().add(uuid);
        Bukkit.getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                skillProfiles.remove(player.getUniqueId());
                Utils.sendMessage(player, TranslationManager.getTranslation("status_profiles_loaded"));
                SkillRegistry.updateSkillProgression(player, withPersistentPerks);
            } else {
                uncacheProfile(uuid);
            }
        });
    }

    public void updateConfigurableProfileStats(UUID uuid, boolean withPersistentPerks){
        AccumulativeStatManager.resetAllCaches(uuid);
        JoinLeaveListener.getLoadedProfiles().add(uuid);
        Bukkit.getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                skillConfigurableProfiles.remove(player.getUniqueId());
                Utils.sendMessage(player, TranslationManager.getTranslation("status_profiles_loaded"));
                SkillRegistry.updateConfigurableSkillProgression(player, withPersistentPerks);
            } else {
                uncacheProfile(uuid);
            }
        });
    }

    public void saveAllProfiles(boolean async) {
        Set<UUID> owners = new HashSet<>(persistentProfiles.asMap().keySet());
        owners.addAll(persistentConfigurableProfiles.asMap().keySet());
        int failures = 0;
        for (UUID uuid : owners) {
            if (async) {
                saveProfileAsync(uuid);
                saveConfigurableProfileAsync(uuid);
            } else {
                try {
                    saveProfile(uuid, true);
                    saveConfigurableProfile(uuid, true);
                } catch (RuntimeException ex) {
                    failures++;
                    ValhallaMMO.logSevere("player save failed, profile retained for retry: " + uuid);
                    ex.printStackTrace();
                }
            }
        }
        if (failures > 0) throw new IllegalStateException("failed to save " + failures + " player(s); see preceding errors");
    }

    public void saveProfileAsync(UUID p) {
        if (!saving.add(p)) return;

        CompletableFuture.runAsync(() -> saveProfile(p, false), profileThreads).whenComplete((ignored, ex) -> {
            if (ex != null) {
                ValhallaMMO.logWarning("Exception when trying to save profile for " + p + ": ");
                ex.printStackTrace();
            }
            saving.remove(p);
        });
    }

    public void saveConfigurableProfileAsync(UUID p) {
        if (!savingConfigurable.add(p)) return;

        CompletableFuture.runAsync(() -> saveConfigurableProfile(p, false), configurableProfileThreads).whenComplete((ignored, ex) -> {
            if (ex != null) {
                ValhallaMMO.logWarning("Exception when trying to save configurable profile for " + p + ": ");
                ex.printStackTrace();
            }
            savingConfigurable.remove(p);
        });
    }

    public void saveProfile(UUID p, boolean localLock) {
        Lock lock = saveLocks.get(p);
        lock.lock();
        try {
            // getIfPresent avoids an old queued save reloading and writing a departed session.
            CompletableFuture<ClassToInstanceMap<Profile>> future = persistentProfiles.getIfPresent(p);
            if (future == null || !future.isDone()) return;
            ClassToInstanceMap<Profile> profiles = future.join();
            for (Profile profile : new ArrayList<>(profiles.values())) {
                if (shouldPersist(profile)) insertOrUpdateProfile(p, profile);
            }
            Player player = Bukkit.getPlayer(p);
            if (player == null || !player.isOnline()) {
                // uncacheProfile invalidates both profile families.
                // Save the configurable family before allowing that eviction.
                CompletableFuture<Map<String, ConfigurableProfile>> configurable = persistentConfigurableProfiles.getIfPresent(p);
                if (configurable != null && !configurable.isDone()) return;
                saveConfigurableProfile(p, true);
                uncacheProfile(p);
            }
        } finally {
            lock.unlock();
        }
    }

    public void saveConfigurableProfile(UUID p, boolean localLock) {
        Lock lock = saveLocks.get(p);
        lock.lock();
        try {
            CompletableFuture<Map<String, ConfigurableProfile>> future = persistentConfigurableProfiles.getIfPresent(p);
            if (future == null || !future.isDone()) return;
            Map<String, ConfigurableProfile> profiles = future.join();
            for (ConfigurableProfile profile : new ArrayList<>(profiles.values())) {
                if (shouldPersist(profile)) insertOrUpdateProfile(p, profile);
            }
            Player player = Bukkit.getPlayer(p);
            if ((player == null || !player.isOnline()) && persistentProfiles.getIfPresent(p) == null) uncacheProfile(p);
        } finally {
            lock.unlock();
        }
    }

    public void uncacheProfile(UUID p) {
        persistentProfiles.synchronous().invalidate(p);
        persistentConfigurableProfiles.synchronous().invalidate(p);
        skillProfiles.remove(p);
        skillConfigurableProfiles.remove(p);
        JoinLeaveListener.getLoadedProfiles().remove(p);
        ProfileCache.resetCache(p);
    }

    public void uncacheAllProfiles() {
        persistentProfiles.synchronous().invalidateAll();
        persistentConfigurableProfiles.synchronous().invalidateAll();
        skillProfiles.clear();
        skillConfigurableProfiles.clear();
        JoinLeaveListener.getLoadedProfiles().clear();
        ProfileCache.resetAllCaches();
    }

    public abstract int minimumProfileThreadCount();
    public Connection getConnection() { return getConnection(false); }
    public abstract Connection getConnection(boolean migrating);
    public abstract void addColumnIfNotExists(String tableName, String columnName, String columnType);
    public abstract String getType();

    public boolean isLoaded(UUID p) {
        CompletableFuture<ClassToInstanceMap<Profile>> future = persistentProfiles.getIfPresent(p);
        return future != null && future.isDone();
    }

    public boolean isConfigurableProfilesLoaded(UUID p) {
        CompletableFuture<Map<String, ConfigurableProfile>> future = persistentConfigurableProfiles.getIfPresent(p);
        return future != null && future.isDone();
    }

    public <T extends Profile> void trySetPersistentProfile(UUID p, Profile profile, Class<T> type) {
        if (type.isInstance(profile)) {
            setPersistentProfile(p, type.cast(profile), type);
        } else {
            ValhallaMMO.logWarning("Tried to set persistent profile of type " + type.getSimpleName() + " for player " + p + ", but the profile is of type " + profile.getClass().getSimpleName() + ". This is likely a bug in the plugin, please report it.");
        }
    }
    public <T extends Profile> void trySetSkillProfile(UUID p, Profile profile, Class<T> type) {
        if (type.isInstance(profile)) {
            setSkillProfile(p, type.cast(profile), type);
        } else {
            ValhallaMMO.logWarning("Tried to set skill profile of type " + type.getSimpleName() + " for player " + p + ", but the profile is of type " + profile.getClass().getSimpleName() + ". This is likely a bug in the plugin, please report it.");
        }
    }

    public <T extends Profile> void trySetPersistentConfigurableProfile(UUID p, ConfigurableProfile profile, String type) {
        if (type.equals(profile.getSkillTypeName())) {
            setPersistentConfigurableProfile(p, profile, type);
        } else {
            ValhallaMMO.logWarning("Tried to set persistent configurable profile of type " + type + " for player " + p + ", but the profile is of type " + profile.getClass().getSimpleName() + ". This is likely a bug in the plugin, please report it.");
        }
    }
    public <T extends Profile> void trySetSkillConfigurableProfile(UUID p, ConfigurableProfile profile, String type) {
        if (type.equals(profile.getSkillTypeName())) {
            setSkillConfigurableProfile(p, profile, type);
        } else {
            ValhallaMMO.logWarning("Tried to set skill profile of type " + type + " for player " + p + ", but the profile is of type " + profile.getClass().getSimpleName() + ". This is likely a bug in the plugin, please report it.");
        }
    }

    public <T extends Profile> void setPersistentProfile(UUID p, T profile, Class<T> type) {
        ClassToInstanceMap<Profile> profiles = persistentProfiles.get(p).join();
        profiles.put(type, profile);
        persistentProfiles.put(p, CompletableFuture.completedFuture(profiles));
        scheduleProfilePersisting(p, type);
    }
    public <T extends Profile> void setSkillProfile(UUID p, T profile, Class<T> type) {
        ClassToInstanceMap<Profile> profiles = skillProfiles.getOrDefault(p, MutableClassToInstanceMap.create());
        profiles.put(type, profile);
        skillProfiles.put(p, profiles);
    }

    public void setPersistentConfigurableProfile(UUID p, ConfigurableProfile profile, String type) {
        Map<String, ConfigurableProfile> profiles = persistentConfigurableProfiles.get(p).join();
        profiles.put(type, profile);
        persistentConfigurableProfiles.put(p, CompletableFuture.completedFuture(profiles));
        scheduleConfigurableProfilePersisting(p, type);
    }

    public void setSkillConfigurableProfile(UUID p, ConfigurableProfile profile, String type) {
        Map<String, ConfigurableProfile> profiles = skillConfigurableProfiles.getOrDefault(p, new HashMap<>());
        profiles.put(type, profile);
        skillConfigurableProfiles.put(p, profiles);
    }

    public <T extends Profile> T getPersistentProfile(UUID p, Class<T> type) {
        CompletableFuture<ClassToInstanceMap<Profile>> future = persistentProfiles.getIfPresent(p);
        if (future == null || !future.isDone()) return null; // Profile not loaded yet
        ClassToInstanceMap<Profile> profiles = future.join();
        return profiles == null ? null : profiles.getInstance(type);
    }

    public <T extends Profile> T getSkillProfile(UUID p, Class<T> type) {
        ClassToInstanceMap<Profile> profiles = skillProfiles.get(p);
        return profiles == null ? null : profiles.getInstance(type);
    }

    public ConfigurableProfile getPersistentConfigurableProfile(UUID p, String type) {
        CompletableFuture<Map<String, ConfigurableProfile>> future = persistentConfigurableProfiles.getIfPresent(p);
        if (future == null || !future.isDone()) return null; // Profile not loaded yet
        Map<String, ConfigurableProfile> profiles = future.join();
        return profiles == null ? null : profiles.get(type);
    }

    public ConfigurableProfile getSkillConfigurableProfile(UUID p, String type) {
        Map<String, ConfigurableProfile> profiles = skillConfigurableProfiles.get(p);
        return profiles == null ? null : profiles.get(type);
    }

    public Map<Integer, LeaderboardEntry> queryLeaderboardEntries(LeaderboardManager.Leaderboard leaderboard) {
        Map<Integer, LeaderboardEntry> entries = new HashMap<>();
        Profile profile = ProfileRegistry.getRegisteredProfiles().get(leaderboard.profile());
        try(PreparedStatement stmt = getConnection().prepareStatement(leaderboardQuery(profile, leaderboard.mainStat(), leaderboard.extraStats()))) {
            try (ResultSet set = stmt.executeQuery()) {
                int rank = 1;
                while (set.next()) {
                    double value = set.getDouble("main_stat");
                    UUID uuid = UUID.fromString(set.getString("owner"));
                    OfflinePlayer player = ValhallaMMO.getInstance().getServer().getOfflinePlayer(uuid);
                    if (LeaderboardManager.getExcludedPlayers().contains(player.getName())) continue;
                    Map<String, Double> extraStat = new HashMap<>();
                    for (Pair<String, Double> e : leaderboard.extraStats().values()) {
                        extraStat.put(e.getOne(), set.getDouble(e.getOne()));
                    }
                    entries.put(rank, new LeaderboardEntry(player.getName(), player.getUniqueId(), value, rank, extraStat));
                    rank++;
                }
            }
        } catch (SQLException ex){
            ValhallaMMO.logWarning("Could not fetch leaderboard due to an exception: ");
            ex.printStackTrace();
        }
        return entries;
    }

    public void resetProfile(Player p, ResetType resetType) {
        UUID uuid = p.getUniqueId();
        boolean runPersistentStartingPerks = false;
        switch (resetType){
            case STATS_ONLY -> {
                // Only resets persistent stat profile, keeping skill progress
                for (Profile profileType : ProfileRegistry.getRegisteredProfiles().values()) {
                    Profile persistentProfile = ProfileRegistry.getPersistentProfile(p, profileType.getClass());
                    double totalEXP = persistentProfile.getTotalEXP();
                    double EXP = persistentProfile.getEXP();
                    int level = persistentProfile.getLevel();
                    int ngPlus = persistentProfile.getNewGamePlus();
                    Profile resetProfile = profileType.getBlankProfile(p.getUniqueId());
                    resetProfile.setTotalEXP(totalEXP);
                    resetProfile.setEXP(EXP);
                    resetProfile.setLevel(level);
                    resetProfile.setNewGamePlus(ngPlus);
                    runPersistentStartingPerks = true;
                    trySetPersistentProfile(uuid, resetProfile, profileType.getClass());
                }
                for (ConfigurableProfile profileType : ProfileRegistry.getRegisteredConfigurableProfiles().values()) {
                    ConfigurableProfile persistentProfile = ProfileRegistry.getPersistentProfile(p, profileType.getClass());
                    double totalEXP = persistentProfile.getTotalEXP();
                    double EXP = persistentProfile.getEXP();
                    int level = persistentProfile.getLevel();
                    int ngPlus = persistentProfile.getNewGamePlus();
                    ConfigurableProfile resetProfile = (ConfigurableProfile) profileType.getBlankProfile(p.getUniqueId());
                    resetProfile.setTotalEXP(totalEXP);
                    resetProfile.setEXP(EXP);
                    resetProfile.setLevel(level);
                    resetProfile.setNewGamePlus(ngPlus);
                    runPersistentStartingPerks = true;
                    trySetPersistentConfigurableProfile(uuid, resetProfile, profileType.getSkillTypeName());
                }
                markProfilesReset(p.getUniqueId(), ProfileRegistry.getRegisteredProfiles().keySet());
            }
            case SKILLS_ONLY -> {
                // Only resets skill progress, keeping persistent stats
                PowerProfile powerProfile = ProfileRegistry.getPersistentProfile(p, PowerProfile.class);
                for (Profile profileType : ProfileRegistry.getRegisteredProfiles().values()) {
                    // setting persistent properties to 0, removing perks from unlocked perks and permalocked perks
                    Profile profile = getPersistentProfile(uuid, profileType.getClass());
                    profile.setEXP(0);
                    profile.setTotalEXP(0);
                    profile.setLevel(0);
                    profile.setNewGamePlus(0);
                    trySetPersistentProfile(uuid, profile, profileType.getClass());

                    Skill associatedSkill = SkillRegistry.getSkill(profileType.getSkillTypeName());
                    associatedSkill.getPerks().forEach(perk -> {
                        Collection<String> unlockedPerks = powerProfile.getUnlockedPerks();
                        unlockedPerks.remove(perk.getName());
                        powerProfile.setUnlockedPerks(unlockedPerks);
                    });
                    trySetSkillProfile(uuid, profileType.getBlankProfile(p), profileType.getClass());
                }
                for (ConfigurableProfile profileType : ProfileRegistry.getRegisteredConfigurableProfiles().values()) {
                    // setting persistent properties to 0, removing perks from unlocked perks and permalocked perks
                    ConfigurableProfile profile = getPersistentConfigurableProfile(uuid, profileType.getSkillTypeName());
                    profile.setEXP(0);
                    profile.setTotalEXP(0);
                    profile.setLevel(0);
                    profile.setNewGamePlus(0);
                    trySetPersistentConfigurableProfile(uuid, profile, profileType.getSkillTypeName());

                    Skill associatedSkill = SkillRegistry.getSkill(profileType.getSkillTypeName());
                    associatedSkill.getPerks().forEach(perk -> {
                        Collection<String> unlockedPerks = powerProfile.getUnlockedPerks();
                        unlockedPerks.remove(perk.getName());
                        powerProfile.setUnlockedPerks(unlockedPerks);
                    });
                    trySetSkillConfigurableProfile(uuid, (ConfigurableProfile) profileType.getBlankProfile(p), profileType.getSkillTypeName());
                }
                setPersistentProfile(uuid, powerProfile, PowerProfile.class);
                markProfilesReset(p.getUniqueId(), ProfileRegistry.getRegisteredProfiles().keySet());
            }
            case SKILLS_AND_STATS -> {
                // set both persistent and skill stats to 0
                for (Profile profileType : ProfileRegistry.getRegisteredProfiles().values()) {
                    trySetPersistentProfile(uuid, profileType.getBlankProfile(p), profileType.getClass());
                    trySetSkillProfile(uuid, profileType.getBlankProfile(p), profileType.getClass());
                }
                for (ConfigurableProfile profileType : ProfileRegistry.getRegisteredConfigurableProfiles().values()) {
                    trySetPersistentConfigurableProfile(uuid, (ConfigurableProfile) profileType.getBlankProfile(p), profileType.getSkillTypeName());
                    trySetSkillConfigurableProfile(uuid, (ConfigurableProfile) profileType.getBlankProfile(p), profileType.getSkillTypeName());
                }
                runPersistentStartingPerks = true;
                markProfilesReset(p.getUniqueId(), ProfileRegistry.getRegisteredProfiles().keySet());
            }
            case SKILLS_REFUND_EXP -> {
                PowerProfile powerProfile = ProfileRegistry.getPersistentProfile(p, PowerProfile.class);
                powerProfile.setUnlockedPerks(new HashSet<>());
                powerProfile.setFakeUnlockedPerks(new HashSet<>());
                powerProfile.setPermanentlyLockedPerks(new HashSet<>());
                ProfileRegistry.setPersistentProfile(p, powerProfile, PowerProfile.class);
                setSkillProfile(uuid, ProfileRegistry.getBlankProfile(p, PowerProfile.class), PowerProfile.class);

                // resets skill progress but leaves persistent progress untouched, and updates based on that
                for (Profile profileType : ProfileRegistry.getRegisteredProfiles().values()) {
                    if (profileType instanceof PowerProfile) continue;
                    trySetSkillProfile(uuid, profileType.getBlankProfile(p), profileType.getClass());
                }
                for (ConfigurableProfile profileType : ProfileRegistry.getRegisteredConfigurableProfiles().values()) {
                    trySetSkillConfigurableProfile(uuid, (ConfigurableProfile) profileType.getBlankProfile(p), profileType.getSkillTypeName());
                }
                markProfilesReset(p.getUniqueId(), ProfileRegistry.getRegisteredProfiles().keySet());
            }
        }
        SkillRegistry.updateSkillProgression(p, runPersistentStartingPerks);
    }

    public <S extends Skill> void resetSkillProgress(Player p, Class<S> resetSkill) {
        UUID uuid = p.getUniqueId();
        PowerProfile powerProfile = ProfileRegistry.getPersistentProfile(p, PowerProfile.class);
        Skill associatedSkill = SkillRegistry.getSkill(resetSkill);

        Profile profile = getPersistentProfile(uuid, associatedSkill.getProfileType());
        profile.setEXP(0);
        profile.setLevel(0);
        trySetPersistentProfile(uuid, profile, associatedSkill.getProfileType());

        Collection<String> unlockedPerks = powerProfile.getUnlockedPerks();
        Collection<String> unlockedPerksCopy = new HashSet<>(powerProfile.getUnlockedPerks());
        unlockedPerks.removeAll(associatedSkill.getPerks().stream().map(Perk::getName).collect(Collectors.toSet()));
        powerProfile.setUnlockedPerks(unlockedPerks);
        setPersistentProfile(uuid, powerProfile, PowerProfile.class);

        List<Perk> invertedPerks = new ArrayList<>(associatedSkill.getPerks());
        Collections.reverse(invertedPerks);
        invertedPerks.forEach(perk -> {
            if (!unlockedPerksCopy.contains(perk.getName())) return;
            for (ResourceExpense expense : perk.getExpenses()){
                if (!expense.isRefundable()) continue;
                expense.refund(p);
            }
        });

        trySetSkillProfile(uuid, profile.getBlankProfile(uuid), associatedSkill.getProfileType());
        SkillRegistry.updateSkillProgression(p, false);
        Collection<Class<? extends Profile>> profilesToReset = resetProfiles.getOrDefault(p.getUniqueId(), new HashSet<>());
        profilesToReset.add(profile.getClass());
        markProfilesReset(p.getUniqueId(), profilesToReset);
    }

    public void resetConfigurableSkillProgress(Player p, String type) {
        UUID uuid = p.getUniqueId();
        PowerProfile powerProfile = ProfileRegistry.getPersistentProfile(p, PowerProfile.class);
        Skill associatedSkill = SkillRegistry.getSkill(type);
        if (!(associatedSkill instanceof ConfigurableSkill c)) throw new IllegalArgumentException("Invalid configurable skill type given");

        ConfigurableProfile profile = getPersistentConfigurableProfile(uuid, c.getType());
        profile.setEXP(0);
        profile.setLevel(0);
        trySetPersistentConfigurableProfile(uuid, profile, c.getType());

        Collection<String> unlockedPerks = powerProfile.getUnlockedPerks();
        Collection<String> unlockedPerksCopy = new HashSet<>(powerProfile.getUnlockedPerks());
        unlockedPerks.removeAll(associatedSkill.getPerks().stream().map(Perk::getName).collect(Collectors.toSet()));
        powerProfile.setUnlockedPerks(unlockedPerks);
        setPersistentProfile(uuid, powerProfile, PowerProfile.class);

        List<Perk> invertedPerks = new ArrayList<>(associatedSkill.getPerks());
        Collections.reverse(invertedPerks);
        invertedPerks.forEach(perk -> {
            if (!unlockedPerksCopy.contains(perk.getName())) return;
            for (ResourceExpense expense : perk.getExpenses()){
                if (!expense.isRefundable()) continue;
                expense.refund(p);
            }
        });

        trySetSkillConfigurableProfile(uuid, (ConfigurableProfile) profile.getBlankProfile(uuid), c.getType());
        SkillRegistry.updateSkillProgression(p, false);
        Collection<String> profilesToReset = resetConfigurableProfiles.getOrDefault(p.getUniqueId(), new HashSet<>());
        profilesToReset.add(profile.getSkillTypeName());
        markConfigurableProfilesReset(p.getUniqueId(), profilesToReset);
    }

    /**
     * Tells the plugin that this profile type of a player should be persisted, even if the profile doesn't meet the necessary EXP requirements
     * @param p the player who's profile should be saved
     * @param typeToSave the profile type the plugin should persist
     */
    public static void scheduleProfilePersisting(UUID p, Class<? extends Profile> typeToSave){
        PROFILES_TO_SAVE.computeIfAbsent(p, ignored -> ConcurrentHashMap.newKeySet()).add(typeToSave);
    }

    public static void scheduleConfigurableProfilePersisting(UUID p, String type){
        CONFIGURABLE_PROFILES_TO_SAVE.computeIfAbsent(p, ignored -> ConcurrentHashMap.newKeySet()).add(type);
    }

    private static final Map<UUID, Collection<Class<? extends Profile>>> resetProfiles = new ConcurrentHashMap<>();
    private static final Map<UUID, Collection<String>> resetConfigurableProfiles = new ConcurrentHashMap<>();

    @SuppressWarnings("all")
    public boolean shouldPersist(Profile profile){
        if (profile.getOwner() == null) return false;
        Collection<Class<? extends Profile>> profilesToReset = resetProfiles.getOrDefault(profile.getOwner(), new HashSet<>());
        if (profilesToReset.contains(profile.getClass())) {
            profilesToReset.remove(profile.getClass());
            resetProfiles.put(profile.getOwner(), profilesToReset);
            return true;
        }
        // The dirty queue also tracks level-zero experience, perks, and points.
        return PROFILES_TO_SAVE.getOrDefault(profile.getOwner(), Set.of()).contains(profile.getClass());
    }

    public boolean shouldPersist(ConfigurableProfile profile){
        if (profile.getOwner() == null) return false;
        Collection<String> profilesToReset = resetConfigurableProfiles.getOrDefault(profile.getOwner(), new HashSet<>());
        if (profilesToReset.contains(profile.getSkillTypeName())) {
            profilesToReset.remove(profile.getSkillTypeName());
            resetConfigurableProfiles.put(profile.getOwner(), profilesToReset);
            return true;
        }
        return CONFIGURABLE_PROFILES_TO_SAVE.getOrDefault(profile.getOwner(), Set.of()).contains(profile.getSkillTypeName());
    }

    public static String serializeStringSet(Collection<String> stringSet) {
        return String.join("<>", stringSet);
    }

    public static Set<String> deserializeStringSet(String serializedStringSet) {
        Set<String> set = new HashSet<>(Arrays.asList(serializedStringSet.split("<>")));
        set.removeIf(String::isEmpty);
        return set;
    }

    public static void markProfilesReset(UUID owner, Collection<Class<? extends Profile>> profileTypes){
        resetProfiles.put(owner, profileTypes);
    }

    public static void markConfigurableProfilesReset(UUID owner, Collection<String> profileTypes){
        resetConfigurableProfiles.put(owner, profileTypes);
    }
}

