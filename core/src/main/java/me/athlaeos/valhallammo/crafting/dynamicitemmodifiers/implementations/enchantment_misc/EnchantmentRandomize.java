package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.enchantment_misc;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.EnchantingItemPropertyManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.Enchanter;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.*;

public class EnchantmentRandomize extends DynamicItemModifier {
    private int level = 10;
    private boolean scaleWithSkill = false;
    private boolean includeTreasure = false;

    public EnchantmentRandomize(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (level == 0) return;

        Map<Enchantment, Integer> chosenEnchantments = Enchanter.getRandomEnchantments(context.getItem().getItem(), context.getItem().getMeta(), level, includeTreasure);
        if (chosenEnchantments.isEmpty()) return;
        if (context.getItem().getItem().getType() == Material.BOOK) context.getItem().type(Material.ENCHANTED_BOOK);

        for (Enchantment e : chosenEnchantments.keySet()){
            int lv = chosenEnchantments.get(e);
            if (scaleWithSkill){
                int skill = (int) AccumulativeStatManager.getCachedStats("ENCHANTING_QUALITY", context.getCrafter(), 10000, true);
                skill = (int) (skill * (1 + AccumulativeStatManager.getCachedStats("ENCHANTING_FRACTION_QUALITY", context.getCrafter(), 10000, true)));
                lv = EnchantingItemPropertyManager.getScaledLevel(e, skill, lv);
            }
            if (context.getItem().getMeta() instanceof EnchantmentStorageMeta eMeta) eMeta.addStoredEnchant(e, lv, false);
            else context.getItem().enchant(e, lv);
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            level = Math.max(1, level + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 5 : 1)));
        } else if (button == 17) {
            scaleWithSkill = !scaleWithSkill;
        }else if (button == 13) {
            includeTreasure = !includeTreasure;
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.ENCHANTED_BOOK)
                        .name("&eWhat level should be enchanted at?")
                        .lore("&fEnchanted at level &e" + level,
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 10")
                        .get()).map(level > 0 ? Set.of(
                new Pair<>(17,
                        new ItemBuilder(Material.ENCHANTED_BOOK)
                                .name("&eShould they scale with skill?")
                                .lore(scaleWithSkill ? "&eYes" : "&eNo",
                                        "&fEnchantments that scale will ",
                                        "&fgain/lose levels based on ",
                                        "&fenchanting skill.",
                                        "&6Click to toggle")
                                .get()),
                new Pair<>(13,
                        new ItemBuilder(Material.CHEST)
                                .name("&eInclude treasure enchantments?")
                                .lore(includeTreasure ? "&eYes" : "&eNo",
                                        "&fTreasure enchantments do not",
                                        "&fnaturally occur through enchanting.",
                                        "&fExamples: Mending, Curse of Binding.",
                                        "&fenchanting skill.",
                                        "&6Click to toggle")
                                .get())
        ) : new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.ENCHANTING_TABLE).get();
    }

    @Override
    public String getDisplayName() {
        return "&dEnchant Item Randomly";
    }

    @Override
    public String getDescription() {
        return "&fEnchants item randomly given a level";
    }

    @Override
    public String getActiveDescription() {
        return "&fEnchants item randomly at level &e" + level + (includeTreasure ? " &fincluding treasure enchantments" : "");
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ENCHANTMENT_MISC.id());
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setScaleWithSkill(boolean scaleWithSkill) {
        this.scaleWithSkill = scaleWithSkill;
    }

    public void setIncludeTreasure(boolean includeTreasure) {
        this.includeTreasure = includeTreasure;
    }

    @Override
    public DynamicItemModifier copy() {
        EnchantmentRandomize m = new EnchantmentRandomize(getName());
        m.setIncludeTreasure(this.includeTreasure);
        m.setScaleWithSkill(this.scaleWithSkill);
        m.setLevel(this.level);
        m.setPriority(this.getPriority());
        return m;
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
