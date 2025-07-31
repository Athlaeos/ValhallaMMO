package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.parties.Party;
import me.athlaeos.valhallammo.parties.PartyManager;
import me.athlaeos.valhallammo.placeholder.Placeholder;
import org.bukkit.entity.Player;

public class PartyRankPlaceholder extends Placeholder {
    public PartyRankPlaceholder(String placeholder) {
        super(placeholder);
    }

    @Override
    public String parse(String s, Player p) {
        Party party = PartyManager.getParty(p);
        if (party == null) return "";
        PartyManager.PartyRank rank = PartyManager.getPartyRanks().get(party.getMembers().getOrDefault(p.getUniqueId(), ""));
        String title = party.getLeader().equals(p.getUniqueId()) ? PartyManager.getLeaderTitle() : rank != null ? rank.title() : "";
        return s.replace(this.placeholder, title);
    }
}
