package me.athlaeos.valhallammo.version.conversion_dto;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public abstract class Profile implements Cloneable{
    protected NamespacedKey key;
    protected UUID owner;
    protected int level;
    protected double exp;
    protected double lifetimeEXP;

    public Profile(Player owner){
        if (owner == null) return;
        this.owner = owner.getUniqueId();
    }

    public int getLevel() {
        return level;
    }

    public double getExp() {
        return exp;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public double getLifetimeEXP() {
        return lifetimeEXP;
    }

    public void setLifetimeEXP(double lifetimeEXP) {
        this.lifetimeEXP = lifetimeEXP;
    }

    public abstract Profile fetchProfile(Player p, DatabaseConnection conn) throws SQLException;

    @Override
    public Profile clone() throws CloneNotSupportedException {
        return (Profile) super.clone();
    }
}
