package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.utility.BlockStore;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.Map;
import java.util.Set;

public class PlayerPlacedBlockFilter extends LootPredicate {
    @Override
    public String getKey() {
        return "player_placed_block";
    }

    @Override
    public Material getIcon() {
        return Material.BRICKS;
    }

    @Override
    public String getDisplayName() {
        return "&fPlayer Placed";
    }

    @Override
    public String getDescription() {
        return "&fRequires the block to be placed by a player";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the block to " + (isInverted() ? "&cNOT&f " : "") + "be placed by a player";
    }

    @Override
    public LootPredicate createNew() {
        return new PlayerPlacedBlockFilter();
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
                        .get()).map(Set.of()
        );
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
    }

    @Override
    public boolean test(LootContext context) {
        Block b = context.getLocation().getBlock();
        return BlockStore.isPlaced(b) != inverted;
    }

    @Override
    public boolean isCompatibleWithLootType(LootTable.LootType type) {
        return type == LootTable.LootType.BREAK;
    }
}
