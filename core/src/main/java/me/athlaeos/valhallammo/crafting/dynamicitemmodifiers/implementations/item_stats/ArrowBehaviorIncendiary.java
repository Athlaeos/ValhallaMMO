package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.item.arrow_attributes.ArrowBehaviorRegistry;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArrowBehaviorIncendiary extends DynamicItemModifier {
    private int duration = 60;
    private int radius = 3;
    private double density = 0.3;

    public ArrowBehaviorIncendiary(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        ArrowBehaviorRegistry.addBehavior(outputItem.getMeta(), ArrowBehaviorRegistry.INCENDIARY.getName(), duration, radius, density);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 6) duration = Math.max(0, duration + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 20 : 1)));
        else if (button == 8) radius = Math.max(0, radius + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 3 : 1)));
        else if (button == 17) density = Math.min(1, Math.max(0, density + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01))));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(6,
                new ItemBuilder(Material.FLINT_AND_STEEL)
                        .name("&6Entity Fire Duration")
                        .lore(String.format("&fEntity fire duration: %s", StringUtils.toTimeStamp2(duration, 20, true)),
                                String.format("&fFire radius: &e%d", radius),
                                String.format("&fFire density: &e%.1f%%", density * 100),
                                "&6Click to add/subtract 1 tick",
                                "&6Shift-Click to add/subtract 20 ticks")
                        .get()).map(
                                Set.of(
                                        new Pair<>(8,
                                                new ItemBuilder(Material.GOLDEN_PICKAXE)
                                                        .name("&6Fire Radius")
                                                        .lore(String.format("&fEntity fire duration: %s", StringUtils.toTimeStamp2(duration, 20, true)),
                                                                String.format("&fFire radius: &e%d", radius),
                                                                String.format("&fFire density: &e%.1f%%", density * 100),
                                                                "&6Click to add/subtract 1 block",
                                                                "&6Shift-Click to add/subtract 3 blocks")
                                                        .get()
                                        ),
                                        new Pair<>(17,
                                                new ItemBuilder(Material.FLINT_AND_STEEL)
                                                        .name("&6Lights Fire to Environment")
                                                        .lore(String.format("&fEntity fire duration: %s", StringUtils.toTimeStamp2(duration, 20, true)),
                                                                String.format("&fFire radius: &e%d", radius),
                                                                String.format("&fFire density: &e%.1f%%", density * 100),
                                                                "&6Click to add/subtract 1%",
                                                                "&6Shift-Click to add/subtract 10%")
                                                        .get()
                                        )
                                )
        );
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.FLINT_AND_STEEL).get();
    }

    @Override
    public String getDisplayName() {
        return "&eArrow Behavior: Incendiary";
    }

    @Override
    public String getDescription() {
        return "&fCauses the arrow to light the area, and all entities in the radius, on fire";
    }

    @Override
    public String getActiveDescription() {
        return String.format("&fCauses the arrow to light the area, and all entities in the radius, on fire. " +
                "/n&eFire Duration: %s/n&eFire Radius: %d/n&eFire Density: %.1f%%", StringUtils.toTimeStamp2(duration, 20, true), radius, density * 100);
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CUSTOM_ATTRIBUTES.id());
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    @Override
    public DynamicItemModifier copy() {
        ArrowBehaviorIncendiary m = new ArrowBehaviorIncendiary(getName());
        m.setDensity(this.density);
        m.setDuration(this.duration);
        m.setRadius(this.radius);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 3) return "Three arguments are expected: the first and second an integer, the second a double";
        try {
            duration = Integer.parseInt(args[0]);
            radius = Integer.parseInt(args[1]);
            density = StringUtils.parseDouble(args[2]);
        } catch (NumberFormatException ignored){
            return "Three arguments are expected: the first and second an integer, the second a double. At least one was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<duration_ticks>");
        if (currentArg == 1) return List.of("<radius>");
        if (currentArg == 2) return List.of("<density>", "0.1", "0.25", "0.33", "0.5", "0.67", "0.75", "0.9", "1.0");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 3;
    }
}
