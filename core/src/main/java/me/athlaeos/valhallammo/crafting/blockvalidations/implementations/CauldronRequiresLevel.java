package me.athlaeos.valhallammo.crafting.blockvalidations.implementations;

import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.inventory.ItemStack;

public class CauldronRequiresLevel extends Validation {
    @Override
    public String id() {
        return "REQUIRES_CAULDRON_LEVEL";
    }

    @Override
    public ItemStack icon() {
        return new ItemBuilder(Material.CAULDRON)
                .name("&9Requires Fluid Level")
                .lore("&fRequires the cauldron's fluid",
                        "&flevel to be at least 1").get();
    }

    @Override
    public String activeDescription() {
        return "&fRequires a fluid level";
    }

    @Override
    public String validationError() {
        return TranslationManager.getTranslation("validation_warning_not_full_enough");
    }

    @Override
    public boolean isCompatible(Material block) {
        return block.toString().contains("CAULDRON");
    }

    @Override
    public boolean validate(Block b) {
        if (b.getBlockData() instanceof Levelled l) return l.getLevel() > 0;
        return false;
    }

    @Override
    public void execute(Block b) {
    }
}
