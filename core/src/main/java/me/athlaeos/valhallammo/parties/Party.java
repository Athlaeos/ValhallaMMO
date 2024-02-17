package me.athlaeos.valhallammo.parties;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Party {
    private String id;
    private UUID leader;
    private final Map<UUID, String> members = new HashMap<>();
    private double exp = 0;
    private boolean isOpen = false;

    private String displayName;
    private String description = "";

    private boolean friendlyFireEnabled = false;

    private Boolean expSharingEnabled = null;
    private Boolean itemSharingEnabled = null;
    private final Map<String, Integer> ints = new HashMap<>();
    private final Map<String, Float> floats = new HashMap<>();
    private final Map<String, Boolean> bools = new HashMap<>();
    private final Map<String, Double> companyStatsPerMember = new HashMap<>();

    public Party(String id, String displayName, UUID leader){
        this.id = id;
        this.displayName = displayName;
        this.leader = leader;
    }

    public String getId() { return id; }
    public UUID getLeader() { return leader; }
    public Map<UUID, String> getMembers() { return members; }
    public double getExp() { return exp; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Boolean isExpSharingEnabled() { return expSharingEnabled; }
    public Boolean isItemSharingEnabled() { return itemSharingEnabled; }
    public Map<String, Integer> getInts() { return ints; }
    public Map<String, Float> getFloats() { return floats; }
    public Map<String, Boolean> getBools() { return bools; }
    public Map<String, Double> getCompanyStatsPerMember() { return companyStatsPerMember; }
    public boolean isFriendlyFireEnabled() { return friendlyFireEnabled; }
    public void setOpen(boolean open) { isOpen = open; }

    public void setLeader(UUID leader) { this.leader = leader; }
    public void setExp(double exp) { this.exp = exp; }
    public void setDescription(String description) { this.description = description; }
    public void setFriendlyFireEnabled(boolean friendlyFireEnabled) { this.friendlyFireEnabled = friendlyFireEnabled; }
    public void setExpSharingEnabled(Boolean expSharingEnabled) { this.expSharingEnabled = expSharingEnabled; }
    public void setItemSharingEnabled(Boolean itemSharingEnabled) { this.itemSharingEnabled = itemSharingEnabled; }
    public boolean isOpen() { return isOpen; }
    public void setId(String id) { this.id = id; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
