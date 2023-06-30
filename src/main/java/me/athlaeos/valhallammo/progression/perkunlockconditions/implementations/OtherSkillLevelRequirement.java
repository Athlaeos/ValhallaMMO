package me.athlaeos.valhallammo.progression.perkunlockconditions.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileManager;
import me.athlaeos.valhallammo.progression.perkunlockconditions.UnlockCondition;
import me.athlaeos.valhallammo.progression.skills.Skill;
import me.athlaeos.valhallammo.progression.skills.SkillRegistry;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtherSkillLevelRequirement implements UnlockCondition {
    private final Map<String, Integer> moreLevelRequirements = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public void initCondition(Object value) {
        if (value instanceof List<?>){
            for (String p : (List<String>) value){
                String[] split = p.split(":");
                if (split.length < 2) continue;
                try {
                    int level = Integer.parseInt(split[1]);
                    String s = split[0];
                    moreLevelRequirements.put(s, level);
                } catch (IllegalArgumentException ignored){
                    ValhallaMMO.logWarning("Invalid other_levels_required condition in perk. Should be formatted as SKILL:LEVEL, where LEVEL is an integer.");
                }
            }
        }
    }

    @Override
    public String getValuePlaceholder() {
        return "other_levels_required";
    }

    @Override
    public String getFailurePlaceholder() {
        return "warning_other_levels";
    }

    @Override
    public boolean canUnlock(Player p, boolean forceTrue) {
        if (forceTrue) return true;
        for (String s : moreLevelRequirements.keySet()){
            Skill skill = SkillRegistry.getSkill(s);
            if (skill == null) continue;
            Profile profile = ProfileManager.getPersistentProfile(p, skill.getProfileType());
            if (moreLevelRequirements.get(s) > profile.getLevel()) return false;
        }
        return true;
    }

    @Override
    public UnlockCondition createInstance() {
        return new OtherSkillLevelRequirement();
    }

    @Override
    public String getFailedConditionMessage() {
        return TranslationManager.getTranslation("perk_requirement_warning_levels");
    }

    @Override
    public List<String> getConditionMessages() {
        String entry = TranslationManager.getTranslation("perk_other_level_requirement");
        List<String> result = new ArrayList<>();
        for (String r : moreLevelRequirements.keySet()){
            Skill skill = SkillRegistry.getSkill(r);
            if (skill != null) result.add(Utils.chat(
                    entry.replace("%skill%", skill.getDisplayName()).replace("%level_required%", moreLevelRequirements.get(r).toString()))
            );
        }
        return result;
    }
}
