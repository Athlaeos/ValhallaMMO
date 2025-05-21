package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.enchantment_misc;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.*;

public class EnchantmentsClear extends DynamicItemModifier {
    private boolean ignoreCursed = false;
    private static final Collection<Enchantment> curseEnchantments = Set.of(Enchantment.BINDING_CURSE, Enchantment.VANISHING_CURSE);

    public EnchantmentsClear(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (context.getItem().getMeta() instanceof EnchantmentStorageMeta eMeta){
            eMeta.getEnchants().keySet().stream().filter(e -> !ignoreCursed || !curseEnchantments.contains(e))
                    .forEach(eMeta::removeStoredEnchant);
            if (eMeta.getEnchants().isEmpty() && context.getItem().getItem().getType() == Material.ENCHANTED_BOOK) context.getItem().type(Material.BOOK);
        } else {
            context.getItem().getItem().getEnchantments().keySet().stream().filter(e -> !ignoreCursed || !curseEnchantments.contains(e))
                    .forEach(e -> context.getItem().getItem().removeEnchantment(e));
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            ignoreCursed = !ignoreCursed;
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.ENCHANTED_BOOK)
                        .name("&eShould curse enchantments be ignored?")
                        .lore("&fCurses ignored: &e" + (ignoreCursed ? "Yes" : "No"),
                                "&6Click to toggle")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.GRINDSTONE).get();
    }

    @Override
    public String getDisplayName() {
        return "&cRemove All Enchantments";
    }

    @Override
    public String getDescription() {
        return "&fRemoves all enchantments";
    }

    @Override
    public String getActiveDescription() {
        return "&fRemoves all enchantments " + (ignoreCursed ? "&eignoring curse enchantments" : "");
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ENCHANTMENT_MISC.id());
    }

    public void setIgnoreCursed(boolean ignoreCursed) {
        this.ignoreCursed = ignoreCursed;
    }

    @Override
    public DynamicItemModifier copy() {
        EnchantmentsClear m = new EnchantmentsClear(getName());
        m.setIgnoreCursed(this.ignoreCursed);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument expected: a yes/no answer";
        ignoreCursed = args[0].equalsIgnoreCase("yes");
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("yes", "no");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
