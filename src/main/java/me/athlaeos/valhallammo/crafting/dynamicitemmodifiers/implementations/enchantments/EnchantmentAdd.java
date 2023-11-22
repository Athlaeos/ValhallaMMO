package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.enchantments;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.EnchantingItemPropertyManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EnchantmentAdd extends DynamicItemModifier {
    private final String enchantment;
    private int level = 1;
    private final Material icon;
    private boolean scaleWithSkill = false;

    public EnchantmentAdd(String name, Enchantment enchantment, Material icon) {
        super(name);
        this.enchantment = enchantment.getKey().getKey();
        this.icon = icon;
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(enchantment));
        if (e == null) {
            ValhallaMMO.logWarning("EnchantmentAdd modifier was instantiated with an enchantment that doesn't exist: " + enchantment);
            return;
        }
        if (level <= 0) outputItem.getItem().removeEnchantment(e);
        else {
            if (scaleWithSkill){
                int skill = (int) AccumulativeStatManager.getCachedStats("ENCHANTING_QUALITY", crafter, 10000, true);
                skill = (int) (skill * (1 + AccumulativeStatManager.getCachedStats("ENCHANTING_FRACTION_QUALITY", crafter, 10000, true)));
                level = EnchantingItemPropertyManager.getScaledLevel(e, skill, level);
            }
            outputItem.enchant(e, level);
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            level = Math.max(0, level + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 5 : 1)));
        } else if (button == 17 && level > 0) {
            scaleWithSkill = !scaleWithSkill;
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(enchantment));
        if (e == null) return new HashMap<>();
        String enchant = StringUtils.toPascalCase(e.getKey().getKey().replace("_", " "));
        return new Pair<>(12,
                new ItemBuilder(Material.ENCHANTED_BOOK)
                        .name("&eWhat level should the enchantment be?")
                        .lore(level <= 0 ? "&fRemoves " + enchant + "," : "&fAdds " + enchant + " " + StringUtils.toRoman(level) + ",",
                                level <= 0 ? "&for cancels if not present" : "&for cancels if already present",
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 5")
                        .get()).map(level > 0 ? Set.of(
                new Pair<>(17,
                        new ItemBuilder(Material.ENCHANTED_BOOK)
                                .name("&eShould this enchantment level scale?")
                                .lore(scaleWithSkill ? "&eYes" : "&eNo",
                                        "&fEnchantments that scale will ",
                                        "&fgain/lose levels based on ",
                                        "&fenchanting skill.",
                                        "&6Click to toggle")
                                .get())
        ) : new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(icon).get();
    }

    @Override
    public String getDisplayName() {
        Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(enchantment));
        if (e == null) return "&cInvalid enchantment";
        String enchant = StringUtils.toPascalCase(e.getKey().getKey().replace("_", " "));
        return "&fAdds/removes &e" + enchant + "&f (ADD/REMOVE)";
    }

    @Override
    public String getDescription() {
        Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(enchantment));
        if (e == null) return "&cInvalid enchantment";
        String enchant = StringUtils.toPascalCase(e.getKey().getKey().replace("_", " "));
        return "&fAdds &e" + enchant + "&f on the item if level > 0 or cancels the recipe is the item already has it.";
    }

    @Override
    public String getActiveDescription() {
        Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(enchantment));
        if (e == null) return "&cInvalid enchantment";
        String enchant = StringUtils.toPascalCase(e.getKey().getKey().replace("_", " "));
        return level > 0 ?
                "&fAdds &e" + enchant + " " + StringUtils.toRoman(level) + " &for cancels if the item already has the enchantment" :
                "&fRemoves &e" + enchant + " &for cancels if the item doesn't have it yet";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ENCHANTMENTS.id());
    }

    @Override
    public DynamicItemModifier createNew() {
        Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(enchantment));
        if (e == null) throw new IllegalStateException("Enchantment " + enchantment + " is invalid");
        return new EnchantmentAdd(getName(), e, icon);
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 2) return "Two arguments expected: an integer and a yes/no answer";
        try {
            level = Integer.parseInt(args[0]);
            scaleWithSkill = args[1].equalsIgnoreCase("yes");
        } catch (NumberFormatException ignored){
            return "Two arguments expected: an integer and a yes/no answer. Invalid number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<level>");
        if (currentArg == 1) return List.of("yes", "no");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 2;
    }
}
