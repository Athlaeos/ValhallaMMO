package me.athlaeos.valhallammo.hooks;

import me.athlaeos.valhallammo.parties.Party;
import me.athlaeos.valhallammo.parties.PartyManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PAPIRelationalPlaceholder extends PlaceholderExpansion implements Relational {
    @Override
    public @NotNull String getIdentifier() {
        return "valhallammo";
    }

    @Override
    public @NotNull String getAuthor() {
        return "athlaeos";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, Player player1, String s) {
        if (s.equalsIgnoreCase("in_same_party")){
            Party p1 = PartyManager.getParty(player);
            Party p2 = PartyManager.getParty(player1);
            return String.valueOf(p1 != null && p2 != null && p1.getId().equalsIgnoreCase(p2.getId()));
        }
        return "";
    }
}
