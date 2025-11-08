package me.athlaeos.valhallammo.crafting.blockvalidations.implementations;

import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.inventory.ItemStack;

public class CampfireUnlit extends Validation {
    @Override
    public String id() {
        return "REQUIREMENT_CAMPFIRE_UNLIT";
    }

    @Override
    public ItemStack icon() {
        return new ItemBuilder(Material.CAMPFIRE)
                .name("&6Require Unlit Campfire")
                .lore("&fRequires the campfire to not",
                        "&fbe lit").get();
    }

    @Override
    public String activeDescription() {
        return "&fCampfire must not be lit";
    }

    @Override
    public String validationError() {
        return TranslationManager.getTranslation("validation_warning_campfire_lit");
    }

    @Override
    public boolean isCompatible(String block) {
        return block.equals("CAMPFIRE") || block.equals("SOUL_CAMPFIRE");
    }

    @Override
    public boolean validate(Block b) {
        if (b.getBlockData() instanceof Lightable l) return !l.isLit();
        return false;
    }

    @Override
    public void execute(Block b) {
        // do nothing
    }
}
