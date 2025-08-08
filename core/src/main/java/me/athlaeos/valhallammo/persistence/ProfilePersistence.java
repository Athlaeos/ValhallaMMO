package me.athlaeos.valhallammo.persistence;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.listeners.JoinLeaveListener;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.LeaderboardEntry;
import me.athlaeos.valhallammo.playerstats.LeaderboardManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.skills.perkresourcecost.ResourceExpense;
import me.athlaeos.valhallammo.skills.skills.Perk;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.ResetType;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public abstract class ProfilePersistence {
    private static final Map<UUID, Collection<Class<? extends Profile>>> PROFILES_TO_SAVE = new HashMap<>();

    public final ScheduledExecutorService profileThreads;
    protected final AsyncLoadingCache<UUID, ClassToInstanceMap<Profile>> persistentProfiles;
    protected final Map<UUID, ClassToInstanceMap<Profile>> skillProfiles;
    protected final Set<UUID> saving = new HashSet<>();

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
        skillProfiles = new HashMap<>();
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
    }

    @SuppressWarnings("unchecked")
    public <T extends Profile> boolean fillProfile(UUID player, T profile) {
        Class<T> clazz = (Class<T>) profile.getClass();
        try {
            PreparedStatement stmt = getConnection().prepareStatement("SELECT * FROM " + profile.getTableName() + " WHERE owner = ?;");
            stmt.setString(1, player.toString());
            ResultSet result = stmt.executeQuery();
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
        } catch (SQLException ex){
            ValhallaMMO.logSevere("SQLException when trying to fetch " + player + "'s profile of type " + clazz.getSimpleName() + ". ");
            ex.printStackTrace();
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
            ValhallaMMO.getInstance().getServer().getLogger().severe("SQLException when trying to save profile for profile type " + profile.getClass().getName() + ". ");
            exception.printStackTrace();
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
            whereClauses.add(String.format("main_stat >= %.2f", mainStat.getTwo()));
        for (String stat : extraStats.keySet()){
            Pair<String, Double> statWithMinimum = extraStats.get(stat);
            if (statWithMinimum == null || statWithMinimum.getTwo() == null) continue;
            whereClauses.add(String.format("%s >= %.2f", stat, statWithMinimum.getTwo()));
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
        AccumulativeStatManager.resetAllCaches(uuid);
        JoinLeaveListener.getLoadedProfiles().add(uuid);
        boolean finalRunPersistentStartingPerks = runPersistentStartingPerks;
        Bukkit.getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                Utils.sendMessage(player, TranslationManager.getTranslation("status_profiles_loaded"));
                SkillRegistry.updateSkillProgression(player, finalRunPersistentStartingPerks);
            } else {
                uncacheProfile(uuid);
            }
        });
        return profiles;
    }

    public void saveAllProfiles(boolean async) {
        for (UUID uuid : persistentProfiles.asMap().keySet()) {
            if (async) {
                saveProfileAsync(uuid);
            } else {
                saveProfile(uuid);
            }
        }
    }
    public void saveProfileAsync(UUID p) {
        if (!saving.add(p)) return;

        CompletableFuture.runAsync(() -> saveProfile(p), profileThreads).whenComplete((ignored, ex) -> {
            if (ex != null) {
                ValhallaMMO.logWarning("Exception when trying to save profile for " + p + ": ");
                ex.printStackTrace();
            }
            saving.remove(p);
        });
    }

    public void saveProfile(UUID p) {
        if (!isLoaded(p)) return;
        else if (!saving.add(p)) return;

        ClassToInstanceMap<Profile> profiles = persistentProfiles.get(p).join();
        for (Profile profile : profiles.values()) {
            if (shouldPersist(profile)) insertOrUpdateProfile(p, profile);
        }

        saving.remove(p);
        Player player = Bukkit.getPlayer(p);
        if (player == null || !player.isOnline()) uncacheProfile(p);
    }

    public void uncacheProfile(UUID p) {
        persistentProfiles.synchronous().invalidate(p);
        skillProfiles.remove(p);
        JoinLeaveListener.getLoadedProfiles().remove(p);
        ProfileCache.resetCache(p);
    }

    public void uncacheAllProfiles() {
        persistentProfiles.synchronous().invalidateAll();
        skillProfiles.clear();
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



    public Map<Integer, LeaderboardEntry> queryLeaderboardEntries(LeaderboardManager.Leaderboard leaderboard) {
        Map<Integer, LeaderboardEntry> entries = new HashMap<>();
        Profile profile = ProfileRegistry.getRegisteredProfiles().get(leaderboard.profile());
        try {
            PreparedStatement stmt = getConnection().prepareStatement(leaderboardQuery(profile, leaderboard.mainStat(), leaderboard.extraStats()));
            ResultSet set = stmt.executeQuery();
            int rank = 1;
            while (set.next()){
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

                    Skill associatedSkill = SkillRegistry.getSkill(profileType.getSkillType());
                    associatedSkill.getPerks().forEach(perk -> {
                        Collection<String> unlockedPerks = powerProfile.getUnlockedPerks();
                        unlockedPerks.remove(perk.getName());
                        powerProfile.setUnlockedPerks(unlockedPerks);
                    });
                    trySetSkillProfile(uuid, profileType.getBlankProfile(p), profileType.getClass());
                }
                setPersistentProfile(uuid, powerProfile, PowerProfile.class);
            }
            case SKILLS_AND_STATS -> {
                // set both persistent and skill stats to 0
                for (Profile profileType : ProfileRegistry.getRegisteredProfiles().values()) {
                    trySetPersistentProfile(uuid, profileType.getBlankProfile(p), profileType.getClass());
                    trySetSkillProfile(uuid, profileType.getBlankProfile(p), profileType.getClass());
                }
                runPersistentStartingPerks = true;
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
    }

    /**
     * Tells the plugin that this profile type of a player should be persisted, even if the profile doesn't meet the necessary EXP requirements
     * @param p the player who's profile should be saved
     * @param typeToSave the profile type the plugin should persist
     */
    public static void scheduleProfilePersisting(UUID p, Class<? extends Profile> typeToSave){
        Collection<Class<? extends Profile>> types = PROFILES_TO_SAVE.getOrDefault(p, new HashSet<>());
        types.add(typeToSave);
        PROFILES_TO_SAVE.put(p, types);
    }

    @SuppressWarnings("all")
    public boolean shouldPersist(Profile profile){
        if (profile.getOwner() == null || (profile.getLevel() == 0 && profile.getNewGamePlus() == 0 && !profile.shouldForcePersist())) return false;
        return PROFILES_TO_SAVE.getOrDefault(profile.getOwner(), Set.of()).contains(profile.getClass());
    }

    public static String serializeStringSet(Collection<String> stringSet) {
        return String.join("<>", stringSet);
    }

    public static Set<String> deserializeStringSet(String serializedStringSet) {
        Set<String> set = new HashSet<>(Arrays.asList(serializedStringSet.split("<>")));
        set.removeIf(String::isEmpty);
        return set;
    }
}

