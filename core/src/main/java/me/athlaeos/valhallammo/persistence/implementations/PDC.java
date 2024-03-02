package me.athlaeos.valhallammo.persistence.implementations;

import com.google.gson.Gson;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.persistence.ProfilePersistence;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class PDC extends ProfilePersistence {
    private final Gson gson = new Gson();
    private final Map<UUID, Map<Class<? extends Profile>, Profile>> persistentProfiles = new HashMap<>();
    private final Map<UUID, Map<Class<? extends Profile>, Profile>> skillProfiles = new HashMap<>();

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
        if (persistentProfiles.containsKey(p.getUniqueId())) return;
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
            Map<Class<? extends Profile>, Profile> profiles = persistentProfiles.getOrDefault(p.getUniqueId(), new HashMap<>());
            for (Profile pr : ProfileRegistry.getRegisteredProfiles().values()) {
                String jsonProfile = p.getPersistentDataContainer().get(pr.getKey(), PersistentDataType.STRING);
                Profile loadedProfile = pr.getBlankProfile(p);
                PersistableProfile tempProfile = gson.fromJson(jsonProfile, PersistableProfile.class);

                for (String s : pr.intStatNames()) loadedProfile.setInt(s, tempProfile.intStats.getOrDefault(s, pr.getDefaultInt(s)));
                for (String s : pr.floatStatNames()) loadedProfile.setFloat(s, tempProfile.floatStats.getOrDefault(s, pr.getDefaultFloat(s)));
                for (String s : pr.doubleStatNames()) loadedProfile.setDouble(s, tempProfile.doubleStats.getOrDefault(s, pr.getDefaultDouble(s)));
                for (String s : pr.stringSetStatNames()) loadedProfile.setStringSet(s, tempProfile.stringSetStats.getOrDefault(s, new HashSet<>()));
                for (String s : pr.booleanStatNames()) loadedProfile.setBoolean(s, tempProfile.booleanStats.getOrDefault(s, pr.getDefaultBoolean(s)));

                profiles.put(pr.getClass(), loadedProfile);
            }
            persistentProfiles.put(p.getUniqueId(), profiles);
            p.sendMessage(Utils.chat(TranslationManager.getTranslation("status_profiles_loaded")));

            SkillRegistry.updateSkillProgression(p, false);
        });
    }

    @Override
    public void saveAllProfiles() {
        for (UUID p : new HashSet<>(persistentProfiles.keySet())){
            Player player = ValhallaMMO.getInstance().getServer().getPlayer(p);
            if (player == null || !player.isOnline()) {
                persistentProfiles.remove(p);
                continue;
            }
            saveProfile(player);
        }
    }

    @Override
    public void saveProfile(Player p) {
        if (persistentProfiles.containsKey(p.getUniqueId())){
            for (Profile pr : persistentProfiles.get(p.getUniqueId()).values()){
                PersistableProfile tempProfile = new PersistableProfile();

                for (String s : pr.intStatNames()) tempProfile.intStats.put(s, pr.getInt(s));
                for (String s : pr.floatStatNames()) tempProfile.floatStats.put(s, pr.getFloat(s));
                for (String s : pr.doubleStatNames()) tempProfile.doubleStats.put(s, pr.getDouble(s));
                for (String s : pr.stringSetStatNames()) tempProfile.stringSetStats.put(s, pr.getStringSet(s));
                for (String s : pr.booleanStatNames()) tempProfile.booleanStats.put(s, pr.getBoolean(s));

                String jsonProfile = gson.toJson(tempProfile);
                p.getPersistentDataContainer().set(pr.getKey(), PersistentDataType.STRING, jsonProfile);
            }
        }
    }

    /**
     * A standard {@link Profile} is too complicated to persist efficiently, so we simply store all of a profile's stats in
     * this temporary PersistableProfile object for easier fetching and storing. This also allows us to
     * replace missing values with a default value in case new stats were added to a profile type
     */
    private static class PersistableProfile{
        private final Map<String, Collection<String>> stringSetStats = new HashMap<>();
        private final Map<String, Integer> intStats = new HashMap<>();
        private final Map<String, Float> floatStats = new HashMap<>();
        private final Map<String, Double> doubleStats = new HashMap<>();
        private final Map<String, Boolean> booleanStats = new HashMap<>();
    }
}
