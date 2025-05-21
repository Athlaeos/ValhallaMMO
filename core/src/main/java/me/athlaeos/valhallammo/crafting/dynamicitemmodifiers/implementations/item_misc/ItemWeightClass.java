package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.WeightClass;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class ItemWeightClass extends DynamicItemModifier {
    private WeightClass weightClass = WeightClass.LIGHT;

    public ItemWeightClass(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        WeightClass.setWeightClass(context.getItem().getMeta(), weightClass);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            List<WeightClass> weightClasses = Arrays.asList(WeightClass.values());
            int currentClass = weightClasses.indexOf(weightClass);
            if (e.isLeftClick()) {
                if (currentClass + 1 >= weightClasses.size()) currentClass = 0;
                else currentClass++;
            } else {
                if (currentClass - 1 < 0) currentClass = weightClasses.size() - 1;
                else currentClass--;
            }
            weightClass = weightClasses.get(currentClass);
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(switch (weightClass){
                    case HEAVY -> Material.NETHERITE_CHESTPLATE;
                    case LIGHT -> Material.LEATHER_CHESTPLATE;
                    case WEIGHTLESS -> Material.FEATHER;
                })
                        .name("&eWhich weight class should it be?")
                        .lore("&fWeight class set to &e" + weightClass,
                                "&cHeavy&f items are affected by",
                                "&fthe Heavy Armor/Heavy Weapons",
                                "&fskills. ",
                                "&bLight&f items are affected by",
                                "&fthe Light Armor/Light Weapons",
                                "&fskills. ",
                                "&aWeightless&f items aren't affected",
                                "&fby any skills.")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.ANVIL).get();
    }

    @Override
    public String getDisplayName() {
        return "&eWeight Class";
    }

    @Override
    public String getDescription() {
        return "&fChanges the weight class of an item.";
    }

    @Override
    public String getActiveDescription() {
        return "&fWeight class set to &e" + weightClass;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setWeightClass(WeightClass weightClass) {
        this.weightClass = weightClass;
    }

    @Override
    public DynamicItemModifier copy() {
        ItemWeightClass m = new ItemWeightClass(getName());
        m.setWeightClass(this.weightClass);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "You must indicate the weight class to set the item to";
        try {
            weightClass = WeightClass.valueOf(args[0]);
        } catch (IllegalArgumentException ignored){
            return "Invalid weight class, valid classes are: " + Arrays.stream(WeightClass.values()).map(WeightClass::toString).collect(Collectors.joining(", "));
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return Arrays.stream(WeightClass.values()).map(WeightClass::toString).toList();
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
