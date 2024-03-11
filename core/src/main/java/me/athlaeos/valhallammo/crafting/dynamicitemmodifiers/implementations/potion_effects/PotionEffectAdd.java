package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_effects;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PotionEffectAdd extends DynamicItemModifier {
    private final String effect;
    private double value = 0;
    private long duration = 1800;
    private int charges = -1;
    private final double smallIncrement;
    private final double bigIncrement;
    private final Material icon;

    public PotionEffectAdd(String name, String attribute, double smallIncrement, double bigIncrement, Material icon) {
        super(name);
        this.effect = attribute;
        this.smallIncrement = smallIncrement;
        this.bigIncrement = bigIncrement;
        this.icon = icon;
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        PotionEffectWrapper effect = PotionEffectRegistry.getEffect(this.effect);
        effect.setAmplifier(value);
        effect.setDuration(duration);
        effect.setCharges(ItemUtils.isConsumable(outputItem.getItem().getType()) ? -1 : charges);
        PotionEffectRegistry.addDefaultEffect(outputItem.getMeta(), effect);
        PotionEffectRegistry.updateItemName(outputItem.getMeta(), false, false);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 11) {
            value = value + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? bigIncrement : smallIncrement));
            PotionEffectWrapper effect = PotionEffectRegistry.getEffect(this.effect);
            if (effect.isVanilla()) value = Math.max(0, value);
        }
        else if (button == 13) duration = Math.max(0, duration + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 300 : 20)));
        else if (button == 17) charges = Math.max(-1, charges + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 5 : 1)));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        PotionEffectWrapper effect = PotionEffectRegistry.getEffect(this.effect);
        return new Pair<>(11,
                new ItemBuilder(Material.PAPER)
                        .name("&dHow strong should this effect be?")
                        .lore("&f" + effect.getEffect().toLowerCase().replace("_", " ") + " " + effect.getFormat().format(value + (effect.isVanilla() ? 1 : 0)) + " &f(" + StringUtils.toTimeStamp(duration, 20) + ")",
                                "&e" + String.format("%.2f", value),
                                "&6Click to add/subtract " + smallIncrement,
                                "&6Shift-Click to add/subtract " + bigIncrement)
                        .get()).map(Set.of(
                new Pair<>(13,
                        new ItemBuilder(Material.PAPER)
                                .name("&dHow long should the duration be?")
                                .lore("&f" + effect.getEffect().toLowerCase().replace("_", " ") + " " + effect.getFormat().format(value + (effect.isVanilla() ? 1 : 0)) + " &f(" + StringUtils.toTimeStamp(duration, 20) + ")",
                                        "&6Click to add/subtract 1 second",
                                        "&6Shift-Click to add/subtract 15 seconds")
                                .get()),
                new Pair<>(17,
                        new ItemBuilder(Material.FIREWORK_STAR)
                                .name("&dHow many charges should it have?")
                                .lore("&e" + (charges < 0 ? "Infinite uses" : (charges == 0 ? "No uses" : charges + " uses")),
                                        "&fPotion effects can be applied to",
                                        "&foffensive items too. The 'charges'",
                                        "&frepresent how many times you can hit",
                                        "&fan entity and inflict them with the",
                                        "&feffect before it disappears off the",
                                        "&fitem. ",
                                        "&cIncompatible with consumables .",
                                        "&6Click to add/subtract 1",
                                        "&6Shift-Click to add/subtract 5")
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(icon).get();
    }

    @Override
    public String getDisplayName() {
        PotionEffectWrapper effect = PotionEffectRegistry.getEffect(this.effect);
        return (effect.isVanilla() ? "&bVanilla" : "&dCustom") + " Potion Effect: " + effect.getEffect().toLowerCase().replace("_", " ") + " (ADD)";
    }

    @Override
    public String getDescription() {
        PotionEffectWrapper effect = PotionEffectRegistry.getEffect(this.effect);
        return "&fAdds " + effect.getEffect().toLowerCase().replace("_", " ") + " as default effect to the item. ";
    }

    @Override
    public String getActiveDescription() {
        PotionEffectWrapper effect = PotionEffectRegistry.getEffect(this.effect);
        return "&fAdds " + effect.getEffect().toLowerCase().replace("_", " ") + " " + effect.getFormat().format(value + (effect.isVanilla() ? 1 : 0)) + " &f(" + StringUtils.toTimeStamp(duration, 20) + ")" + " to the item. ";
    }

    @Override
    public Collection<String> getCategories() {
        PotionEffectWrapper effect = PotionEffectRegistry.getEffect(this.effect);
        return effect.isVanilla() ? Set.of(ModifierCategoryRegistry.VANILLA_POTION_EFFECTS.id()) : Set.of(ModifierCategoryRegistry.CUSTOM_POTION_EFFECTS.id());
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setCharges(int charges) {
        this.charges = charges;
    }

    @Override
    public DynamicItemModifier copy() {
        PotionEffectAdd m = new PotionEffectAdd(getName(), effect, smallIncrement, bigIncrement, icon);
        m.setCharges(this.charges);
        m.setDuration(this.duration);
        m.setValue(this.value);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 3) return "Three arguments expected: a double and two integers";
        try {
            value = StringUtils.parseDouble(args[0]);
            duration = Long.parseLong(args[1]);
            charges = Integer.parseInt(args[2]);
        } catch (NumberFormatException ignored){
            return "Three arguments expected: a double and two integers. At least one was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<value>");
        if (currentArg == 1) return List.of("<duration>");
        if (currentArg == 2) return List.of("<charges>");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 3;
    }
}
