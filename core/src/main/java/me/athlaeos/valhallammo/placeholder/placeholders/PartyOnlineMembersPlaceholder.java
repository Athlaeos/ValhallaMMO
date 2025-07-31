package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.parties.Party;
import me.athlaeos.valhallammo.parties.PartyManager;
import me.athlaeos.valhallammo.placeholder.Placeholder;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class PartyOnlineMembersPlaceholder extends Placeholder {
    public PartyOnlineMembersPlaceholder(String placeholder) {
        super(placeholder);
    }

    @Override
    public String parse(String s, Player p) {
        Party party = PartyManager.getParty(p);
        return s.replace(this.placeholder, party == null ? "" : PartyManager.getOnlinePartyMembers(party).stream().map(Player::getName).collect(Collectors.joining(", ")));
    }
}
