package me.athlaeos.valhallammo.crafting.blockvalidations.implementations;

import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Set;

public class HotBlockUnder extends Validation {
    @Override
    public String id() {
        return "REQUIREMENT_HOT_FLOOR";
    }

    @Override
    public ItemStack icon() {
        return new ItemBuilder(Material.MAGMA_BLOCK)
                .name("&6Require Hot Floor")
                .lore("&fRequires the block to be",
                        "&fabove a hot block, such as",
                        "&flava, fire, magma.").get();
    }

    @Override
    public String activeDescription() {
        return "&fMust be situated above hot block";
    }

    @Override
    public String validationError() {
        return TranslationManager.getTranslation("validation_warning_not_above_hot_block");
    }

    @Override
    public boolean isCompatible(Material block) {
        return true;
    }

    private static final Collection<Material> hotBlocks = Set.of(Material.MAGMA_BLOCK, Material.LAVA, Material.FIRE, Material.CAMPFIRE, Material.SOUL_CAMPFIRE);

    @Override
    public boolean validate(Block b) {
        return hotBlocks.contains(b.getLocation().subtract(0, 1, 0).getBlock().getType());
    }

    @Override
    public void execute(Block b) {
        // do nothing
    }
}
