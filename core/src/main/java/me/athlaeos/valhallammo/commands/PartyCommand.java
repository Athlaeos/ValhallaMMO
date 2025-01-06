package me.athlaeos.valhallammo.commands;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.parties.Party;
import me.athlaeos.valhallammo.parties.PartyManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PartyCommand implements TabExecutor {
    private boolean badUsage(CommandSender sender, String usage){
        Utils.sendMessage(sender, "&c" + usage);
        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            Utils.sendMessage(sender, "&cOnly players may execute this command");
            return true;
        }
        if (args.length == 0){
            PartyManager.ErrorStatus infoStatus = PartyManager.displayPartyInfo(p);
            if (infoStatus != null) infoStatus.sendErrorMessage(p);
            return true;
        }
        switch (args[0]){
            case "create" -> {
                if (args.length == 1) return badUsage(p, "/party create <name>");
                String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                PartyManager.ErrorStatus verifyStatus = PartyManager.validatePartyName(name);
                if (verifyStatus != null) {
                    verifyStatus.sendErrorMessage(p);
                    return true;
                }
                PartyManager.ErrorStatus registryStatus = PartyManager.registerParty(p, PartyManager.createParty(p, name));
                if (registryStatus != null) registryStatus.sendErrorMessage(p);
                else Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_created"));
                return true;
            }
            case "disband" -> {
                if (args.length == 1) Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_disband_confirmation"));
                else if (args.length == 2) {
                    if (args[1].equalsIgnoreCase("confirm")) Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_disband_confirm_confirmation"));
                    else if (args[1].equalsIgnoreCase("really")) {
                        PartyManager.ErrorStatus disbandStatus = PartyManager.disbandParty(p);
                        if (disbandStatus != null) disbandStatus.sendErrorMessage(p);
                    }
                }
                return true;
            }
            case "info" -> {
                if (args.length > 1){
                    Party party = PartyManager.getAllParties().get(args[1]);
                    if (party == null) {
                        PartyManager.ErrorStatus.PARTY_DOES_NOT_EXIST.sendErrorMessage(p);
                        return true;
                    }
                    PartyManager.ErrorStatus displayStatus = PartyManager.displayPartyInfo(p, party);
                    if (displayStatus != null) displayStatus.sendErrorMessage(p);
                } else {
                    PartyManager.ErrorStatus displayStatus = PartyManager.displayPartyInfo(p);
                    if (displayStatus != null) displayStatus.sendErrorMessage(p);
                }
                return true;
            }
            case "description" -> {
                String description = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "";
                PartyManager.ErrorStatus verifyStatus = PartyManager.changeDescription(p, description);
                if (verifyStatus != null){
                    verifyStatus.sendErrorMessage(p);
                    return true;
                }
                Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_description_updated"));
                return true;
            }
            case "rename" -> {
                String name = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "";
                PartyManager.ErrorStatus verifyStatus = PartyManager.changeName(p, name);
                if (verifyStatus != null){
                    verifyStatus.sendErrorMessage(p);
                    return true;
                }
                Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_name_updated"));
                return true;
            }
            case "invite" -> {
                if (args.length == 1) return badUsage(p, "/party invite <player>");
                Player target = ValhallaMMO.getInstance().getServer().getPlayer(args[1]);
                if (target == null) {
                    Utils.sendMessage(p, TranslationManager.getTranslation("error_command_player_offline"));
                    return true;
                }
                PartyManager.ErrorStatus inviteStatus = PartyManager.invite(p, target);
                if (inviteStatus != null){
                    inviteStatus.sendErrorMessage(p, target);
                    return true;
                }
                Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_invite_sent"));
                return true;
            }
            case "kick" -> {
                if (args.length == 1) return badUsage(p, "/party kick <player>");
                Party party = PartyManager.getParty(p);
                if (party == null){
                    PartyManager.ErrorStatus.NOT_IN_PARTY.sendErrorMessage(p);
                    return true;
                }
                Map<String, OfflinePlayer> offlineMembers = Utils.getPlayersFromUUIDs(party.getMembers().keySet());
                OfflinePlayer target = offlineMembers.get(args[1]);
                if (target == null) {
                    Utils.sendMessage(p, TranslationManager.getTranslation("error_command_player_offline"));
                    return true;
                }
                PartyManager.ErrorStatus kickStatus = PartyManager.kickMember(p, target.getUniqueId());
                if (kickStatus != null){
                    kickStatus.sendErrorMessage(p);
                    return true;
                }
                Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_player_kicked"));
                return true;
            }
            case "transferleader" -> {
                if (args.length == 1) return badUsage(p, "/party transferleader <player>");
                Player target = ValhallaMMO.getInstance().getServer().getPlayer(args[1]);
                if (target == null) {
                    Utils.sendMessage(p, TranslationManager.getTranslation("error_command_player_offline"));
                    return true;
                }
                PartyManager.ErrorStatus inviteStatus = PartyManager.changeLeader(p, target);
                if (inviteStatus != null){
                    inviteStatus.sendErrorMessage(p, target);
                    return true;
                }
                Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_leadership_transferred"));
                return true;
            }
            case "setrank" -> {
                if (args.length <= 2) return badUsage(p, "/party setrank <player> <rank>");
                Player target = ValhallaMMO.getInstance().getServer().getPlayer(args[1]);
                if (target == null) {
                    Utils.sendMessage(p, TranslationManager.getTranslation("error_command_player_offline"));
                    return true;
                }
                PartyManager.ErrorStatus rankChangeStatus = PartyManager.setMemberRank(p, target, args[2]);
                if (rankChangeStatus != null){
                    rankChangeStatus.sendErrorMessage(p, target);
                    return true;
                }
                Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_rank_changed"));
                return true;
            }
            case "join" -> {
                if (args.length == 1) return badUsage(p, "/party join <party>");
                PartyManager.ErrorStatus joinStatus = PartyManager.acceptInvite(p, args[1]);
                if (joinStatus != null){
                    joinStatus.sendErrorMessage(p);
                    return true;
                }
                Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_joined"));
                return true;
            }
            case "leave" -> {
                PartyManager.ErrorStatus leaveStatus = PartyManager.leaveParty(p);
                if (leaveStatus != null){
                    leaveStatus.sendErrorMessage(p);
                    return true;
                }
                Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_left"));
                return true;
            }
            case "expsharing" -> {
                PartyManager.ErrorStatus expSharingToggleStatus = PartyManager.toggleExpSharing(p);
                if (expSharingToggleStatus != null){
                    expSharingToggleStatus.sendErrorMessage(p);
                    return true;
                }
                Party party = PartyManager.getParty(p);
                if (party == null) {
                    PartyManager.ErrorStatus.PARTY_DOES_NOT_EXIST.sendErrorMessage(p);
                    return true;
                }
                if (party.isExpSharingEnabled() != null && party.isExpSharingEnabled()) Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_exp_sharing_enabled"));
                else Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_exp_sharing_disabled"));
                return true;
            }
            case "itemsharing" -> {
                PartyManager.ErrorStatus itemSharingToggleStatus = PartyManager.toggleItemSharing(p);
                if (itemSharingToggleStatus != null){
                    itemSharingToggleStatus.sendErrorMessage(p);
                    return true;
                }
                Party party = PartyManager.getParty(p);
                if (party == null) {
                    PartyManager.ErrorStatus.PARTY_DOES_NOT_EXIST.sendErrorMessage(p);
                    return true;
                }
                if (party.isItemSharingEnabled() != null && party.isItemSharingEnabled()) Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_item_sharing_enabled"));
                else Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_item_sharing_disabled"));
                return true;
            }
            case "friendlyfire" -> {
                PartyManager.ErrorStatus itemSharingToggleStatus = PartyManager.toggleFriendlyFire(p);
                if (itemSharingToggleStatus != null){
                    itemSharingToggleStatus.sendErrorMessage(p);
                    return true;
                }
                Party party = PartyManager.getParty(p);
                if (party == null) {
                    PartyManager.ErrorStatus.PARTY_DOES_NOT_EXIST.sendErrorMessage(p);
                    return true;
                }
                if (party.isFriendlyFireEnabled()) Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_friendly_fire_enabled"));
                else Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_friendly_fire_disabled"));
                return true;
            }
            case "open" -> {
                PartyManager.ErrorStatus openStatus = PartyManager.togglePartyOpen(p);
                if (openStatus != null){
                    openStatus.sendErrorMessage(p);
                    return true;
                }
                Party party = PartyManager.getParty(p);
                if (party == null) {
                    PartyManager.ErrorStatus.PARTY_DOES_NOT_EXIST.sendErrorMessage(p);
                    return true;
                }
                if (party.isOpen()) Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_open"));
                else Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_closed"));
                return true;
            }
            case "shareitem" -> {
                if (args.length == 1) return badUsage(p, "/party shareitem <player>");
                Player target = ValhallaMMO.getInstance().getServer().getPlayer(args[1]);
                if (target == null) {
                    Utils.sendMessage(p, TranslationManager.getTranslation("error_command_player_offline"));
                    return true;
                }
                PartyManager.ErrorStatus transferStatus = PartyManager.shareItem(p, target);
                if (transferStatus != null){
                    transferStatus.sendErrorMessage(p, target);
                    return true;
                }
                Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_item_shared"));
                return true;
            }
            case "muteinvites" -> {
                if (PartyManager.toggleInviteMute(p)) Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_invites_muted"));
                else Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_invites_unmuted"));
                return true;
            }
            case "chat" -> {
                PartyManager.ErrorStatus openStatus = PartyManager.togglePartyChat(p);
                if (openStatus != null){
                    openStatus.sendErrorMessage(p);
                    return true;
                }
                if (PartyManager.getPartyChatPlayers().contains(p.getUniqueId())) Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_chat_enabled"));
                else Utils.sendMessage(p, TranslationManager.getTranslation("status_command_party_chat_disabled"));
                return true;
            }
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player p){
            Party party = PartyManager.getParty(p);
            if (args.length == 1) {
                if (party != null) return List.of("chat", "info", "invite",
                        "shareitem", "open", "setrank", "kick", "expsharing", "itemsharing", "friendlyfire", "description", "rename",
                        "transferleader", "leave", "disband");
                else return List.of("create", "muteinvites", "info", "join");
            } else if (args.length == 2 && (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("info"))) return new ArrayList<>(PartyManager.getAllParties().keySet());
            else if (args.length == 3 && args[0].equalsIgnoreCase("setrank")) return new ArrayList<>(PartyManager.getPartyRanks().keySet());
        }
        return null;
    }
}
