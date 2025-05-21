package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.ItemSkillRequirements;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SkillRequirementAdd extends DynamicItemModifier {
    private final String skill;
    private int levelRequired;
    private boolean shouldFulfillAll = false;

    public SkillRequirementAdd(String name, String skill) {
        super(name);
        this.skill = skill;
    }

    @Override
    public void processItem(ModifierContext context) {
        if (!context.shouldExecuteUsageMechanics()) return;
        Skill s = SkillRegistry.getSkill(skill);
        ItemSkillRequirements.addSkillRequirement(context.getItem().getMeta(), s, levelRequired);
        ItemSkillRequirements.setAnySkillMatch(context.getItem().getMeta(), !shouldFulfillAll);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        Skill s = SkillRegistry.getSkill(skill);
        if (button == 11)
            levelRequired = Math.max(0, Math.min(s.getMaxLevel(), levelRequired + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1))));
        else if (button == 13)
            shouldFulfillAll = !shouldFulfillAll;
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        Skill s = SkillRegistry.getSkill(skill);
        if (s == null) return new HashMap<>();
        return new Pair<>(11,
                new ItemBuilder(s.getIcon())
                        .name("&eWhat minimum " + s.getDisplayName() + " &elevel should player be?")
                        .lore("&fSet to &e" + levelRequired,
                                "&fSkill level requirements don't",
                                "&foutright prevent items from being",
                                "&fused or equipped, but their stats",
                                "&fwill be much lower and the item",
                                "&fbreaks much faster.",
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 10")
                        .get()).map(Set.of(
                new Pair<>(13,
                        new ItemBuilder(s.getIcon())
                                .name("&eShould player meet ALL required skills? ")
                                .lore("&fSet to &e" + (shouldFulfillAll ? "Yes" : "No"),
                                        "&fIf enabled, the player needs to meet",
                                        "&fall of the skill requirements placed",
                                        "&fon the item. If disabled, the player",
                                        "&fonly needs to meet one of the skill",
                                        "&frequirements.",
                                        "&6Click to toggle")
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        Skill s = SkillRegistry.getSkill(skill);
        if (s == null) return null;
        return new ItemBuilder(s.getIcon()).get();
    }

    @Override
    public String getDisplayName() {
        Skill s = SkillRegistry.getSkill(skill);
        if (s == null) return "";
        return "&eRequire Minimum " + s.getDisplayName() + " &eLevel";
    }

    @Override
    public String getDescription() {
        Skill s = SkillRegistry.getSkill(skill);
        if (s == null) return "";
        return "&fRequires the player to reach a minimum " + s.getDisplayName() + " &elevel to be able to utilize the item well.";
    }

    @Override
    public String getActiveDescription() {
        Skill s = SkillRegistry.getSkill(skill);
        if (s == null) return "";
        return String.format("&fRequires the player to reach level %d in " + s.getDisplayName() + " &eto be able to utilize the item well", levelRequired);
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setLevelRequired(int levelRequired) {
        this.levelRequired = levelRequired;
    }

    public void setShouldFulfillAll(boolean shouldFulfillAll) {
        this.shouldFulfillAll = shouldFulfillAll;
    }

    @Override
    public DynamicItemModifier copy() {
        SkillRequirementAdd m = new SkillRequirementAdd(getName(), skill);
        m.setLevelRequired(this.levelRequired);
        m.setShouldFulfillAll(this.shouldFulfillAll);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 2) return "Two arguments are expected: an integer and a yes/no answer";
        try {
            levelRequired = Integer.parseInt(args[0]);
            shouldFulfillAll = args[1].equalsIgnoreCase("yes");
        } catch (NumberFormatException ignored){
            return "Two arguments are expected: an integer and a yes/no answer. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<amount>");
        if (currentArg == 1) return List.of("<should_meet_all_requirements>", "yes", "no");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 2;
    }
}
