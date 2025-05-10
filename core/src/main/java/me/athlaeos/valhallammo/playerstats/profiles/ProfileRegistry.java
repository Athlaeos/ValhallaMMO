package me.athlaeos.valhallammo.playerstats.profiles;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.persistence.*;
import me.athlaeos.valhallammo.persistence.implementations.PDC;
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
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileRegistry {
    private static ProfilePersistence persistence = null;
    private static final int delay_profile_saving = ConfigManager.getConfig("config.yml").reload().get().getInt("db_persist_delay");
    private static Map<Class<? extends Profile>, Profile> registeredProfiles = Collections.unmodifiableMap(new HashMap<>());

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
        Map<Class<? extends Profile>, Profile> profiles = new HashMap<>(registeredProfiles);
        profiles.put(p.getClass(), p);
        registeredProfiles = Collections.unmodifiableMap(profiles);
        p.registerPerkRewards();

        persistence.onProfileRegistration(p);

        for (String numberStat : p.getNumberStatProperties().keySet()) {
            StatFormat format = p.getNumberStatProperties().get(numberStat).getFormat();
            if (format == null) continue;
            PlaceholderRegistry.registerPlaceholder(new NumericProfileStatPlaceholder("%" + p.getClass().getSimpleName().toLowerCase(Locale.US) + "_" + numberStat.toLowerCase(Locale.US) + "%", p.getClass(), numberStat, format));
        }
        PlaceholderRegistry.registerPlaceholder(new ProfileNextLevelPlaceholder("%" + p.getClass().getSimpleName().toLowerCase(Locale.US) + "_next_level%", p.getClass(), StatFormat.INT));
        PlaceholderRegistry.registerPlaceholder(new ProfileNextLevelEXPPlaceholder("%" + p.getClass().getSimpleName().toLowerCase(Locale.US) + "_next_level_exp%", p.getClass(), StatFormat.INT));
    }

    public static void setupDatabase(){
        if (persistence != null) return;
        persistence = new SQL();
        if (((Database) persistence).getConnection() == null) persistence = new SQLite(); // if SQL connection fails, choose SQLite
        if (((Database) persistence).getConnection() == null) persistence = new PDC(); // if SQLite fails, choose PDC

        ValhallaMMO.getInstance().getServer().getScheduler().runTaskTimerAsynchronously(ValhallaMMO.getInstance(), () -> {
            saveAll();
            LeaderboardManager.refreshLeaderboards();
        }, delay_profile_saving, delay_profile_saving);
    }

    public static void saveAll(){
        persistence.saveAllProfiles();
        ProfileCache.cleanCache();
        LeaderboardManager.refreshLeaderboards();
    }

    /**
     * Grabs the persistence implementation used. By default, this may be {@link SQL}, {@link SQLite},
     * or {@link PDC} (Persistent Data Container). If a SQL connection could not be made, SQLite is attempted. If such a connection could
     * still not be made, whether the file could not be created or the SQLite library is absent, PDC is used.
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

    /**
     * Sets a profile as the player's persistent profile.
     * Persistent profiles are profiles that are persisted regularly and on leaving the server and so should be used
     * to store data that the player should realistically keep.
     * @param p the player to set the profile to
     * @param profile the profile to set
     * @param type the class of profile it should be saved as
     */
    public static void setPersistentProfile(Player p, Profile profile, Class<? extends Profile> type) {
        persistence.setPersistentProfile(p, profile, type);
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
        persistence.setSkillProfile(p, profile, type);
    }

    public static <T extends Profile> T getPersistentProfile(Player p, Class<T> type) {
        T profile = persistence.getPersistentProfile(p, type);
        return profile == null ? getBlankProfile(p, type) : profile;
    }

    public static <T extends Profile> T getSkillProfile(Player p, Class<T> type) {
        T profile = persistence.getSkillProfile(p, type);
        return profile == null ? getBlankProfile(p, type) : profile;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Profile> T getMergedProfile(Player p, Class<T> type) {
        Profile p1 = getPersistentProfile(p, type);
        Profile p2 = getSkillProfile(p, type);
        return (T) p2.merge(p1, p);
    }

    /**
     * Returns a map with all the registered profiles. Modifying this map will not remove or create any new registered profile types.
     * Registering profile types may only be done through {@link ProfileRegistry#registerProfileType(Profile)}
     * @return a new map containing all registered profiles
     */
    public static Map<Class<? extends Profile>, Profile> getRegisteredProfiles() {
        return new HashMap<>(registeredProfiles);
    }

    @SuppressWarnings("unchecked") // Registered profiles will always match the class type given how registerProfileType() works
    public static <T extends Profile> T getBlankProfile(Player owner, Class<T> type){
        if (!registeredProfiles.containsKey(type)) throw new IllegalArgumentException("Profile type " + type.getSimpleName() + " was not yet registered for usage");
        return (T) registeredProfiles.get(type).getBlankProfile(owner);
    }

    public static void reset(Player p, ResetType type) {
        persistence.resetProfile(p, type);
    }

    public static void reset(Player p, Class<? extends Skill> type) {
        persistence.resetSkillProgress(p, type);
    }
}
