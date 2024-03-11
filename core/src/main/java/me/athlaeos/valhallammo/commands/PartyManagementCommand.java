package me.athlaeos.valhallammo.commands;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.parties.Party;
import me.athlaeos.valhallammo.parties.PartyManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.StringUtils;
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

public class PartyManagementCommand implements TabExecutor {
    private boolean badUsage(CommandSender sender, String usage){
        Utils.sendMessage(sender, "&c" + usage);
        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("valhalla.manageparties")){
            PartyManager.ErrorStatus.NO_PERMISSION.sendErrorMessage(sender);
            return true;
        }
        if (args.length == 0) return badUsage(sender, "/parties <subcommand>");
        switch (args[0]){
            case "create" -> {
                if (args.length <= 2) return badUsage(sender, "/parties create <partyname> <leader>");
                Player leader = ValhallaMMO.getInstance().getServer().getPlayer(args[1]);
                if (leader == null){
                    Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_player_offline"));
                    return true;
                }
                String partyName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                PartyManager.ErrorStatus verifyStatus = PartyManager.validatePartyName(partyName);
                if (verifyStatus != null){
                    verifyStatus.sendErrorMessage(sender);
                    return true;
                }
                PartyManager.ErrorStatus creationStatus = PartyManager.registerParty(leader, PartyManager.createParty(leader, partyName));
                if (creationStatus != null){
                    creationStatus.sendErrorMessage(sender);
                    return true;
                }
                Utils.sendMessage(leader, TranslationManager.getTranslation("status_command_party_created_by_admin"));
                Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_created"));
                return true;
            }
            case "kickmember" -> {
                if (args.length <= 2) return badUsage(sender, "/parties kickmember <party> <player>");
                Party party = PartyManager.getAllParties().get(args[1]);
                if (party == null){
                    PartyManager.ErrorStatus.PARTY_DOES_NOT_EXIST.sendErrorMessage(sender);
                    return true;
                }
                Map<String, OfflinePlayer> offlineMembers = Utils.getPlayersFromUUIDs(party.getMembers().keySet());
                OfflinePlayer target = offlineMembers.get(args[2]);
                if (target == null) {
                    Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_player_offline"));
                    return true;
                }
                PartyManager.ErrorStatus kickStatus = PartyManager.forceKickMember(sender, target.getUniqueId());
                if (kickStatus != null){
                    kickStatus.sendErrorMessage(sender);
                    return true;
                }
                Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_player_kicked"));
                return true;
            }
            case "addmember" -> {
                if (args.length <= 2) return badUsage(sender, "/parties addmember <party> <player>");
                Player target = ValhallaMMO.getInstance().getServer().getPlayer(args[2]);
                if (target == null){
                    Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_player_offline"));
                    return true;
                }
                Party party = PartyManager.getAllParties().get(args[1]);
                if (party == null){
                    PartyManager.ErrorStatus.PARTY_DOES_NOT_EXIST.sendErrorMessage(sender);
                    return true;
                }
                PartyManager.ErrorStatus joinStatus = PartyManager.joinParty(target, party);
                if (joinStatus != null){
                    joinStatus.sendErrorMessage(sender);
                    return true;
                }
                Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_joined"));
                return true;
            }
            case "setdescription" -> {
                if (args.length <= 2) return badUsage(sender, "/parties setdescription <party> <description>");
                Party party = PartyManager.getAllParties().get(args[1]);
                if (party == null){
                    PartyManager.ErrorStatus.PARTY_DOES_NOT_EXIST.sendErrorMessage(sender);
                    return true;
                }
                String description = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                party.setDescription(Utils.chat(description));
                Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_description_updated"));
                return true;
            }
            case "setname" -> {
                if (args.length <= 2) return badUsage(sender, "/parties setname <party> <name>");
                Party party = PartyManager.getAllParties().get(args[1]);
                if (party == null){
                    PartyManager.ErrorStatus.PARTY_DOES_NOT_EXIST.sendErrorMessage(sender);
                    return true;
                }
                String name = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                party.setDisplayName(Utils.chat(name));
                Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_name_updated"));
                return true;
            }
            case "transferleadership" -> {
                if (args.length <= 2) return badUsage(sender, "/parties transferleadership <party> <newleader>");
                Party party = PartyManager.getAllParties().get(args[1]);
                if (party == null){
                    PartyManager.ErrorStatus.PARTY_DOES_NOT_EXIST.sendErrorMessage(sender);
                    return true;
                }
                Map<String, OfflinePlayer> offlineMembers = Utils.getPlayersFromUUIDs(party.getMembers().keySet());
                OfflinePlayer target = offlineMembers.get(args[2]);
                if (target == null) {
                    Utils.sendMessage(sender, TranslationManager.getTranslation("error_command_player_offline"));
                    return true;
                }
                party.getMembers().put(party.getLeader(), PartyManager.getLowestRank());
                party.setLeader(target.getUniqueId());
                party.getMembers().remove(target.getUniqueId());
                Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_player_kicked"));
                return true;
            }
            case "delete" -> {
                if (args.length <= 1) return badUsage(sender, "/parties delete <party>");
                Party party = PartyManager.getAllParties().get(args[1]);
                if (party == null){
                    PartyManager.ErrorStatus.PARTY_DOES_NOT_EXIST.sendErrorMessage(sender);
                    return true;
                }
                PartyManager.ErrorStatus deletionStatus = PartyManager.disbandParty(party);
                if (deletionStatus != null){
                    deletionStatus.sendErrorMessage(sender);
                    return true;
                }
                Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_disbanded"));
                return true;
            }
            case "setboolstat" -> {
                if (args.length <= 3) return badUsage(sender, "/parties setboolstat <party> <stat> <value>");
                Party party = PartyManager.getAllParties().get(args[1]);
                if (party == null){
                    PartyManager.ErrorStatus.PARTY_DOES_NOT_EXIST.sendErrorMessage(sender);
                    return true;
                }
                if (!PartyManager.getDefaultBools().containsKey(args[2])){
                    Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_invalid_stat").replace("%stats%", String.join(", ", PartyManager.getDefaultBools().keySet())));
                    return true;
                }
                if (!args[3].equalsIgnoreCase("true") && !args[3].equalsIgnoreCase("false") && !args[3].equalsIgnoreCase("default")){
                    Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_invalid_boolean").replace("%arg%", args[3]));
                    return true;
                }
                Boolean value = args[3].equalsIgnoreCase("default") ? null : args[3].equalsIgnoreCase("true");
                if (value == null) party.getBools().remove(args[2]);
                else party.getBools().put(args[2], value);
                Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_stats_updated"));
                return true;
            }
            case "setintstat" -> {
                if (args.length <= 3) return badUsage(sender, "/parties setintstat <party> <stat> <value>");
                Party party = PartyManager.getAllParties().get(args[1]);
                if (party == null){
                    PartyManager.ErrorStatus.PARTY_DOES_NOT_EXIST.sendErrorMessage(sender);
                    return true;
                }
                if (!PartyManager.getDefaultInts().containsKey(args[2])){
                    Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_invalid_stat").replace("%stats%", String.join(", ", PartyManager.getDefaultInts().keySet())));
                    return true;
                }
                if (args[3].equalsIgnoreCase("default")) party.getInts().remove(args[2]);
                else {
                    Integer value = Catch.catchOrElse(() -> Integer.parseInt(args[3]), null);
                    if (value == null){
                        Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_invalid_number").replace("%arg%", args[3]));
                        return true;
                    }
                    party.getInts().put(args[2], value);
                }
                Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_stats_updated"));
                return true;
            }
            case "setfloatstat" -> {
                if (args.length <= 3) return badUsage(sender, "/parties setfloatstat <party> <stat> <value>");
                Party party = PartyManager.getAllParties().get(args[1]);
                if (party == null){
                    PartyManager.ErrorStatus.PARTY_DOES_NOT_EXIST.sendErrorMessage(sender);
                    return true;
                }
                if (!PartyManager.getDefaultFloats().containsKey(args[2])){
                    Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_invalid_stat").replace("%stats%", String.join(", ", PartyManager.getDefaultFloats().keySet())));
                    return true;
                }
                if (args[3].equalsIgnoreCase("default")) party.getFloats().remove(args[2]);
                else {
                    Float value = Catch.catchOrElse(() -> StringUtils.parseFloat(args[3]), null);
                    if (value == null){
                        Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_invalid_number").replace("%arg%", args[3]));
                        return true;
                    }
                    party.getFloats().put(args[2], value);
                }
                Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_stats_updated"));
                return true;
            }
            case "exp" -> {
                if (args.length <= 2) return badUsage(sender, "/parties exp <party> <value>");
                Party party = PartyManager.getAllParties().get(args[1]);
                if (party == null){
                    PartyManager.ErrorStatus.PARTY_DOES_NOT_EXIST.sendErrorMessage(sender);
                    return true;
                }

                Float value = Catch.catchOrElse(() -> StringUtils.parseFloat(args[2]), null);
                if (value == null){
                    Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_invalid_number").replace("%arg%", args[2]));
                    return true;
                }
                PartyManager.addEXP(party, value);
                Utils.sendMessage(sender, TranslationManager.getTranslation(value > 0 ? "status_command_party_exp_granted" : "status_command_party_exp_removed"));
                return true;
            }
            case "addcompanystat" -> {
                if (args.length <= 3) return badUsage(sender, "/parties addcompanystat <party> <stat> <value>");
                Party party = PartyManager.getAllParties().get(args[1]);
                if (party == null){
                    PartyManager.ErrorStatus.PARTY_DOES_NOT_EXIST.sendErrorMessage(sender);
                    return true;
                }
                if (!AccumulativeStatManager.getSources().containsKey(args[2])){
                    Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_invalid_company_stat"));
                    return true;
                }

                Double value = Catch.catchOrElse(() -> StringUtils.parseDouble(args[3]), null);
                if (value == null){
                    Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_invalid_number").replace("%arg%", args[2]));
                    return true;
                }
                if (value == 0) party.getCompanyStatsPerMember().remove(args[2]);
                else party.getCompanyStatsPerMember().put(args[2], value);
                Utils.sendMessage(sender, TranslationManager.getTranslation("status_command_party_stats_updated"));
                return true;
            }
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("valhalla.manageparties")) return new ArrayList<>();
        if (args.length == 1) return List.of("create", "kickmember", "addmember", "setdescription", "setname",
                "transferleadership", "setboolstat", "setintstat", "setfloatstat", "addcompanystat", "delete", "exp");
        if (args.length == 2){
            return switch (args[0]){
                case "create" -> List.of("<name>");
                case "kickmember", "addcompanystat", "addmember", "setdescription", "setname", "delete", "transferownership", "setboolstat", "setintstat", "setfloatstat", "exp" -> new ArrayList<>(PartyManager.getAllParties().keySet());
                default -> null;
            };
        }
        if (args.length == 3){
            return switch (args[0]){
                case "setdescription" -> List.of("<description>");
                case "setname" -> List.of("<name>");
                case "setboolstat" -> new ArrayList<>(PartyManager.getDefaultBools().keySet());
                case "setintstat" -> new ArrayList<>(PartyManager.getDefaultInts().keySet());
                case "setfloatstat" -> new ArrayList<>(PartyManager.getDefaultFloats().keySet());
                case "addcompanystat" -> new ArrayList<>(AccumulativeStatManager.getSources().keySet());
                case "exp" -> List.of("1", "5", "100", "1000", "10000", "...");
                default -> null;
            };
        }
        if (args.length == 4){
            return switch (args[0]){
                case "setboolstat" -> List.of("default", "true", "false");
                case "setintstat" -> List.of("default", "1", "2", "5", "10", "...");
                case "setfloatstat" -> List.of("default", "1.0", "2.0", "5.0", "10.0", "...");
                case "addcompanystat" -> List.of("0.1", "0.2", "0.5", "1.0", "...");
                default -> null;
            };
        }
        return null;
    }
}
