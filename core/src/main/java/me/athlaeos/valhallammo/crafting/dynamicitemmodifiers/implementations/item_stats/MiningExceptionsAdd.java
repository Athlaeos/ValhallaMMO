package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.MiningSpeed;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class MiningExceptionsAdd extends DynamicItemModifier {
    private final Map<Material, Double> exceptions = new HashMap<>();
    private Material exceptionMaterial = Material.STONE;
    private double value = 2;

    public MiningExceptionsAdd(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        for (Material m : exceptions.keySet()){
            MiningSpeed.addException(outputItem.getMeta(), m, exceptions.get(m));
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 11){
            if (!ItemUtils.isEmpty(e.getCursor()) && !e.getCursor().getType().isBlock()) exceptionMaterial = e.getCursor().getType();
        } else if (button == 13){
            value = Math.min(0, value + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1 : 0.1)));
        } else if (button == 17){
            if (e.isShiftClick()) exceptions.clear();
            else exceptions.put(exceptionMaterial, value);
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(11,
                new ItemBuilder(exceptionMaterial)
                        .name("&fSelect Material")
                        .lore("&e" + exceptionMaterial + "&7 is selected",
                                "&fClick with a block in your inventory",
                                "&fto select different material.")
                        .get()).map(Set.of(
                new Pair<>(13,
                        new ItemBuilder(Material.DIAMOND_PICKAXE)
                                .name("&fMultiplier Value")
                                .lore("&fCurrently selected: &e" + value,
                                        "&6Click to add/subtract 0.1",
                                        "&6Shift-Click to add/subtract 1")
                                .get()),
                new Pair<>(17,
                        new ItemBuilder(Material.STRUCTURE_VOID)
                                .name("&fConfirm Exception")
                                .lore("&f" + exceptionMaterial + " will be mined at " + value + "x",
                                        "&fspeed.",
                                        "&6Click to add exception",
                                        "&cShift-Click to clear exceptions",
                                        "&fCurrent exceptions:")
                                .appendLore(exceptions.entrySet().stream().map(e -> "&e" + e.getKey() + ": " + e.getValue() + "x").toList())
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.WOODEN_PICKAXE).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Mining Speed Exceptions";
    }

    @Override
    public String getDescription() {
        return "&fAdds a mining speed exception to the item. Blocks mined by these items will mine at a different multiplier than other blocks";
    }

    @Override
    public String getActiveDescription() {
        return "&fAdds the following mining speed exceptions to the item: /n&e" +
                (exceptions.entrySet().stream().map(e -> "&e" + e.getKey() + ": " + e.getValue() + "x"))
                .collect(Collectors.joining("/n&e"));
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CUSTOM_ATTRIBUTES.id());
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setExceptionMaterial(Material exceptionMaterial) {
        this.exceptionMaterial = exceptionMaterial;
    }

    public Map<Material, Double> getExceptions() {
        return exceptions;
    }

    @Override
    public DynamicItemModifier copy() {
        MiningExceptionsAdd m = new MiningExceptionsAdd(getName());
        m.setExceptionMaterial(this.exceptionMaterial);
        m.getExceptions().putAll(this.exceptions);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 2) return "Two argument are expected: a material and a double";
        try {
            Material m = Material.valueOf(args[0]);
            double value = StringUtils.parseDouble(args[1]);
            exceptions.put(m, value);
        } catch (IllegalArgumentException ignored){
            return "Two argument are expected: a material and a double. At least one was not a valid argument";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return Arrays.stream(Material.values()).map(Object::toString).collect(Collectors.toList());
        if (currentArg == 1) return List.of("<multiplier>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 2;
    }
}
