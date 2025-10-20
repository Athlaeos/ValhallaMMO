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
        SharingDetails details = getSharingDetails(e);
        if (details == null) return;
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
            details.sharedWith.forEach(p -> e.getLeveledSkill().addEXP(p, details.fraction, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.EXP_SHARE));
        });
    }

    public static SharingDetails getSharingDetails(PlayerSkillExperienceGainEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) ||
                e.getReason() != PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_PARTY_EXPSHARING) ||
                e.getLeveledSkill() instanceof PowerSkill) return null;
        Party party = PartyManager.getParty(e.getPlayer());
        if (party == null) return null;
        Collection<Player> nearbyMembers = PartyManager.membersInEXPSharingRadius(e.getPlayer());
        boolean expSharingEnabled = party.isExpSharingEnabled() != null ? party.isExpSharingEnabled() : PartyManager.getBoolStat("exp_sharing", party);
        double expForParty = e.getAmount() * PartyManager.getPartyEXPConversionRate();
        if (!nearbyMembers.isEmpty()) PartyManager.addEXP(party, expForParty);
        if (!expSharingEnabled) return null;

        if (nearbyMembers.isEmpty()) return null;
        nearbyMembers.add(e.getPlayer());
        double expSharingMultiplier = PartyManager.getTotalFloatStat("exp_sharing_multiplier", party);
        if (expSharingMultiplier <= 0) return null;
        double fraction = (e.getAmount() / (nearbyMembers.size())) * expSharingMultiplier;
        return new SharingDetails(nearbyMembers, fraction);
    }

    public record SharingDetails(Collection<Player> sharedWith, double fraction){}
}
