package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;

import java.util.*;
import java.util.stream.Collectors;

public class MinedWithTaggedToolFilter extends LootPredicate {
    private final Collection<Integer> tags = new HashSet<>();
    private int tag = 0;

    @Override
    public String getKey() {
        return "mined_with_tagged_tool";
    }

    @Override
    public Material getIcon() {
        return Material.NAME_TAG;
    }

    @Override
    public String getDisplayName() {
        return "&fMined with Tagged Tool";
    }

    @Override
    public String getDescription() {
        return "&fRequires the block to be mined with a tool having all of the specified smithing tags";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the block to " + (isInverted() ? "&cNOT&f " : "") + "be mined with a tool having all of &e" + tags.stream().map(t -> t + Objects.requireNonNullElse(
                "(" + SmithingItemPropertyManager.getTagLore(t) + ")",
                "")).collect(Collectors.joining(", "));
    }

    @Override
    public LootPredicate createNew() {
        return new MinedWithTaggedToolFilter();
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
                                                .name("&eSelect Entity Type")
                                                .lore("&f" + tag,
                                                        "&6Click to cycle",
                                                        "&6Shift-Click to add to selection",
                                                        "&cMiddle-Click to clear selection",
                                                        "&fCurrently: ")
                                                .appendLore(tags.isEmpty() ? List.of("&cNone, condition always passes") : tags.stream().map(t -> "&f>" + t + Objects.requireNonNullElse(
                                                        "(" + SmithingItemPropertyManager.getTagLore(t) + ")",
                                                        "")).toList())
                                                .get()))
        );
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
        else if (button == 12){
            if (e.isShiftClick()) tags.add(tag);
            else if (e.getClick() == ClickType.MIDDLE) tags.clear();
            else tag = Math.max(0, tag + (e.isLeftClick() ? 1 : -1));
        }
    }

    @Override
    public boolean test(LootContext context) {
        if (tags.isEmpty()) return inverted;
        ItemStack tool = null;
        if (context.getKiller() instanceof Player p){
            tool = p.getInventory().getItemInMainHand();
        } else if (context.getLootedEntity() instanceof Player p){
            tool = p.getInventory().getItemInMainHand();
        }
        if (ItemUtils.isEmpty(tool)) return inverted;
        ItemMeta meta = ItemUtils.getItemMeta(tool);
        if (meta == null) return inverted;

        return SmithingItemPropertyManager.getTags(meta).keySet().containsAll(tags) != inverted;
    }

    @Override
    public boolean isCompatibleWithLootType(LootTable.LootType type) {
        return type == LootTable.LootType.BREAK || type == LootTable.LootType.KILL;
    }
}
