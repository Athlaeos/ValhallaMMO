package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.MathUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.*;
import java.util.stream.Collectors;

public class BlockSurroundedMaterialFilter extends LootPredicate {
    private static final List<AreaType> areaTypes = List.of(AreaType.values());
    private final Collection<Material> materials = new HashSet<>();
    private AreaType areaType = AreaType.TOUCHING;
    private int quantity = 5;

    @Override
    public String getKey() {
        return "surrounded_by_blocks";
    }

    @Override
    public Material getIcon() {
        return Material.BRICKS;
    }

    @Override
    public String getDisplayName() {
        return "&fSurrounded by Blocks";
    }

    @Override
    public String getDescription() {
        return "&fRequires the block to be surrounded by a minimum of the given blocks";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the block to " + (isInverted() ? "&cNOT&f " : "") + "be surrounded by at least " + quantity + " of &e" + materials.stream().map(b -> b.toString().toLowerCase(java.util.Locale.US)).collect(Collectors.joining(", "));
    }

    @Override
    public LootPredicate createNew() {
        return new BlockSurroundedMaterialFilter();
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(2,
                new ItemBuilder(Material.TNT)
                        .name("&eInvert Condition")
                        .lore(inverted ? "&cCondition is inverted" : "&aCondition not inverted",
                                "&fInverted conditions must &cnot &fpass",
                                "&fthis condition. ",
                                "&6Click to toggle")
                        .get()).map(Set.of(
                                new Pair<>(12,
                                        new ItemBuilder(Material.GRASS_BLOCK)
                                                .name("&eSelect Block Material")
                                                .lore("&6Click with item in cursor to",
                                                        "&6add required block type",
                                                        "&cShift-Click to clear list",
                                                        "&fCurrently: ")
                                                .appendLore(materials.isEmpty() ? List.of("&cNone, condition always passes") : materials.stream().map(b -> "&f>" + b.toString().toLowerCase(java.util.Locale.US)).toList())
                                                .get()),
                new Pair<>(11,
                        new ItemBuilder(Material.STRUCTURE_VOID)
                                .name("&eSelect Area Type")
                                .lore("&fDetermines the scan area",
                                        switch (areaType){
                                            case TOUCHING -> "&eDirectly adjacent (6 blocks)";
                                            case CUBE3X3 -> "&eWithin 3x3 cube (26 blocks)";
                                            case CUBE5X5 -> "&eWithin 5x5 cube (124 blocks)";
                                        },
                                        "&6Click to cycle")
                                .get()),
                new Pair<>(13,
                        new ItemBuilder(Material.GRASS_BLOCK)
                                .name("&eSelect Block Count")
                                .lore("&f" + quantity,
                                        "&fWithin the given area around",
                                        "&fthe block, " + quantity + " blocks",
                                        "&fmust match or be similar to one of",
                                        "&fthe given materials",
                                        "&6Click to add/subtract 1",
                                        "&cShift-Click to add/subtract 10")
                                .get()))
        );
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
        else if (button == 11){
            int currentIndex = areaType == null ? -1 : areaTypes.indexOf(areaType);
            currentIndex = Math.max(0, Math.min(areaTypes.size() - 1, currentIndex + (e.isLeftClick() ? 1 : -1)));
            areaType = areaTypes.get(currentIndex);
        } else if (button == 12){
            if (ItemUtils.isEmpty(e.getCursor()) && !e.isShiftClick()) materials.add(Material.AIR);
            else if (!ItemUtils.isEmpty(e.getCursor()) && e.getCursor().getType().isBlock() && !e.isShiftClick()) materials.add(ItemUtils.getBaseMaterial(e.getCursor().getType()));
            else if (e.isShiftClick()) materials.clear();
        } else if (button == 13) quantity = Math.max(0, quantity + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
    }

    @Override
    public boolean test(LootContext context) {
        if (materials.isEmpty()) return inverted;
        Block b = context.getLocation().getBlock();
        int found = 0;
        for (Block surrounding : areaType.getSurroundingBlocks(b)){
            if (materials.contains(Material.AIR) && surrounding.getType().isAir()) found++;
            else if (materials.stream().anyMatch(m -> ItemUtils.isSimilarMaterial(m, surrounding.getType()))) found++;
        }
        return found >= quantity != this.inverted;
    }

    private enum AreaType{
        TOUCHING(new int[]{ 1, 0, 0}, new int[]{ -1, 0, 0}, new int[]{ 0, 1, 0}, new int[]{ 0, -1, 0}, new int[]{ 0, 0, 1}, new int[]{ 0, 0, -1}),
        CUBE3X3(MathUtils.getOffsetsBetweenPoints(new int[]{-1, -1, -1}, new int[]{1, 1, 1})),
        CUBE5X5(MathUtils.getOffsetsBetweenPoints(new int[]{-2, -2, -2}, new int[]{2, 2, 2}));
        private final int[][] offsets;

        AreaType(int[]... offsets){
            this.offsets = offsets;
        }

        public Collection<Block> getSurroundingBlocks(Block center){
            Collection<Block> blocks = new HashSet<>();
            for (int[] offset : offsets){
                blocks.add(center.getLocation().add(offset[0], offset[1], offset[2]).getBlock());
            }
            blocks.remove(center);
            return blocks;
        }
    }
}
