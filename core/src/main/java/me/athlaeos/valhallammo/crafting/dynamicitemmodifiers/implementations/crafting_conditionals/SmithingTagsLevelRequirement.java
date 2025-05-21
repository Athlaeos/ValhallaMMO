package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.crafting_conditionals;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class SmithingTagsLevelRequirement extends DynamicItemModifier {
    private Collection<LevelRequirement> tags = new HashSet<>();
    private int tag = 0;
    private int level = 0;
    private boolean lower;

    public SmithingTagsLevelRequirement(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        Map<Integer, Integer> tagsToCheck = SmithingItemPropertyManager.getTags(context.getItem().getMeta());
        for (LevelRequirement tag : tags){
            int tagLevel = tagsToCheck.getOrDefault(tag.tag, 0);
            if (tagLevel <= 0 && !tag.lower) {
                String message = SmithingItemPropertyManager.getTagRequiredErrors().get(tag.tag);
                failedRecipe(context.getItem(), Objects.requireNonNullElseGet(message, () -> TranslationManager.getTranslation("modifier_warning_required_smithing_tag")));
                break;
            } else {
                if ((tag.lower && tagLevel > tag.level) || (!tag.lower && tagLevel < tag.level)) {
                    String message = SmithingItemPropertyManager.getTagRequiredErrors().get(tag.tag);
                    failedRecipe(context.getItem(), Objects.requireNonNullElseGet(message, () -> TranslationManager.getTranslation("modifier_warning_required_smithing_tag").replace("%level%", String.valueOf(tag.level))));
                    break;
                }
            }
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 11)
            tag = Math.max(0, tag + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
        else if (button == 12)
            level = Math.max(0, level + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
        else if (button == 13)
            lower = !lower;
        else if (button == 17){
            if (e.isShiftClick()) tags.clear();
            else tags.add(new LevelRequirement(tag, level, lower));
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        List<String> tagLore = tags.isEmpty() ? List.of("&cNone") : tags.stream().map(t -> {
            String lore = SmithingItemPropertyManager.getTagLore(t.tag);
            if (lore == null) lore = String.valueOf(t.tag);
            return "&e" + lore + (t.lower ? " &cLower than " : " &aHigher than ") + StringUtils.toRoman(t.level);
        }).collect(Collectors.toList());
        return new Pair<>(11,
                new ItemBuilder(Material.WRITABLE_BOOK)
                        .name("&fTag Selection?")
                        .lore("&fTag &e" + tag + "&7 (" + Objects.requireNonNullElse(
                                SmithingItemPropertyManager.getTagLore(tag),
                                "invisible") + "&7)",
                                "&eMust be " + (lower ? "lower than " : "higher than ") + StringUtils.toRoman(level),
                                "&fRepresents the tag the item should have",
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 10",
                                "&fCurrent tags:")
                        .appendLore(tagLore)
                        .get()).map(Set.of(
                                new Pair<>(12,
                                        new ItemBuilder(Material.WRITABLE_BOOK)
                                                .name("&fTag level?")
                                                .lore("&fTag &e" + tag + "&7 (" + Objects.requireNonNullElse(
                                                                SmithingItemPropertyManager.getTagLore(tag),
                                                                "invisible") + "&7)",
                                                        "&eMust be " + (lower ? "lower than " : "higher than ") + StringUtils.toRoman(level),
                                                        "&fRepresents the level the tag should have",
                                                        "&6Click to add/subtract 1",
                                                        "&6Shift-Click to add/subtract 10",
                                                        "&fCurrent tags:")
                                                .appendLore(tagLore)
                                                .get()),

                new Pair<>(13,
                        new ItemBuilder(Material.WRITABLE_BOOK)
                                .name("&fLower or higher?")
                                .lore("&fTag &e" + tag + "&7 (" + Objects.requireNonNullElse(
                                                SmithingItemPropertyManager.getTagLore(tag),
                                                "invisible") + "&7)",
                                        "&eMust be " + (lower ? "lower than " : "higher than ") + StringUtils.toRoman(level),
                                        "&fRepresents if the level should be",
                                        "&flower or higher than what the item has",
                                        "&6Click to add/subtract 1",
                                        "&6Shift-Click to add/subtract 10",
                                        "&fCurrent tags:")
                                .appendLore(tagLore)
                                .get()),
                new Pair<>(17,
                        new ItemBuilder(Material.STRUCTURE_VOID)
                                .name("&fConfirm Tag")
                                .lore("&fCurrently selected: &e" + tag,
                                        "&eMust be " + (lower ? "lower than " : "higher than ") + StringUtils.toRoman(level),
                                        "&cIf the item lacks any one of these",
                                        "&ctags, or fails the level requirement, ",
                                        "&crecipe is cancelled and an ",
                                        "&cerror message is announced to",
                                        "&cthe player.",
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
        return new ItemBuilder(Material.MAP).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Item Tags (ALL MATCH + LEVEL REQUIREMENT)";
    }

    @Override
    public String getDescription() {
        return "&fIf the item lacks any of the given tags, or the item's tags do not match the level requirement, recipe is cancelled with an error message forwarded to the player. Error messages defined in skills/smithing.yml";
    }

    @Override
    public String getActiveDescription() {
        return "&fChecks if the item lacks any of the given tags: /n &e" + (tags.isEmpty() ? List.of("&cNone") : tags.stream().map(t -> {
            String lore = SmithingItemPropertyManager.getTagLore(t.tag);
            if (lore == null) lore = String.valueOf(t.tag);
            return "&e" + lore + (t.lower ? " &cLower than " : " &aHigher than ") + StringUtils.toRoman(t.level);
        }).collect(Collectors.joining("/n")) + "/n&fRecipe is cancelled if item doesn't have tags matching these level requirements");
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CRAFTING_CONDITIONALS.id());
    }

    public Collection<LevelRequirement> getTags() {
        return tags;
    }

    @Override
    public DynamicItemModifier copy() {
        SmithingTagsLevelRequirement m = new SmithingTagsLevelRequirement(getName());
        m.getTags().addAll(this.tags);
        m.setPriority(this.getPriority());
        return m;
    }

    public void setTags(Collection<LevelRequirement> tags) {
        this.tags = tags;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        return "Honestly, this modifier is too complex to even want to use manually";
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("nuh_uh");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }

    public static class LevelRequirement{
        private int tag;
        private int level;
        private boolean lower;
        public LevelRequirement(int tag, int level, boolean lower){
            this.tag = tag;
            this.level = level;
            this.lower = lower;
        }

        public int getTag() { return tag; }
        public int getLevel() { return level; }
        public boolean isLower() { return lower; }

        public void setLevel(int level) { this.level = level; }
        public void setLower(boolean lower) { this.lower = lower; }
        public void setTag(int tag) { this.tag = tag; }
    }
}
