package me.athlaeos.valhallammo.crafting.blockvalidations.implementations;

import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class BlockSurrounded extends Validation {
    private final Material blockRequired;
    private final int amountRequired;
    private final String errorMessage;

    public BlockSurrounded(Material blockRequired, int amountRequired, String errorMessage){
        this.blockRequired = blockRequired;
        this.amountRequired = amountRequired;
        this.errorMessage = errorMessage;
    }

    @Override
    public String id() {
        return "BLOCK_SURROUNDED_BY_" + blockRequired;
    }

    @Override
    public ItemStack icon() {
        String name = StringUtils.toPascalCase(blockRequired.toString().replace("_", " "));
        return new ItemBuilder(blockRequired)
                .name("&eSurrounded by " + name)
                .lore("&fRequires the block to be",
                        "&fsurrounded by " + amountRequired + " " + name).get();
    }

    @Override
    public String activeDescription() {
        String name = StringUtils.toPascalCase(blockRequired.toString().replace("_", " "));
        return "&fMust be surrounded by " + amountRequired + " " + name;
    }

    @Override
    public String validationError() {
        return errorMessage;
    }

    @Override
    public boolean isCompatible(String block) {
        return true;
    }

    @Override
    public boolean validate(Block b) {
        int found = 0;
        for (int x = b.getX() - 1; x <= b.getX() + 1; x++){
            for (int y = b.getY() - 1; y <= b.getY() + 1; y++){
                for (int z = b.getZ() - 1; z <= b.getZ() + 1; z++){
                    Block block = b.getWorld().getBlockAt(x, y, z);
                    if (ItemUtils.isSimilarMaterial(blockRequired, block.getType())) found++;
                    if (found >= amountRequired) return true;
                }
            }
        }
        return false;
    }

    @Override
    public void execute(Block b) {
        // do nothing
    }
}
