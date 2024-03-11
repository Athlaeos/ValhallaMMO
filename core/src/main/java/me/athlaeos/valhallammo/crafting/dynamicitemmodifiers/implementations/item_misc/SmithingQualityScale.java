package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.item.MaterialClass;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SmithingQualityScale extends DynamicItemModifier {
    private double skillEfficiency = 1;

    public SmithingQualityScale(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        double skill = AccumulativeStatManager.getCachedStats("SMITHING_QUALITY_GENERAL", crafter, 10000, use);
        double skillMultiplier = 1 + AccumulativeStatManager.getCachedStats("SMITHING_FRACTION_QUALITY_GENERAL", crafter, 10000, use);
        MaterialClass materialClass = MaterialClass.getMatchingClass(outputItem.getMeta());
        if (materialClass != null){
            skill += AccumulativeStatManager.getCachedStats("SMITHING_QUALITY_" + materialClass, crafter, 10000, use);
            skillMultiplier += AccumulativeStatManager.getCachedStats("SMITHING_FRACTION_QUALITY_" + materialClass, crafter, 10000, use);
        }
        SmithingItemPropertyManager.setQuality(outputItem.getMeta(), (int) (skillMultiplier * skill * skillEfficiency));
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12)
            skillEfficiency = Math.max(0, skillEfficiency + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01)));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&eHow much quality to set?")
                        .lore("&fSets the quality to &e" + StatFormat.PERCENTILE_BASE_1_P1.format(skillEfficiency),
                                "&fof the player's smithing skill.",
                                "",
                                "&fFor example, 300 smithing skill",
                                "&fis converted to &e" + (int) (Math.floor(skillEfficiency * 300)) + " &fquality",
                                "&6Click to add/subtract 1%",
                                "&6Shift-Click to add/subtract 10%")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.NETHER_STAR).get();
    }

    @Override
    public String getDisplayName() {
        return "&eSmithing Quality (DYNAMIC)";
    }

    @Override
    public String getDescription() {
        return "&fSets an item's quality based on player skill.";
    }

    @Override
    public String getActiveDescription() {
        return "&fSets the item's quality to &e" + StatFormat.PERCENTILE_BASE_1_P1.format(skillEfficiency) + "&f of the player's skill";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setSkillEfficiency(double skillEfficiency) {
        this.skillEfficiency = skillEfficiency;
    }

    @Override
    public DynamicItemModifier copy() {
        SmithingQualityScale m = new SmithingQualityScale(getName());
        m.setSkillEfficiency(this.skillEfficiency);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One number is expected: a double.";
        try {
            skillEfficiency = StringUtils.parseDouble(args[0]);
        } catch (NumberFormatException ignored){
            return "One number is expected: a double. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<skill_quality_fraction>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
