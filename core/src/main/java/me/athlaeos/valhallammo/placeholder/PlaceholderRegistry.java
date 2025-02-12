package me.athlaeos.valhallammo.placeholder;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.hooks.PAPIHook;
import me.athlaeos.valhallammo.placeholder.placeholders.*;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PlaceholderRegistry {
    private static final Map<String, Placeholder> placeholders = new HashMap<>();

    static {
        registerPlaceholder(new SpendableSkillPointsPlaceholder("%skillpoints%"));
        registerPlaceholder(new SpendablePrestigePointsPlaceholder("%prestigepoints%"));
        registerPlaceholder(new RegionalDifficultyLevelPlaceholder("%difficulty_regional%"));
        registerPlaceholder(new RegionalDifficultyLevelRoundedPlaceholder("%difficulty_regional_rounded%"));
        registerPlaceholder(new PartyNamePlaceholder("%party_name%"));
        registerPlaceholder(new PartyDescriptionPlaceholder("%party_description%"));
    }

    public static void registerPlaceholder(Placeholder p) {
        placeholders.put(p.getPlaceholder(), p);
    }

    public static Map<String, Placeholder> getPlaceholders() {
        return placeholders;
    }

    private static final Map<String, Collection<Placeholder>> placeholderCache = new HashMap<>();

    public static String parse(String stringToParse, Player p) {
        String result = stringToParse;
        boolean cache = !placeholderCache.containsKey(stringToParse);
        Collection<Placeholder> placeholdersToCache = new HashSet<>();
        for (Placeholder s : placeholderCache.getOrDefault(stringToParse, placeholders.values())) {
            if (stringToParse.contains(s.getPlaceholder())) {
                result = s.parse(result, p);
                if (cache) placeholdersToCache.add(s);
            }
        }
        if (cache) placeholderCache.put(stringToParse, placeholdersToCache);
        return result;
    }

    public static String parsePapi(String stringToParse, Player p){
        if (ValhallaMMO.isHookFunctional(PAPIHook.class)) return PAPIHook.parse(p, stringToParse);
        return stringToParse;
    }
}
