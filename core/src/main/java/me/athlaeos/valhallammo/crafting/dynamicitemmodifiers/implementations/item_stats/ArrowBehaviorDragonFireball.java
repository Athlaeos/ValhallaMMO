package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.arrow_attributes.ArrowBehaviorRegistry;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArrowBehaviorDragonFireball extends DynamicItemModifier {
    private double radius = 3;
    private boolean incendiary = false;

    public ArrowBehaviorDragonFireball(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        ArrowBehaviorRegistry.addBehavior(context.getItem().getMeta(), ArrowBehaviorRegistry.FIREBALL_DRAGON.getName(), radius, incendiary ? 1 : 0);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 11) radius = Math.max(0, radius + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1 : 0.1)));
        else if (button == 13) incendiary = !incendiary;
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(11,
                new ItemBuilder(Material.TNT)
                        .name("&6Explosion Radius")
                        .lore(String.format("&fExplosion Radius: %.1f", radius),
                                String.format("&fLights fire to environment: &e%s", incendiary ? "Yes" : "No"),
                                "&6Click to add/subtract 0.1",
                                "&6Shift-Click to add/subtract 1")
                        .get()).map(
                                Set.of(
                                        new Pair<>(13,
                                                new ItemBuilder(Material.FLINT_AND_STEEL)
                                                        .name("&6Lights Fire to Environment")
                                                        .lore(String.format("&fExplosion Radius: %.1f", radius),
                                                                String.format("&fLights fire to environment: &e%s", incendiary ? "Yes" : "No"),
                                                                "&6Click to toggle on/off")
                                                        .get()
                                        )
                                )
        );
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.DRAGON_BREATH).get();
    }

    @Override
    public String getDisplayName() {
        return "&dArrow Behavior: Dragon Fireball";
    }

    @Override
    public String getDescription() {
        return "&fTurns the arrow into a draconic fireball when shot";
    }

    @Override
    public String getActiveDescription() {
        return String.format("&fTurns the arrow into a draconic fireball when shot. /n&eBlast Radius: %.1f/n&eIncendiary: %s", radius, incendiary ? "Yes" : "No");
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CUSTOM_ATTRIBUTES.id());
    }

    public void setIncendiary(boolean incendiary) {
        this.incendiary = incendiary;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public DynamicItemModifier copy() {
        ArrowBehaviorDragonFireball m = new ArrowBehaviorDragonFireball(getName());
        m.setIncendiary(this.incendiary);
        m.setRadius(this.radius);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 2) return "Two arguments are expected: the first a double, the second a yes/no answer";
        try {
            radius = Integer.parseInt(args[0]);
            incendiary = args[1].equalsIgnoreCase("yes");
        } catch (NumberFormatException ignored){
            return "Two arguments are expected: the first a double, the second a yes/no answer. At least one was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<explosion_radius>");
        if (currentArg == 1) return List.of("<should_ignite_terrain>", "yes", "no");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 2;
    }
}
