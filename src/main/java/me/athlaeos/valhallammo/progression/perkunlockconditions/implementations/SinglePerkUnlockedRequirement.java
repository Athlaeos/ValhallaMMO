package me.athlaeos.valhallammo.progression.perkunlockconditions.implementations;

import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileManager;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.progression.Perk;
import me.athlaeos.valhallammo.progression.perkunlockconditions.UnlockCondition;
import me.athlaeos.valhallammo.progression.skills.PerkRegistry;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SinglePerkUnlockedRequirement implements UnlockCondition {
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
        return "requireperk_one";
    }

    @Override
    public boolean canUnlock(Player p, boolean forceTrue) {
        PowerProfile profile = ProfileManager.getPersistentProfile(p, PowerProfile.class);
        return perks == null || perks.isEmpty() ||
                profile.getUnlockedPerks().stream().anyMatch(perks::contains);
    }

    @Override
    public UnlockCondition createInstance() {
        return new SinglePerkUnlockedRequirement();
    }

    @Override
    public String getFailedConditionMessage() {
        return TranslationManager.getTranslation("perk_requirement_warning_perks");
    }

    @Override
    public String getFailurePlaceholder() {
        return "warning_one_perks";
    }

    @Override
    public List<String> getConditionMessages() {
        String format = TranslationManager.getTranslation("perk_format_requirement_one");
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
