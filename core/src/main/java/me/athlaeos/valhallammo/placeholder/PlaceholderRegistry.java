package me.athlaeos.valhallammo.placeholder;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.hooks.PAPIHook;
import me.athlaeos.valhallammo.placeholder.placeholders.*;
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
        registerPlaceholder(new PartyExpPlaceholder("%party_current_exp%"));
        registerPlaceholder(new PartyExpRequiredPlaceholder("%party_exp_next_level%"));
        registerPlaceholder(new PartyExpSharingPlaceholder("%party_exp_sharing_enabled%"));
        registerPlaceholder(new PartyItemSharingPlaceholder("%party_item_sharing_enabled%"));
        registerPlaceholder(new PartyLeaderPlaceholder("%party_leader%"));
        registerPlaceholder(new PartyLevelNumericPlaceholder("%party_level_numeric%"));
        registerPlaceholder(new PartyLevelPlaceholder("%party_level_name%"));
        registerPlaceholder(new PartyLevelRomanPlaceholder("%party_level_roman%"));
        registerPlaceholder(new PartyMemberCapPlaceholder("%party_member_capacity%"));
        registerPlaceholder(new PartyMemberCountPlaceholder("%party_member_count%"));
        registerPlaceholder(new PartyMembersPlaceholder("%party_members%"));
        registerPlaceholder(new PartyNearbyMembersPlaceholder("%party_members_nearby%"));
        registerPlaceholder(new PartyOnlineMembersPlaceholder("%party_members_online%"));
        registerPlaceholder(new PartyOpenPlaceholder("%party_open%"));
        registerPlaceholder(new PartyRankPlaceholder("%party_rank%"));
        registerPlaceholder(new RecipeUnlockedPlaceholder("%recipe_unlocked_%"));
        registerPlaceholder(new RecipeUnlockedPlaceholder("%perk_unlocked_%"));
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
            if (s.matchString(stringToParse)) {
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
