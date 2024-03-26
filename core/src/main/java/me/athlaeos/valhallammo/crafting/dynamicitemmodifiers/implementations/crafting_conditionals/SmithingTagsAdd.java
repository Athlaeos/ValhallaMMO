package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.crafting_conditionals;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class SmithingTagsAdd extends DynamicItemModifier {
    private final Collection<Integer> tags = new HashSet<>();
    private Map<Integer, Integer> newTags = new HashMap<>();
    private int tag = 0;
    private int level = 1;

    public SmithingTagsAdd(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        if (newTags == null) newTags = new HashMap<>();
        if (!tags.isEmpty()) {
            tags.forEach(t -> newTags.put(t, 1));
            tags.clear();
        }

        Map<Integer, Integer> existingTags = SmithingItemPropertyManager.getTags(outputItem.getMeta());
        newTags.keySet().forEach(t ->
                SmithingItemPropertyManager.addTag(outputItem.getMeta(), t, existingTags.getOrDefault(t, 0) + newTags.get(t))
        );
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 11)
            tag = Math.max(0, tag + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
        else if (button == 13)
            level = Math.max(1, level + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
        else if (button == 17){
            if (e.isShiftClick()) newTags.clear();
            else newTags.put(tag, level);
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        List<String> tagLore = newTags.isEmpty() ? List.of("&cNone") : newTags.keySet().stream().map(t ->
                "&e" + Objects.requireNonNullElse(
                        String.format("%s %s", SmithingItemPropertyManager.getTagLore(t), StringUtils.toRoman(newTags.getOrDefault(t, 1))),
                        String.format("%d %s &7(invisible)", t, StringUtils.toRoman(newTags.getOrDefault(t, 1)))))
                .collect(Collectors.toList());
        return new Pair<>(11,
                new ItemBuilder(Material.WRITABLE_BOOK)
                        .name("&fTag Selection?")
                        .lore("&fTag &e" + tag + "&7 (" + Objects.requireNonNullElse(
                                SmithingItemPropertyManager.getTagLore(tag),
                                "invisible") + "&7)",
                                "&fClick the button below to add",
                                "&fthis tag to the list.",
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 10",
                                "&fCurrent tags:")
                        .appendLore(tagLore)
                        .get()).map(Set.of(
                new Pair<>(13,
                        new ItemBuilder(Material.STRUCTURE_VOID)
                                .name("&fConfirm Tag")
                                .lore("&fCurrently selected: &e" + tag + " " + StringUtils.toRoman(newTags.getOrDefault(tag, 1)),
                                        "&6Click to add selected tag to",
                                        "&6the list.",
                                        "&6Shift-Click to clear list",
                                        "&fCurrent tags:")
                                .appendLore(tagLore)
                                .get()),
                new Pair<>(17,
                        new ItemBuilder(Material.STRUCTURE_VOID)
                                .name("&fConfirm Tag")
                                .lore("&fCurrently selected: &e" + tag + " " + StringUtils.toRoman(newTags.getOrDefault(tag, 1)),
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
        return "&7Item Tags (ADD)";
    }

    @Override
    public String getDescription() {
        return "&fAdds a number of item tags to the item. Item tags are useful for recipe condition checking";
    }

    @Override
    public String getActiveDescription() {
        if (newTags == null) newTags = new HashMap<>();
        if (!tags.isEmpty()) {
            tags.forEach(t -> newTags.put(t, 1));
            tags.clear();
        }
        return "&fAdds the following tags to the item: /n&e" + (newTags.isEmpty() ? List.of("&cNone") : newTags.keySet().stream().map(t ->
                "&e" + Objects.requireNonNullElse(
                        SmithingItemPropertyManager.getTagLore(t) + " " + StringUtils.toRoman(Math.max(1, newTags.get(t))),
                        t + " " + StringUtils.toRoman(Math.max(1, newTags.get(t)))))
                .collect(Collectors.joining(", ")));
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CRAFTING_CONDITIONALS.id());
    }

    public Map<Integer, Integer> getNewTags() {
        if (newTags == null) newTags = new HashMap<>();
        if (!tags.isEmpty()) {
            tags.forEach(t -> newTags.put(t, 1));
            tags.clear();
        }
        return newTags;
    }

    @Override
    public DynamicItemModifier copy() {
        SmithingTagsAdd m = new SmithingTagsAdd(getName());
        m.getNewTags().putAll(this.newTags);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument is expected: a string joining all numeric tags and levels together (example: 1:1,2:5,3:1,4:2)";
        for (String t : args[0].split(",")){
            String[] split = t.split(":");
            Integer tag = Catch.catchOrElse((() -> Integer.parseInt(split[0])), null);
            if (tag == null) return "One argument is expected: a string joining all numeric tags together (example: 1,2,3,4). One of them was not a number";
            Integer level = Catch.catchOrElse(() -> Integer.parseInt(split[1]), null);
            newTags.put(tag, level == null ? 1 : Math.max(1, level));
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
