package me.athlaeos.valhallammo.crafting.blockvalidations;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public abstract class Validation {
    public abstract String id();
    public abstract ItemStack icon();
    public abstract String activeDescription();
    public abstract String validationError();
    public abstract boolean isCompatible(String type);
    public abstract boolean validate(Block b);
    public abstract void execute(Block b);
}
