package me.athlaeos.valhallammo.crafting.blockvalidations.implementations;

import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Set;

public class BlockSurroundedTiered extends Validation {
    private final Material icon;
    private final Collection<Material> betterMaterials;
    private final int amountRequired;
    private final String errorMessage;

    /**
     * Creates a validation requiring the block to be surrounded by amountRequired blocks. If not, the errorMessage is displayed to the player.
     * blockRequired must contain at least 1 material, representing the icon of the validation. The following materials are considered materials
     * also valid for this validation, or in the traditional use case "better" materials that can also be used for the construction.
     * @param amountRequired the amount of valid blocks required
     * @param errorMessage the error message if not enough of said valid blocks are present
     * @param blockRequired the block(s) required for the construction. Must contain at least 1 entry
     */
    public BlockSurroundedTiered(int amountRequired, String errorMessage, Material... blockRequired){
        this.icon = blockRequired[0];
        this.betterMaterials = Set.of(blockRequired);
        this.amountRequired = amountRequired;
        this.errorMessage = errorMessage;
    }

    @Override
    public String id() {
        return "BLOCK_SURROUNDED_BY_" + icon + "_OR_BETTER";
    }

    @Override
    public ItemStack icon() {
        String name = StringUtils.toPascalCase(icon.toString().replace("_", " "));
        return new ItemBuilder(icon)
                .name("&eSurrounded by " + name + " or better")
                .lore("&fRequires the block to be",
                        "&fsurrounded by " + amountRequired + " " + name).get();
    }

    @Override
    public String activeDescription() {
        String name = StringUtils.toPascalCase(icon.toString().replace("_", " "));
        return "&fMust be surrounded by " + amountRequired + " " + name + " or better";
    }

    @Override
    public String validationError() {
        return errorMessage;
    }

    @Override
    public boolean isCompatible(Material block) {
        return true;
    }

    @Override
    public boolean validate(Block b) {
        int found = 0;
        for (int x = b.getX() - 1; x <= b.getX() + 1; x++){
            for (int y = b.getY() - 1; y <= b.getY() + 1; y++){
                for (int z = b.getZ() - 1; z <= b.getZ() + 1; z++){
                    Block block = b.getWorld().getBlockAt(x, y, z);
                    if (betterMaterials.stream().anyMatch(m -> ItemUtils.isSimilarMaterial(m, block.getType()))) found++;
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
