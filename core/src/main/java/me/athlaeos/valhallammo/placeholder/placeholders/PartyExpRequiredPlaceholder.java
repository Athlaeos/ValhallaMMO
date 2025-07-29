package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.parties.Party;
import me.athlaeos.valhallammo.parties.PartyManager;
import me.athlaeos.valhallammo.placeholder.Placeholder;
import org.bukkit.entity.Player;

public class PartyExpRequiredPlaceholder extends Placeholder {
    public PartyExpRequiredPlaceholder(String placeholder) {
        super(placeholder);
    }

    @Override
    public String parse(String s, Player p) {
        Party party = PartyManager.getParty(p);
        if (party == null) return "";
        Pair<PartyManager.PartyLevel, Double> level = PartyManager.getPartyLevel(party);
        PartyManager.PartyLevel nextLevel = level == null ? null : PartyManager.getPartyLevels().get(level.getOne().getLevel() + 1);
        String expForNext = nextLevel == null ?
                TranslationManager.getTranslation("max_level") : // if either current level or max level are null, assume max level is reached
                String.format("%,.1f", PartyManager.getLevelRequirements().getOrDefault(nextLevel.getLevel(), 0D) - PartyManager.getLevelRequirements().getOrDefault(level.getOne().getLevel(), 0D));
        return s.replace(this.placeholder, String.format("%.1f", expForNext));
    }
}
