package me.athlaeos.valhallammo.persistence;

import me.athlaeos.valhallammo.skills.perkresourcecost.ResourceExpense;
import me.athlaeos.valhallammo.skills.skills.Perk;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.ResetType;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public abstract class ProfilePersistence {

    public abstract void setPersistentProfile(Player p, Profile profile, Class<? extends Profile> type);
    public abstract void setSkillProfile(Player p, Profile profile, Class<? extends Profile> type);

    public abstract <T extends Profile> T getPersistentProfile(Player p, Class<T> type);
    public abstract <T extends Profile> T getSkillProfile(Player p, Class<T> type);

    public abstract void onProfileRegistration(Profile profile);

    public void resetProfile(Player p, ResetType resetType) {
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
                    Profile resetProfile = profileType.getBlankProfile(p);
                    resetProfile.setTotalEXP(totalEXP);
                    resetProfile.setEXP(EXP);
                    resetProfile.setLevel(level);
                    resetProfile.setNewGamePlus(ngPlus);
                    runPersistentStartingPerks = true;
                    setPersistentProfile(p, resetProfile, profileType.getClass());
                }
            }
            case SKILLS_ONLY -> {
                // Only resets skill progress, keeping persistent stats
                PowerProfile powerProfile = ProfileRegistry.getPersistentProfile(p, PowerProfile.class);
                for (Profile profileType : ProfileRegistry.getRegisteredProfiles().values()) {
                    // setting persistent properties to 0, removing perks from unlocked perks and permalocked perks
                    Profile profile = getPersistentProfile(p, profileType.getClass());
                    profile.setEXP(0);
                    profile.setTotalEXP(0);
                    profile.setLevel(0);
                    profile.setNewGamePlus(0);
                    setPersistentProfile(p, profile, profileType.getClass());

                    Skill associatedSkill = SkillRegistry.getSkill(profileType.getSkillType());
                    associatedSkill.getPerks().forEach(perk -> {
                        Collection<String> unlockedPerks = powerProfile.getUnlockedPerks();
                        unlockedPerks.remove(perk.getName());
                        powerProfile.setUnlockedPerks(unlockedPerks);
                    });
                    setSkillProfile(p, profileType.getBlankProfile(p), profileType.getClass());
                }
                setPersistentProfile(p, powerProfile, PowerProfile.class);
            }
            case SKILLS_AND_STATS -> {
                // set both persistent and skill stats to 0
                for (Profile profileType : ProfileRegistry.getRegisteredProfiles().values()) {
                    setPersistentProfile(p, profileType.getBlankProfile(p), profileType.getClass());
                    setSkillProfile(p, profileType.getBlankProfile(p), profileType.getClass());
                }
                runPersistentStartingPerks = true;
            }
            case SKILLS_REFUND_EXP -> {
                PowerProfile powerProfile = ProfileRegistry.getPersistentProfile(p, PowerProfile.class);
                powerProfile.setUnlockedPerks(new HashSet<>());
                powerProfile.setFakeUnlockedPerks(new HashSet<>());
                powerProfile.setPermanentlyLockedPerks(new HashSet<>());
                ProfileRegistry.setPersistentProfile(p, powerProfile, PowerProfile.class);
                setSkillProfile(p, ProfileRegistry.getRegisteredProfiles().get(PowerProfile.class).getBlankProfile(p), PowerProfile.class);

                // resets skill progress but leaves persistent progress untouched, and updates based on that
                for (Profile profileType : ProfileRegistry.getRegisteredProfiles().values()) {
                    if (profileType instanceof PowerProfile) continue;
                    setSkillProfile(p, profileType.getBlankProfile(p), profileType.getClass());
                }
            }
        }
        SkillRegistry.updateSkillProgression(p, runPersistentStartingPerks);
    }

    public void resetSkillProgress(Player p, Class<? extends Skill> resetSkill) {
        PowerProfile powerProfile = ProfileRegistry.getPersistentProfile(p, PowerProfile.class);
        Skill associatedSkill = SkillRegistry.getSkill(resetSkill);

        Profile profile = getPersistentProfile(p, associatedSkill.getProfileType());
        profile.setEXP(0);
        profile.setLevel(0);
        setPersistentProfile(p, profile, associatedSkill.getProfileType());

        Collection<String> unlockedPerks = powerProfile.getUnlockedPerks();
        Collection<String> unlockedPerksCopy = new HashSet<>(powerProfile.getUnlockedPerks());
        unlockedPerks.removeAll(associatedSkill.getPerks().stream().map(Perk::getName).collect(Collectors.toSet()));
        powerProfile.setUnlockedPerks(unlockedPerks);
        setPersistentProfile(p, powerProfile, PowerProfile.class);

        List<Perk> invertedPerks = new ArrayList<>(associatedSkill.getPerks());
        Collections.reverse(invertedPerks);
        invertedPerks.forEach(perk -> {
            if (!unlockedPerksCopy.contains(perk.getName())) return;
            for (ResourceExpense expense : perk.getExpenses()){
                if (!expense.isRefundable()) continue;
                expense.refund(p);
            }
        });

        setSkillProfile(p, profile.getBlankProfile(p), associatedSkill.getProfileType());
        SkillRegistry.updateSkillProgression(p, false);
    }

    public abstract void loadProfile(Player p);

    public abstract void saveAllProfiles();

    public abstract void saveProfile(Player p);

    public static String serializeStringSet(Collection<String> stringSet) {
        return String.join("<>", stringSet);
    }

    private static final Map<UUID, Collection<Class<? extends Profile>>> profilesToSave = new HashMap<>();

    /**
     * Tells the plugin that this profile type of a player should be persisted, even if the profile doesn't meet the necessary EXP requirements
     * @param p the player who's profile should be saved
     * @param typeToSave the profile type the plugin should persist
     */
    public static void scheduleProfilePersisting(Player p, Class<? extends Profile> typeToSave){
        Collection<Class<? extends Profile>> types = profilesToSave.getOrDefault(p.getUniqueId(), new HashSet<>());
        types.add(typeToSave);
        profilesToSave.put(p.getUniqueId(), types);
    }
    @SuppressWarnings("all")
    public boolean shouldPersist(Profile profile){
        if (profile.getOwner() == null) return false;
        return profilesToSave.getOrDefault(profile.getOwner(), new HashSet<>()).contains(profile.getClass());
    }

    public static Collection<String> deserializeStringSet(String serializedStringSet) {
        Collection<String> set = new HashSet<>(Arrays.asList(serializedStringSet.split("<>")));
        set.removeIf(String::isEmpty);
        return set;
    }
}

