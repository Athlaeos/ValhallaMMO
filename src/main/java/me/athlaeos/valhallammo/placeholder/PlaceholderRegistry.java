package me.athlaeos.valhallammo.placeholder;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.hooks.PAPIHook;
import me.athlaeos.valhallammo.placeholder.placeholders.*;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlaceholderRegistry {
    private static final Map<String, Placeholder> placeholders = new HashMap<>();

    {
        // auto-registering profile stat placeholders
        for (Profile profile : ProfileRegistry.getRegisteredProfiles().values()){
            for (String intStat : profile.intStatNames()) {
                StatFormat format = profile.getNumberStatProperties().get(intStat).getFormat();
                if (format == null) continue;
                registerPlaceholder(new NumericProfileStatPlaceholder("%" + profile.getClass().getSimpleName().toLowerCase() + "_" + intStat.toLowerCase() + "%", profile.getClass(), intStat, format));
            }
            registerPlaceholder(new ProfileNextLevelPlaceholder("%" + profile.getClass().getSimpleName().toLowerCase() + "_next_level%", profile.getClass(), StatFormat.INT));
            registerPlaceholder(new ProfileNextLevelEXPPlaceholder("%" + profile.getClass().getSimpleName().toLowerCase() + "_next_level_exp%", profile.getClass(), StatFormat.INT));
        }

        for (String statSource : AccumulativeStatManager.getSources().keySet()) {
            registerPlaceholder(new TotalStatPlaceholder("%stat_source_" + statSource.toLowerCase() + "%", statSource));
        }

        registerPlaceholder(new SpendableSkillPointsPlaceholder("%skillpoints%"));
        registerPlaceholder(new SpendablePrestigePointsPlaceholder("%prestigepoints%"));
        registerPlaceholder(new RegionalDifficultyLevelPlaceholder("%difficulty_regional%"));
        registerPlaceholder(new RegionalDifficultyLevelRoundedPlaceholder("%difficulty_regional_rounded%"));
    }

    public static void registerPlaceholder(Placeholder p) {
        placeholders.put(p.getPlaceholder(), p);
    }

    public static Map<String, Placeholder> getPlaceholders() {
        return placeholders;
    }

    public static String parse(String stringToParse, Player p) {
        for (Placeholder s : placeholders.values()) {
            if (stringToParse.contains(s.getPlaceholder())) {
                stringToParse = s.parse(stringToParse, p);
            }
        }
        if (ValhallaMMO.isHookFunctional(PAPIHook.class)) stringToParse = PAPIHook.parse(p, stringToParse);
        return stringToParse;
    }
}
