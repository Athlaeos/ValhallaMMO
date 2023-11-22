package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.entities.EntityClassification;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.listeners.LootListener;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.*;
import java.util.stream.Collectors;

public class ContextEntityKillerFilter extends LootPredicate {
    private static final List<EntityType> types = new ArrayList<>(EntityClassification.getEntityTypes((c) -> c != EntityClassification.UNALIVE));
    static { // put any more entities here that might be responsible for killing
        types.add(EntityType.FALLING_BLOCK); // anvil
    }
    private final Collection<EntityType> entities = new HashSet<>();
    private EntityType entity = EntityType.PLAYER;

    public ContextEntityKillerFilter(){
        types.sort(Comparator.comparing(EntityType::toString));
    }

    @Override
    public String getKey() {
        return "context_entity_killer";
    }

    @Override
    public Material getIcon() {
        return Material.IRON_SWORD;
    }

    @Override
    public String getDisplayName() {
        return "&fEntity Killer";
    }

    @Override
    public String getDescription() {
        return "&fRequires responsible killer to be one of the given entity types. If none selected, the entity must be killed by nothing";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the responsible killer to " + (isInverted() ? "&cNOT&f " : "") + "be one of &e" + entities.stream().map(EntityType::toString).collect(Collectors.joining(", "));
    }

    @Override
    public LootPredicate createNew() {
        return new ContextEntityKillerFilter();
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        String info;
        if (types.isEmpty()) info = "&cNo entities found(???)";
        else {
            if (entity == null) entity = types.get(0);
            int currentIndex = types.indexOf(entity);
            String before = currentIndex <= 0 ? "" : types.get(currentIndex - 1) + " > &e";
            String after = currentIndex + 1 >= types.size() ? "" : "&f > " + types.get(currentIndex + 1);
            info = "&f" + before + entity + after;
        }
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
                                                .name("&eSelect Entity Type")
                                                .lore(info,
                                                        "&6Click to cycle",
                                                        "&6Shift-Click to add to selection",
                                                        "&cMiddle-Click to clear selection",
                                                        "&fCurrently: ")
                                                .appendLore(types.isEmpty() ? List.of("&cNone, condition always passes") : types.stream().map(b -> "&f>" + b).toList())
                                                .get()))
        );
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
        else if (button == 12){
            if (e.isShiftClick()) entities.add(entity);
            else if (e.getClick() == ClickType.MIDDLE) entities.clear();
            else {
                if (types.isEmpty()) return;
                int currentIndex = entity == null ? -1 : types.indexOf(entity);
                currentIndex = Math.max(0, Math.min(types.size() - 1, currentIndex + (e.isLeftClick() ? 1 : -1)));
                entity = types.get(currentIndex);
            }
        }
    }

    @Override
    public boolean test(LootContext context) {
        if (context.getLootedEntity() == null) return inverted;
        Entity killer = LootListener.getRealKiller(context.getLootedEntity());
        if (killer == null) return inverted;
        return entities.contains(killer.getType()) != inverted;
    }

    @Override
    public boolean isCompatibleWithLootType(LootTable.LootType type) {
        return type == LootTable.LootType.KILL;
    }
}
