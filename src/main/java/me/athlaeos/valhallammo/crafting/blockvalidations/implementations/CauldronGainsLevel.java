package me.athlaeos.valhallammo.crafting.blockvalidations.implementations;

import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.inventory.ItemStack;

public class CauldronGainsLevel extends Validation {
    @Override
    public String id() {
        return "GAINS_CAULDRON_LEVEL";
    }

    @Override
    public ItemStack icon() {
        return new ItemBuilder(Material.CAULDRON)
                .name("&9Gain Fluid Level")
                .lore("&fFills the cauldron with 1",
                        "&flayer of water. Must not",
                        "&fbe full.").get();
    }

    @Override
    public String activeDescription() {
        return "&fGains a water level";
    }

    @Override
    public String validationError() {
        return TranslationManager.getTranslation("validation_warning_full");
    }

    @Override
    public boolean isCompatible(Material block) {
        return block == Material.CAULDRON || block.toString().equalsIgnoreCase("WATER_CAULDRON");
    }

    @Override
    public boolean validate(Block b) {
        if (b.getBlockData() instanceof Levelled l) return l.getLevel() >= l.getMaximumLevel();
        return false;
    }

    @Override
    public void execute(Block b) {
        if (b.getBlockData() instanceof Levelled l && l.getLevel() < l.getMaximumLevel()){
            if (l.getLevel() == 0) b.setType(ItemUtils.stringToMaterial("WATER_CAULDRON", Material.CAULDRON));
            l.setLevel(l.getLevel() + 1);
            b.setBlockData(l);
        }
    }
}
