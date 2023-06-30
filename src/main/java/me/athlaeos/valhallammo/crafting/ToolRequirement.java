package me.athlaeos.valhallammo.crafting;

public class ToolRequirement {
    private ToolRequirementType toolRequirementType = ToolRequirementType.NOT_REQUIRED;
    private int requiredToolID = -1;

    public ToolRequirement(ToolRequirementType toolRequirementType, int requiredToolID){
        this.toolRequirementType = toolRequirementType;
        this.requiredToolID = requiredToolID;
    }

    public boolean canCraft(int heldToolID){
        return toolRequirementType.check(heldToolID, requiredToolID);
    }

    public int getRequiredToolID() {
        return requiredToolID;
    }

    public ToolRequirementType getToolRequirementType() {
        return toolRequirementType;
    }
    
    public void setToolRequirementType(ToolRequirementType toolRequirementType) {
        this.toolRequirementType = toolRequirementType;
    }
    
    public void setRequiredToolID(int requiredToolID) {
        this.requiredToolID = requiredToolID;
    }
}


