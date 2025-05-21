package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnchantmentGlint extends DynamicItemModifier {
    private Boolean glint = true;

    public EnchantmentGlint(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        ValhallaMMO.getNms().setGlint(context.getItem().getMeta(), glint);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            if (e.isShiftClick()) glint = null;
            else glint = glint == null || !glint;
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.BOOK)
                        .name("&eShould the item glow?")
                        .lore("7fSet to &e" + (glint == null ? "reset" : (glint ? "yes" : "no")),
                                "&fIf yes, the item will have an",
                                "&fenchantment glint even without",
                                "&fenchantments.",
                                "&fIf no, the item will not have its",
                                "&fenchantment glint even with",
                                "&fenchantments.",
                                "&fIf reset, it will behave as normal.",
                                "&6Click to toggle",
                                "&6Shift-Click to reset")
                        .get()).map(Set.of());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.ENCHANTED_BOOK).get();
    }

    @Override
    public String getDisplayName() {
        return "&aSet Enchantment Glint";
    }

    @Override
    public String getDescription() {
        return "&fSets/removes an enchantment glint on the item";
    }

    @Override
    public String getActiveDescription() {
        return "&fMakes the item " + (glint == null ? "glow like normal." : (glint ? "glow as if it has enchantments" : "stop glowing even if it has enchantments"));
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setGlint(Boolean glint) {
        this.glint = glint;
    }

    @Override
    public DynamicItemModifier copy() {
        EnchantmentGlint m = new EnchantmentGlint(getName());
        m.setGlint(this.glint);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1)
            return "You must indicate if the item should glow or not";
        try {
            glint = args[0].equalsIgnoreCase("reset") ? null : Boolean.parseBoolean(args[0]);
        } catch (IllegalArgumentException ignored) {
            return "Invalid option";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<glow>", "true", "false", "reset");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
