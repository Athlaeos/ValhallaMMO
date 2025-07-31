package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.parties.Party;
import me.athlaeos.valhallammo.parties.PartyManager;
import me.athlaeos.valhallammo.placeholder.Placeholder;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.entity.Player;

public class PartyLevelRomanPlaceholder extends Placeholder {
    public PartyLevelRomanPlaceholder(String placeholder) {
        super(placeholder);
    }

    @Override
    public String parse(String s, Player p) {
        Party party = PartyManager.getParty(p);
        if (party == null) return "";
        Pair<PartyManager.PartyLevel, Double> level = PartyManager.getPartyLevel(party);
        if (level == null) return "";
        return s.replace(this.placeholder, StringUtils.toRoman(level.getOne().getLevel()));
    }
}
