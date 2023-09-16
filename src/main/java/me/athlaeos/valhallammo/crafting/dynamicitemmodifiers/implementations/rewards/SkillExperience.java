package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SkillExperience extends DynamicItemModifier {
    private final String skill;
    private double amount = 300;

    public SkillExperience(String name, String skill) {
        super(name);
        this.skill = skill;
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        if (!use) return;
        Skill s = SkillRegistry.getSkill(skill);
        if (s == null) return;
        s.addEXP(crafter, amount * timesExecuted, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 11)
            amount = amount + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1 : 0.1));
        else if (button == 12)
            amount = amount + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 100 : 10));
        else if (button == 13)
            amount = amount + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10000 : 1000));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        Skill s = SkillRegistry.getSkill(skill);
        if (s == null) return new HashMap<>();
        return new Pair<>(11,
                new ItemBuilder(s.getIcon())
                        .name("&eHow much " + s.getDisplayName() + " &eEXP should be given?")
                        .lore("&fSet to &e" + amount,
                                "&6Click to add/subtract 0.1",
                                "&6Shift-Click to add/subtract 1")
                        .get()).map(Set.of(
                new Pair<>(12,
                        new ItemBuilder(s.getIcon())
                                .name("&eHow much " + s.getDisplayName() + " &eEXP should be given?")
                                .lore("&fSet to &e" + amount,
                                        "&6Click to add/subtract 10",
                                        "&6Shift-Click to add/subtract 100")
                                .get()),
                new Pair<>(13,
                        new ItemBuilder(s.getIcon())
                                .name("&eHow much " + s.getDisplayName() + " &eEXP should be given?")
                                .lore("&fSet to &e" + amount,
                                        "&6Click to add/subtract 1000",
                                        "&6Shift-Click to add/subtract 10000")
                                .get())
        ));
    }

    @Override
    public boolean requiresPlayer() {
        return true;
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
        return "&eGrant " + s.getDisplayName() + " &eEXP";
    }

    @Override
    public String getDescription() {
        Skill s = SkillRegistry.getSkill(skill);
        if (s == null) return "";
        return "&fGives the player an amount of " + s.getDisplayName() + " &eEXP";
    }

    @Override
    public String getActiveDescription() {
        Skill s = SkillRegistry.getSkill(skill);
        if (s == null) return "";
        return String.format("&fGives the player %.1f " + s.getDisplayName() + " &eEXP", amount);
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.REWARDS.id());
    }

    @Override
    public DynamicItemModifier createNew() {
        return new SkillExperience(getName(), skill);
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument is expected: a double";
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException ignored){
            return "One argument is expected: a double. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<amount>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
