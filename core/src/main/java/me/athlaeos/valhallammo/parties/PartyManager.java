package me.athlaeos.valhallammo.parties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.placeholder.PlaceholderRegistry;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PartyManager {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();

    private static final Map<String, Party> allParties = new HashMap<>();
    private static final Map<UUID, Party> partiesByMember = new HashMap<>();
    private static final TreeMap<Integer, PartyLevel> partyLevels = new TreeMap<>();
    private static final Map<Integer, Double> levelRequirements = new HashMap<>();
    private static final Map<String, PartyRank> partyRanks = new HashMap<>();
    private static String leaderTitle;
    private static String lowestRank;
    private static String highestRank;

    private static final Map<UUID, Collection<String>> partyInvites = new HashMap<>();
    private static final Collection<UUID> partyChatPlayers = new HashSet<>();
    private static final Collection<UUID> partySpyPlayers = new HashSet<>();

    private static boolean enabledParties = true;
    private static boolean enabledEXPSharing;
    private static boolean enabledItemSharing;
    private static int partyCreationCooldown;
    private static int itemSharingCooldown;
    private static double partyEXPConversionRate;
    private static int partyRenameCooldown;
    private static int partyDescriptionChangeCooldown;

    private static Pattern legalCharactersPattern = null;
    private static int partyNameMinimum;
    private static int partyNameMaximum;
    private static int partyDescriptionMaximum;

    private static String partyChatFormat;
    private static String partySpyFormat;
    private static List<String> partyInfoFormat;
    private static String partyLevelUpFormat;

    private static final Map<String, Integer> defaultInts = new HashMap<>();
    private static final Map<String, Float> defaultFloats = new HashMap<>();
    private static final Map<String, Boolean> defaultBools = new HashMap<>();
    private static final Map<String, Double> defaultCompanyStats = new HashMap<>();

    static {
        defaultInts.put("party_capacity", 0);
        defaultInts.put("exp_sharing_radius", 0);
        defaultInts.put("item_sharing_radius", 0);
        defaultInts.put("company_radius", 0);
        defaultInts.put("company_member_cap", 0);
        defaultFloats.put("exp_sharing_multiplier", 0F);
        defaultBools.put("party_chat", false);
        defaultBools.put("exp_sharing", false);
        defaultBools.put("item_sharing", false);
        defaultBools.put("name_colors", false);
        defaultBools.put("description_colors", false);
        defaultBools.put("rename_party", false);
    }

    public static Collection<Player> membersInEXPSharingRadius(Player p){
        Collection<Player> nearbyMembers = new HashSet<>();
        if (!enabledEXPSharing) return nearbyMembers;
        Party party = getParty(p);
        if (party == null) return nearbyMembers;
        float expSharingMultiplier = getTotalFloatStat("exp_sharing_multiplier", party);
        int expSharingRadius = getTotalIntStat("exp_sharing_radius", party);
        if (expSharingRadius == 0 || expSharingMultiplier <= 0) return nearbyMembers;

        return membersInRadius(p, expSharingRadius).stream().filter(pl ->
                !WorldGuardHook.inDisabledRegion(pl.getLocation(), pl, WorldGuardHook.VMMO_PARTY_EXPSHARING)).collect(Collectors.toSet()
        );
    }

    public static Collection<Player> membersInRadius(Player p, int radius){
        Party party = getParty(p);
        Collection<Player> nearbyMembers = new HashSet<>();
        if (party == null || radius == 0 || (party.isExpSharingEnabled() != null && !party.isExpSharingEnabled())) return nearbyMembers;

        for (Player member : getOnlinePartyMembers(party)){
            if (member.getUniqueId().equals(p.getUniqueId())) continue; // nearby players should not include the player themselves
            if (radius < 0 || (p.getWorld().getName().equals(member.getWorld().getName()) &&
                    p.getLocation().distanceSquared(member.getLocation()) < radius * radius))
                nearbyMembers.add(member); // if radius is negative, or players are in the same world and within the radius, increment nearby
        }
        return nearbyMembers;
    }

    public static double getCompanyStats(Player p, String stat){
        if (!enabledParties) return 0;
        Party party = getParty(p);
        if (party == null) return 0;
        Map<String, Double> companyStats = getCompanyStats(party);
        if (!companyStats.containsKey(stat)) return 0;
        int companyRadius = getTotalIntStat("company_radius", party);
        int maxAccompanied = getTotalIntStat("company_member_cap", party);
        if (companyRadius == 0 || maxAccompanied == 0) return 0;
        Collection<Player> members = membersInRadius(p, companyRadius);
        int nearby = members.size();
        return companyStats.get(stat) * (maxAccompanied >= 0 ? Math.min(maxAccompanied, nearby) : nearby);
    }

    public static Party getParty(Player p){
        return partiesByMember.get(p.getUniqueId());
    }

    public static ErrorStatus validatePartyName(String name){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        if (name.length() > partyNameMaximum) return ErrorStatus.EXCEEDED_CHARACTER_LIMIT;
        if (name.length() < partyNameMinimum) return ErrorStatus.NOT_ENOUGH_CHARACTERS;
        if (legalCharactersPattern != null && !legalCharactersPattern.matcher(name).matches()) return ErrorStatus.ILLEGAL_CHARACTERS_USED;
        return null;
    }

    public static ErrorStatus validateDescription(String description){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        if (description.length() > partyDescriptionMaximum) return ErrorStatus.EXCEEDED_CHARACTER_LIMIT;
        if (legalCharactersPattern != null && !legalCharactersPattern.matcher(description).matches()) return ErrorStatus.ILLEGAL_CHARACTERS_USED;
        return null;
    }

    public static ErrorStatus registerParty(Player leader, Party party){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        if (!leader.hasPermission("valhalla.createparty")) return ErrorStatus.NO_PERMISSION;
        if (allParties.containsKey(party.getId())) return ErrorStatus.PARTY_ALREADY_EXISTS;
        if (!Timer.isCooldownPassed(leader.getUniqueId(), "cooldown_party_creation")) return ErrorStatus.ON_COOLDOWN;
        Party playersParty = getParty(leader);
        if (playersParty != null) return ErrorStatus.ALREADY_IN_PARTY;
        allParties.put(party.getId(), party);
        partiesByMember.put(leader.getUniqueId(), party);
        Timer.setCooldownIgnoreIfPermission(leader, partyCreationCooldown, "cooldown_party_creation");
        return null;
    }

    public static ErrorStatus displayPartyInfo(Player p){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        Party party = getParty(p);
        if (party == null) return ErrorStatus.NOT_IN_PARTY;
        return displayPartyInfo(p, party);
    }

    public static ErrorStatus displayPartyInfo(Player p, Party party){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        PartyRank rank = partyRanks.get(party.getMembers().getOrDefault(p.getUniqueId(), ""));
        String title = party.getLeader().equals(p.getUniqueId()) ? leaderTitle : rank != null ? rank.title : "";
        boolean expSharingEnabled = party.isExpSharingEnabled() != null ? party.isExpSharingEnabled() : getBoolStat("exp_sharing", party);
        boolean itemSharingEnabled = party.isItemSharingEnabled() != null ? party.isItemSharingEnabled() : getBoolStat("item_sharing", party);
        Pair<PartyLevel, Double> level = getPartyLevel(party);
        PartyLevel nextLevel = level == null ? null : partyLevels.get(level.getOne().level + 1);
        String expForNext = nextLevel == null ?
                TranslationManager.getTranslation("max_level") : // if either current level or max level are null, assume max level is reached
                String.format("%,.1f", levelRequirements.getOrDefault(nextLevel.level, 0D) - levelRequirements.getOrDefault(level.getOne().level, 0D));
        partyInfoFormat.forEach(s -> p.sendMessage(Utils.chat(PlaceholderRegistry.parsePapi(PlaceholderRegistry.parse(s
                .replace("%rank%", title)
                .replace("%level%", level != null ? level.getOne().name : "")
                .replace("%level_numeric%", level != null ? String.valueOf(level.getOne().level) : "")
                .replace("%level_roman%", level != null ? StringUtils.toRoman(level.getOne().level) : "")
                .replace("%exp%", String.format("%,.1f", level != null ? level.getTwo() : p.getExp()))
                .replace("%exp_next%", expForNext)
                .replace("%name%", party.getDisplayName())
                .replace("%description%", party.getDescription())
                .replace("%member_count%", String.valueOf(party.getMembers().size() + 1))
                .replace("%member_cap%", String.valueOf(getTotalIntStat("party_capacity", party))), p), p)
                .replace("%status_exp_sharing%", TranslationManager.getTranslation("translation_" + expSharingEnabled))
                .replace("%status_item_sharing%", TranslationManager.getTranslation("translation_" + itemSharingEnabled))
                .replace("%status_open%", TranslationManager.getTranslation("translation_" + party.isOpen()))
                .replace("%member_list%", String.join(", ", getPartyMembers(party).keySet()))
        )));
        return null;
    }

    public static ErrorStatus hasPermission(Player p, String permission){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        if (p.hasPermission("valhalla.manageparties")) return null;
        Party party = getParty(p);
        if (party == null) return ErrorStatus.NOT_IN_PARTY;
        if (party.getLeader().equals(p.getUniqueId())) return null;
        PartyRank rank = partyRanks.get(party.getMembers().getOrDefault(p.getUniqueId(), ""));
        if (rank == null) return ErrorStatus.SENDER_NO_RANK;
        if (!rank.permissions.contains(permission)) return ErrorStatus.NO_PERMISSION;
        return null;
    }

    public static ErrorStatus changeDescription(Player p, String description){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        Party party = getParty(p);
        if (party == null) return ErrorStatus.NOT_IN_PARTY;
        if (!Timer.isCooldownPassed(p.getUniqueId(), "cooldown_party_description_change")) return ErrorStatus.ON_COOLDOWN;
        ErrorStatus permissionStatus = hasPermission(p, "manage_description");
        if (permissionStatus != null) return permissionStatus;
        ErrorStatus descriptionStatus = validateDescription(description);
        if (descriptionStatus != null) return descriptionStatus;
        if (getBoolStat("description_colors", party)) party.setDescription(Utils.chat(description));
        else party.setDescription(description);
        Timer.setCooldownIgnoreIfPermission(p, partyDescriptionChangeCooldown, "cooldown_party_description_change");
        return null;
    }

    public static ErrorStatus changeName(Player p, String name){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        Party party = getParty(p);
        if (party == null) return ErrorStatus.NOT_IN_PARTY;
        if (!Timer.isCooldownPassed(p.getUniqueId(), "cooldown_party_rename")) return ErrorStatus.ON_COOLDOWN;
        ErrorStatus permissionStatus = hasPermission(p, "manage_name");
        if (permissionStatus != null) return permissionStatus;
        ErrorStatus nameChangeStatus = validatePartyName(name);
        if (nameChangeStatus != null) return nameChangeStatus;
        String newId = convertNameToID(name);
        if (allParties.containsKey(newId)) return ErrorStatus.PARTY_ALREADY_EXISTS;

        allParties.remove(party.getId());
        party.setId(newId);
        allParties.put(newId, party);

        if (getBoolStat("party_colors", party)) party.setDisplayName(Utils.chat(name));
        else party.setDescription(name);
        Timer.setCooldownIgnoreIfPermission(p, partyRenameCooldown, "cooldown_party_rename");
        return null;
    }

    public static ErrorStatus togglePartyChat(Player p){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        Party party = getParty(p);
        if (party == null) return ErrorStatus.NOT_IN_PARTY;
        ErrorStatus chatStatus = hasPermission(p, "party_chat");
        if (chatStatus != null) return chatStatus;
        if (!partyChatPlayers.remove(p.getUniqueId())) partyChatPlayers.add(p.getUniqueId());
        return null;
    }

    public static ErrorStatus shareItem(Player from, Player to){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        if (!enabledItemSharing) return ErrorStatus.FEATURE_NOT_UNLOCKED;
        if (from.getUniqueId().equals(to.getUniqueId())) return ErrorStatus.TARGET_SENDER_SAME;
        Party fromParty = getParty(from);
        if (fromParty == null) return ErrorStatus.SENDER_NO_PARTY;
        if (WorldGuardHook.inDisabledRegion(from.getLocation(), from, WorldGuardHook.VMMO_PARTY_ITEMSHARING)) return ErrorStatus.FORBIDDEN_REGION;
        Party toParty = getParty(to);
        if (toParty == null) return ErrorStatus.TARGET_NO_PARTY;
        if (WorldGuardHook.inDisabledRegion(to.getLocation(), to, WorldGuardHook.VMMO_PARTY_ITEMSHARING)) return ErrorStatus.FORBIDDEN_REGION;
        if (!fromParty.getId().equals(toParty.getId())) return ErrorStatus.NOT_IN_SAME_PARTY;
        boolean itemSharingEnabled = fromParty.isItemSharingEnabled() != null ? fromParty.isItemSharingEnabled() : getBoolStat("item_sharing", fromParty);
        if (!itemSharingEnabled) return ErrorStatus.FEATURE_NOT_UNLOCKED;
        int reach = getTotalIntStat("item_sharing_radius", fromParty);
        if (reach >= 0 && (!from.getWorld().getName().equals(to.getWorld().getName()) || from.getLocation().distanceSquared(to.getLocation()) > reach * reach)) {
            return ErrorStatus.OUT_OF_RANGE;
        }
        if (!Timer.isCooldownPassed(from.getUniqueId(), "cooldown_share_item")) return ErrorStatus.ON_COOLDOWN;
        ItemStack hand = from.getInventory().getItemInMainHand();
        if (ItemUtils.isEmpty(hand)) return ErrorStatus.NO_ITEM;
        ItemBuilder handMeta = new ItemBuilder(hand);
        to.sendMessage(Utils.chat(TranslationManager.getTranslation("status_command_party_item_received")
                .replace("%player%", from.getName())
                .replace("%amount%", String.valueOf(hand.getAmount()))
                .replace("%item%", ItemUtils.getItemName(handMeta)))
        );
        ItemUtils.addItem(to, hand.clone(), true);
        from.getInventory().setItemInMainHand(null);
        Timer.setCooldownIgnoreIfPermission(from, itemSharingCooldown, "cooldown_share_item");
        return null;
    }

    public static ErrorStatus togglePartyOpen(Player p){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        Party party = getParty(p);
        if (party == null) return ErrorStatus.NOT_IN_PARTY;
        ErrorStatus openStatus = hasPermission(p, "open_party");
        if (openStatus != null) return openStatus;
        party.setOpen(!party.isOpen());
        return null;
    }

    public static ErrorStatus toggleItemSharing(Player p){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        Party party = getParty(p);
        if (party == null) return ErrorStatus.NOT_IN_PARTY;
        ErrorStatus toggleStatus = hasPermission(p, "toggle_item_sharing");
        if (toggleStatus != null) return toggleStatus;
        if (!getBoolStat("item_sharing", party)) return ErrorStatus.FEATURE_NOT_UNLOCKED;
        party.setItemSharingEnabled(party.isItemSharingEnabled() == null || !party.isItemSharingEnabled());
        return null;
    }

    public static ErrorStatus toggleFriendlyFire(Player p){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        Party party = getParty(p);
        if (party == null) return ErrorStatus.NOT_IN_PARTY;
        ErrorStatus toggleStatus = hasPermission(p, "toggle_friendly_fire");
        if (toggleStatus != null) return toggleStatus;
        party.setFriendlyFireEnabled(!party.isFriendlyFireEnabled());
        return null;
    }

    public static ErrorStatus toggleExpSharing(Player p){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        Party party = getParty(p);
        if (party == null) return ErrorStatus.NOT_IN_PARTY;
        ErrorStatus toggleStatus = hasPermission(p, "toggle_exp_sharing");
        if (toggleStatus != null) return toggleStatus;
        if (!getBoolStat("exp_sharing", party)) return ErrorStatus.FEATURE_NOT_UNLOCKED;
        party.setExpSharingEnabled(party.isExpSharingEnabled() == null || !party.isExpSharingEnabled());
        return null;
    }

    public static ErrorStatus leaveParty(Player p){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        Party party = getParty(p);
        if (party == null) return ErrorStatus.NOT_IN_PARTY;
        if (party.getLeader().equals(p.getUniqueId())) return ErrorStatus.CANNOT_KICK_LEADER;
        party.getMembers().remove(p.getUniqueId());
        partiesByMember.remove(p.getUniqueId());
        partyChatPlayers.remove(p.getUniqueId());
        return null;
    }

    public static ErrorStatus acceptInvite(Player p, String party){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        Party existingParty = getParty(p);
        if (existingParty != null) return ErrorStatus.ALREADY_IN_PARTY;
        Party toJoin = allParties.get(party);
        if (toJoin == null) return ErrorStatus.PARTY_DOES_NOT_EXIST;
        if (!toJoin.isOpen() && !partyInvites.getOrDefault(p.getUniqueId(), new HashSet<>()).contains(party)) return ErrorStatus.NOT_INVITED;
        ErrorStatus joinStatus = joinParty(p, toJoin);
        if (joinStatus != null) return joinStatus;
        Collection<Player> members = getOnlinePartyMembers(toJoin);
        for (Player member : members) {
            Utils.sendMessage(member, PlaceholderRegistry.parsePapi(PlaceholderRegistry.parse(TranslationManager.getTranslation("status_command_party_member_joined")
                    .replace("%player%", p.getName())
                    .replace("%party%", toJoin.getDisplayName()), member), member)
            );
        }
        return null;
    }

    public static ErrorStatus joinParty(Player p, Party party){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        if (party == null || !allParties.containsKey(party.getId())) return ErrorStatus.PARTY_DOES_NOT_EXIST;
        Party existingParty = getParty(p);
        if (existingParty != null || party.getMembers().containsKey(p.getUniqueId()) || party.getLeader().equals(p.getUniqueId())) return ErrorStatus.ALREADY_IN_PARTY;
        if (party.getMembers().size() + 1 >= getTotalIntStat("party_capacity", party)) return ErrorStatus.MEMBER_CAP_REACHED;
        party.getMembers().put(p.getUniqueId(), lowestRank);
        partiesByMember.put(p.getUniqueId(), party);
        return null;
    }

    public static ErrorStatus kickMember(Player kicker, UUID kicked){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        Party party = getParty(kicker);
        if (party == null) return ErrorStatus.SENDER_NO_PARTY;
        Party kickedParty = partiesByMember.get(kicked);
        if (kickedParty == null) return ErrorStatus.TARGET_NO_PARTY;
        if (!party.getId().equals(kickedParty.getId())) return ErrorStatus.NOT_IN_SAME_PARTY;
        if (party.getLeader().equals(kicked)) return ErrorStatus.CANNOT_KICK_LEADER;
        ErrorStatus kickStatus = hasPermission(kicker, "kick_members");
        if (kickStatus != null) return kickStatus;
        PartyRank kickerRank = partyRanks.get(party.getMembers().getOrDefault(kicker.getUniqueId(), ""));
        PartyRank kickedRank = partyRanks.get(party.getMembers().getOrDefault(kicked, ""));
        if (party.getLeader().equals(kicker.getUniqueId()) || kickedRank == null || kickedRank.rating < kickerRank.rating){
            party.getMembers().remove(kicked);
            partiesByMember.remove(kicked);
            Player k = ValhallaMMO.getInstance().getServer().getPlayer(kicked);
            if (k != null) Utils.sendMessage(k, TranslationManager.getTranslation("status_command_party_member_kicked"));
            return null;
        }
        return ErrorStatus.TARGET_HIGHER_RANK;
    }

    public static ErrorStatus forceKickMember(CommandSender kicker, UUID kicked){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        Party kickedParty = partiesByMember.get(kicked);
        if (kickedParty == null) return ErrorStatus.TARGET_NO_PARTY;
        if (kickedParty .getLeader().equals(kicked)) return ErrorStatus.CANNOT_KICK_LEADER;
        if (!kicker.hasPermission("valhalla.manageparties")) return ErrorStatus.NO_PERMISSION;
        kickedParty.getMembers().remove(kicked);
        partiesByMember.remove(kicked);
        Player k = ValhallaMMO.getInstance().getServer().getPlayer(kicked);
        if (k != null) Utils.sendMessage(k, TranslationManager.getTranslation("status_command_party_member_kicked"));
        return null;
    }

    public static ErrorStatus disbandParty(Player p){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        Party party = getParty(p);
        if (party == null) return ErrorStatus.NOT_IN_PARTY;
        if (!party.getLeader().equals(p.getUniqueId())) return ErrorStatus.NO_PERMISSION;
        return disbandParty(party);
    }

    public static ErrorStatus disbandParty(Party party){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        for (UUID uuid : party.getMembers().keySet()){
            partiesByMember.remove(uuid);
            Player online = ValhallaMMO.getInstance().getServer().getPlayer(uuid);
            if (online != null) Utils.sendMessage(online, TranslationManager.getTranslation("status_command_party_disbanded"));
        }
        partiesByMember.remove(party.getLeader());
        Player online = ValhallaMMO.getInstance().getServer().getPlayer(party.getLeader());
        if (online != null) Utils.sendMessage(online, TranslationManager.getTranslation("status_command_party_disbanded"));
        allParties.remove(party.getId());
        return null;
    }

    public static ErrorStatus invite(Player inviter, Player who){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        Party inviterParty = getParty(inviter);
        if (inviterParty == null) return ErrorStatus.NOT_IN_PARTY;
        Party invitedParty = getParty(who);
        if (invitedParty != null) return ErrorStatus.ALREADY_IN_PARTY;
        if (inviterParty.getMembers().size() + 1 >= getTotalIntStat("party_capacity", inviterParty)) return ErrorStatus.MEMBER_CAP_REACHED;
        ErrorStatus inviteStatus = hasPermission(inviter, "invite_members");
        if (inviteStatus != null) return inviteStatus;
        Collection<String> existingInvites = partyInvites.getOrDefault(who.getUniqueId(), new HashSet<>());
        if (existingInvites.contains(inviterParty.getId())) return ErrorStatus.TARGET_NOT_INVITEABLE;
        existingInvites.add(inviterParty.getId());
        partyInvites.put(who.getUniqueId(), existingInvites);
        if (!hasInvitesMuted(who)) Utils.sendMessage(who,
                TranslationManager.getTranslation("status_command_party_invite_received")
                        .replace("%party%", inviterParty.getDisplayName())
                        .replace("%player%", inviter.getName())
                        .replace("%party_id%", inviterParty.getId())
        );
        return null;
    }

    public static ErrorStatus setMemberRank(Player who, Player target, String newRank){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        Party party = getParty(who);
        if (party == null) return ErrorStatus.NOT_IN_PARTY;
        Party targetParty = getParty(target);
        if (targetParty == null) return ErrorStatus.TARGET_NO_PARTY;
        PartyRank rankToGrant = partyRanks.get(newRank);
        if (rankToGrant == null) return ErrorStatus.RANK_NOT_FOUND;
        if (!party.getId().equals(targetParty.getId())) return ErrorStatus.NOT_IN_SAME_PARTY;
        ErrorStatus rankupStatus = hasPermission(who, "manage_roles");
        if (rankupStatus != null) return rankupStatus;
        boolean allowed = party.getLeader().equals(who.getUniqueId());
        if (!allowed) {
            PartyRank rank = partyRanks.get(party.getMembers().getOrDefault(who.getUniqueId(), ""));
            if (rank == null) return ErrorStatus.SENDER_NO_RANK;
            if (rankToGrant.rating > rank.rating) return ErrorStatus.RANK_HIGHER_SENDER;
            PartyRank targetRank = partyRanks.get(party.getMembers().getOrDefault(who.getUniqueId(), ""));
            allowed = !party.getLeader().equals(target.getUniqueId()) && (targetRank == null || targetRank.rating < rank.rating);
        }
        if (!allowed) return ErrorStatus.TARGET_HIGHER_RANK;
        party.getMembers().put(target.getUniqueId(), rankToGrant.id);
        return null;
    }

    public static ErrorStatus changeLeader(Player leader, Player to){
        if (!enabledParties) return ErrorStatus.PARTIES_DISABLED;
        Party party = getParty(leader);
        if (party == null) return ErrorStatus.NOT_IN_PARTY;
        Party targetParty = getParty(to);
        if (targetParty == null) return ErrorStatus.TARGET_NO_PARTY;
        if (!party.getId().equals(targetParty.getId())) return ErrorStatus.NOT_IN_SAME_PARTY;
        if (!party.getLeader().equals(leader.getUniqueId())) return ErrorStatus.SENDER_NOT_LEADER;
        if (leader.getUniqueId().equals(to.getUniqueId())) return ErrorStatus.TARGET_SENDER_SAME;
        party.setLeader(to.getUniqueId());
        party.getMembers().put(leader.getUniqueId(), highestRank);
        return null;
    }

    public static boolean toggleInviteMute(Player p){
        if (p.hasMetadata("valhallammo_party_invite_mute")) {
            p.removeMetadata("valhallammo_party_invite_mute", ValhallaMMO.getInstance());
            return false;
        }
        p.setMetadata("valhallammo_party_invite_mute", new FixedMetadataValue(ValhallaMMO.getInstance(), (byte) 1));
        return true;
    }

    public static boolean hasInvitesMuted(Player p){
        return p.hasMetadata("valhallammo_party_invite_mute");
    }

    public static Party createParty(Player leader, String name){
        if (!enabledParties) return null;
        return new Party(convertNameToID(name), name, leader.getUniqueId());
    }

    private static String convertNameToID(String name){
        return name.toLowerCase(java.util.Locale.US).replaceAll(" ", "_").replaceAll("&[0-9a-fA-FkmolnrKMOLNR]", "");
    }

    /**
     * Gets the level of the party given their current exp, returned as a pair where value 1 is the level and value 2 is the exp the party has
     * after their level's exp requirement is subtracted (true exp value). For example, if a party has 1500 exp total and reaching level 1 requires
     * 1000 exp, their true exp would be 500 since that's what's left.
     * @param p the party to grab their level from
     * @return a pair with the PartyLevel and true exp
     */
    public static Pair<PartyLevel, Double> getPartyLevel(Party p){
        if (!enabledParties) return null;
        if (partyLevelCache.containsKey(p.getId()) && partyLevelCache.get(p.getId()).getOne() + 3000L > System.currentTimeMillis())
            return new Pair<>(partyLevelCache.get(p.getId()).getTwo(), p.getExp() - levelRequirements.get(partyLevelCache.get(p.getId()).getTwo().level));
        int lv = 0;
        for (int level : levelRequirements.keySet()){
            if (p.getExp() < levelRequirements.get(level)) break;
            lv = level;
        }
        partyLevelCache.put(p.getId(), new Pair<>(System.currentTimeMillis(), partyLevels.get(lv)));
        return new Pair<>(partyLevels.get(lv), p.getExp() - levelRequirements.get(lv));
    }

    private static final Map<String, Pair<Long, PartyLevel>> partyLevelCache = new HashMap<>();

    public static void addEXP(Party party, double exp){
        Pair<PartyManager.PartyLevel, Double> level = PartyManager.getPartyLevel(party);
        party.setExp(party.getExp() + exp);
        if (level != null && level.getOne().nextLevelEXP >= 0 && party.getExp() >= level.getOne().nextLevelEXP){
            partyLevelCache.remove(party.getId());
            level = getPartyLevel(party);
            if (level == null) return;
            for (Player p : PartyManager.getOnlinePartyMembers(party)){
                if (PartyManager.getPartyLevelUpFormat() != null)
                    Utils.sendMessage(p, PlaceholderRegistry.parsePapi(PlaceholderRegistry.parse(PartyManager.getPartyLevelUpFormat()
                            .replace("%party%", party.getDisplayName())
                            .replace("%level%", level.getOne().getName())
                            .replace("%level_numeric%", String.valueOf(level.getOne().getLevel()))
                            .replace("%level_roman%", StringUtils.toRoman(level.getOne().getLevel())), p), p)
                    );
                if (level.getOne().getLevelUpDescription() != null)
                    level.getOne().getLevelUpDescription().forEach(d -> Utils.sendMessage(p, Utils.chat(PlaceholderRegistry.parsePapi(PlaceholderRegistry.parse(d, p), p))));
            }
        }
    }

    public static boolean togglePartySpy(Player p){
        if (!partySpyPlayers.remove(p.getUniqueId())){
            partySpyPlayers.add(p.getUniqueId());
            return true;
        }
        return false;
    }

    /**
     * Gathers the total value of the given Float type stat. Considers the level of the party, the party's exclusive stats, and
     * the default value of the stat.
     * @param stat the stat to fetch
     * @param p the party of which to fetch the stat from
     * @return the total stat value
     */
    public static float getTotalFloatStat(String stat, Party p){
        if (!defaultFloats.containsKey(stat) || !enabledParties) return 0;
        float def = defaultFloats.getOrDefault(stat, 0F) + p.getFloats().getOrDefault(stat, 0F);
        Pair<PartyLevel, Double> partyLevel = getPartyLevel(p);
        if (partyLevel == null) return def;
        for (int i = 0; i <= partyLevel.getOne().level; i++) {
            if (partyLevels.containsKey(i)) def += partyLevels.get(i).floatStatBuffs.getOrDefault(stat, 0F);
        }
        return def;
    }

    /**
     * Gathers the total value of the given Integer type stat. Considers the level of the party, the party's exclusive stats, and
     * the default value of the stat.
     * @param stat the stat to fetch
     * @param p the party of which to fetch the stat from
     * @return the total stat value
     */
    public static int getTotalIntStat(String stat, Party p){
        if (!defaultInts.containsKey(stat) || !enabledParties) return 0;
        int def = defaultInts.getOrDefault(stat, 0) + p.getInts().getOrDefault(stat, 0);
        Pair<PartyLevel, Double> partyLevel = getPartyLevel(p);
        if (partyLevel == null) return def;
        for (int i = 0; i <= partyLevel.getOne().level; i++) {
            if (partyLevels.containsKey(i)) def += partyLevels.get(i).intStatBuffs.getOrDefault(stat, 0);
        }
        return def;
    }

    /**
     * Gathers the value of the given Boolean type stat. Considers the level of the party, the party's exclusive stats, and
     * the default value of the stat. Any party-defined stat takes priority, so if a feature is specifically locked for a party it will
     * stay locked. Aside from that though, a `true` value takes priority so if any of the fetched booleans are true the end result will be true.
     * @param stat the stat to fetch
     * @param p the party of which to fetch the stat from
     * @return the total stat value
     */
    public static boolean getBoolStat(String stat, Party p){
        if (!defaultBools.containsKey(stat) || !enabledParties) return false;
        if (p.getBools().containsKey(stat)) return p.getBools().get(stat);
        boolean def = defaultBools.getOrDefault(stat, false);
        Pair<PartyLevel, Double> partyLevel = getPartyLevel(p);
        if (partyLevel == null || def) return def;
        for (int i = 0; i <= partyLevel.getOne().level; i++) {
            if (partyLevels.containsKey(i) && partyLevels.get(i).boolStats.getOrDefault(stat, false)) return true;
        }
        return false;
    }

    public static Map<String, Double> getCompanyStats(Party p){
        if (!enabledParties) return new HashMap<>();
        Map<String, Double> companyStats = new HashMap<>(defaultCompanyStats);
        Pair<PartyLevel, Double> partyLevel = getPartyLevel(p);
        if (partyLevel == null) return companyStats;
        for (int i = 0; i <= partyLevel.getOne().level; i++) {
            PartyLevel level = partyLevels.get(i);
            if (level == null) continue;
            for (String stat : level.companyStats.keySet()) {
                if (companyStats.containsKey(stat)) companyStats.put(stat, companyStats.getOrDefault(stat, 0D) + level.companyStats.get(stat));
                else companyStats.put(stat, level.companyStats.get(stat));
            }
        }
        return companyStats;
    }

    public static Collection<Player> getOnlinePartyMembers(Party party){
        Collection<UUID> members = new HashSet<>(party.getMembers().keySet());
        members.add(party.getLeader());
        return new HashSet<>(Utils.getOnlinePlayersFromUUIDs(members).values());
    }

    public static Map<String, OfflinePlayer> getPartyMembers(Party party){
        Collection<UUID> members = new HashSet<>(party.getMembers().keySet());
        members.add(party.getLeader());
        return Utils.getPlayersFromUUIDs(members);
    }

    /**
     * Registers an integer stat to be used by parties. Must be registered in plugin onLoad, not onEnable
     */
    public static void registerIntStat(String key){ defaultInts.put(key, 0); }

    /**
     * Registers a float stat to be used by parties. Must be registered in plugin onLoad, not onEnable
     */
    public static void registerFloatStat(String key){ defaultFloats.put(key, 0F); }

    /**
     * Registers a boolean stat to be used by parties. Must be registered in plugin onLoad, not onEnable
     */
    public static void registerBooleanStat(String key){ defaultBools.put(key, false); }

    @SuppressWarnings("all")
    public static void loadParties(){
        YamlConfiguration config = ConfigManager.getConfig("parties.yml").reload().get();
        enabledParties = config.getBoolean("enabled");
        if (!enabledParties) return;
        enabledEXPSharing = config.getBoolean("exp_sharing");
        enabledItemSharing = config.getBoolean("item_sharing");
        partyCreationCooldown = config.getInt("party_creation_cooldown");
        itemSharingCooldown = config.getInt("item_share_cooldown");
        partyEXPConversionRate = config.getDouble("party_exp_rate");
        partyRenameCooldown = config.getInt("party_rename_cooldown");
        partyDescriptionChangeCooldown = config.getInt("party_description_change_cooldown");

        String pattern = config.getString("legal_characters_regex");
        if (StringUtils.isEmpty(pattern)) legalCharactersPattern = Pattern.compile(pattern);
        partyNameMinimum = config.getInt("name_character_minimum");
        partyNameMaximum = config.getInt("name_character_maximum");
        partyDescriptionMaximum = config.getInt("description_character_maximum");

        partyChatFormat = config.getString("party_chat_format");
        partySpyFormat = config.getString("party_spy_format");
        partyInfoFormat = TranslationManager.translateListPlaceholders(config.getStringList("party_info_format"));
        partyLevelUpFormat = config.getString("party_levelup_format");

        for (String intStat : defaultInts.keySet()) defaultInts.put(intStat, config.getInt("defaults." + intStat, 0));
        for (String floatStat : defaultFloats.keySet()) defaultFloats.put(floatStat, (float) config.getDouble("defaults." + floatStat, 0F));
        for (String boolStat : defaultBools.keySet()) defaultBools.put(boolStat, config.getBoolean("defaults." + boolStat, false));

        ConfigurationSection defaultStatSection = config.getConfigurationSection("default_company_stats");
        if (defaultStatSection != null){
            for (String stat : defaultStatSection.getKeys(false)) {
                if (!AccumulativeStatManager.getSources().containsKey(stat)) continue;
                defaultCompanyStats.put(stat, config.getDouble("default_company_stats." + stat));
            }
        }

        ConfigurationSection levelSection = config.getConfigurationSection("levels");
        if (levelSection != null){
            double expTotal = 0;
            PartyLevel previousLevel = null;
            for (String level : levelSection.getKeys(false)){
                int lv = Catch.catchOrElse(() -> Integer.parseInt(level), -1);
                if (lv < 0) continue;
                String name = TranslationManager.translatePlaceholders(config.getString("levels." + level + ".name"));
                double requiredEXP = lv == 0 ? 0 : config.getDouble("levels." + level + ".exp_required");
                expTotal += requiredEXP;
                levelRequirements.put(lv, expTotal);
                List<String> levelUpDescription = lv == 0 ? null : TranslationManager.translateListPlaceholders(config.getStringList("levels." + level + ".levelup_description"));
                PartyLevel partyLevel = new PartyLevel(lv, name, requiredEXP, levelUpDescription);
                if (previousLevel != null) previousLevel.setNextLevelEXP(expTotal);
                previousLevel = partyLevel;

                if (lv != 0){
                    for (String intStat : defaultInts.keySet()) {
                        int stat = config.getInt("levels." + level + "." + intStat, 0);
                        if (stat == 0) continue;
                        partyLevel.intStatBuffs.put(intStat, stat);
                    }
                    for (String floatStat : defaultFloats.keySet()) {
                        float stat = (float) config.getDouble("levels." + level + "." + floatStat, 0F);
                        if (stat == 0) continue;
                        partyLevel.floatStatBuffs.put(floatStat, stat);
                    }
                    for (String boolStat : defaultBools.keySet()) {
                        partyLevel.boolStats.put(boolStat, config.getBoolean("levels." + level + "." + boolStat, false));
                    }
                    ConfigurationSection levelStatSection = config.getConfigurationSection("levels." + level + ".stats");
                    if (levelStatSection != null){
                        for (String stat : levelStatSection.getKeys(false)) {
                            if (!AccumulativeStatManager.getSources().containsKey(stat)) continue;
                            partyLevel.companyStats.put(stat, config.getDouble("levels." + level + ".stats." + stat));
                        }
                    }
                }
                partyLevels.put(partyLevel.level, partyLevel);
            }
        }

        leaderTitle = TranslationManager.translatePlaceholders(config.getString("leader_title"));
        PartyRank lowest = null;
        PartyRank highest = null;
        ConfigurationSection rankSection = config.getConfigurationSection("ranks");
        if (rankSection != null){
            for (String rank : rankSection.getKeys(false)){
                int rating = config.getInt("ranks." + rank + ".rating");
                String title = TranslationManager.translatePlaceholders(config.getString("ranks." + rank + ".title"));
                List<String> permissions = config.getStringList("ranks." + rank + ".permissions");
                PartyRank partyRank = new PartyRank(rank, rating, title, permissions);
                if (lowest == null || lowest.rating > rating) lowest = partyRank;
                if (highest == null || highest.rating < rating) highest = partyRank;
                partyRanks.put(rank, partyRank);
            }
        }
        if (lowest != null) lowestRank = lowest.id;
        else {
            enabledParties = false;
            return;
        }
        if (highest != null) highestRank = highest.id;
        else {
            enabledParties = false;
            return;
        }
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new PartyChatListener(), ValhallaMMO.getInstance());
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new PartyEXPGainListener(), ValhallaMMO.getInstance());
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new PartyPvPListener(), ValhallaMMO.getInstance());

        File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/parties.json");
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (BufferedReader setsReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))){
            Party[] sets = gson.fromJson(setsReader, Party[].class);
            if (sets == null) return;
            for (Party party : sets) {
                allParties.put(party.getId(), party);
                party.getMembers().forEach((m, p) -> partiesByMember.put(m, party));
                partiesByMember.put(party.getLeader(), party);
            }
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not load parties from parties.json, " + exception.getMessage());
        } catch (NoClassDefFoundError ignored){}
    }

    @SuppressWarnings("all")
    public static void saveParties(){
        if (!enabledParties) return;
        File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/parties.json");
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8))){
            JsonElement element = gson.toJsonTree(new ArrayList<>(allParties.values()), new TypeToken<ArrayList<Party>>(){}.getType());
            gson.toJson(element, writer);
            writer.flush();
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not save parties to parties.json, " + exception.getMessage());
        }
    }

    public static String getPartyChatFormat() { return partyChatFormat; }
    public static String getPartySpyFormat() { return partySpyFormat; }
    public static String getLeaderTitle() { return leaderTitle; }

    public static Map<String, Party> getAllParties() { return new HashMap<>(allParties); }
    public static TreeMap<Integer, PartyLevel> getPartyLevels() { return new TreeMap<>(partyLevels); }
    public static Map<String, PartyRank> getPartyRanks() { return new HashMap<>(partyRanks); }
    public static Collection<UUID> getPartyChatPlayers() { return partyChatPlayers; }
    public static Collection<UUID> getPartySpyPlayers() { return partySpyPlayers; }
    public static Map<UUID, Collection<String>> getPartyInvites() { return partyInvites; }
    public static double getPartyEXPConversionRate() { return partyEXPConversionRate; }
    public static String getPartyLevelUpFormat() { return partyLevelUpFormat; }
    public static String getLowestRank() { return lowestRank; }
    public static String getHighestRank() { return highestRank; }
    public static Map<String, Boolean> getDefaultBools() { return new HashMap<>(defaultBools); }
    public static Map<String, Float> getDefaultFloats() { return new HashMap<>(defaultFloats); }
    public static Map<String, Integer> getDefaultInts() { return new HashMap<>(defaultInts); }

    public static class PartyLevel {
        private final int level;
        private final String name;
        private final double requiredEXP;
        private double nextLevelEXP = -1;
        private final List<String> levelUpDescription;

        public PartyLevel(int level, String name, double requiredEXP, List<String> levelUpDescription){
            this.level = level;
            this.name = name;
            this.requiredEXP = requiredEXP;
            this.levelUpDescription = levelUpDescription;
        }
        private final Map<String, Integer> intStatBuffs = new HashMap<>();
        private final Map<String, Float> floatStatBuffs = new HashMap<>();
        private final Map<String, Boolean> boolStats = new HashMap<>();
        private final Map<String, Double> companyStats = new HashMap<>();

        public int getLevel() { return level; }
        public double getNextLevelEXP() { return nextLevelEXP; }
        public String getName() { return name; }
        public double getRequiredEXP() { return requiredEXP; }
        public List<String> getLevelUpDescription() { return levelUpDescription; }

        public void setNextLevelEXP(double nextLevelEXP) { this.nextLevelEXP = nextLevelEXP; }
    }

    public record PartyRank(String id, int rating, String title, Collection<String> permissions) {
        public PartyRank(String id, int rating, String title, Collection<String> permissions) {
            this.id = id;
            this.rating = rating;
            this.title = title;
            this.permissions = new HashSet<>(permissions);
        }
    }

    public static Map<Integer, Double> getLevelRequirements() {
        return levelRequirements;
    }

    public enum ErrorStatus{
        NO_PERMISSION("error_command_no_permission"),
        TARGET_NOT_INVITEABLE("status_command_party_member_not_inviteable"),
        FORBIDDEN_REGION("status_command_party_forbidden_region"),
        TARGET_NO_PARTY("status_command_party_target_no_party"),
        SENDER_NO_PARTY("status_command_party_sender_no_party"),
        TARGET_HIGHER_RANK("status_command_party_target_higher_rank"),
        RANK_HIGHER_SENDER("status_command_party_rank_higher_sender"),
        TARGET_SENDER_SAME("status_command_party_target_sender_same"),
        SENDER_NOT_LEADER("status_command_party_sender_not_leader"),
        SENDER_NO_RANK("status_command_party_sender_no_rank"),
        NOT_IN_SAME_PARTY("status_command_party_not_in_same_party"),
        ALREADY_IN_PARTY("status_command_party_already_in_party"),
        NOT_IN_PARTY("status_command_party_not_in_party"),
        PARTY_DOES_NOT_EXIST("status_command_party_party_not_found"),
        RANK_NOT_FOUND("status_command_party_rank_not_found"),
        PARTY_ALREADY_EXISTS("status_command_party_party_already_exists"),
        FEATURE_NOT_UNLOCKED("status_command_party_feature_not_unlocked"),
        CANNOT_KICK_LEADER("status_command_party_cannot_kick_leader"),
        ON_COOLDOWN("status_command_party_on_cooldown"),
        NO_ITEM("status_command_party_no_item_held"),
        EXCEEDED_CHARACTER_LIMIT("status_command_party_character_limit_reached"),
        NOT_ENOUGH_CHARACTERS("status_command_party_name_not_long_enough"),
        ILLEGAL_CHARACTERS_USED("status_command_party_invalid_characters_used"),
        NOT_INVITED("status_command_party_not_invited"),
        OUT_OF_RANGE("status_command_item_share_outranged"),
        PARTIES_DISABLED("status_command_parties_disabled"),
        MEMBER_CAP_REACHED("status_command_party_member_cap_reached");

        final String message;
        ErrorStatus(String translationPath){
            this.message = TranslationManager.getTranslation(translationPath);
        }

        public void sendErrorMessage(CommandSender p){
            Utils.sendMessage(p, message);
        }
        public void sendErrorMessage(CommandSender p, Player target){
            Utils.sendMessage(p, message.replace("%player%", target.getName()));
        }

        public String getMessage() {
            return message;
        }
    }
}
