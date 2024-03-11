package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.dom.Scaling;
import me.athlaeos.valhallammo.item.CustomDurabilityManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.item.MaterialClass;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.*;

public class DurabilityRepairScale extends DynamicItemModifier {
    private double skillEfficiency = 1;
    private static final Scaling scaling = Scaling.fromConfig("skills/smithing.yml", "scaling_repair");

    public DurabilityRepairScale(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        if (!(outputItem.getMeta() instanceof Damageable) || outputItem.getItem().getType().getMaxDurability() <= 0 || scaling == null) return;
        double skill = Math.round(skillEfficiency * AccumulativeStatManager.getCachedStats("SMITHING_QUALITY_GENERAL", crafter, 10000, use));
        double skillMultiplier = 1 + AccumulativeStatManager.getCachedStats("SMITHING_FRACTION_QUALITY_GENERAL", crafter, 10000, use);
        MaterialClass materialClass = MaterialClass.getMatchingClass(outputItem.getMeta());
        if (materialClass != null){
            skill += AccumulativeStatManager.getCachedStats("SMITHING_QUALITY_" + materialClass, crafter, 10000, use);
            skillMultiplier += AccumulativeStatManager.getCachedStats("SMITHING_FRACTION_QUALITY_" + materialClass, crafter, 10000, use);
        }
        int itemDurability = CustomDurabilityManager.getDurability(outputItem.getMeta(), false);
        int maxDurability = CustomDurabilityManager.getDurability(outputItem.getMeta(), true);
        if (itemDurability >= maxDurability) return;
        if (itemDurability > 0){
            // Item has custom durability
            double fractionToRepair = scaling.evaluate(scaling.getExpression().replace("%rating%", String.valueOf((skill * skillMultiplier * skillEfficiency))));
            int addDurability = (int) (fractionToRepair * (double) maxDurability);
            CustomDurabilityManager.damage(outputItem.getMeta(), -addDurability);
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12)
            skillEfficiency = Math.max(0, skillEfficiency + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01)));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.NETHER_STAR)
                        .name("&fSkill Efficiency")
                        .lore(String.format("&fSkill efficiency: %.0f%%", skillEfficiency * 100),
                                "&fSkill efficiency determines how much",
                                "&fof the player's skill is used in the",
                                "&fformula.",
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
        return "&7Repair (DYNAMIC)";
    }

    @Override
    public String getDescription() {
        return "&fRepairs the item based on player skill.";
    }

    @Override
    public String getActiveDescription() {
        return "&fRepairing the item with &e" + StatFormat.PERCENTILE_BASE_1_P1.format(skillEfficiency) + "&f player skill efficiency";
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
        DurabilityRepairScale m = new DurabilityRepairScale(getName());
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
        if (currentArg == 0) return List.of("<skill_efficiency_fraction>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
