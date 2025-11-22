package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.utility.BlockStore;
import me.athlaeos.valhallammo.utility.BlockUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.Map;
import java.util.Set;

public class BlockRewardableFilter extends LootPredicate {
    @Override
    public String getKey() {
        return "block_rewardable";
    }

    @Override
    public Material getIcon() {
        return Material.CARROT;
    }

    @Override
    public String getDisplayName() {
        return "&fBlock Rewardable";
    }

    @Override
    public String getDescription() {
        return "&fRequires the block to be pass certain conditions which would naturally allow it to drop 'rewards'. Essentially, regular blocks must not be placed by a player, and crops must be fully grown";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the block to " + (isInverted() ? "&cNOT&f " : "") + "be eligible for rewards";
    }

    @Override
    public LootPredicate createNew() {
        return new BlockRewardableFilter();
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
        return BlockUtils.canReward(b) != inverted;
    }

    @Override
    public boolean isCompatibleWithLootType(LootTable.LootType type) {
        return type == LootTable.LootType.BREAK;
    }
}
