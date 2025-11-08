package me.athlaeos.valhallammo.crafting.blockvalidations.implementations;

import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class Waterlogged extends Validation {
    @Override
    public String id() {
        return "REQUIREMENT_WATERLOGGED";
    }

    @Override
    public ItemStack icon() {
        return new ItemBuilder(Material.WATER_BUCKET)
                .name("&9Require Waterlogging")
                .lore("&fRequires the block to be",
                        "&fwaterlogged.").get();
    }

    @Override
    public String activeDescription() {
        return "&fMust be waterlogged";
    }

    @Override
    public String validationError() {
        return TranslationManager.getTranslation("validation_warning_not_waterlogged");
    }

    @Override
    public boolean isCompatible(String block) {
        Material vanilla = Catch.catchOrElse(() -> Material.valueOf(block), null);
        if (vanilla == null) return false;
        return vanilla.createBlockData() instanceof org.bukkit.block.data.Waterlogged;
    }

    @Override
    public boolean validate(Block b) {
        if (b.getBlockData() instanceof org.bukkit.block.data.Waterlogged w) return w.isWaterlogged();
        return false;
    }

    @Override
    public void execute(Block b) {
        // do nothing
    }
}
