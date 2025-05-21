package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_conditionals;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.AlchemyItemPropertyManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class AlchemyTagsRemove extends DynamicItemModifier {
    private final Collection<Integer> tags = new HashSet<>();
    private int tag = 0;

    public AlchemyTagsRemove(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        AlchemyItemPropertyManager.removeTag(context.getItem().getMeta(), tags.toArray(new Integer[0]));
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12)
            tag = Math.max(0, tag + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
        else if (button == 17){
            if (e.isShiftClick()) tags.clear();
            else tags.add(tag);
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        List<String> tagLore = tags.isEmpty() ? List.of("&cNone") : tags.stream().map(t ->
                "&e" + Objects.requireNonNullElse(
                        AlchemyItemPropertyManager.getTagLore(t),
                        String.valueOf(t)))
                .collect(Collectors.toList());
        return new Pair<>(12,
                new ItemBuilder(Material.WRITABLE_BOOK)
                        .name("&fTag Selection?")
                        .lore("&fTag &e" + tag + "&7 (" + Objects.requireNonNullElse(
                                AlchemyItemPropertyManager.getTagLore(tag),
                                "invisible") + "&7)",
                                "&fClick the button below to add",
                                "&fthis tag to the list.",
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 10",
                                "&fCurrent tags:")
                        .appendLore(tagLore)
                        .get()).map(Set.of(
                new Pair<>(17,
                        new ItemBuilder(Material.STRUCTURE_VOID)
                                .name("&fConfirm Tag")
                                .lore("&fCurrently selected: &e" + tag,
                                        "&6Click to add selected tag to",
                                        "&6the list.",
                                        "&6Shift-Click to clear list",
                                        "&fCurrent tags:")
                                .appendLore(tagLore)
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.PAPER).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Potion Tags (REMOVE)";
    }

    @Override
    public String getDescription() {
        return "&fRemoves a number of item tags from the item. Item tags are useful for recipe condition checking";
    }

    @Override
    public String getActiveDescription() {
        return "&fRemoves the following tags from the item: /n&e" + (tags.isEmpty() ? List.of("&cNone") : tags.stream().map(t ->
                "&e" + Objects.requireNonNullElse(
                        AlchemyItemPropertyManager.getTagLore(t),
                        String.valueOf(t)))
                .collect(Collectors.joining(", ")));
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.POTION_CONDITIONALS.id());
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public Collection<Integer> getTags() {
        return tags;
    }

    @Override
    public DynamicItemModifier copy() {
        AlchemyTagsRemove m = new AlchemyTagsRemove(getName());
        m.setTag(this.tag);
        m.getTags().addAll(this.tags);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument is expected: a string joining all numeric tags together (example: 1,2,3,4)";
        for (String t : args[0].split(",")){
            try {
                int tag = Integer.parseInt(t);
                tags.add(tag);
            } catch (NumberFormatException ignored){
                return "One argument is expected: a string joining all numeric tags together (example: 1,2,3,4). One of them was not a number";
            }
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<tags_separated_by_commas>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
