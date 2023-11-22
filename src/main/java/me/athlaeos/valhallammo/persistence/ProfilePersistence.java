package me.athlaeos.valhallammo.persistence;

import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.playerstats.LeaderboardEntry;
import me.athlaeos.valhallammo.skills.skills.Perk;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.ResetType;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ProfilePersistence {

    public abstract void setPersistentProfile(Player p, Profile profile, Class<? extends Profile> type);
    public abstract void setSkillProfile(Player p, Profile profile, Class<? extends Profile> type);

    public abstract void queryLeaderboardEntries(Class<? extends Profile> profile, String stat, int page, Action<List<LeaderboardEntry>> callback, Collection<String> extraStats);

    public abstract <T extends Profile> T getPersistentProfile(Player p, Class<T> type);
    public abstract <T extends Profile> T getSkillProfile(Player p, Class<T> type);

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
                    Profile resetProfile = profileType.getBlankProfile(p);
                    resetProfile.setTotalEXP(totalEXP);
                    resetProfile.setEXP(EXP);
                    resetProfile.setLevel(level);
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
                // resets both skill and persistent progress, but refunds exp
                for (Profile profileType : ProfileRegistry.getRegisteredProfiles().values()) {
                    Profile profile = getPersistentProfile(p, profileType.getClass());
                    double totalEXP = profile.getTotalEXP();

                    setPersistentProfile(p, profileType.getBlankProfile(p), profileType.getClass());
                    setSkillProfile(p, profileType.getBlankProfile(p), profileType.getClass());

                    SkillRegistry.getSkill(profile.getSkillType()).addEXP(p, totalEXP, true, PlayerSkillExperienceGainEvent.ExperienceGainReason.RESET);
                }
                runPersistentStartingPerks = true;
            }
        }
        SkillRegistry.updateSkillProgression(p, runPersistentStartingPerks);
    }

    public void resetSkillProgress(Player p, Class<? extends Skill> resetSkill) {
        PowerProfile powerProfile = ProfileRegistry.getPersistentProfile(p, PowerProfile.class);
        Skill associatedSkill = SkillRegistry.getSkill(resetSkill);

        Profile profile = getPersistentProfile(p, associatedSkill.getProfileType());
        profile.setEXP(0);
        profile.setTotalEXP(0);
        profile.setLevel(0);
        setPersistentProfile(p, profile, associatedSkill.getProfileType());

        Collection<String> unlockedPerks = powerProfile.getUnlockedPerks();
        unlockedPerks.removeAll(associatedSkill.getPerks().stream().map(Perk::getName).collect(Collectors.toSet()));
        powerProfile.setUnlockedPerks(unlockedPerks);

        setSkillProfile(p, profile.getBlankProfile(p), associatedSkill.getProfileType());

        setPersistentProfile(p, powerProfile, PowerProfile.class);
        SkillRegistry.updateSkillProgression(p, false);
    }

    public abstract void loadProfile(Player p);

    public abstract void saveAllProfiles();

    public abstract void saveProfile(Player p);

    public static String serializeStringSet(Collection<String> stringSet) {
        return String.join("<>", stringSet);
    }

    public static Collection<String> deserializeStringSet(String serializedStringSet) {
        Collection<String> set = new HashSet<>(Arrays.asList(serializedStringSet.split("<>")));
        set.removeIf(String::isEmpty);
        return set;
    }
}

