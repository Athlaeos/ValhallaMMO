package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.dom.CustomDamageType;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.listeners.EntityDamagedListener;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.*;

public class DamageTypeDeathCauseFilter extends LootPredicate {
    private final Collection<String> damageTypes = new HashSet<>();
    private String damageType = null;

    @Override
    public String getKey() {
        return "death_cause";
    }

    @Override
    public Material getIcon() {
        return Material.GOLDEN_SWORD;
    }

    @Override
    public String getDisplayName() {
        return "&fDamage Type Cause of Death";
    }

    @Override
    public String getDescription() {
        return "&fRequires the entity to have died from a type of damage. ";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the entity to " + (isInverted() ? "&cNOT&f " : "") + "have died from &e" + String.join(", ", damageTypes);
    }

    @Override
    public LootPredicate createNew() {
        return new DamageTypeDeathCauseFilter();
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        List<String> damageTypes = new ArrayList<>(CustomDamageType.getRegisteredTypes().keySet());
        String info;
        if (damageType == null) damageType = damageTypes.get(0);
        int currentIndex = damageTypes.indexOf(damageType);
        String before = currentIndex <= 0 ? "" : damageTypes.get(currentIndex - 1) + " > &e";
        String after = currentIndex + 1 >= damageTypes.size() ? "" : "&f > " + damageTypes.get(currentIndex + 1);
        info = "&f" + before + damageType + after;

        return new Pair<>(2,
                new ItemBuilder(Material.TNT)
                        .name("&eInvert Condition")
                        .lore(inverted ? "&cCondition is inverted" : "&aCondition not inverted",
                                "&fInverted conditions must &cnot &fpass",
                                "&fthis condition. ",
                                "&6Click to toggle")
                        .get()).map(Set.of(
                                new Pair<>(12,
                                        new ItemBuilder(Material.GOLDEN_SWORD)
                                                .name("&eSelect Damage Type")
                                                .lore(info,
                                                        "&6Click to cycle",
                                                        "&6Shift-Click to add to selection",
                                                        "&cMiddle-Click to clear selection",
                                                        "&fCurrently: ")
                                                .appendLore(damageTypes.isEmpty() ? List.of("&cNone, condition always passes") : damageTypes.stream().map(b -> "&f>" + b).toList())
                                                .get()))
        );
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
        else if (button == 12){
            if (e.isShiftClick()) damageTypes.add(damageType);
            else if (e.getClick() == ClickType.MIDDLE) damageTypes.clear();
            else {
                List<String> damageTypes = new ArrayList<>(CustomDamageType.getRegisteredTypes().keySet());
                if (damageTypes.isEmpty()) return;
                int currentIndex = damageType == null ? -1 : damageTypes.indexOf(damageType);
                currentIndex = Math.max(0, Math.min(damageTypes.size() - 1, currentIndex + (e.isLeftClick() ? 1 : -1)));
                damageType = damageTypes.get(currentIndex);
            }
        }
    }

    @Override
    public boolean test(LootContext context) {
        if (!(context.getLootedEntity() instanceof LivingEntity l)) return inverted;
        String lastDamageCause = EntityDamagedListener.getLastDamageCause(l);
        CustomDamageType customType = lastDamageCause == null ? null : CustomDamageType.getCustomType(lastDamageCause);
        if (damageTypes.isEmpty() || customType == null) return inverted;
        return damageTypes.contains(customType.getType()) != inverted;
    }

    @Override
    public boolean isCompatibleWithLootType(LootTable.LootType type) {
        return type == LootTable.LootType.KILL;
    }
}
