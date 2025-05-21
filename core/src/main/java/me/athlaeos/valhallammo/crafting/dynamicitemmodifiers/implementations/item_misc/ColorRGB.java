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

public class ColorRGB extends DynamicItemModifier {
    private int red = 0;
    private int green = 0;
    private int blue = 0;

    public ColorRGB(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        context.getItem().color(Color.fromRGB(red, green, blue));
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 11 || button == 12 || button == 13) {
            ItemStack cursor = e.getCursor();
            if (!ItemUtils.isEmpty(cursor)) {
                ItemMeta meta = cursor.getItemMeta();
                if (meta instanceof Colorable c && c.getColor() != null) {
                    Color color = c.getColor().getColor();
                    red = color.getRed();
                    green = color.getGreen();
                    blue = color.getBlue();
                }
            } else {
                if (button == 11)
                    red = Math.min(255, Math.max(0, red + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 25 : 1))));
                else if (button == 12)
                    green = Math.min(255, Math.max(0, green + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 25 : 1))));
                else
                    blue = Math.min(255, Math.max(0, blue + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 25 : 1))));
            }
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        String hex = Utils.rgbToHex(red, green, blue);
        return new Pair<>(11,
                new ItemBuilder(Material.POTION)
                        .name("&cHow red should it be?")
                        .lore("&6Click with another item to copy",
                                "&6its custom model data over.",
                                "&fSet to &" + hex + hex,
                                "&c" + red + "&7| &a" + green + "&7| &b" + blue,
                                "&6Click to add/subtract 1000000",
                                "&6Shift-Click to add/subtract 100000")
                        .color(Color.fromRGB(red, green, blue))
                        .flag(ConventionUtils.getHidePotionEffectsFlag())
                        .get()).map(Set.of(
                new Pair<>(12,
                        new ItemBuilder(Material.POTION)
                                .name("&aHow green should it be?")
                                .lore("&6Click with another item to copy",
                                        "&6its custom model data over.",
                                        "&fSet to &" + hex + hex,
                                        "&c" + red + "&7| &a" + green + "&7| &b" + blue,
                                        "&6Click to add/subtract 10000",
                                        "&6Shift-Click to add/subtract 1000")
                                .color(Color.fromRGB(red, green, blue))
                                .flag(ConventionUtils.getHidePotionEffectsFlag())
                                .get()),
                new Pair<>(13,
                        new ItemBuilder(Material.POTION)
                                .name("&bHow blue should it be?")
                                .lore("&6Click with another item to copy",
                                        "&6its custom model data over.",
                                        "&fSet to &" + hex + hex,
                                        "&c" + red + "&7| &a" + green + "&7| &b" + blue,
                                        "&6Click to add/subtract 25",
                                        "&6Shift-Click to add/subtract 1")
                                .color(Color.fromRGB(red, green, blue))
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
        return "&aSet Leather/Potion Color (RGB)";
    }

    @Override
    public String getDescription() {
        return "&fSets a custom leather or potion color to the item";
    }

    @Override
    public String getActiveDescription() {
        String hex = Utils.rgbToHex(red, green, blue);
        return "&fSets the custom color of the item to &" + hex + hex;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id(), ModifierCategoryRegistry.POTION_MISC.id());
    }

    public void setRed(int red) {
        this.red = red;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    @Override
    public DynamicItemModifier copy() {
        ColorRGB m = new ColorRGB(getName());
        m.setRed(this.red);
        m.setGreen(this.green);
        m.setBlue(this.blue);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 3)
            return "Three arguments are expected: all integers representing the RGB values of the item";
        try {
            red = Integer.parseInt(args[0]);
            green = Integer.parseInt(args[1]);
            blue = Integer.parseInt(args[2]);
        } catch (IllegalArgumentException ignored) {
            return "Three arguments are expected: all integers representing the RGB values of the item. One of them is not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<red>");
        if (currentArg == 1) return List.of("<green>");
        if (currentArg == 2) return List.of("<blue>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 3;
    }
}
