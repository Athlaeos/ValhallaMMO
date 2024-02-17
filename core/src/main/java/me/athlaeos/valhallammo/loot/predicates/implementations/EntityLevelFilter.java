package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.entities.MonsterScalingManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.Map;
import java.util.Set;

public class EntityLevelFilter extends LootPredicate {
    private int from = 0;

    @Override
    public String getKey() {
        return "entity_level";
    }

    @Override
    public Material getIcon() {
        return Material.GOLDEN_SWORD;
    }

    @Override
    public String getDisplayName() {
        return "&fEntity Level";
    }

    @Override
    public String getDescription() {
        return "&fRequires the entity to have a level minimum";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires entity to " + (isInverted() ? "&cNOT&f " : "") + "be above level&e" + from;
    }

    @Override
    public LootPredicate createNew() {
        return new EntityLevelFilter();
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
                                        new ItemBuilder(Material.LIGHT)
                                                .name("&eSelect Level Requirement")
                                                .lore("&eIs currently " + from,
                                                        "&fLevel must " + (isInverted() ? "&cNOT&f " : "") + "be above " + from,
                                                        "&6Click to add/subtract 1",
                                                        "&6Shift-Click to add/subtract 10")
                                                .get())));
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
        else if (button == 12) from += (e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1);
    }

    @Override
    public boolean test(LootContext context) {
        if (context.getLootedEntity() == null || !(context.getLootedEntity() instanceof LivingEntity e)) return inverted;
        return MonsterScalingManager.getLevel(e) >= from != inverted;
    }

    @Override
    public boolean isCompatibleWithLootType(LootTable.LootType type) {
        return true;
    }
}
