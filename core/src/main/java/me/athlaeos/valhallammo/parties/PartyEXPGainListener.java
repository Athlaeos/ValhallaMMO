package me.athlaeos.valhallammo.parties;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.skills.skills.implementations.PowerSkill;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collection;

public class PartyEXPGainListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEXPGain(PlayerSkillExperienceGainEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) ||
                e.getReason() != PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_PARTY_EXPSHARING) ||
                e.getLeveledSkill() instanceof PowerSkill) return;
        Party party = PartyManager.getParty(e.getPlayer());
        if (party == null) return;
        Collection<Player> nearbyMembers = PartyManager.membersInEXPSharingRadius(e.getPlayer());
        boolean expSharingEnabled = party.isExpSharingEnabled() != null ? party.isExpSharingEnabled() : PartyManager.getBoolStat("exp_sharing", party);
        double expForParty = e.getAmount() * PartyManager.getPartyEXPConversionRate();
        if (!nearbyMembers.isEmpty()) PartyManager.addEXP(party, expForParty);
        if (!expSharingEnabled) return;

        if (nearbyMembers.isEmpty()) return;
        nearbyMembers.add(e.getPlayer());
        double expSharingMultiplier = PartyManager.getTotalFloatStat("exp_sharing_multiplier", party);
        if (expSharingMultiplier <= 0) return;
        double fraction = (e.getAmount() / (nearbyMembers.size())) * expSharingMultiplier;
        e.setCancelled(true);
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
            nearbyMembers.forEach(p -> e.getLeveledSkill().addEXP(p, fraction, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.EXP_SHARE));
        });
    }
}
