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

public class ArrowBehaviorVeryExplosive extends DynamicItemModifier {
    private double radius = 3;
    private boolean destructive = false;
    private boolean incendiary = false;
    private int count = 1;

    public ArrowBehaviorVeryExplosive(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        ArrowBehaviorRegistry.addBehavior(context.getItem().getMeta(), ArrowBehaviorRegistry.EXPLODING_REPEATEDLY.getName(), radius, destructive ? 1 : 0, incendiary ? 1 : 0, count);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 6) radius = Math.max(0, radius + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1 : 0.1)));
        else if (button == 7) count = Math.max(1, count + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 5 : 1)));
        else if (button == 8) destructive = !destructive;
        else if (button == 17) incendiary = !incendiary;
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(6,
                new ItemBuilder(Material.TNT)
                        .name("&6Explosion Radius")
                        .lore(String.format("&fExplosion Radius: %.1f", radius),
                                String.format("&fExplosion Count: %d", count),
                                String.format("&fDestructive to environment: &e%s", destructive ? "Yes" : "No"),
                                String.format("&fLights fire to environment: &e%s", incendiary ? "Yes" : "No"),
                                "&6Click to add/subtract 0.1",
                                "&6Shift-Click to add/subtract 1")
                        .get()).map(
                                Set.of(
                                        new Pair<>(7,
                                                new ItemBuilder(Material.GOLDEN_PICKAXE)
                                                        .name("&6Amount of Explosions")
                                                        .lore(String.format("&fExplosion Radius: %.1f", radius),
                                                                String.format("&fExplosion Count: %d", count),
                                                                String.format("&fDestructive to environment: &e%s", destructive ? "Yes" : "No"),
                                                                String.format("&fLights fire to environment: &e%s", incendiary ? "Yes" : "No"),
                                                                "&6Click to toggle on/off")
                                                        .get()
                                        ),
                                        new Pair<>(8,
                                                new ItemBuilder(Material.GOLDEN_PICKAXE)
                                                        .name("&6Destructive to Environment")
                                                        .lore(String.format("&fExplosion Radius: %.1f", radius),
                                                                String.format("&fExplosion Count: %d", count),
                                                                String.format("&fDestructive to environment: &e%s", destructive ? "Yes" : "No"),
                                                                String.format("&fLights fire to environment: &e%s", incendiary ? "Yes" : "No"),
                                                                "&6Click to toggle on/off")
                                                        .get()
                                        ),
                                        new Pair<>(17,
                                                new ItemBuilder(Material.FLINT_AND_STEEL)
                                                        .name("&6Lights Fire to Environment")
                                                        .lore(String.format("&fExplosion Radius: %.1f", radius),
                                                                String.format("&fExplosion Count: %d", count),
                                                                String.format("&fDestructive to environment: &e%s", destructive ? "Yes" : "No"),
                                                                String.format("&fLights fire to environment: &e%s", incendiary ? "Yes" : "No"),
                                                                "&6Click to toggle on/off")
                                                        .get()
                                        )
                                )
        );
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.TNT_MINECART).get();
    }

    @Override
    public String getDisplayName() {
        return "&6Arrow Behavior: &6&lVery Explosive";
    }

    @Override
    public String getDescription() {
        return "&fCauses the arrow to explode multiple times when landing";
    }

    @Override
    public String getActiveDescription() {
        return String.format("&fCauses the arrow to explode repeatedly when landing. /n&eExplosion Count: %d/n&eBlast Radius: %.1f/n&eDestructive: %s/n&eIncendiary: %s", count, radius, destructive ? "Yes" : "No", incendiary ? "Yes" : "No");
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CUSTOM_ATTRIBUTES.id());
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setIncendiary(boolean incendiary) {
        this.incendiary = incendiary;
    }

    public void setDestructive(boolean destructive) {
        this.destructive = destructive;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public DynamicItemModifier copy() {
        ArrowBehaviorVeryExplosive m = new ArrowBehaviorVeryExplosive(getName());
        m.setCount(this.count);
        m.setDestructive(this.destructive);
        m.setIncendiary(this.incendiary);
        m.setRadius(this.radius);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 4) return "Four arguments are expected: the first a double, the second and third a yes/no answer, the last an integer";
        try {
            radius = Integer.parseInt(args[0]);
            destructive = args[1].equalsIgnoreCase("yes");
            incendiary = args[2].equalsIgnoreCase("yes");
            count = Integer.parseInt(args[3]);
        } catch (NumberFormatException ignored){
            return "Four arguments are expected: the first a double, the second and third a yes/no answer, the last an integer. At least one was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<explosion_radius>");
        if (currentArg == 1) return List.of("<should_destroy_terrain>", "yes", "no");
        if (currentArg == 2) return List.of("<should_ignite_terrain>", "yes", "no");
        if (currentArg == 4) return List.of("<explosion_count>");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 4;
    }
}
