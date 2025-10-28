package me.athlaeos.valhallammo.playerstats.profiles;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.persistence.*;
import me.athlaeos.valhallammo.persistence.implementations.RedisLockedSQL;
import me.athlaeos.valhallammo.persistence.implementations.SQL;
import me.athlaeos.valhallammo.persistence.implementations.SQLite;
import me.athlaeos.valhallammo.placeholder.PlaceholderRegistry;
import me.athlaeos.valhallammo.placeholder.placeholders.NumericProfileStatPlaceholder;
import me.athlaeos.valhallammo.placeholder.placeholders.ProfileNextLevelEXPPlaceholder;
import me.athlaeos.valhallammo.placeholder.placeholders.ProfileNextLevelPlaceholder;
import me.athlaeos.valhallammo.playerstats.LeaderboardManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.*;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ProfileRegistry {
    private static ProfilePersistence persistence = null;
    private static final int delay_profile_saving = ConfigManager.getConfig("config.yml").reload().get().getInt("db_persist_delay");
    private static Map<Class<? extends Profile>, Profile> registeredProfiles = Collections.unmodifiableMap(new HashMap<>());
    private static final boolean savingProfilesMessage = ValhallaMMO.getPluginConfig().getBoolean("saving_profiles_notification");

    public static void registerDefaultProfiles(){
        registerProfileType(new PowerProfile(null));
        registerProfileType(new AlchemyProfile(null));
        registerProfileType(new SmithingProfile(null));
        registerProfileType(new EnchantingProfile(null));
        registerProfileType(new HeavyWeaponsProfile(null));
        registerProfileType(new LightWeaponsProfile(null));
        registerProfileType(new ArcheryProfile(null));
        registerProfileType(new HeavyArmorProfile(null));
        registerProfileType(new LightArmorProfile(null));
        registerProfileType(new MiningProfile(null));
        registerProfileType(new FarmingProfile(null));
        registerProfileType(new WoodcuttingProfile(null));
        registerProfileType(new DiggingProfile(null));
        registerProfileType(new FishingProfile(null));
        registerProfileType(new MartialArtsProfile(null));
        registerProfileType(new TradingProfile(null));
    }

    /**
     * Registers a new type of profile, this profile will be persisted in whatever storage method is used.
     * @param p the profile to persist. Any profile properties or owner is not relevant here and may be null.
     */
    public static void registerProfileType(Profile p){
        ClassToInstanceMap<Profile> profiles = MutableClassToInstanceMap.create(new HashMap<>(registeredProfiles));
        profiles.put(p.getClass(), p);
        registeredProfiles = ImmutableClassToInstanceMap.copyOf(profiles);
        p.initStats();
        p.registerPerkRewards();

        persistence.createProfileTable(p);

        for (String numberStat : p.getNumberStatProperties().keySet()) {
            StatFormat format = p.getNumberStatProperties().get(numberStat).getFormat();
            if (format == null) continue;
            PlaceholderRegistry.registerPlaceholder(new NumericProfileStatPlaceholder("%" + p.getClass().getSimpleName().toLowerCase(Locale.US) + "_" + numberStat.toLowerCase(Locale.US) + "%", p.getClass(), numberStat, format));
        }
        PlaceholderRegistry.registerPlaceholder(new ProfileNextLevelPlaceholder("%" + p.getClass().getSimpleName().toLowerCase(Locale.US) + "_next_level%", p.getClass(), StatFormat.INT));
        PlaceholderRegistry.registerPlaceholder(new ProfileNextLevelEXPPlaceholder("%" + p.getClass().getSimpleName().toLowerCase(Locale.US) + "_next_level_exp%", p.getClass(), StatFormat.INT));
    }

    private static long lastSaved = 0;
    public static boolean setupDatabase() {
        if (persistence != null) return true;

        var config = ValhallaMMO.getPluginConfig();
        String type = config.getString("db_type", "sqlite").toLowerCase(Locale.ROOT);
        boolean redis = config.getBoolean("redis_lock", false);
        if (type.equals("mysql")) {
            if (redis) {
                persistence = new RedisLockedSQL();
                if (((RedisLockedSQL) persistence).getPool() == null) {
                    return false;
                }
                ((RedisLockedSQL) persistence).startSaveQueue();
            } else {
                persistence = new SQL();
            }
        } else {
            if (!type.equals("sqlite")) {
                ValhallaMMO.logWarning("Invalid database type " + type + ", defaulting to sqlite");
            }
            if (redis) {
                ValhallaMMO.logWarning("Redis locking is enabled, but database is not MySQL, no redis locking will be used.");
            }
            persistence = new SQLite();
        }

        if (persistence.getConnection() == null) return false; // if SQLite fails, stop the plugin

        persistence.profileThreads.scheduleAtFixedRate(() -> {
            if (lastSaved + delay_profile_saving > System.currentTimeMillis()) return;
            try {
                long start = System.currentTimeMillis();
                if (savingProfilesMessage) ValhallaMMO.logFine("Starting saving all profiles");
                saveAll(false);
                long end = System.currentTimeMillis();
                if (savingProfilesMessage) ValhallaMMO.logFine("Finished saving all profiles in " + ((end - start) / 1000d) + "s");
                LeaderboardManager.refreshLeaderboards();
            } catch (Exception e) {
                throw new RuntimeException("An error occurred while saving profiles", e);
            }
            lastSaved = System.currentTimeMillis();
        }, delay_profile_saving, delay_profile_saving, TimeUnit.MILLISECONDS);
        return true;
    }

    public static void saveAll(boolean async){
        persistence.saveAllProfiles(async);
        ProfileCache.cleanCache();
        LeaderboardManager.refreshLeaderboards();
    }

    /**
     * Grabs the persistence implementation used. By default, this may be {@link SQL} or {@link SQLite}
     * If a SQL connection could not be made, SQLite is attempted. If such a connection could
     * still not be made, whether the file could not be created or the SQLite library is absent, the plugin will disable.
     * @return the persistence implementation
     */
    public static ProfilePersistence getPersistence() {
        return persistence;
    }

    /**
     * Sets the persistence implementation. Should be done around the start of the server, before any players have joined.
     * @param p an alternative persistence implementation
     */
    @SuppressWarnings("unused") // This method is purely here in case other plugins want to change the persistence method of the plugin
    public static void setPersistence(ProfilePersistence p) {
        if (p == null) return;
        persistence = p;
    }

    public static boolean isLoaded(PlayerEvent e) {
        return persistence.isLoaded(e.getPlayer().getUniqueId());
    }

    public static boolean isLoaded(Player p) {
        return p != null && persistence.isLoaded(p.getUniqueId());
    }

    /**
     * Sets a profile as the player's persistent profile.
     * Persistent profiles are profiles that are persisted regularly and on leaving the server and so should be used
     * to store data that the player should realistically keep.
     * @param p the player to set the profile to
     * @param profile the profile to set
     * @param type the class of profile it should be saved as
     */
    public static void setPersistentProfile(Player p, Profile profile, Class<? extends Profile> type) {
        persistence.trySetPersistentProfile(p.getUniqueId(), profile, type);
    }

    public static void setBlankSkillProfile(Player p, Class<? extends Profile> type) {
        setSkillProfile(p, getBlankProfile(p, type), type);
    }

    /**
     * Sets a profile as the player's skill profile.
     * Skill profiles are profiles that aren't persisted, where its stats and contents are calculated when the player
     * joins the server. This means that any changes made to a skill profile will not stay, and you may want to consider
     * using {@link ProfileRegistry#setPersistentProfile(Player, Profile, Class)} instead. <br>
     * This type of profile exists so that a separation exists between skill-acquired stats and manually given stats,
     * and that a player's skill-based stats may be updated without needing to be reset should any changes to the skill's
     * respective config be made.
     * @param p the player to set the profile to
     * @param profile the profile to set
     * @param type the class of profile it should be saved as
     */
    public static void setSkillProfile(Player p, Profile profile, Class<? extends Profile> type) {
        persistence.trySetSkillProfile(p.getUniqueId(), profile, type);
    }

    public static <P extends Profile> P getPersistentProfile(Player p, Class<P> type) {
        P profile = persistence.getPersistentProfile(p.getUniqueId(), type);
        return profile == null ? getBlankProfile(p, type) : profile;
    }

    public static <P extends Profile> P getSkillProfile(Player p, Class<P> type) {
        P profile = persistence.getSkillProfile(p.getUniqueId(), type);
        return profile == null ? getBlankProfile(p, type) : profile;
    }

    @SuppressWarnings("unchecked")
    public static <P extends Profile> P getMergedProfile(Player p, Class<P> type) {
        P p1 = getPersistentProfile(p, type);
        P p2 = getSkillProfile(p, type);
        return (P) p2.merge(p1, p);
    }

    /**
     * Returns a map with all the registered profiles. Modifying this map will not remove or create any new registered profile types.
     * Registering profile types may only be done through {@link ProfileRegistry#registerProfileType(Profile)}
     * @return a new map containing all registered profiles
     */
    public static Map<Class<? extends Profile>, Profile> getRegisteredProfiles() {
        return new HashMap<>(registeredProfiles);
    }

    public static <P extends Profile> P getBlankProfile(Class<P> type){
        return getBlankProfile((UUID) null, type);
    }

    public static <P extends Profile> P getBlankProfile(Player owner, Class<P> type){
        return getBlankProfile(owner.getUniqueId(), type);
    }

    @SuppressWarnings("unchecked") // Registered profiles will always match the class type given how registerProfileType() works
    public static <P extends Profile> P getBlankProfile(UUID owner, Class<P> type){
        if (!registeredProfiles.containsKey(type)) throw new IllegalArgumentException("Profile type " + type.getSimpleName() + " was not yet registered for usage");
        return (P) registeredProfiles.get(type).getBlankProfile(owner);
    }

    public static void reset(Player p, ResetType type) {
        persistence.resetProfile(p, type);
    }

    public static <S extends Skill> void reset(Player p, Class<S> type) {
        persistence.resetSkillProgress(p, type);
    }

    public static <P extends Profile> P copyDefaultStats(P profile) {
        P defaultProfile = (P) registeredProfiles.get(profile.getClass());
        if (defaultProfile == null) return profile;
        profile.copyStats(defaultProfile);
        return profile;
    }
}
