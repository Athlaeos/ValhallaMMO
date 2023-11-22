package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.MiningSpeed;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MinedWithPreferredToolFilter extends LootPredicate {
    @Override
    public String getKey() {
        return "mined_with_preferred_tool";
    }

    @Override
    public Material getIcon() {
        return Material.STONE_PICKAXE;
    }

    @Override
    public String getDisplayName() {
        return "&fMined with Preferred Tool";
    }

    @Override
    public String getDescription() {
        return ValhallaMMO.getNms() == null ? "&cFeature disabled due to version incompatibility" : "&fRequires the block to be mined with its preferred tool";
    }

    @Override
    public String getActiveDescription() {
        return ValhallaMMO.getNms() == null ? "&cFeature disabled due to version incompatibility" : "&fRequires the block to " + (isInverted() ? "&cNOT&f " : "") + "be mined with its preferred tool";
    }

    @Override
    public LootPredicate createNew() {
        return new MinedWithPreferredToolFilter();
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        if (ValhallaMMO.getNms() == null) return new HashMap<>();
        return new Pair<>(2,
                new ItemBuilder(Material.TNT)
                        .name("&eInvert Condition")
                        .lore(inverted ? "&cCondition is inverted" : "&aCondition not inverted",
                                "&fInverted conditions must &cnot &fpass",
                                "&fthis condition. ",
                                "&6Click to toggle")
                        .get()).map(new HashSet<>());
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (ValhallaMMO.getNms() == null) return;
        if (button == 2) inverted = !inverted;
    }

    @Override
    public boolean test(LootContext context) {
        if (!(context.getLootedEntity() instanceof Player p)) return inverted;
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (ItemUtils.isEmpty(tool)) return inverted;
        ItemMeta meta = ItemUtils.getItemMeta(tool);
        if (meta == null) return inverted;
        Block b = context.getLocation().getBlock();
        Map<Material, Material> hardnessTranslations = MiningSpeed.getHardnessTranslations(meta);
        // changing tool power to be equal to that if the tool mined a different type of block
        float toolStrength = !hardnessTranslations.isEmpty() && hardnessTranslations.containsKey(b.getType()) ?
                ValhallaMMO.getNms().toolPower(tool, hardnessTranslations.get(b.getType())) :
                ValhallaMMO.getNms().toolPower(tool, b);
        return toolStrength > 1 != inverted;
    }

    @Override
    public boolean isCompatibleWithLootType(LootTable.LootType type) {
        return type == LootTable.LootType.BREAK;
    }
}
