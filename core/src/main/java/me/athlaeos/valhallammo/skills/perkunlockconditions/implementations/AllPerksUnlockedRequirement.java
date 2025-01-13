package me.athlaeos.valhallammo.skills.perkunlockconditions.implementations;

import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.skills.skills.Perk;
import me.athlaeos.valhallammo.skills.perkunlockconditions.UnlockCondition;
import me.athlaeos.valhallammo.skills.skills.PerkRegistry;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class AllPerksUnlockedRequirement implements UnlockCondition {
    private List<String> perks = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override
    public void initCondition(Object value) {
        if (value instanceof List<?>){
            perks = (List<String>) value;
        }
    }

    @Override
    public String getValuePlaceholder() {
        return "requireperk_all";
    }

    @Override
    public boolean canUnlock(Player p, boolean forceTrue) {
        PowerProfile profile = ProfileRegistry.getPersistentProfile(p, PowerProfile.class);
        Collection<String> unlocked = new HashSet<>(profile.getUnlockedPerks());
        unlocked.addAll(profile.getPermanentlyUnlockedPerks());
        return perks == null || perks.isEmpty() ||
                unlocked.containsAll(perks);
    }

    @Override
    public UnlockCondition createInstance() {
        return new AllPerksUnlockedRequirement();
    }

    @Override
    public String getFailedConditionMessage() {
        return TranslationManager.getTranslation("perk_requirement_warning_perks");
    }

    @Override
    public String getFailurePlaceholder() {
        return "warning_all_perks";
    }

    @Override
    public List<String> getConditionMessages() {
        String format = TranslationManager.getTranslation("perk_format_requirement_all");
        String entry = TranslationManager.getTranslation("perk_format_requirement");
        List<String> result = new ArrayList<>();
        result.add(format);
        for (String p : perks){
            Perk perk = PerkRegistry.getPerk(p);
            if (perk != null) result.add(Utils.chat(entry.replace("%perk_required%", perk.getDisplayName())));
        }
        return result;
    }
}
