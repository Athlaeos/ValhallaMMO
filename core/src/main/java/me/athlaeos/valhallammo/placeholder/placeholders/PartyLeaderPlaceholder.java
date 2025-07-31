package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.parties.Party;
import me.athlaeos.valhallammo.parties.PartyManager;
import me.athlaeos.valhallammo.placeholder.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PartyLeaderPlaceholder extends Placeholder {
    public PartyLeaderPlaceholder(String placeholder) {
        super(placeholder);
    }

    @Override
    public String parse(String s, Player p) {
        Party party = PartyManager.getParty(p);
        if (party == null) return "";
        OfflinePlayer leader = ValhallaMMO.getInstance().getServer().getOfflinePlayer(party.getLeader());
        return s.replace(this.placeholder, leader.getName() == null ? "???" : leader.getName());
    }
}
