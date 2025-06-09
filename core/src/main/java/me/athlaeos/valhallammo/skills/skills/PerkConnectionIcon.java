package me.athlaeos.valhallammo.skills.skills;

import org.bukkit.Material;

public class PerkConnectionIcon {
    private final Skill parentSkill;
    private final Perk parentPerk;
    private final Material lockedMaterial;
    private final Material unlockableMaterial;
    private final Material unlockedMaterial;
    private final int lockedData;
    private final int unlockableData;
    private final int unlockedData;
    private int x;
    private int y;

    public PerkConnectionIcon(Skill parentSkill, Perk parentPerk, int x, int y, Material lockedMaterial, Material unlockableMaterial, Material unlockedMaterial, int locked, int unlockable, int unlocked){
        this.parentSkill = parentSkill;
        this.parentPerk = parentPerk;
        this.x = x;
        this.y = y;
        this.lockedMaterial = lockedMaterial;
        this.unlockableMaterial = unlockableMaterial;
        this.unlockedMaterial = unlockedMaterial;
        this.lockedData = locked;
        this.unlockableData = unlockable;
        this.unlockedData = unlocked;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLockedData() {
        return lockedData;
    }

    public Material getUnlockableMaterial() {
        return unlockableMaterial;
    }

    public Material getUnlockedMaterial() {
        return unlockedMaterial;
    }

    public int getUnlockableData() {
        return unlockableData;
    }

    public int getUnlockedData() {
        return unlockedData;
    }

    public Material getLockedMaterial() {
        return lockedMaterial;
    }

    public Skill getParentSkill() {
        return parentSkill;
    }

    public Perk getParentPerk() {
        return parentPerk;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }
}
