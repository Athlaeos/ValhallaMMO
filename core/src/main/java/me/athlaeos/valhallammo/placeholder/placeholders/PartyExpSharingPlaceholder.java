package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.parties.Party;
import me.athlaeos.valhallammo.parties.PartyManager;
import me.athlaeos.valhallammo.placeholder.Placeholder;
import org.bukkit.entity.Player;

public class PartyExpSharingPlaceholder extends Placeholder {
    public PartyExpSharingPlaceholder(String placeholder) {
        super(placeholder);
    }

    @Override
    public String parse(String s, Player p) {
        Party party = PartyManager.getParty(p);
        return s.replace(this.placeholder, party == null ? "" : TranslationManager.getTranslation("translation_" + party.isExpSharingEnabled()));
    }
}
