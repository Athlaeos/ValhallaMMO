package me.athlaeos.valhallammo.crafting.blockvalidations.implementations;

import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.inventory.ItemStack;

public class NotWaterlogged extends Validation {
    @Override
    public String id() {
        return "REQUIREMENT_NOT_WATERLOGGED";
    }

    @Override
    public ItemStack icon() {
        return new ItemBuilder(Material.BUCKET)
                .name("&eForbid Waterlogging")
                .lore("&fRequires the block to not be",
                        "&fwaterlogged.").get();
    }

    @Override
    public String activeDescription() {
        return "&fMust not be waterlogged";
    }

    @Override
    public String validationError() {
        return TranslationManager.getTranslation("validation_warning_waterlogged");
    }

    @Override
    public boolean isCompatible(Material block) {
        return block.createBlockData() instanceof Waterlogged;
    }

    @Override
    public boolean validate(Block b) {
        if (b.getBlockData() instanceof Waterlogged w) return !w.isWaterlogged();
        return true;
    }

    @Override
    public void execute(Block b) {
        // do nothing
    }
}
