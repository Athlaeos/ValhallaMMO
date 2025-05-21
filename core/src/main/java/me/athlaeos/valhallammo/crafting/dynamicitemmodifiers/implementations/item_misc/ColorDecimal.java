package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.ConventionUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Colorable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ColorDecimal extends DynamicItemModifier {
    private int decimal = 0;

    public ColorDecimal(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        String hex = String.format("#%06X", decimal);
        context.getItem().color(Utils.hexToRgb(hex));
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 11 || button == 12 || button == 13) {
            ItemStack cursor = e.getCursor();
            if (!ItemUtils.isEmpty(cursor)) {
                ItemMeta meta = cursor.getItemMeta();
                if (meta instanceof Colorable c && c.getColor() != null) {
                    Color color = c.getColor().getColor();
                    decimal = Integer.parseInt(Utils.rgbToHex(color.getRed(), color.getGreen(), color.getBlue()).replaceFirst("#", ""), 16);
                }
            } else {
                if (button == 11)
                    decimal = Math.min(16777215, Math.max(0, decimal + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1000000 : 100000))));
                else if (button == 12)
                    decimal = Math.min(16777215, Math.max(0, decimal + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10000 : 1000))));
                else
                    decimal = Math.min(16777215, Math.max(0, decimal + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 25 : 1))));
            }
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        String hex = String.format("#%06X", decimal);
        Color color = Utils.hexToRgb(hex);

        return new Pair<>(11,
                new ItemBuilder(Material.POTION)
                        .name("&cHow red should it be?")
                        .lore("&6Click with another item to copy",
                                "&6its custom model data over.",
                                "&fSet to &" + hex + decimal,
                                "&c" + color.getRed() + "&7 | &a" + color.getGreen() + "&7 | &b" + color.getBlue(),
                                "&6Click to add/subtract 100000",
                                "&6Shift-Click to add/subtract 1000000")
                        .color(color)
                        .flag(ConventionUtils.getHidePotionEffectsFlag())
                        .get()).map(Set.of(
                new Pair<>(12,
                        new ItemBuilder(Material.POTION)
                                .name("&aHow green should it be?")
                                .lore("&6Click with another item to copy",
                                        "&6its custom model data over.",
                                        "&fSet to &" + hex + decimal,
                                        "&c" + color.getRed() + "&7 | &a" + color.getGreen() + "&7 | &b" + color.getBlue(),
                                        "&6Click to add/subtract 1000",
                                        "&6Shift-Click to add/subtract 10000")
                                .color(color)
                                .flag(ConventionUtils.getHidePotionEffectsFlag())
                                .get()),
                new Pair<>(13,
                        new ItemBuilder(Material.POTION)
                                .name("&bHow blue should it be?")
                                .lore("&6Click with another item to copy",
                                        "&6its custom model data over.",
                                        "&fSet to &" + hex + decimal,
                                        "&c" + color.getRed() + "&7 | &a" + color.getGreen() + "&7 | &b" + color.getBlue(),
                                        "&6Click to add/subtract 25",
                                        "&6Shift-Click to add/subtract 1")
                                .color(color)
                                .flag(ConventionUtils.getHidePotionEffectsFlag())
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.POTION).flag(ConventionUtils.getHidePotionEffectsFlag()).get();
    }

    @Override
    public String getDisplayName() {
        return "&aSet Leather/Potion Color (Decimal)";
    }

    @Override
    public String getDescription() {
        return "&fSets a custom leather or potion color to the item";
    }

    @Override
    public String getActiveDescription() {
        String hex = String.format("#%06X", decimal);
        return "&fSets the custom color of the item to &" + hex + hex;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id(), ModifierCategoryRegistry.POTION_MISC.id());
    }

    public void setDecimal(int decimal) {
        this.decimal = decimal;
    }

    @Override
    public DynamicItemModifier copy() {
        ColorDecimal m = new ColorDecimal(getName());
        m.setDecimal(this.decimal);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1)
            return "One argument expected: an integer representing the decimal value of the hexadecimal color code";
        try {
            decimal = Integer.parseInt(args[0]);
        } catch (IllegalArgumentException ignored) {
            return "One argument expected: an integer representing the decimal value of the hexadecimal color code. This is a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<color_decimal>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
