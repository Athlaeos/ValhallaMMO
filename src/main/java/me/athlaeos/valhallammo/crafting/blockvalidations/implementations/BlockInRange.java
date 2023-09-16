package me.athlaeos.valhallammo.crafting.blockvalidations.implementations;

import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class BlockInRange extends Validation {
    private final Material blockRequired;
    private final Material icon;
    private final int radius;
    private final int heightDifference;
    private final String errorMessage;

    public BlockInRange(Material blockRequired, int radius, int heightDifference, String errorMessage){
        this.blockRequired = blockRequired;
        this.icon = blockRequired;
        this.radius = radius;
        this.heightDifference = heightDifference;
        this.errorMessage = errorMessage;
    }

    public BlockInRange(Material blockRequired, Material icon, int radius, int heightDifference, String errorMessage){
        this.blockRequired = blockRequired;
        this.icon = icon;
        this.radius = radius;
        this.heightDifference = heightDifference;
        this.errorMessage = errorMessage;
    }

    @Override
    public String id() {
        return "BLOCK_NEARBY_" + blockRequired;
    }

    @Override
    public ItemStack icon() {
        String name = StringUtils.toPascalCase(blockRequired.toString().replace("_", " "));
        return new ItemBuilder(icon)
                .name("&e" + name + " within " + radius + " blocks")
                .lore("&fRequires " + name + " to be within",
                        "&f" + radius + " blocks to the block").get();
    }

    @Override
    public String activeDescription() {
        String name = StringUtils.toPascalCase(blockRequired.toString().replace("_", " "));
        return "&fMust have " + name + " within " + radius + " blocks";
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
        for (int x = b.getX() - radius; x <= b.getX() + radius; x++){
            for (int y = b.getY() - heightDifference; y <= b.getY() + heightDifference; y++){
                for (int z = b.getZ() - radius; z <= b.getZ() + radius; z++){
                    Block block = b.getWorld().getBlockAt(x, y, z);
                    if (ItemUtils.isSimilarMaterial(blockRequired, block.getType())) return true;
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
