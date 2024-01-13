package me.athlaeos.valhallammo.parties;

import me.athlaeos.valhallammo.placeholder.PlaceholderRegistry;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PartyChatListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e){
        if (!e.getMessage().startsWith("pc:") && !PartyManager.getPartyChatPlayers().contains(e.getPlayer().getUniqueId())) return;
        if (PartyManager.getPartyChatPlayers().contains(e.getPlayer().getUniqueId()) && e.getMessage().startsWith("!")) {
            // party chat messages starting with ! will be sent in regular chat
            e.setMessage(e.getMessage().replaceFirst("!", ""));
            return;
        }
        Party party = PartyManager.getParty(e.getPlayer());
        if (party == null && e.getMessage().startsWith("pc:")) {
            e.setCancelled(true);
            PartyManager.ErrorStatus.NOT_IN_PARTY.sendErrorMessage(e.getPlayer());
            return;
        } else if (party == null) return;
        e.setMessage(e.getMessage().replaceFirst("pc:", ""));
        PartyManager.ErrorStatus chatStatus = PartyManager.hasPermission(e.getPlayer(), "party_chat");
        if (chatStatus != null) {
            chatStatus.sendErrorMessage(e.getPlayer());
            e.setCancelled(true);
            return;
        }
        PartyManager.PartyRank rank = PartyManager.getPartyRanks().get(party.getMembers().getOrDefault(e.getPlayer().getUniqueId(), ""));
        String title = party.getLeader().equals(e.getPlayer().getUniqueId()) ? PartyManager.getLeaderTitle() : rank != null ? rank.title() : "";
        String newChatFormat = PartyManager.getPartyChatFormat()
                .replace("%rank%", title)
                .replace("%party%", party.getDisplayName());
        newChatFormat = PlaceholderRegistry.parse(newChatFormat, e.getPlayer());
        newChatFormat = PlaceholderRegistry.parsePapi(newChatFormat, e.getPlayer());
        e.getRecipients().clear();
        e.setCancelled(true);
        for (Player p : PartyManager.getOnlinePartyMembers(party)) p.sendMessage(String.format(Utils.chat(newChatFormat), e.getPlayer().getDisplayName(), e.getMessage()));

        String spyChatMessage = PartyManager.getPartySpyFormat()
                .replace("%rank%", title)
                .replace("%party%", party.getDisplayName());
        spyChatMessage = PlaceholderRegistry.parse(spyChatMessage, e.getPlayer());
        spyChatMessage = PlaceholderRegistry.parsePapi(spyChatMessage, e.getPlayer());
        for (Player p : Utils.getOnlinePlayersFromUUIDs(PartyManager.getPartySpyPlayers()).values()) {
            if (party.getMembers().containsKey(p.getUniqueId())) continue; // do not send messages to party spies if the messages come from their own party
            if (party.getLeader().equals(p.getUniqueId())) continue; // ... and neither to their leader
            p.sendMessage(String.format(Utils.chat(spyChatMessage), e.getPlayer().getDisplayName(), e.getMessage()));
        }
    }
}
