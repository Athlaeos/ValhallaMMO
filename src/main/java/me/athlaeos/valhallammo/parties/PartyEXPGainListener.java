package me.athlaeos.valhallammo.parties;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.placeholder.PlaceholderRegistry;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collection;

public class PartyEXPGainListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEXPGain(PlayerSkillExperienceGainEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.isCancelled() ||
                e.getReason() != PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) return;
        Party party = PartyManager.getParty(e.getPlayer());
        if (party == null) return;
        double expForParty = e.getAmount() * PartyManager.getPartyEXPConversionRate();
        PartyManager.addEXP(party, expForParty);

        Collection<Player> nearbyMembers = PartyManager.membersInEXPSharingRadius(e.getPlayer());
        if (nearbyMembers.isEmpty()) return;
        double expSharingMultiplier = PartyManager.getTotalFloatStat("exp_sharing_multiplier", party);
        double fraction = (e.getAmount() / (nearbyMembers.size() + 1)) * expSharingMultiplier;
        e.setCancelled(true);
        nearbyMembers.forEach(p -> e.getLeveledSkill().addEXP(p, fraction, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.EXP_SHARE));
    }
}
