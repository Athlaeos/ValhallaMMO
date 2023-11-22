package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.HashSet;
import java.util.Map;

public class MinedWithFireAspectFilter extends LootPredicate {
    @Override
    public String getKey() {
        return "mined_with_fire_aspect";
    }

    @Override
    public Material getIcon() {
        return Material.FIRE_CHARGE;
    }

    @Override
    public String getDisplayName() {
        return "&fMined with Fire Aspect";
    }

    @Override
    public String getDescription() {
        return "&fRequires the block to be mined with Fire Aspect";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the block to " + (isInverted() ? "&cNOT&f " : "") + "be mined with Fire Aspect";
    }

    @Override
    public LootPredicate createNew() {
        return new MinedWithFireAspectFilter();
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
                        .get()).map(new HashSet<>());
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
    }

    @Override
    public boolean test(LootContext context) {
        if (!(context.getLootedEntity() instanceof Player p) || ItemUtils.isEmpty(p.getInventory().getItemInMainHand())) return inverted;
        return p.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.FIRE_ASPECT) > 0 != inverted;
    }

    @Override
    public boolean isCompatibleWithLootType(LootTable.LootType type) {
        return type == LootTable.LootType.BREAK;
    }
}
